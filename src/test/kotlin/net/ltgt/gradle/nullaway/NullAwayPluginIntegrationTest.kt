package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.TruthJUnit.assume
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import org.gradle.api.JavaVersion
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
        assume().that(GradleVersion.version(testGradleVersion)).isAtLeast(GradleVersion.version("6.6"))
        assume().that(
            JavaVersion.current() < JavaVersion.VERSION_16 ||
                GradleVersion.version(testGradleVersion).baseVersion >= GradleVersion.version("7.0")
        ).isTrue()

        // given
        testProjectDir.writeSuccessSource()

        // Prime the configuration cache
        testProjectDir.buildWithArgs("--configuration-cache", "compileJava")

        // when
        val result = testProjectDir.buildWithArgs("--configuration-cache", "compileJava")

        // then
        assertThat(result.output).contains("Reusing configuration cache.")
    }
}
