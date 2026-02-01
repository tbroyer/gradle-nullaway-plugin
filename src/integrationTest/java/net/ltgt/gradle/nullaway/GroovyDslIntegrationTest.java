package net.ltgt.gradle.nullaway;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroovyDslIntegrationTest extends BaseIntegrationTest {
  @Override
  protected Path getSettingsFile() {
    return projectDir.resolve("settings.gradle");
  }

  @Override
  protected Path getBuildFile() {
    return projectDir.resolve("build.gradle");
  }

  @BeforeEach
  void setup() throws Exception {
    Files.writeString(
        getBuildFile(),
        // language=groovy
        """
        import net.ltgt.gradle.errorprone.CheckSeverity

        plugins {
            id("java-library")
            id("net.ltgt.errorprone")
            id("net.ltgt.nullaway")
        }

        repositories {
            mavenCentral()
        }
        dependencies {
            errorprone "com.google.errorprone:error_prone_core:%s"
            errorprone "com.uber.nullaway:nullaway:%s"
        }

        tasks.withType(JavaCompile).configureEach {
            options.compilerArgs.add("-Werror")
        }

        nullaway {
            annotatedPackages.add("test")
        }

        if (GradleVersion.current() < GradleVersion.version("7.0")) {
            allprojects {
                configurations.all {
                    attributes.attribute(Attribute.of("org.gradle.jvm.environment", String), "standard-jvm")
                }
            }
        }
        """
            .formatted(errorproneVersion, nullawayVersion));
  }

  @Test
  void canDisableNullAway() throws Exception {
    // given
    Files.writeString(
        getBuildFile(),
        // language=groovy
        """

        tasks.withType(JavaCompile).configureEach {
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
        // language=groovy
        """

        tasks.withType(JavaCompile).configureEach {
            options.errorprone.nullaway {
                severity = CheckSeverity.DEFAULT
                onlyNullMarked = false
                annotatedPackages = project.nullaway.annotatedPackages
                unannotatedSubPackages = ["test.dummy"]
                unannotatedClasses = ["test.Unannotated"]
                knownInitializers = ["com.foo.Bar.method"]
                excludedClassAnnotations = ["com.example.NullAwayExcluded"]
                excludedClasses = ["test.Excluded"]
                excludedFieldAnnotations = ["javax.ws.rs.core.Context"]
                customInitializerAnnotations = ["com.foo.Initializer"]
                externalInitAnnotations = ["com.example.ExternalInit"]
                treatGeneratedAsUnannotated = true
                acknowledgeRestrictiveAnnotations = true
                checkOptionalEmptiness = true
                suggestSuppressions = true
                assertsEnabled = true
                exhaustiveOverride = true
                castToNonNullMethod = "com.foo.Bar.castToNonNull"
                checkOptionalEmptinessCustomClasses = ["com.foo.Optional"]
                autoFixSuppressionComment = "Auto-fix\\\\u0020suppression"
                handleTestAssertionLibraries = true
                acknowledgeAndroidRecent = true
                checkContracts = true
                customContractAnnotations = ["com.example.Contract"]
                customNullableAnnotations = ["com.example.CouldBeNull"]
                customNonnullAnnotations = ["com.example.MustNotBeNull"]
                customGeneratedCodeAnnotations = ["com.example.Generated"]
                jspecifyMode = true
                extraFuturesClasses = ["com.example.Future"]
                suppressionNameAliases = ["NullIssue"]
                warnOnGenericInferenceFailure = true
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
}
