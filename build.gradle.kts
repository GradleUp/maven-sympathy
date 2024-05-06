import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm").version("2.0.0-RC2")
    id("java-gradle-plugin")
    id("maven-publish")
    id("signing")
}

val pluginDescription = "maven-sympathy checks that your project dependencies play nice with Maven resolution strategy. See https://jakewharton.com/nonsensical-maven-is-still-a-gradle-problem/ for more details."

gradlePlugin {
    plugins {
        create("maven-sympathy") {
            id = "com.gradleup.maven-sympathy"
            implementationClass = "com.gradleup.maven.sympathy.MavenSympathyPlugin"
            this.description = pluginDescription
            this.displayName = "maven-sympathy"
        }
    }
}

group = "com.gradleup.maven-sympathy"
version = "0.0.1"

tasks.withType(JavaCompile::class.java) {
    options.release.set(8)
}
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjdk-release=8")
        jvmTarget.set(JvmTarget.JVM_1_8)
        @Suppress("DEPRECATION")
        apiVersion.set(KotlinVersion.KOTLIN_1_6)
        @Suppress("DEPRECATION")
        languageVersion.set(KotlinVersion.KOTLIN_1_6)
    }
    coreLibrariesVersion = "1.6.21"
}

publishing {
    repositories {
        repositories {
            maven {
                name = "OssStaging"
                url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("OSSRH_USER")
                    password = System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }
    publications.configureEach {
        this as MavenPublication
        if (name == "pluginMaven") {
            artifact(tasks.register("emptySources", Jar::class.java) {
                archiveClassifier = "sources"
            })
            artifact(tasks.register("emptyDocs", Jar::class.java) {
                archiveClassifier = "javadoc"
            })

            groupId = project.rootProject.group.toString()
            version = project.rootProject.version.toString()
            artifactId = project.name
        }

        pom {
            name.set(project.name)
            description.set(pluginDescription)
            url.set("https://github.com/gradleup/maven-sympathy")

            scm {
                url.set("https://github.com/gradleup/maven-sympathy")
                connection.set("https://github.com/gradleup/maven-sympathy")
                developerConnection.set("https://github.com/gradleup/maven-sympathy")
            }

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/gradleup/maven-sympathy/blob/master/LICENSE")
                }
            }

            developers {
                developer {
                    id.set("GradleUp developers")
                    name.set("GradleUp developers")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)

    useInMemoryPgpKeys(System.getenv("GPG_KEY"), System.getenv("GPG_KEY_PASSWORD"))
}


fun isTag(): Boolean {
    val ref = System.getenv("GITHUB_REF")

    return ref?.startsWith("refs/tags/") == true
}

tasks.register("ci")

if (isTag()) {
    rootProject.tasks.named("ci") {
        dependsOn(tasks.named("publishAllPublicationsToOssStagingRepository"))
    }
}


tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}

tasks.withType(Sign::class.java).configureEach {
    isEnabled = System.getenv("GPG_KEY").isNullOrBlank().not()
}