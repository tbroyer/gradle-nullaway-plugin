@file:Suppress("ktlint:standard:no-wildcard-imports")

package net.ltgt.gradle.nullaway

import net.ltgt.gradle.errorprone.CheckSeverity
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.*

open class NullAwayOptions internal constructor(
    objectFactory: ObjectFactory,
    nullawayExtension: NullAwayExtension,
) {
    /**
     * The severity of the NullAway check.
     *
     * Almost equivalent to `options.errorprone.check("NullAway", severity)` (NullAway won't actually appear in `options.errorprone.checks`).
     * Can be set to [CheckSeverity.OFF] to disable NullAway.
     *
     * @see net.ltgt.gradle.errorprone.ErrorProneOptions.check
     * @see net.ltgt.gradle.errorprone.ErrorProneOptions.checks
     * @see enable
     * @see disable
     * @see warn
     * @see error
     */
    @get:Input val severity = objectFactory.property<CheckSeverity>().convention(CheckSeverity.DEFAULT)

    /**
     * Indicates that the [annotatedPackages] flag has been deliberately omitted, and that NullAway can proceed with only treating `@NullMarked` code as annotated, in accordance with the JSpecify specification; maps to `-XepOpt:NullAway:OnlyNullMarked`.
     *
     * If this option is passed, then [annotatedPackages] must be empty. Note that even if this flag is omitted (and [annotatedPackages] is non-empty), any `@NullMarked` code will still be treated as annotated.
     *
     * Defaults to the [value configured at the project-level][NullAwayExtension.onlyNullMarked]
     */
    @get:Input @get:Optional
    val onlyNullMarked = objectFactory.property<Boolean>().convention(nullawayExtension.onlyNullMarked)

    /**
     * The list of packages that should be considered properly annotated according to the NullAway convention; maps to `-XepOpt:NullAway:AnnotatedPackages`.
     *
     * This can be used to add to or override the [`annotatedPackages`][NullAwayExtension.annotatedPackages] at the project level.
     *
     * Defaults to the [list configured at the project-level][NullAwayExtension.annotatedPackages].
     */
    @get:Input @get:Optional
    val annotatedPackages = objectFactory.listProperty<String>().convention(nullawayExtension.annotatedPackages)

    /** A list of subpackages to be excluded from the [annotatedPackages] list; maps to `-XepOpt:NullAway:UnannotatedSubPackages`. */
    @get:Input @get:Optional
    val unannotatedSubPackages = objectFactory.listProperty<String>()

    /** A list of classes within annotated packages that should be treated as unannotated; maps to `-XepOpt:NullAway:UnannotatedClasses`. */
    @get:Input @get:Optional
    val unannotatedClasses = objectFactory.listProperty<String>()

    /** The fully qualified name of those methods from third-party libraries that NullAway should treat as initializers; maps to `-XepOpt:NullAway:KnownInitializers`. */
    @get:Input @get:Optional
    val knownInitializers = objectFactory.listProperty<String>()

    /** A list of annotations that cause classes to be excluded from nullability analysis; maps tp `-XepOpt:NullAway:ExcludedClassAnnotations`. */
    @get:Input @get:Optional
    val excludedClassAnnotations = objectFactory.listProperty<String>()

    /** A list of classes to be excluded from the nullability analysis; maps to `-XepOpt:NullAway:ExcludedClasses`. */
    @get:Input @get:Optional
    val excludedClasses = objectFactory.listProperty<String>()

    /** A list of annotations that cause fields to be excluded from being checked for proper initialization; maps to `-XepOpt:NullAway:ExcludedFieldAnnotations`. */
    @get:Input @get:Optional
    val excludedFieldAnnotations = objectFactory.listProperty<String>()

    /** A list of annotations that should be considered equivalent to `@Initializer` annotations, and thus mark methods as initializers; maps to `-XepOpt:NullAway:CustomInitializerAnnotations`. */
    @get:Input @get:Optional
    val customInitializerAnnotations = objectFactory.listProperty<String>()

    /** A list of annotations for classes that are "externally initialized;" maps to `-XepOpt:NullAway:ExternalInitAnnotations`. */
    @get:Input @get:Optional
    val externalInitAnnotations = objectFactory.listProperty<String>()

    /** If set to true, NullAway treats any class annotated with `@Generated` as if its APIs are unannotated when analyzing uses from other classes; maps to `-XepOpt:NullAway:TreatGeneratedAsUnannotated`. */
    @get:Input @get:Optional
    val treatGeneratedAsUnannotated = objectFactory.property<Boolean>()

    /**
     * If set to true, NullAway will acknowledge nullability annotations whenever they are available in _unannotated_ code and also more restrictive than it's optimistic defaults; maps to `-XepOpt:NullAway:AcknowledgeRestrictiveAnnotations`.
     *
     * @see acknowledgeAndroidRecent
     */
    @get:Input @get:Optional
    val acknowledgeRestrictiveAnnotations = objectFactory.property<Boolean>()

    /**
     * If set to true, NullAway will check for `.get()` accesses to potentially empty `Optional` values, analogously to how it handles dereferences to `@Nullable` values; maps to `-XepOpt:NullAway:CheckOptionalEmptiness`.
     *
     * @see checkOptionalEmptinessCustomClasses
     */
    @get:Input @get:Optional
    val checkOptionalEmptiness = objectFactory.property<Boolean>()

    /**
     * If set to true, NullAway will use Error Prone's suggested fix functionality to suggest suppressing any warning that it finds; maps to `-XepOpt:NullAway:SuggestSuppressions`.
     *
     * @see autoFixSuppressionComment
     */
    @get:Input @get:Optional
    val suggestSuppressions = objectFactory.property<Boolean>()

    /**
     * If set to true, NullAway will handle assertions, and use that to reason about the possibility of null dereferences in the code that follows these assertions; maps to `-XepOpt:NullAway:AssertsEnabled`.
     *
     * This assumes that assertions will always be enabled at runtime (`java` run with `-ea` JVM argument).
     */
    @get:Input
    @get:Optional
    val assertsEnabled = objectFactory.property<Boolean>()

    /** If set to true, NullAway will check every method to see whether or not it overrides a method of a super-type, rather than relying only on the `@Override` annotation; maps to `-XepOpt:NullAway:ExhaustiveOverride`. */
    @get:Input
    @get:Optional
    val exhaustiveOverride = objectFactory.property<Boolean>()

    /** The fully qualified name of a method to be used for downcasting to a non-null value rather than standard suppressions in some instances; maps to `-XepOpt:NullAway:CastToNonNullMethod`. */
    @get:Input @get:Optional
    val castToNonNullMethod = objectFactory.property<String>()

    /**
     * A list of classes to be treated as `Optional` implementations (e.g. Guava's `com.google.common.base.Optional`); maps to `-XepOpt:NullAway:CheckOptionalEmptinessCustomClasses`.
     *
     * @see checkOptionalEmptiness
     */
    @get:Input @get:Optional
    val checkOptionalEmptinessCustomClasses = objectFactory.listProperty<String>()

    /**
     * A comment that will be added alongside the `@SuppressWarnings("NullAway")` annotation when [suggestSuppressions] is set to true; maps to `-XepOpt:NullAway:AutoFixSuppressionComment`.
     *
     * @see suggestSuppressions
     */
    @get:Input @get:Optional
    val autoFixSuppressionComment = objectFactory.property<String>()

    /** If set to true, NullAway will handle assertions from test libraries, like `assertThat(...).isNotNull()`, and use that to reason about the possibility of null dereferences in the code that follows these assertions; maps to `-XepOpt:NullAway:HandleTestAssertionLibraries`. */
    @get:Input @get:Optional
    val handleTestAssertionLibraries = objectFactory.property<Boolean>()

    /**
     * If set to true, treats `@RecentlyNullable` as `@Nullable`, and `@RecentlyNonNull` as `@NonNull`; maps to `-XepOpt:NullAway:AcknowledgeAndroidRecent`.
     *
     * Requires that [acknowledgeRestrictiveAnnotations] is also set to true.
     *
     * @see acknowledgeRestrictiveAnnotations
     */
    @get:Input @get:Optional
    val acknowledgeAndroidRecent = objectFactory.property<Boolean>()

    /** If set to true, NullAway will check `@Contract` annotations; maps to `-XepOpt:NullAway:CheckContracts`. */
    @get:Input @get:Optional
    val checkContracts = objectFactory.property<Boolean>()

    /** A list of annotations that should be considered equivalent to `@Contract` annotations; maps to `-XepOpt:NullAway:CustomContractAnnotations`. */
    @get:Input @get:Optional
    val customContractAnnotations = objectFactory.listProperty<String>()

    /** A list of annotations that should be considered equivalent to `@Nullable` annotations; maps to `-XepOpt:NullAway:CustomNullableAnnotations`. */
    @get:Input @get:Optional
    val customNullableAnnotations = objectFactory.listProperty<String>()

    /** A list of annotations that should be considered equivalent to `@NonNull` annotations, for the cases where NullAway cares about such annotations (see e.g. [acknowledgeRestrictiveAnnotations]); maps to `-XepOpt:NullAway:CustomNonnullAnnotations`. */
    @get:Input @get:Optional
    val customNonnullAnnotations = objectFactory.listProperty<String>()

    /** A list of annotations that should be considered equivalent to `@Generated` annotations, for the cases where NullAway cares about such annotations (see e.g. [treatGeneratedAsUnannotated]); maps to `-XepOpt:NullAway:CustomGeneratedCodeAnnotations`. */
    @get:Input @get:Optional
    val customGeneratedCodeAnnotations = objectFactory.listProperty<String>()

    /** If set to true, enables new checks based on JSpecify (like checks for generic types); maps to `-XepOpt:NullAway:JSpecifyMode`. */
    @get:Input
    @get:Optional
    val jspecifyMode = objectFactory.property<Boolean>().convention(nullawayExtension.jspecifyMode)

    /**
     * A list of classes to be treated equivalently to Guava `Futures` and `FluentFuture`; maps to `-XepOpt:NullAway:ExtraFuturesClasses`.
     *
     * This special support will likely be removed once NullAway's JSpecify support is more complete.
     */
    @get:Input @get:Optional
    val extraFuturesClasses = objectFactory.listProperty<String>()

    /**
     * A list of names to suppress NullAway using a `@SuppressWarnings` annotation, similar to `@SuppressWarnings("NullAway")`; maps to `-XepOpt:NullAway:SuppressionNameAliases`.
     *
     * This is useful when other warnings are already suppressed in the codebase and NullAway should be suppressed as well, such as with JetBrains' [`DataFlowIssue`](https://www.jetbrains.com/help/inspectopedia/DataFlowIssue.html) inspection.
     */
    @get:Input @get:Optional
    val suppressionNameAliases = objectFactory.listProperty<String>()

    /**
     * If set to true, NullAway will issue a warning when generic type inference fails to infer a type argument's nullability; maps to `-XepOpt:NullAway:WarnOnGenericInferenceFailure`.
     */
    @get:Input @get:Optional
    val warnOnGenericInferenceFailure = objectFactory.property<Boolean>()

    /**
     * Enable NullAway.
     *
     * Equivalent to setting [severity] to [CheckSeverity.DEFAULT].
     */
    fun enable() = severity.set(CheckSeverity.DEFAULT)

    /**
     * Disable NullAway.
     *
     * Equivalent to setting [severity] to [CheckSeverity.OFF].
     */
    fun disable() = severity.set(CheckSeverity.OFF)

    /**
     * Enable NullAway as a warning.
     *
     * Equivalent to setting [severity] to [CheckSeverity.WARN]
     */
    fun warn() = severity.set(CheckSeverity.WARN)

    /**
     * Enable NullAway as an error.
     *
     * Equivalent to setting [severity] to [CheckSeverity.ERROR]
     */
    fun error() = severity.set(CheckSeverity.ERROR)

    internal fun asArguments(): Iterable<String> =
        sequenceOf(
            "-Xep:NullAway${severity.getOrElse(CheckSeverity.DEFAULT).asArg}",
            listOption("AnnotatedPackages", annotatedPackages),
            booleanOption("OnlyNullMarked", onlyNullMarked),
            listOption("UnannotatedSubPackages", unannotatedSubPackages),
            listOption("UnannotatedClasses", unannotatedClasses),
            listOption("KnownInitializers", knownInitializers),
            listOption("ExcludedClassAnnotations", excludedClassAnnotations),
            listOption("ExcludedClasses", excludedClasses),
            listOption("ExcludedFieldAnnotations", excludedFieldAnnotations),
            listOption("CustomInitializerAnnotations", customInitializerAnnotations),
            listOption("ExternalInitAnnotations", externalInitAnnotations),
            booleanOption("TreatGeneratedAsUnannotated", treatGeneratedAsUnannotated),
            booleanOption("AcknowledgeRestrictiveAnnotations", acknowledgeRestrictiveAnnotations),
            booleanOption("CheckOptionalEmptiness", checkOptionalEmptiness),
            booleanOption("SuggestSuppressions", suggestSuppressions),
            booleanOption("AssertsEnabled", assertsEnabled),
            booleanOption("ExhaustiveOverride", exhaustiveOverride),
            stringOption("CastToNonNullMethod", castToNonNullMethod),
            listOption("CheckOptionalEmptinessCustomClasses", checkOptionalEmptinessCustomClasses),
            stringOption("AutoFixSuppressionComment", autoFixSuppressionComment),
            booleanOption("HandleTestAssertionLibraries", handleTestAssertionLibraries),
            booleanOption("AcknowledgeAndroidRecent", acknowledgeAndroidRecent),
            booleanOption("CheckContracts", checkContracts),
            listOption("CustomContractAnnotations", customContractAnnotations),
            listOption("CustomNullableAnnotations", customNullableAnnotations),
            listOption("CustomNonnullAnnotations", customNonnullAnnotations),
            listOption("CustomGeneratedCodeAnnotations", customGeneratedCodeAnnotations),
            booleanOption("JSpecifyMode", jspecifyMode),
            listOption("ExtraFuturesClasses", extraFuturesClasses),
            listOption("SuppressionNameAliases", suppressionNameAliases),
            booleanOption("WarnOnGenericInferenceFailure", warnOnGenericInferenceFailure),
        ).filterNotNull()
            .asIterable()

    private fun listOption(
        name: String,
        value: Provider<List<String>>,
    ) = value.orNull.takeUnless { it.isNullOrEmpty() }?.let { "-XepOpt:NullAway:$name=${it.joinToString(separator = ",")}" }

    private fun booleanOption(
        name: String,
        value: Provider<Boolean>,
    ): String? = value.orNull?.let { "-XepOpt:NullAway:$name=$it" }

    private fun stringOption(
        name: String,
        value: Provider<String>,
    ): String? = value.orNull?.let { "-XepOpt:NullAway:$name=$it" }
}

private val CheckSeverity.asArg: String
    get() =
        when (this) {
            CheckSeverity.DEFAULT -> ""
            else -> ":$name"
        }
