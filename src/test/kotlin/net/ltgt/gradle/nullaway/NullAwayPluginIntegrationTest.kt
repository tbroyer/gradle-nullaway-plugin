package net.ltgt.gradle.nullaway

import net.ltgt.gradle.errorprone.ErrorPronePlugin

class NullAwayPluginIntegrationTest : AbstractPluginIntegrationTest(
    buildFileContent =
        """
        plugins {
            `java-library`
            id("${ErrorPronePlugin.PLUGIN_ID}")
            id("${NullAwayPlugin.PLUGIN_ID}")
        }
    """,
    compileTaskName = ":compileJava"
)
