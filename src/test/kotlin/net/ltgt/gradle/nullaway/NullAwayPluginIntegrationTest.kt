package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth
import com.google.common.truth.TruthJUnit
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Test

class NullAwayPluginIntegrationTest : AbstractPluginIntegrationTest(
    buildFileContent = """
        plugins {
            `java-library`
            id("${ErrorPronePlugin.PLUGIN_ID}")
            id("${NullAwayPlugin.PLUGIN_ID}")
        }
    """,
    compileTaskName = ":compileJava"
) {

    @Test
    fun `is configuration-cache friendly`() {
        TruthJUnit.assume().that(GradleVersion.version(testGradleVersion)).isAtLeast(GradleVersion.version("6.6"))

        // given
        testProjectDir.writeSuccessSource()

        // Prime the configuration cache
        testProjectDir.buildWithArgs("--configuration-cache", "compileJava")

        // when
        val result = testProjectDir.buildWithArgs("--configuration-cache", "compileJava")

        // then
        Truth.assertThat(result.output).contains("Reusing configuration cache.")
    }
}
