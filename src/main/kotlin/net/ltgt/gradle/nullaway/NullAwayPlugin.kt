@file:Suppress("ktlint:standard:no-wildcard-imports")

package net.ltgt.gradle.nullaway

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.ErrorProneOptions
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.util.GradleVersion

private const val EXTENSION_NAME = "nullaway"

class NullAwayPlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_ID = "net.ltgt.nullaway"
    }

    override fun apply(project: Project) =
        with(project) {
            if (GradleVersion.current() < GradleVersion.version("6.8")) {
                throw UnsupportedOperationException("$PLUGIN_ID requires at least Gradle 6.8")
            }

            val extension = extensions.create(EXTENSION_NAME, NullAwayExtension::class)

            pluginManager.withPlugin(ErrorPronePlugin.PLUGIN_ID) {
                tasks.withType<JavaCompile>().configureEach {
                    val nullawayOptions =
                        (options.errorprone as ExtensionAware).extensions.create(
                            EXTENSION_NAME,
                            NullAwayOptions::class,
                            extension,
                        )
                    options.errorprone.errorproneArgumentProviders.add(
                        object : CommandLineArgumentProvider, Named {
                            @Internal override fun getName() = EXTENSION_NAME

                            @Suppress("unused")
                            @Nested
                            @Optional
                            fun getNullAwayOptions() =
                                nullawayOptions.takeUnless {
                                    nullawayOptions.severity.getOrElse(CheckSeverity.DEFAULT) == CheckSeverity.OFF
                                }

                            override fun asArguments() = nullawayOptions.asArguments()
                        },
                    )
                }
            }
        }
}

val ErrorProneOptions.nullaway
    get() = (this as ExtensionAware).extensions.getByName<NullAwayOptions>(EXTENSION_NAME)

fun ErrorProneOptions.nullaway(action: Action<in NullAwayOptions>) = (this as ExtensionAware).extensions.configure(EXTENSION_NAME, action)
