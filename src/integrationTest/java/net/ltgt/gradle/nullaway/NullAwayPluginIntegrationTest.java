package net.ltgt.gradle.nullaway;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class NullAwayPluginIntegrationTest extends BaseIntegrationTest {
  @BeforeEach
  void setup() throws Exception {
    Files.writeString(
        getBuildFile(),
        // language=kts
        """
        import net.ltgt.gradle.errorprone.*
        import net.ltgt.gradle.nullaway.nullaway

        plugins {
            `java-library`
            id("net.ltgt.errorprone")
            id("net.ltgt.nullaway")
        }

        repositories {
            mavenCentral()
        }
        dependencies {
            errorprone("com.google.errorprone:error_prone_core:%s")
            errorprone("com.uber.nullaway:nullaway:%s")

            compileOnlyApi("org.jspecify:jspecify:1.0.1")
        }

        tasks.withType<JavaCompile>().configureEach {
            options.errorprone.nullaway.error()
        }

        nullaway {
            annotatedPackages.add("test")
        }
        """
            .formatted(errorproneVersion, nullawayVersion));
  }

  @Test
  void missingAnnotatedPackagesOption() throws Exception {
    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

        nullaway {
            annotatedPackages.empty()
        }
        """,
        StandardOpenOption.APPEND);
    writeSuccessSource();

    // when
    var result = buildWithArgsAndFail("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.FAILED);
    assertThat(result.getOutput())
        .contains(
            " specify annotated packages, using the -XepOpt:NullAway:AnnotatedPackages=[...] flag");
  }

  @Test
  void compilationSucceeds() throws Exception {
    // given
    writeSuccessSource();

    // when
    var result = buildWithArgs("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);
  }

  @Test
  void compilationFails() throws Exception {
    // given
    writeFailureSource();

    // when
    var result = buildWithArgsAndFail("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.FAILED);
    assertThat(result.getOutput()).contains(FAILURE_SOURCE_COMPILATION_ERROR);
  }

  @Test
  void onlyNullMarkedOption_falsePositiveIfUnannotated() throws Exception {
    assume().that(nullawaySupportsOnlyNullMarked).isTrue();
    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

        nullaway {
            onlyNullMarked.set(true)
            annotatedPackages.empty()
        }
        """,
        StandardOpenOption.APPEND);
    writeFailureSource();

    // when
    var result = buildWithArgs("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);
  }

  @Test
  void onlyNullMarkedOption_compilationFails() throws Exception {
    assume().that(nullawaySupportsOnlyNullMarked).isTrue();
    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

        nullaway {
            onlyNullMarked.set(true)
            annotatedPackages.empty()
        }
        dependencies {
            implementation("org.jspecify:jspecify:1.0.1")
        }
        """,
        StandardOpenOption.APPEND);
    writeFailureSource();
    Files.writeString(
        projectDir.resolve("src/main/java/test/package-info.java"),
        // language=java
        """
        @NullMarked
        package test;

        import org.jspecify.annotations.NullMarked;
        """);

    // when
    var result = buildWithArgsAndFail("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.FAILED);
    assertThat(result.getOutput()).contains(FAILURE_SOURCE_COMPILATION_ERROR);
  }

  @Test
  void requireExplicitNullMarkingOption() throws Exception {
    assume().that(nullawaySupportsRequiresExplicitNullMarking).isTrue();

    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

        tasks.withType<JavaCompile>().configureEach {
            options.errorprone.nullaway {
                requireExplicitNullMarking { error() }
            }
        }
        """,
        StandardOpenOption.APPEND);
    writeSuccessSource();

    // when
    var result = buildWithArgsAndFail("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.FAILED);
    assertThat(result.getOutput()).contains(FAILURE_REQUIRE_EXPLICIT_NULL_MARKING_ERROR);
  }

  @Test
  void jspecifyUnrecognizedAnnotationLocation() throws Exception {
    assume().that(nullawaySupportsJSpecifyUnrecognizedAnnotationLocation).isTrue();

    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

        tasks.withType<JavaCompile>().configureEach {
            options.errorprone.nullaway {
                jspecifyUnrecognizedAnnotationLocation { error() }
            }
        }
        """,
        StandardOpenOption.APPEND);
    Files.writeString(
        Files.createDirectories(projectDir.resolve("src/main/java/test")).resolve("Failure.java"),
        // language=java
        """
        package test;

        import org.jspecify.annotations.Nullable;

        public class Failure<@Nullable T> {}
        """);

    // when
    var result = buildWithArgsAndFail("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.FAILED);
    assertThat(result.getOutput())
        .contains("Failure.java:5: error: [JSpecifyUnrecognizedAnnotationLocation]");
  }

  @Test
  void canDisableNullAway() throws Exception {
    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

        tasks.withType<JavaCompile>().configureEach {
            options.errorprone.nullaway.disable()
        }
        """,
        StandardOpenOption.APPEND);
    writeFailureSource();

    // when
    var result = buildWithArgs("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);
  }

  @Test
  void canConfigureNullAway() throws Exception {
    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

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
                assertsEnabled.set(true)
                exhaustiveOverride.set(true)
                castToNonNullMethod.set("com.foo.Bar.castToNonNull")
                checkOptionalEmptinessCustomClasses.add("com.foo.Optional")
                autoFixSuppressionComment.set("Auto-fix\\\\u0020suppression")
                handleTestAssertionLibraries.set(true)
                acknowledgeAndroidRecent.set(true)
                checkContracts.set(true)
                customContractAnnotations.add("com.example.Contract")
                customNullableAnnotations.add("com.example.CouldBeNull")
                customNonnullAnnotations.add("com.example.MustNotBeNull")
                jspecifyMode.set(true)
                extraFuturesClasses.add("com.example.Future")
                suppressionNameAliases.add("NullIssue")
                warnOnGenericInferenceFailure.set(true)
                handleWildcardGenerics.set(true)
                jspecifyExperimental.set(true)
                jspecifyJdkModels.set(true)
                requireExplicitNullMarking { severity.unset() }
                jspecifyUnrecognizedAnnotationLocation { severity.unset() }
            }
        }
        """,
        StandardOpenOption.APPEND);
    writeSuccessSource();

    // when
    var result = buildWithArgs("compileJava");

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);
  }

  @Test
  void playsNicelyWithUpToDateChecks() throws Exception {
    // given
    Files.writeString(
        getBuildFile(),
        // language=kts
        """

        tasks.withType<JavaCompile>().configureEach {
            options.errorprone {
                // Make it work even in NullAway versions without the additional checks
                ignoreUnknownCheckNames = true
                nullaway {
                    if (project.hasProperty("disable-nullaway")) {
                        disable()
                    }
                    if (project.hasProperty("require-explicit-null-marking")) {
                        requireExplicitNullMarking.warn()
                    }
                    if (project.hasProperty("jspecify-unrecognized-annotation-location")) {
                        jspecifyUnrecognizedAnnotationLocation.warn()
                    }
                    autoFixSuppressionComment.set(project.property("autofix-comment") as String)
                }
            }
        }
        """,
        StandardOpenOption.APPEND);
    writeSuccessSource();

    // when
    var result = buildWithArgs("compileJava", "-Pautofix-comment=foo");
    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);

    // when
    result = buildWithArgs("compileJava", "-Pautofix-comment=bar");
    // then
    // (specifically, we don't want UP_TO_DATE)
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);

    // when
    result = buildWithArgs("compileJava", "-Pdisable-nullaway", "-Pautofix-comment=bar");
    // then
    // (specifically, we don't want UP_TO_DATE)
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);

    // when
    result = buildWithArgs("compileJava", "-Pdisable-nullaway", "-Pautofix-comment=baz");
    // then
    // Changing a property while the check is disabled has no impact on up-to-date checks
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.UP_TO_DATE);

    // when
    result =
        buildWithArgs(
            "compileJava",
            "-Pdisable-nullaway",
            "-Pautofix-comment=baz",
            "-Prequire-explicit-null-marking");
    // then
    // (specifically, we don't want UP_TO_DATE)
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);

    // when
    result =
        buildWithArgs(
            "compileJava",
            "-Pdisable-nullaway",
            "-Pautofix-comment=baz",
            "-Prequire-explicit-null-marking",
            "-Pjspecify-unrecognized-annotation-location");
    // then
    // (specifically, we don't want UP_TO_DATE)
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);
  }

  @Test
  void isConfigurationCacheFriendly() throws Exception {
    // given
    writeSuccessSource();

    // Prime the configuration cache
    buildWithArgs("--configuration-cache", "compileJava");

    // when
    var result = buildWithArgs("--configuration-cache", "compileJava");

    // then
    assertThat(result.getOutput()).contains("Reusing configuration cache.");
  }

  // Inspired by
  // https://docs.gradle.org/current/userguide/build_cache.html#sec:task_output_caching_example
  @Test
  @DisabledOnOs(OS.WINDOWS) // See https://github.com/gradle/gradle/issues/12535
  void isBuildCacheFriendly(@TempDir Path testKitDir, @TempDir Path otherDir) throws Exception {
    // given
    writeSuccessSource();

    // Prime the build cache
    var result =
        prepareBuild("--build-cache", "compileJava").withTestKitDir(testKitDir.toFile()).build();
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.SUCCESS);

    // Delete the local state
    prepareBuild("clean").withTestKitDir(testKitDir.toFile()).build();

    // when
    result =
        prepareBuild("--build-cache", "compileJava").withTestKitDir(testKitDir.toFile()).build();

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.FROM_CACHE);

    // Test "relocatability"
    Files.move(projectDir, otherDir, StandardCopyOption.REPLACE_EXISTING);

    // when
    result =
        prepareBuild("--build-cache", "compileJava")
            .withTestKitDir(testKitDir.toFile())
            .withProjectDir(otherDir.toFile())
            .build();

    // then
    assertThat(requireNonNull(result.task(":compileJava")).getOutcome())
        .isEqualTo(TaskOutcome.FROM_CACHE);
  }
}
