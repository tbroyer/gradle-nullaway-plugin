# gradle-nullaway-plugin

This plugin is a companion to the [`net.ltgt.errorprone` Gradle plugin]
that adds a Gradle DSL to configure [NullAway]

[`net.ltgt.errorprone` Gradle plugin]: https://github.com/tbroyer/gradle-errorprone-plugin
[NullAway]: https://github.com/uber/NullAway

## Requirements

This plugin requires using at least Gradle 5.2,
and applying the `net.ltgt.errorprone` plugin (it won't do anything otherwise).

## Usage

```gradle
plugins {
    id("net.ltgt.errorprone") version "<error prone plugin version>"
    id("net.ltgt.nullaway") version "<plugin version>"
}
```

then add the NullAway dependency to the `errorprone` configuration:

```gradle
dependencies {
    errorprone("com.uber.nullaway:nullaway:$nullawayVersion")
}
```

and finally configure NullAway's annotated packages:

```gradle
nullaway {
    annotatedPackages.add("net.ltgt")
}
```

## Configuration

Other [NullAway flags], as well as the check severity, can be configured on the `JavaCompile` tasks:

[NullAway flags]: https://github.com/uber/NullAway/wiki/Configuration

```gradle
tasks.withType(JavaCompile).configureEach {
    options.errorprone.nullaway {
        severity = CheckSeverity.ERROR
        unannotatedSubPackages.add("com.foo.baz")
    }
}
```

<details>
<summary>with Kotlin DSL</summary>

```kotlin
import net.ltgt.errorprone.*
import net.ltgt.nullaway.nullaway

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.nullaway {
        severity.set(CheckSeverity.ERROR)
        unannotatedSubPackages.add("com.foo.baz")
    }
}
```

</details>


### Properties

| Property | Description
| :------- | :----------
| `severity`               | The check severity. Equivalent to `options.errorprone.check("NullAway", severity)`. Can be set to `CheckSeverity.OFF` to disable NullAway.
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
