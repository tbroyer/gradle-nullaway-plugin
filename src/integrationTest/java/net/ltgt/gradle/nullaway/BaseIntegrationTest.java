package net.ltgt.gradle.nullaway;

import static com.google.common.truth.TruthJUnit.assume;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.gradle.api.JavaVersion;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class BaseIntegrationTest {
  public static final GradleVersion testGradleVersion =
      Optional.ofNullable(System.getProperty("test.gradle-version"))
          .map(GradleVersion::version)
          .orElseGet(GradleVersion::current);

  public static final JavaVersion testJavaVersion =
      Optional.ofNullable(System.getProperty("test.java-version"))
          .map(JavaVersion::toVersion)
          .orElseGet(JavaVersion::current);
  public static final String testJavaHome =
      System.getProperty("test.java-home", System.getProperty("java.home"));

  public static final String errorproneVersion = computeJvmCompatibleErrorProneVersion();

  private static String computeJvmCompatibleErrorProneVersion() {
    if (testJavaVersion.isCompatibleWith(JavaVersion.VERSION_21)) {
      return requireNonNull(System.getProperty("errorprone.version"));
    }
    if (testJavaVersion.isCompatibleWith(JavaVersion.VERSION_17)) {
      return "2.42.0";
    }
    if (testJavaVersion.isCompatibleWith(JavaVersion.VERSION_11)) {
      return "2.31.0";
    }
    return "2.10.0";
  }

  public static String nullawayVersion = computeJvmCompatibleNullAwayVersion();

  private static String computeJvmCompatibleNullAwayVersion() {
    if (testJavaVersion.isCompatibleWith(JavaVersion.VERSION_17)) {
      return requireNonNull(System.getProperty("nullaway.version"));
    }
    if (testJavaVersion.isCompatibleWith(JavaVersion.VERSION_11)) {
      return "0.12.15";
    }
    return "0.10.26";
  }

  // XXX: same test (reversed) as in nullawayVersion above
  public static final boolean nullawaySupportsOnlyNullMarked =
      testJavaVersion.isCompatibleWith(JavaVersion.VERSION_11);

  @TempDir protected Path projectDir;

  protected Path getSettingsFile() {
    return projectDir.resolve("settings.gradle.kts");
  }

  protected Path getBuildFile() {
    return projectDir.resolve("build.gradle.kts");
  }

  @BeforeEach
  void setupProject() throws Exception {
    assumeCompatibleGradleAndJavaVersions();

    var gradleProperties = new Properties();
    gradleProperties.setProperty("org.gradle.java.home", testJavaHome);
    try (var os = Files.newOutputStream(projectDir.resolve("gradle.properties"))) {
      gradleProperties.store(os, null);
    }

    Files.createFile(getSettingsFile());
    Files.createFile(getBuildFile());
  }

  protected static final String FAILURE_SOURCE_COMPILATION_ERROR =
      "Failure.java:8: warning: [NullAway]";

  protected final void writeSuccessSource() throws IOException {
    Files.writeString(
        Files.createDirectories(projectDir.resolve("src/main/java/test")).resolve("Success.java"),
        // language=java
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
        """);
  }

  protected final void writeFailureSource() throws IOException {
    Files.writeString(
        Files.createDirectories(projectDir.resolve("src/main/java/test")).resolve("Failure.java"),
        // language=java
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
        """);
  }

  protected final BuildResult buildWithArgs(String... args) throws Exception {
    return prepareBuild(args).build();
  }

  protected final BuildResult buildWithArgsAndFail(String... args) throws Exception {
    return prepareBuild(args).buildAndFail();
  }

  protected final GradleRunner prepareBuild(String... args) throws Exception {
    return GradleRunner.create()
        .withGradleVersion(testGradleVersion.getVersion())
        .withProjectDir(projectDir.toFile())
        .withPluginClasspath()
        .withArguments(args);
  }

  // Based on https://docs.gradle.org/current/userguide/compatibility.html#java_runtime
  private static final Map<JavaVersion, GradleVersion> COMPATIBLE_GRADLE_VERSIONS =
      Map.of(
          JavaVersion.VERSION_16, GradleVersion.version("7.0"),
          JavaVersion.VERSION_17, GradleVersion.version("7.3"),
          JavaVersion.VERSION_18, GradleVersion.version("7.5"),
          JavaVersion.VERSION_19, GradleVersion.version("7.6"),
          JavaVersion.VERSION_20, GradleVersion.version("8.3"),
          JavaVersion.VERSION_21, GradleVersion.version("8.5"),
          JavaVersion.VERSION_22, GradleVersion.version("8.8"),
          JavaVersion.VERSION_23, GradleVersion.version("8.10"),
          JavaVersion.VERSION_24, GradleVersion.version("8.14"));

  private static void assumeCompatibleGradleAndJavaVersions() {
    assume()
        .that(testGradleVersion)
        .isAtLeast(
            COMPATIBLE_GRADLE_VERSIONS.getOrDefault(testJavaVersion, GradleVersion.version("6.8")));
  }
}
