package net.ltgt.gradle.nullaway;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class NullAwayExtension {
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
}
