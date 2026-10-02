import net.ltgt.gradle.errorprone.errorprone
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.plugin.compatibility.compatibility

plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.errorprone)
    alias(libs.plugins.nullaway)
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.spotless)
    alias(libs.plugins.nosphereGithubActions)
}

group = "net.ltgt.gradle"

dependencies {
    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

nullaway {
    onlyNullMarked = true
    jspecifyMode = true
}
tasks {
    withType<JavaCompile>().configureEach {
        options.release = 21
        options.compilerArgs.addAll(listOf("-Werror", "-Xlint:all"))
        options.errorprone {
            error("RequireExplicitNullMarking")
            error("JSpecifyUnrecognizedAnnotationLocation")
        }
    }
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            noTimestamp()
            quiet()
            addBooleanOption("Xdoclint:-missing", true)
        }
    }
}

tasks.compileJava {
    options.release = 8
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:-options,-processing",
            "-Anet.ltgt.gradle.kotlin.accessors.generator.kotlinModuleName=${project.name}",
        ),
    )
    options.errorprone {
        // Gradle uses javax.inject in a specific way
        disable("InjectOnConstructorOfAbstractClass")
    }
}

// See https://github.com/gradle/gradle/issues/7974
val additionalPluginClasspath = configurations.create("additionalPluginClasspath")

// The ErrorProne plugin is in the [plugins] section for better Dependabot integration
val LibrariesForLibs.errorproneGradlePlugin
    get() = plugins.errorprone.map { dependencies.create("${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}") }

dependencies {
    compileOnly(libs.errorproneGradlePlugin)

    additionalPluginClasspath(libs.errorproneGradlePlugin)

    compileOnly(libs.kotlinAccessorsGenerator.annotations)
    annotationProcessor(libs.kotlinAccessorsGenerator.processor)
}

tasks {
    pluginUnderTestMetadata {
        this.pluginClasspath.from(additionalPluginClasspath)
    }
    check {
        dependsOn(testing.suites)
    }
}
testing {
    suites {
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter(libs.versions.junitJupiter)
            dependencies {
                implementation(libs.truth)
            }
            targets.configureEach {
                testTask {
                    testLogging {
                        showExceptions = true
                        showStackTraces = true
                        exceptionFormat = TestExceptionFormat.FULL
                    }
                }
            }
        }
        val test =
            named<JvmTestSuite>("test") {
                dependencies {
                    implementation(project())
                    implementation(libs.errorproneGradlePlugin)
                    implementation(libs.errorprone.checkApi) {
                        exclude(group = "com.google.errorprone", module = "javac")
                    }
                }
            }
        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(gradleTestKit())
            }
            // make plugin-under-test-metadata.properties accessible to TestKit
            gradlePlugin.testSourceSet(sources)
            targets.configureEach {
                testTask {
                    shouldRunAfter(test)

                    val testJavaToolchain = project.findProperty("test.java-toolchain")
                    testJavaToolchain?.also {
                        val launcher =
                            project.javaToolchains.launcherFor {
                                languageVersion.set(JavaLanguageVersion.of(testJavaToolchain.toString()))
                            }
                        val metadata = launcher.get().metadata
                        systemProperty("test.java-version", metadata.languageVersion.asInt())
                        systemProperty("test.java-home", metadata.installationPath.asFile.canonicalPath)
                    }

                    val testGradleVersion = project.findProperty("test.gradle-version")
                    testGradleVersion?.also { systemProperty("test.gradle-version", testGradleVersion) }

                    systemProperty("errorprone.version", libs.versions.errorprone.get())
                    systemProperty("nullaway.version", libs.versions.nullaway.get())
                }
            }
        }
    }
}

gradlePlugin {
    website.set("https://github.com/tbroyer/gradle-nullaway-plugin")
    vcsUrl.set("https://github.com/tbroyer/gradle-nullaway-plugin")
    plugins {
        register("nullaway") {
            id = "net.ltgt.nullaway"
            displayName = "Adds NullAway DSL to Gradle Error Prone plugin"
            description = "Adds NullAway DSL to Gradle Error Prone plugin"
            implementationClass = "net.ltgt.gradle.nullaway.NullAwayPlugin"
            tags.addAll("javac", "error-prone", "nullaway", "nullability")
            compatibility {
                features {
                    configurationCache = true
                    isolatedProjects = true
                }
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Adds NullAway DSL to Gradle Error Prone plugin")
            description.set("Adds NullAway DSL to Gradle Error Prone plugin")
            url.set("https://github.com/tbroyer/gradle-nullaway-plugin")
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            developers {
                developer {
                    name.set("Thomas Broyer")
                    email.set("t.broyer@ltgt.net")
                }
            }
            scm {
                connection.set("https://github.com/tbroyer/gradle-nullaway-plugin.git")
                developerConnection.set("scm:git:ssh://github.com:tbroyer/gradle-nullaway-plugin.git")
                url.set("https://github.com/tbroyer/gradle-nullaway-plugin")
            }
        }
    }
}

spotless {
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
    java {
        googleJavaFormat(libs.versions.googleJavaFormat.get())
    }
}
