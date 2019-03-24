package net.ltgt.gradle.nullaway

import net.ltgt.gradle.errorprone.ErrorProneOptions
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.util.GradleVersion

internal const val EXTENSION_NAME = "nullaway"

class NullAwayPlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_ID = "net.ltgt.nullaway"
    }

    override fun apply(project: Project) = with(project) {
        if (GradleVersion.current() < GradleVersion.version("5.2.1")) {
            throw UnsupportedOperationException("$PLUGIN_ID requires at least Gradle 5.2.1")
        }

        val extension = extensions.create(EXTENSION_NAME, NullAwayExtension::class)

        pluginManager.withPlugin(ErrorPronePlugin.PLUGIN_ID) {
            tasks.withType<JavaCompile>().configureEach {
                val nullawayOptions = (options.errorprone as ExtensionAware).extensions.create(EXTENSION_NAME, NullAwayOptions::class, extension)
                options.errorprone.errorproneArgumentProviders.add(object : CommandLineArgumentProvider, Named {
                    override fun getName() = EXTENSION_NAME

                    override fun asArguments() = nullawayOptions.asArguments()
                })
            }
        }
    }
}

val ErrorProneOptions.nullaway
    get() = (this as ExtensionAware).extensions.getByName<NullAwayOptions>(EXTENSION_NAME)
fun ErrorProneOptions.nullaway(action: Action<in NullAwayOptions>) =
    (this as ExtensionAware).extensions.configure(EXTENSION_NAME, action)
