package com.gradleup.maven.sympathy

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.serialization.Cached
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class SympathyForMrMaven : DefaultTask() {
    private val model = Cached.of(::calculateReport)

    @TaskAction
    fun taskAction() {
        var fail = false
        model.get().upgrades.forEach {
            logger.error("e: direct dependency ${it.requested} of configuration '${it.configuration}' was changed to ${it.selectedVersion}")
            fail = true
        }
        if (fail) {
            throw IllegalStateException("Declared dependencies were upgraded transitively. See task output above. Please update their versions.")
        }
    }

    class Upgrade(val configuration: String, val requested: String, val selectedVersion: String)
    class Report(val upgrades: List<Upgrade>)

    private fun calculateReport(): Report {
        return project.configurations.filter {
            it.isCanBeResolved
        }.flatMap {
            findUpgrades(it)
        }.let {
            Report(it)
        }
    }

    /**
     * See https://jakewharton.com/nonsensical-maven-is-still-a-gradle-problem/
     */
    private fun findUpgrades(configuration: Configuration): List<Upgrade> {
        val upgrades = mutableListOf<Upgrade>()
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
                        upgrades.add(Upgrade(configuration.name, rdr.requested.toString(), selectedVersion.toString()))
                    }
                }
            }
        }

        return upgrades
    }
}