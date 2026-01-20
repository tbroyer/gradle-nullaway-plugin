# gradle-nullaway-plugin

This plugin is a companion to the [`net.ltgt.errorprone` Gradle plugin]
that adds a Gradle DSL to configure [NullAway]

[`net.ltgt.errorprone` Gradle plugin]: https://github.com/tbroyer/gradle-errorprone-plugin
[NullAway]: https://github.com/uber/NullAway

## Requirements

This plugin requires using at least Gradle 6.8,
and applying the `net.ltgt.errorprone` plugin (it won't do anything otherwise).

## Usage

```kotlin
plugins {
    id("net.ltgt.errorprone") version "<error prone plugin version>"
    id("net.ltgt.nullaway") version "<plugin version>"
}
```

then add the NullAway dependency to the `errorprone` configuration:

```kotlin
dependencies {
    errorprone("com.uber.nullaway:nullaway:$nullawayVersion")
}
```

and finally configure NullAway's annotated packages:

```kotlin
nullaway {
    annotatedPackages.add("net.ltgt")
    // OR, starting with NullAway 0.12.3 and if you use JSpecify @NullMarked:
    onlyNullMarked = true
}
```

## Configuration

Other [NullAway flags], as well as the check severity, can be configured on the `JavaCompile` tasks:

[NullAway flags]: https://github.com/uber/NullAway/wiki/Configuration

```kotlin
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.nullaway {
        error()
        unannotatedSubPackages.add("com.foo.baz")
    }
}
```

or with the Groovy DSL:

```gradle
tasks.withType(JavaCompile).configureEach {
    options.errorprone.nullaway {
        error()
        unannotatedSubPackages.add("com.foo.baz")
    }
}
```


### Properties

_Please note that all properties are [lazy](https://docs.gradle.org/current/userguide/lazy_configuration.html#lazy_properties),
so while you can use `=` in place of `.set(…)` in the Groovy DSL,
you cannot use `<<` or `+=` to add to lists for instance._

Each property (except for `severity`) maps to an `-XepOpt:NullAway:[propertyName]=[value]` Error Prone argument.

| Property | Description
| :------- | :----------
| `severity`               | The check severity. Almost equivalent to `options.errorprone.check("NullAway", severity)` (NullAway won't actually appear in `options.errorprone.checks`). Can be set to `CheckSeverity.OFF` to disable NullAway.
| `onlyNullMarked`         | Indicates that the `annotatedPackages` flag has been deliberately omitted, and that NullAway can proceed with only treating `@NullMarked` code as annotated, in accordance with the JSpecify specification. 
| `annotatedPackages`      | The list of packages that should be considered properly annotated according to the NullAway convention. This can be used to add to or override the `annotatedPackages` at the project level.
| `unannotatedSubPackages` | A list of subpackages to be excluded from the AnnotatedPackages list.
| `unannotatedClasses`     | A list of classes within annotated packages that should be treated as unannotated.
| `knownInitializers`      | The fully qualified name of those methods from third-party libraries that NullAway should treat as initializers.
| `excludedClassAnnotations` | A list of annotations that cause classes to be excluded from nullability analysis.
| `excludedClasses`          | A list of classes to be excluded from the nullability analysis.
| `excludedFieldAnnotations` | A list of annotations that cause fields to be excluded from being checked for proper initialization.
| `customInitializerAnnotations` | A list of annotations that should be considered equivalent to `@Initializer` annotations, and thus mark methods as initializers.
| `externalInitAnnotations`      | A list of annotations for classes that are "externally initialized."
| `treatGeneratedAsUnannotated`  | If set to true, NullAway treats any class annotated with `@Generated` as if its APIs are unannotated when analyzing uses from other classes.
| `acknowledgeRestrictiveAnnotations` | If set to true, NullAway will acknowledge nullability annotations whenever they are available in _unannotated_ code and also more restrictive than it's optimistic defaults.
| `checkOptionalEmptiness`            | If set to true, NullAway will check for `.get()` accesses to potentially empty `Optional` values, analogously to how it handles dereferences to `@Nullable` values.
| `checkOptionalEmptinessCustomClasses` | A list of classes to be treated as `Optional` implementations (e.g. Guava's `com.google.common.base.Optional`).
| `suggestSuppressions`          | If set to true, NullAway will use Error Prone's suggested fix functionality to suggest suppressing any warning that it finds.
| `autoFixSuppressionComment`    | A comment that will be added alongside the `@SuppressWarnings("NullAway")` annotation when `isSuggestSuppressions` is set to true.
| `castToNonNullMethod`          | The fully qualified name of a method to be used for downcasting to a non-null value rather than standard suppressions in some instances.
| `isAssertsEnabled`             | (`assertsEnabled` with the Groovy DSL) If set to true, NullAway will handle assertions, and use that to reason about the possibility of null dereferences in the code that follows these assertions. This assumes that assertions will always be enabled at runtime.
| `handleTestAssertionLibraries` | If set to true, NullAway will handle assertions from test libraries, like `assertThat(...).isNotNull()`, and use that to reason about the possibility of null dereferences in the code that follows these assertions.
| `isExhaustiveOverride`         | (`exhaustiveOverride` with the Groovy DSL) If set to true, NullAway will check every method to see whether or not it overrides a method of a super-type, rather than relying only on the `@Override` annotation.
| `acknowledgeAndroidRecent`     | If set to true, treats `@RecentlyNullable` as `@Nullable`, and `@RecentlyNonNull` as `@NonNull`; requires that `acknowledgeRestrictiveAnnotations` is also set to true.
| `checkContracts`               | If set to true, NullAway will check `@Contract` annotations.
| `customContractAnnotations`    | A list of annotations that should be considered equivalent to `@Contract` annotations.
| `customNullableAnnotations`    | A list of annotations that should be considered equivalent to `@Nullable` annotations.
| `customNonnullAnnotations`     | A list of annotations that should be considered equivalent to `@NonNull` annotations, for the cases where NullAway cares about such annotations (see e.g. `acknowledgeRestrictiveAnnotations`).
| `customGeneratedCodeAnnotations` | A list of annotations that should be considered equivalent to `@Generated` annotations, for the cases where NullAway cares about such annotations (see e.g. `treatGeneratedAsUnannotated`).
| `isJSpecifyMode`                 | (`jspecifyMode` with the Groovy DSL) If set to true, enables new checks based on JSpecify (like checks for generic types).
| `extraFuturesClasses`            | A list of classes to be treated equivalently to Guava `Futures` and `FluentFuture`; this special support will likely be removed once NullAway's JSpecify support is more complete.
| `suppressionNameAliases`         | A list of names to suppress NullAway using a `@SuppressWarnings` annotation, similar to `@SuppressWarnings("NullAway")`.
| `warnOnGenericInferenceFailure`  | If set to true, NullAway will issue a warning when generic type inference fails to infer a type argument's nullability.

### Methods

| Method      | Description
|:------------| :----------
| `enable()`  | Enable NullAway. Equivalent to `severity.set(CheckSeverity.DEFAULT)`.
| `disable()` | Disable NullAway. Equivalent to `severity.set(CheckSeverity.OFF)`.
| `warn()`    | Enable NullAway as a warning. Equivalent to `severity.set(CheckSeverity.WARN)`.
| `error()`   | Enable NullAway as an error. Equivalent to `severity.set(CheckSeverity.ERROR)`.
