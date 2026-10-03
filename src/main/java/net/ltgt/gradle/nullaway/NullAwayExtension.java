package net.ltgt.gradle.nullaway;

import net.ltgt.gradle.errorprone.CheckSeverity;
import org.gradle.api.Action;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

public abstract class NullAwayExtension {

  @SuppressWarnings("this-escape")
  public NullAwayExtension() {
    getSeverity().convention(CheckSeverity.DEFAULT);
  }

  /** The severity of the NullAway check. */
  public abstract Property<CheckSeverity> getSeverity();

  /**
   * Enable NullAway.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#DEFAULT}.
   */
  public void enable() {
    getSeverity().set(CheckSeverity.DEFAULT);
  }

  /**
   * Disable NullAway.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#OFF}.
   */
  public void disable() {
    getSeverity().set(CheckSeverity.OFF);
  }

  /**
   * Enable NullAway as a warning.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#WARN}
   */
  public void warn() {
    getSeverity().set(CheckSeverity.WARN);
  }

  /**
   * Enable NullAway as an error.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#ERROR}
   */
  public void error() {
    getSeverity().set(CheckSeverity.ERROR);
  }

  /**
   * Indicates that the {@link #getAnnotatedPackages()} flag has been deliberately omitted, and that
   * NullAway can proceed with only treating {@code @NullMarked} code as annotated, in accordance
   * with the JSpecify specification.
   */
  public abstract Property<Boolean> getOnlyNullMarked();

  /**
   * The list of packages that should be considered properly annotated according to the NullAway
   * convention.
   */
  public abstract ListProperty<String> getAnnotatedPackages();

  /** If set to true, enables new checks based on JSpecify (like checks for generic types) */
  public abstract Property<Boolean> getJspecifyMode();

  /**
   * Convenience option that enables the experimental {@code HandleWildcardGenerics}, {@code
   * JSpecifyJDKModels}, and {@code WarnOnGenericInferenceFailure} features. This option must be
   * used with {@link #getJspecifyMode() jspecifyMode = true}.
   */
  public abstract Property<Boolean> getJspecifyExperimental();

  /** Configures the RequireExplicitNullMarking check's severity. */
  @Nested
  public abstract ConfigurableSeverity getRequireExplicitNullMarking();

  /** Configures the RequireExplicitNullMarking check's severity. */
  public void requireExplicitNullMarking(Action<? super ConfigurableSeverity> configure) {
    configure.execute(getRequireExplicitNullMarking());
  }

  /** Configures the JSpecifyUnrecognizedAnnotationLocation check's severity. */
  @Nested
  public abstract ConfigurableSeverity getJspecifyUnrecognizedAnnotationLocation();

  /** Configures the JSpecifyUnrecognizedAnnotationLocation check's severity. */
  public void jspecifyUnrecognizedAnnotationLocation(
      Action<? super ConfigurableSeverity> configure) {
    configure.execute(getJspecifyUnrecognizedAnnotationLocation());
  }
}
