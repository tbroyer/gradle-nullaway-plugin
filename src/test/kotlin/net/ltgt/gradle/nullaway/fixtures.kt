package net.ltgt.gradle.nullaway

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import java.io.File

val testGradleVersion = System.getProperty("test.gradle-version", GradleVersion.current().version)

const val errorproneVersion = "2.7.1"
const val errorproneJavacVersion = "9+181-r4173-1"
const val nullawayVersion = "0.9.1"

const val FAILURE_SOURCE_COMPILATION_ERROR = "Failure.java:8: warning: [NullAway]"

fun File.writeSuccessSource() {
    File(this.resolve("src/main/java/test").apply { mkdirs() }, "Success.java").apply {
        createNewFile()
        writeText(
            """
            package test;

            public class Success {
                static void log(@Nullable Object x) {
                    if (x != null) {
                        System.out.println(x.toString());
                    }
                }
                static void foo() {
                    log(null);
                }
            }

            @interface Nullable {}
            """.trimIndent()
        )
    }
}

fun File.writeFailureSource() {
    File(this.resolve("src/main/java/test").apply { mkdirs() }, "Failure.java").apply {
        createNewFile()
        writeText(
            """
            package test;

            public class Failure {
                static void log(Object x) {
                    System.out.println(x.toString());
                }
                static void foo() {
                    log(null);
                }
            }
            """.trimIndent()
        )
    }
}

fun File.buildWithArgs(vararg tasks: String): BuildResult =
    prepareBuild(*tasks)
        .build()

fun File.buildWithArgsAndFail(vararg tasks: String): BuildResult =
    prepareBuild(*tasks)
        .buildAndFail()

private fun File.prepareBuild(vararg tasks: String): GradleRunner =
    GradleRunner.create()
        .withGradleVersion(testGradleVersion)
        .withProjectDir(this)
        .withPluginClasspath()
        .withArguments(*tasks)
