package net.ltgt.gradle.nullaway

import com.google.common.truth.TruthJUnit.assume
import java.io.File
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.BeforeEach

class AndroidIntegrationTest : AbstractPluginIntegrationTest(
    buildFileContent =
        """
        plugins {
            id("${ErrorPronePlugin.PLUGIN_ID}")
            id("${NullAwayPlugin.PLUGIN_ID}")
            id("com.android.application")
        }

        android {
            compileSdkVersion(28)
            defaultConfig {
                minSdkVersion(15)
                targetSdkVersion(28)
                versionCode = 1
                versionName = "1.0"
            }
        }

        repositories {
            google()
        }
    """,
    compileTaskName = ":compileDebugJavaWithJavac"
) {
    @BeforeEach
    fun setupAndroid() {
        assume().that(GradleVersion.version(testGradleVersion)).isAtLeast(GradleVersion.version("4.10.1"))

        File(testProjectDir.resolve("src/main").apply { mkdirs() }, "AndroidManifest.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example">
            </manifest>
            """.trimIndent()
        )
    }
}
