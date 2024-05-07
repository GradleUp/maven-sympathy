# Maven Sympathy

A small Gradle plugin that checks that your project dependencies play nice with Maven resolution strategy. 

See https://jakewharton.com/nonsensical-maven-is-still-a-gradle-problem/ for more details.

# Usage:

The plugin is available on Maven Central

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.gradleup.maven-sympathy:maven-sympathy:0.0.2")
    }
}

plugins {
    id("com.gradleup.maven-sympathy")
}
```



It registers a `sympathyForMrMaven` task that checks that none of your project transitive dependencies upgrades the direct ones:

```
$ ./gradlew :sympathyForMrMaven

> Task :sympathyForMrMaven FAILED
e: direct dependency org.jetbrains.kotlin:kotlin-stdlib-common:1.8.21 of configuration 'allSourceSetsCompileDependenciesMetadata' was changed to 1.9.21
e: direct dependency org.jetbrains.kotlin:kotlin-stdlib:1.8.21 of configuration 'allSourceSetsCompileDependenciesMetadata' was changed to 1.9.21

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':sympathyForMrMaven'.
> Declared dependencies were upgraded transitively. See task output above. Please update their versions.
```

`sympathyForMrMaven` is also added as a dependency to the "check" aggregate task. 
