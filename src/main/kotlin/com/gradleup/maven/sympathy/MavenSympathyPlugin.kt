package com.gradleup.maven.sympathy

import org.gradle.api.Plugin
import org.gradle.api.Project

class MavenSympathyPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        val taskProvider = target.tasks.register("sympathyForMrMaven", SympathyForMrMaven::class.java) {
            it.group = "verification"
            it.description = "Checks that your project dependencies play nice with Maven resolution strategy. See https://jakewharton.com/nonsensical-maven-is-still-a-gradle-problem/ for more details."
            it.configurations = target.project.configurations
        }

        target.tasks.named("check") {
            it.dependsOn(taskProvider)
        }
    }
}