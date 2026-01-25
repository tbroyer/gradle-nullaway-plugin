package net.ltgt.gradle.nullaway;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public abstract class NullAwayOptions {
  @Inject
  @SuppressWarnings("this-escape")
  public NullAwayOptions(NullAwayExtension nullawayExtension) {
    getSeverity().convention(CheckSeverity.DEFAULT);
    getOnlyNullMarked().convention(nullawayExtension.getOnlyNullMarked());
    getAnnotatedPackages().convention(nullawayExtension.getAnnotatedPackages());
    getJspecifyMode().convention(nullawayExtension.getJspecifyMode());
  }

  /**
   * The severity of the NullAway check.
   *
   * <p>Almost equivalent to {@code options.errorprone.check("NullAway", severity)} (NullAway won't
   * actually appear in {@code options.errorprone.checks}). Can be set to {@link CheckSeverity#OFF}
   * to disable NullAway.
   *
   * @see ErrorProneOptions#check(String, CheckSeverity)
   * @see ErrorProneOptions#check(String, Provider)
   * @see ErrorProneOptions#getChecks()
   * @see #enable()
   * @see #disable()
   * @see #warn()
   * @see #error()
   */
  @Input
  public abstract Property<CheckSeverity> getSeverity();

  /**
   * Indicates that the {@link #getAnnotatedPackages() annotatedPackages} flag has been deliberately
   * omitted, and that NullAway can proceed with only treating {@code @NullMarked} code as
   * annotated, in accordance with the JSpecify specification; maps to {@code
   * -XepOpt:NullAway:OnlyNullMarked}.
   *
   * <p>If this option is passed, then {@link #getAnnotatedPackages() annotatedPackages} must be
   * empty. Note that even if this flag is omitted (and {@link #getAnnotatedPackages()
   * annotatedPackages} is non-empty), any {@code @NullMarked} code will still be treated as
   * annotated.
   *
   * <p>Defaults to the {@link NullAwayExtension#getOnlyNullMarked() value configured at the
   * project-level}.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getOnlyNullMarked();

  /**
   * The list of packages that should be considered properly annotated according to the NullAway
   * convention; maps to {@code -XepOpt:NullAway:AnnotatedPackages}.
   *
   * <p>This can be used to add to or override the {@link NullAwayExtension#getAnnotatedPackages()
   * annotatedPackages} at the project level.
   *
   * <p>Defaults to the {@link NullAwayExtension#getAnnotatedPackages() list configured at the
   * project-level}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getAnnotatedPackages();

  /**
   * A list of subpackages to be excluded from the {@link #getAnnotatedPackages() annotatedPackages}
   * list; maps to {@code -XepOpt:NullAway:UnannotatedSubPackages}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getUnannotatedSubPackages();

  /**
   * A list of classes within annotated packages that should be treated as unannotated; maps to
   * {@code -XepOpt:NullAway:UnannotatedClasses}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getUnannotatedClasses();

  /**
   * The fully qualified name of those methods from third-party libraries that NullAway should treat
   * as initializers; maps to {@code -XepOpt:NullAway:KnownInitializers}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getKnownInitializers();

  /**
   * A list of annotations that cause classes to be excluded from nullability analysis; maps tp
   * {@code -XepOpt:NullAway:ExcludedClassAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExcludedClassAnnotations();

  /**
   * A list of classes to be excluded from the nullability analysis; maps to {@code
   * -XepOpt:NullAway:ExcludedClasses}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExcludedClasses();

  /**
   * A list of annotations that cause fields to be excluded from being checked for proper
   * initialization; maps to {@code -XepOpt:NullAway:ExcludedFieldAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExcludedFieldAnnotations();

  /**
   * A list of annotations that should be considered equivalent to {@code @Initializer} annotations,
   * and thus mark methods as initializers; maps to {@code
   * -XepOpt:NullAway:CustomInitializerAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getCustomInitializerAnnotations();

  /**
   * A list of annotations for classes that are "externally initialized;" maps to {@code
   * -XepOpt:NullAway:ExternalInitAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExternalInitAnnotations();

  /**
   * If set to true, NullAway treats any class annotated with {@code @Generated} as if its APIs are
   * unannotated when analyzing uses from other classes; maps to {@code
   * -XepOpt:NullAway:TreatGeneratedAsUnannotated}.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getTreatGeneratedAsUnannotated();

  /**
   * If set to true, NullAway will acknowledge nullability annotations whenever they are available
   * in <i>unannotated</i> code and also more restrictive than it's optimistic defaults; maps to
   * {@code -XepOpt:NullAway:AcknowledgeRestrictiveAnnotations}.
   *
   * @see #getAcknowledgeAndroidRecent()
   */
  @Input
  @Optional
  public abstract Property<Boolean> getAcknowledgeRestrictiveAnnotations();

  /**
   * If set to true, NullAway will check for {@code .get()} accesses to potentially empty {@code
   * Optional} values, analogously to how it handles dereferences to {@code @Nullable} values; maps
   * to {@code -XepOpt:NullAway:CheckOptionalEmptiness}.
   *
   * @see #getCheckOptionalEmptinessCustomClasses()
   */
  @Input
  @Optional
  public abstract Property<Boolean> getCheckOptionalEmptiness();

  /**
   * If set to true, NullAway will use Error Prone's suggested fix functionality to suggest
   * suppressing any warning that it finds; maps to {@code -XepOpt:NullAway:SuggestSuppressions}.
   *
   * @see #getAutoFixSuppressionComment()
   */
  @Input
  @Optional
  public abstract Property<Boolean> getSuggestSuppressions();

  /**
   * If set to true, NullAway will handle assertions, and use that to reason about the possibility
   * of null dereferences in the code that follows these assertions; maps to {@code
   * -XepOpt:NullAway:AssertsEnabled}.
   *
   * <p>This assumes that assertions will always be enabled at runtime ({@code java} run with {@code
   * -ea} JVM argument).
   */
  @Input
  @Optional
  public abstract Property<Boolean> getAssertsEnabled();

  /**
   * If set to true, NullAway will check every method to see whether or not it overrides a method of
   * a super-type, rather than relying only on the {@code @Override} annotation; maps to {@code
   * -XepOpt:NullAway:ExhaustiveOverride}.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getExhaustiveOverride();

  /**
   * The fully qualified name of a method to be used for downcasting to a non-null value rather than
   * standard suppressions in some instances; maps to {@code -XepOpt:NullAway:CastToNonNullMethod}.
   */
  @Input
  @Optional
  public abstract Property<String> getCastToNonNullMethod();

  /**
   * A list of classes to be treated as {@code Optional} implementations (e.g. Guava's {@code
   * com.google.common.base.Optional}); maps to {@code
   * -XepOpt:NullAway:CheckOptionalEmptinessCustomClasses}.
   *
   * @see #getCheckOptionalEmptiness()
   */
  @Input
  @Optional
  public abstract ListProperty<String> getCheckOptionalEmptinessCustomClasses();

  /**
   * A comment that will be added alongside the {@code @SuppressWarnings("NullAway")} annotation
   * when {@link #getSuggestSuppressions() suggestSuppressions} is set to true; maps to {@code
   * -XepOpt:NullAway:AutoFixSuppressionComment}.
   *
   * @see #getSuggestSuppressions()
   */
  @Input
  @Optional
  public abstract Property<String> getAutoFixSuppressionComment();

  /**
   * If set to true, NullAway will handle assertions from test libraries, like {@code
   * assertThat(...).isNotNull()}, and use that to reason about the possibility of null dereferences
   * in the code that follows these assertions; maps to {@code
   * -XepOpt:NullAway:HandleTestAssertionLibraries}.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getHandleTestAssertionLibraries();

  /**
   * If set to true, treats {@code @RecentlyNullable} as {code @Nullable}, and
   * {@code @RecentlyNonNull} as {@code @NonNull}; maps to {@code
   * -XepOpt:NullAway:AcknowledgeAndroidRecent}.
   *
   * <p>Requires that {@link #getAcknowledgeRestrictiveAnnotations()
   * acknowledgeRestrictiveAnnotations} is also set to true.
   *
   * @see #getAcknowledgeRestrictiveAnnotations()
   */
  @Input
  @Optional
  public abstract Property<Boolean> getAcknowledgeAndroidRecent();

  /**
   * If set to true, NullAway will check {@code @Contract} annotations; maps to {@code
   * -XepOpt:NullAway:CheckContracts}.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getCheckContracts();

  /**
   * A list of annotations that should be considered equivalent to {@code @Contract} annotations;
   * maps to {@code -XepOpt:NullAway:CustomContractAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getCustomContractAnnotations();

  /**
   * A list of annotations that should be considered equivalent to {@code @Nullable} annotations;
   * maps to {@code -XepOpt:NullAway:CustomNullableAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getCustomNullableAnnotations();

  /**
   * A list of annotations that should be considered equivalent to {@code @NonNull} annotations, for
   * the cases where NullAway cares about such annotations (see e.g. {@link
   * #getAcknowledgeRestrictiveAnnotations() acknowledgeRestrictiveAnnotations}); maps to {@code
   * -XepOpt:NullAway:CustomNonnullAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getCustomNonnullAnnotations();

  /**
   * A list of annotations that should be considered equivalent to {@code @Generated} annotations,
   * for the cases where NullAway cares about such annotations (see e.g. {@link
   * #getTreatGeneratedAsUnannotated() treatGeneratedAsUnannotated}); maps to {@code
   * -XepOpt:NullAway:CustomGeneratedCodeAnnotations}.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getCustomGeneratedCodeAnnotations();

  /**
   * If set to true, enables new checks based on JSpecify (like checks for generic types); maps to
   * {@code -XepOpt:NullAway:JSpecifyMode}.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getJspecifyMode();

  /**
   * A list of classes to be treated equivalently to Guava {@code Futures` and `FluentFuture}; maps
   * to {@code -XepOpt:NullAway:ExtraFuturesClasses}.
   *
   * <p>This special support will likely be removed once NullAway's JSpecify support is more
   * complete.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getExtraFuturesClasses();

  /**
   * A list of names to suppress NullAway using a {@code @SuppressWarnings} annotation, similar to
   * {@code @SuppressWarnings("NullAway")}; maps to {@code -XepOpt:NullAway:SuppressionNameAliases}.
   *
   * <p>This is useful when other warnings are already suppressed in the codebase and NullAway
   * should be suppressed as well, such as with JetBrains' [{@code
   * DataFlowIssue}](https://www.jetbrains.com/help/inspectopedia/DataFlowIssue.html) inspection.
   */
  @Input
  @Optional
  public abstract ListProperty<String> getSuppressionNameAliases();

  /**
   * If set to true, NullAway will issue a warning when generic type inference fails to infer a type
   * argument's nullability; maps to {@code -XepOpt:NullAway:WarnOnGenericInferenceFailure}.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getWarnOnGenericInferenceFailure();

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

  Iterable<String> asArguments() {
    List<String> args = new ArrayList<>();
    args.add("-Xep:NullAway" + severityToArg(getSeverity().getOrElse(CheckSeverity.DEFAULT)));
    maybeAddListOption(args, "AnnotatedPackages", getAnnotatedPackages());
    maybeAddBooleanOption(args, "OnlyNullMarked", getOnlyNullMarked());
    maybeAddListOption(args, "UnannotatedSubPackages", getUnannotatedSubPackages());
    maybeAddListOption(args, "UnannotatedClasses", getUnannotatedClasses());
    maybeAddListOption(args, "KnownInitializers", getKnownInitializers());
    maybeAddListOption(args, "ExcludedClassAnnotations", getExcludedClassAnnotations());
    maybeAddListOption(args, "ExcludedClasses", getExcludedClasses());
    maybeAddListOption(args, "ExcludedFieldAnnotations", getExcludedFieldAnnotations());
    maybeAddListOption(args, "CustomInitializerAnnotations", getCustomInitializerAnnotations());
    maybeAddListOption(args, "ExternalInitAnnotations", getExternalInitAnnotations());
    maybeAddBooleanOption(args, "TreatGeneratedAsUnannotated", getTreatGeneratedAsUnannotated());
    maybeAddBooleanOption(
        args, "AcknowledgeRestrictiveAnnotations", getAcknowledgeRestrictiveAnnotations());
    maybeAddBooleanOption(args, "CheckOptionalEmptiness", getCheckOptionalEmptiness());
    maybeAddBooleanOption(args, "SuggestSuppressions", getSuggestSuppressions());
    maybeAddBooleanOption(args, "AssertsEnabled", getAssertsEnabled());
    maybeAddBooleanOption(args, "ExhaustiveOverride", getExhaustiveOverride());
    maybeAddStringOption(args, "CastToNonNullMethod", getCastToNonNullMethod());
    maybeAddListOption(
        args, "CheckOptionalEmptinessCustomClasses", getCheckOptionalEmptinessCustomClasses());
    maybeAddStringOption(args, "AutoFixSuppressionComment", getAutoFixSuppressionComment());
    maybeAddBooleanOption(args, "HandleTestAssertionLibraries", getHandleTestAssertionLibraries());
    maybeAddBooleanOption(args, "AcknowledgeAndroidRecent", getAcknowledgeAndroidRecent());
    maybeAddBooleanOption(args, "CheckContracts", getCheckContracts());
    maybeAddListOption(args, "CustomContractAnnotations", getCustomContractAnnotations());
    maybeAddListOption(args, "CustomNullableAnnotations", getCustomNullableAnnotations());
    maybeAddListOption(args, "CustomNonnullAnnotations", getCustomNonnullAnnotations());
    maybeAddListOption(args, "CustomGeneratedCodeAnnotations", getCustomGeneratedCodeAnnotations());
    maybeAddBooleanOption(args, "JSpecifyMode", getJspecifyMode());
    maybeAddListOption(args, "ExtraFuturesClasses", getExtraFuturesClasses());
    maybeAddListOption(args, "SuppressionNameAliases", getSuppressionNameAliases());
    maybeAddBooleanOption(
        args, "WarnOnGenericInferenceFailure", getWarnOnGenericInferenceFailure());
    return args;
  }

  private String severityToArg(CheckSeverity severity) {
    return severity == CheckSeverity.DEFAULT ? "" : ":" + severity;
  }

  private void maybeAddBooleanOption(List<String> args, String name, Provider<Boolean> value) {
    if (value.isPresent()) {
      addStringOption(args, name, Boolean.toString(value.get()));
    }
  }

  private void maybeAddListOption(List<String> args, String name, Provider<List<String>> value) {
    List<String> list = value.getOrElse(emptyList());
    if (list.isEmpty()) {
      return;
    }
    addStringOption(args, name, String.join(",", list));
  }

  private void maybeAddStringOption(List<String> args, String name, Provider<String> value) {
    if (value.isPresent()) {
      addStringOption(args, name, value.get());
    }
  }

  private void addStringOption(List<String> args, String name, String value) {
    args.add("-XepOpt:NullAway:" + name + "=" + value);
  }
}
