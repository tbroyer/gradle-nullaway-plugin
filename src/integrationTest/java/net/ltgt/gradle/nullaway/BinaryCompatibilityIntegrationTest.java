package net.ltgt.gradle.nullaway;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BinaryCompatibilityIntegrationTest extends BaseIntegrationTest {
  @TempDir Path conventionPluginProjectDir;
  Path conventionPluginBuildFile;

  @BeforeEach
  void setup() throws Exception {
    Files.writeString(
        getBuildFile(),
        // language=kts
        """
        plugins {
            `java-library`
            id("net.ltgt.errorprone")
            id("net.ltgt.nullaway")
            id("convention.nullaway")
        }

        repositories {
            mavenCentral()
        }

        dependencies {
            errorprone("com.google.errorprone:error_prone_core:%s")
            errorprone("com.uber.nullaway:nullaway:%s")
        }

        tasks.withType<JavaCompile>().configureEach {
            options.compilerArgs.add("-Werror")
        }

        if (GradleVersion.current() < GradleVersion.version("7.0")) {
            allprojects {
                configurations.all {
                    attributes.attribute(Attribute.of("org.gradle.jvm.environment", String::class.java), "standard-jvm")
                }
            }
        }
        """
            .formatted(errorproneVersion, nullawayVersion));
    Files.copy(
        projectDir.resolve("gradle.properties"),
        conventionPluginProjectDir.resolve("gradle.properties"));
    Files.writeString(
        conventionPluginProjectDir.resolve("settings.gradle.kts"),
        // language=kts
        """
        rootProject.name = "convention-plugin"
        """);
    conventionPluginBuildFile =
        Files.createFile(conventionPluginProjectDir.resolve("build.gradle.kts"));
  }

  private void buildConventionPlugin() {
    GradleRunner.create()
        .withGradleVersion(testGradleVersion.getVersion())
        .withProjectDir(conventionPluginProjectDir.toFile())
        .withArguments("assemble")
        .build();
  }

  private BuildResult buildProject(String... args) {
    var runner =
        GradleRunner.create()
            .withGradleVersion(testGradleVersion.getVersion())
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath();
    return runner
        .withPluginClasspath(
            Stream.concat(
                    runner.getPluginClasspath().stream(),
                    Stream.of(
                        conventionPluginProjectDir
                            .resolve("build/libs/convention-plugin.jar")
                            .toFile()))
                .toList())
        .withArguments(args)
        .build();
  }

  @Test
  void isBinaryCompatibleWithPreviousVersions_kotlinDslFlavor() throws Exception {
    // given
    Files.writeString(
        conventionPluginBuildFile,
        // language=kts
        """
        plugins {
            `kotlin-dsl`
        }

        repositories {
            gradlePluginPortal()
        }

        dependencies {
            implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.4.0")
            implementation("net.ltgt.nullaway:net.ltgt.nullaway.gradle.plugin:2.4.1")
        }
        """);
    Files.writeString(
        Files.createDirectories(conventionPluginProjectDir.resolve("src/main/kotlin"))
            .resolve("convention.nullaway.gradle.kts"),
        // language=kts
        """
        import net.ltgt.gradle.errorprone.*
        import net.ltgt.gradle.nullaway.*

        plugins {
            id("net.ltgt.errorprone")
            id("net.ltgt.nullaway")
        }

        configure<NullAwayExtension> {
            annotatedPackages.add("test")
        }

        tasks.withType<JavaCompile>().configureEach {
            options.errorprone.nullaway {
                severity.set(CheckSeverity.DEFAULT)
                onlyNullMarked.set(false)
                unannotatedSubPackages.add("test.dummy")
                unannotatedClasses.add("test.Unannotated")
                knownInitializers.add("com.foo.Bar.method")
                excludedClassAnnotations.add("com.example.NullAwayExcluded")
                excludedClasses.add("test.Excluded")
                excludedFieldAnnotations.add("javax.ws.rs.core.Context")
                customInitializerAnnotations.add("com.foo.Initializer")
                externalInitAnnotations.add("com.example.ExternalInit")
                treatGeneratedAsUnannotated.set(true)
                acknowledgeRestrictiveAnnotations.set(true)
                checkOptionalEmptiness.set(true)
                suggestSuppressions.set(true)
                isAssertsEnabled.set(true)
                isExhaustiveOverride.set(true)
                castToNonNullMethod.set("com.foo.Bar.castToNonNull")
                checkOptionalEmptinessCustomClasses.add("com.foo.Optional")
                autoFixSuppressionComment.set("Auto-fix\\\\u0020suppression")
                handleTestAssertionLibraries.set(true)
                acknowledgeAndroidRecent.set(true)
                checkContracts.set(true)
                customContractAnnotations.add("com.example.Contract")
                customNullableAnnotations.add("com.example.CouldBeNull")
                customNonnullAnnotations.add("com.example.MustNotBeNull")
                isJSpecifyMode.set(true)
                extraFuturesClasses.add("com.example.Future")
                suppressionNameAliases.add("NullIssue")
                warnOnGenericInferenceFailure.set(true)
            }
        }
        """);

    buildConventionPlugin();

    writeSuccessSource();

    // when
    var result = buildProject("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);
  }

  @Test
  void isBinaryCompatibleWithPreviousVersions_javaFlavor() throws Exception {
    // given
    Files.writeString(
        conventionPluginBuildFile,
        // language=kts
        """
        plugins {
            `java-gradle-plugin`
        }

        repositories {
            gradlePluginPortal()
        }

        dependencies {
            implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.4.0")
            implementation("net.ltgt.nullaway:net.ltgt.nullaway.gradle.plugin:2.4.1")
        }

        gradlePlugin {
            plugins {
                create("conventionPlugin") {
                    id = "convention.nullaway"
                    implementationClass = "convention.ConventionNullAwayPlugin"
                }
            }
        }
        """);
    Files.writeString(
        Files.createDirectories(conventionPluginProjectDir.resolve("src/main/java/convention"))
            .resolve("ConventionNullAwayPlugin.java"),
        // language=java
        """
        package convention;

        import net.ltgt.gradle.errorprone.*;
        import net.ltgt.gradle.nullaway.*;
        import org.gradle.api.Plugin;
        import org.gradle.api.Project;
        import org.gradle.api.plugins.ExtensionAware;
        import org.gradle.api.tasks.compile.JavaCompile;

        public class ConventionNullAwayPlugin implements Plugin<Project> {
          @Override
          public void apply(Project project) {
            project.getPluginManager().apply("net.ltgt.errorprone");
            project.getPluginManager().apply("net.ltgt.nullaway");

            project
                .getExtensions()
                .configure(
                    NullAwayExtension.class,
                    nullaway -> {
                      nullaway.getAnnotatedPackages().add("test");
                    });

            project
                .getTasks()
                .withType(JavaCompile.class)
                .configureEach(
                    task -> {
                      ErrorProneOptions errorproneOptions =
                          ((ExtensionAware) task.getOptions())
                              .getExtensions()
                              .getByType(ErrorProneOptions.class);
                      NullAwayOptions nullawayOptions =
                          ((ExtensionAware) errorproneOptions)
                              .getExtensions()
                              .getByType(NullAwayOptions.class);
                      nullawayOptions.getSeverity().set(CheckSeverity.DEFAULT);
                      nullawayOptions.getOnlyNullMarked().set(false);
                      nullawayOptions.getUnannotatedSubPackages().add("test.dummy");
                      nullawayOptions.getUnannotatedClasses().add("test.Unannotated");
                      nullawayOptions.getKnownInitializers().add("com.foo.Bar.method");
                      nullawayOptions.getExcludedClassAnnotations().add("com.example.NullAwayExcluded");
                      nullawayOptions.getExcludedClasses().add("test.Excluded");
                      nullawayOptions.getExcludedFieldAnnotations().add("javax.ws.rs.core.Context");
                      nullawayOptions.getCustomInitializerAnnotations().add("com.foo.Initializer");
                      nullawayOptions.getExternalInitAnnotations().add("com.example.ExternalInit");
                      nullawayOptions.getTreatGeneratedAsUnannotated().set(true);
                      nullawayOptions.getAcknowledgeRestrictiveAnnotations().set(true);
                      nullawayOptions.getCheckOptionalEmptiness().set(true);
                      nullawayOptions.getSuggestSuppressions().set(true);
                      nullawayOptions.getAssertsEnabled().set(true);
                      nullawayOptions.getExhaustiveOverride().set(true);
                      nullawayOptions.getCastToNonNullMethod().set("com.foo.Bar.castToNonNull");
                      nullawayOptions.getCheckOptionalEmptinessCustomClasses().add("com.foo.Optional");
                      nullawayOptions.getAutoFixSuppressionComment().set("Auto-fix\\\\u0020suppression");
                      nullawayOptions.getHandleTestAssertionLibraries().set(true);
                      nullawayOptions.getAcknowledgeAndroidRecent().set(true);
                      nullawayOptions.getCheckContracts().set(true);
                      nullawayOptions.getCustomContractAnnotations().add("com.example.Contract");
                      nullawayOptions.getCustomNullableAnnotations().add("com.example.CouldBeNull");
                      nullawayOptions.getCustomNonnullAnnotations().add("com.example.MustNotBeNull");
                      nullawayOptions.getJspecifyMode().set(true);
                      nullawayOptions.getExtraFuturesClasses().add("com.example.Future");
                      nullawayOptions.getSuppressionNameAliases().add("NullIssue");
                      nullawayOptions.getWarnOnGenericInferenceFailure().set(true);
                    });
          }
        }
        """);

    buildConventionPlugin();

    writeSuccessSource();

    // when
    var result = buildProject("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);
  }
}
