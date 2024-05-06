package com.gradleup.maven.sympathy

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class SympathyForMrMaven : DefaultTask() {
    @get:Internal
    abstract var configurations: ConfigurationContainer

    @TaskAction
    fun taskAction() {
        configurations.filter {
            it.isCanBeResolved
        }.forEach {
            checkConfiguration(it)
        }
    }

    /**
     * See https://jakewharton.com/nonsensical-maven-is-still-a-gradle-problem/
     */
    private fun checkConfiguration(configuration: Configuration) {
        var fail = false
        val root = configuration.incoming.resolutionResult.rootComponent.get()
        (root as ResolvedComponentResult).dependencies.forEach {
            if (it is ResolvedDependencyResult) {
                val rdr = it
                val requested = rdr.requested
                val selected = rdr.selected
                if (requested is ModuleComponentSelector) {
                    val requestedVersion = requested.version
                    val selectedVersion = selected.moduleVersion?.version
                    if (requestedVersion != selectedVersion) {
                        logger.error("e: ${rdr.requested} changed to $selectedVersion")
                        fail = true
                    }
                }
            }
        }
        if (fail) {
            throw IllegalStateException("Declared dependencies were upgraded transitively. See task output above. Please update their versions.")
        }
    }
}