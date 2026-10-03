package net.ltgt.gradle.nullaway;

import net.ltgt.gradle.errorprone.CheckSeverity;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public abstract class ConfigurableSeverity {

  /**
   * The severity of the check.
   *
   * <p>Almost equivalent to {@code options.errorprone.check(…, severity)} (the check won't actually
   * appear in {@code options.errorprone.checks}).
   */
  @Input
  @Optional
  public abstract Property<CheckSeverity> getSeverity();

  /**
   * Enable the check.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#DEFAULT}.
   */
  public void enable() {
    getSeverity().set(CheckSeverity.DEFAULT);
  }

  /**
   * Disable the check.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#OFF}.
   */
  public void disable() {
    getSeverity().set(CheckSeverity.OFF);
  }

  /**
   * Enable the check as a warning.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#WARN}
   */
  public void warn() {
    getSeverity().set(CheckSeverity.WARN);
  }

  /**
   * Enable the check as an error.
   *
   * <p>Equivalent to setting {@link #getSeverity() severity} to {@link CheckSeverity#ERROR}
   */
  public void error() {
    getSeverity().set(CheckSeverity.ERROR);
  }
}
