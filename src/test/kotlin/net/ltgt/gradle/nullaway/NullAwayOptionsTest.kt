package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import com.google.errorprone.ErrorProneOptions
import com.google.errorprone.ErrorProneOptions.Severity
import net.ltgt.gradle.errorprone.CheckSeverity
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NullAwayOptionsTest {
    companion object {
        private val ALL_NULLAWAY_OPTION_NAMES =
            setOf(
                "NullAway:OnlyNullMarked",
                "NullAway:AnnotatedPackages",
                "NullAway:UnannotatedSubPackages",
                "NullAway:UnannotatedClasses",
                "NullAway:UnannotatedClasses",
                "NullAway:KnownInitializers",
                "NullAway:ExcludedClassAnnotations",
                "NullAway:ExcludedClasses",
                "NullAway:ExcludedFieldAnnotations",
                "NullAway:CustomInitializerAnnotations",
                "NullAway:ExternalInitAnnotations",
                "NullAway:TreatGeneratedAsUnannotated",
                "NullAway:AcknowledgeRestrictiveAnnotations",
                "NullAway:CheckOptionalEmptiness",
                "NullAway:SuggestSuppressions",
                "NullAway:AssertsEnabled",
                "NullAway:ExhaustiveOverride",
                "NullAway:CastToNonNullMethod",
                "NullAway:CheckOptionalEmptinessCustomClasses",
                "NullAway:AutoFixSuppressionComment",
                "NullAway:HandleTestAssertionLibraries",
                "NullAway:AcknowledgeAndroidRecent",
                "NullAway:CheckContracts",
                "NullAway:CustomContractAnnotations",
                "NullAway:CustomNullableAnnotations",
                "NullAway:CustomNonnullAnnotations",
                "NullAway:CustomGeneratedCodeAnnotations",
                "NullAway:JSpecifyMode",
                "NullAway:ExtraFuturesClasses",
                "NullAway:SuppressionNameAliases",
                "NullAway:WarnOnGenericInferenceFailure",
            )
    }

    lateinit var objects: ObjectFactory
    lateinit var providers: ProviderFactory

    @BeforeAll
    fun setup(
        @TempDir projectDir: File,
    ) {
        ProjectBuilder.builder().withProjectDir(projectDir).build().let { project ->
            objects = project.objects
            providers = project.providers
        }
    }

    @Test
    fun `generates correct error prone options`() {
        doTestOptions { severity.set(CheckSeverity.DEFAULT) }
        doTestOptions { enable() }
        doTestOptions { disable() }
        doTestOptions { warn() }
        doTestOptions { error() }
        doTestOptions { onlyNullMarked.set(true) }
        doTestOptions { annotatedPackages.add("test") }
        doTestOptions { unannotatedSubPackages.add("test.dummy") }
        doTestOptions { unannotatedClasses.add("test.Unannotated") }
        doTestOptions { knownInitializers.add("com.foo.Bar.method") }
        doTestOptions { excludedClassAnnotations.add("com.example.NullAwayExcluded") }
        doTestOptions { excludedClasses.add("test.Excluded") }
        doTestOptions { excludedFieldAnnotations.add("javax.ws.rs.core.Context") }
        doTestOptions { customInitializerAnnotations.add("com.foo.Initializer") }
        doTestOptions { externalInitAnnotations.add("com.example.ExternalInit") }
        doTestOptions { treatGeneratedAsUnannotated.set(true) }
        doTestOptions { acknowledgeRestrictiveAnnotations.set(true) }
        doTestOptions { checkOptionalEmptiness.set(true) }
        doTestOptions { suggestSuppressions.set(true) }
        doTestOptions { assertsEnabled.set(true) }
        doTestOptions { exhaustiveOverride.set(true) }
        doTestOptions { castToNonNullMethod.set("com.foo.Bar.castToNonNull") }
        doTestOptions { checkOptionalEmptinessCustomClasses.add("com.foo.Optional") }
        doTestOptions { autoFixSuppressionComment.set("Auto-fix\\u0020suppression") }
        doTestOptions { handleTestAssertionLibraries.set(true) }
        doTestOptions { acknowledgeAndroidRecent.set(true) }
        doTestOptions { checkContracts.set(true) }
        doTestOptions { customContractAnnotations.add("com.example.Contract") }
        doTestOptions { customNullableAnnotations.add("com.example.CouldBeNull") }
        doTestOptions { customNonnullAnnotations.add("com.example.MustNotBeNull") }
        doTestOptions { customGeneratedCodeAnnotations.add("com.example.Generated") }
        doTestOptions { jspecifyMode.set(true) }
        doTestOptions { extraFuturesClasses.add("com.example.Future") }
        doTestOptions { suppressionNameAliases.add("NullIssue") }
        doTestOptions { warnOnGenericInferenceFailure.set(true) }

        doTestOptions {
            enable()
            annotatedPackages.add("test")
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
            autoFixSuppressionComment.set("Auto-fix\\u0020suppression")
            handleTestAssertionLibraries.set(true)
            acknowledgeAndroidRecent.set(true)
            checkContracts.set(true)
            customContractAnnotations.add("com.example.Contract")
            customNullableAnnotations.add("com.example.CouldBeNull")
            customNonnullAnnotations.add("com.example.MustNotBeNull")
            customGeneratedCodeAnnotations.add("com.example.Generated")
            jspecifyMode.set(true)
            extraFuturesClasses.add("com.example.Future")
            suppressionNameAliases.add("NullIssue")
            warnOnGenericInferenceFailure.set(true)
        }
    }

    private fun doTestOptions(configure: NullAwayOptions.() -> Unit) {
        val options = NullAwayOptions(objects, NullAwayExtension(objects)).apply(configure)
        val parsedOptions = parseOptions(options)
        assertOptionsEqual(options, parsedOptions)
    }

    private fun parseOptions(options: NullAwayOptions) =
        ErrorProneOptions.processArgs(splitArgs(options.asArguments().joinToString(separator = " ")))

    // This is how JavaC "parses" the -Xplugin: values: https://git.io/vx8yI
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun splitArgs(args: String): Array<String> = (args as java.lang.String).split("""\s+""")

    private fun assertOptionsEqual(
        options: NullAwayOptions,
        parsedOptions: ErrorProneOptions,
    ) {
        assertDefault(parsedOptions, ErrorProneOptions::isDisableAllChecks)
        assertDefault(parsedOptions, ErrorProneOptions::isDisableAllChecks)
        assertDefault(parsedOptions, ErrorProneOptions::isDisableAllWarnings)
        assertDefault(parsedOptions, ErrorProneOptions::isDropErrorsToWarnings)
        assertDefault(parsedOptions, ErrorProneOptions::isEnableAllChecksAsWarnings)
        assertDefault(parsedOptions, ErrorProneOptions::disableWarningsInGeneratedCode)
        assertDefault(parsedOptions, ErrorProneOptions::ignoreUnknownChecks)
        assertDefault(parsedOptions, ErrorProneOptions::isIgnoreSuppressionAnnotations)
        assertDefault(parsedOptions, ErrorProneOptions::isTestOnlyTarget)
        assertDefault(parsedOptions, ErrorProneOptions::getExcludedPattern)

        assertThat(parsedOptions.severityMap).containsExactly("NullAway", options.severity.get().toSeverity())
        assertBooleanOptionEqual(parsedOptions, "NullAway:OnlyNullMarked", options.onlyNullMarked)
        assertListOptionEqual(parsedOptions, "NullAway:AnnotatedPackages", options.annotatedPackages)
        assertListOptionEqual(parsedOptions, "NullAway:UnannotatedSubPackages", options.unannotatedSubPackages)
        assertListOptionEqual(parsedOptions, "NullAway:UnannotatedClasses", options.unannotatedClasses)
        assertListOptionEqual(parsedOptions, "NullAway:KnownInitializers", options.knownInitializers)
        assertListOptionEqual(parsedOptions, "NullAway:ExcludedClassAnnotations", options.excludedClassAnnotations)
        assertListOptionEqual(parsedOptions, "NullAway:ExcludedClasses", options.excludedClasses)
        assertListOptionEqual(parsedOptions, "NullAway:ExcludedFieldAnnotations", options.excludedFieldAnnotations)
        assertListOptionEqual(parsedOptions, "NullAway:CustomInitializerAnnotations", options.customInitializerAnnotations)
        assertListOptionEqual(parsedOptions, "NullAway:ExternalInitAnnotations", options.externalInitAnnotations)
        assertBooleanOptionEqual(parsedOptions, "NullAway:TreatGeneratedAsUnannotated", options.treatGeneratedAsUnannotated)
        assertBooleanOptionEqual(parsedOptions, "NullAway:AcknowledgeRestrictiveAnnotations", options.acknowledgeRestrictiveAnnotations)
        assertBooleanOptionEqual(parsedOptions, "NullAway:CheckOptionalEmptiness", options.checkOptionalEmptiness)
        assertBooleanOptionEqual(parsedOptions, "NullAway:SuggestSuppressions", options.suggestSuppressions)
        assertBooleanOptionEqual(parsedOptions, "NullAway:AssertsEnabled", options.assertsEnabled)
        assertBooleanOptionEqual(parsedOptions, "NullAway:ExhaustiveOverride", options.exhaustiveOverride)
        assertStringOptionEqual(parsedOptions, "NullAway:CastToNonNullMethod", options.castToNonNullMethod)
        assertListOptionEqual(parsedOptions, "NullAway:CheckOptionalEmptinessCustomClasses", options.checkOptionalEmptinessCustomClasses)
        assertStringOptionEqual(parsedOptions, "NullAway:AutoFixSuppressionComment", options.autoFixSuppressionComment)
        assertBooleanOptionEqual(parsedOptions, "NullAway:HandleTestAssertionLibraries", options.handleTestAssertionLibraries)
        assertBooleanOptionEqual(parsedOptions, "NullAway:AcknowledgeAndroidRecent", options.acknowledgeAndroidRecent)
        assertBooleanOptionEqual(parsedOptions, "NullAway:CheckContracts", options.checkContracts)
        assertListOptionEqual(parsedOptions, "NullAway:CustomContractAnnotations", options.customContractAnnotations)
        assertListOptionEqual(parsedOptions, "NullAway:CustomNullableAnnotations", options.customNullableAnnotations)
        assertListOptionEqual(parsedOptions, "NullAway:CustomNonnullAnnotations", options.customNonnullAnnotations)
        assertListOptionEqual(parsedOptions, "NullAway:CustomGeneratedCodeAnnotations", options.customGeneratedCodeAnnotations)
        assertBooleanOptionEqual(parsedOptions, "NullAway:JSpecifyMode", options.jspecifyMode)
        assertListOptionEqual(parsedOptions, "NullAway:ExtraFuturesClasses", options.extraFuturesClasses)
        assertListOptionEqual(parsedOptions, "NullAway:SuppressionNameAliases", options.suppressionNameAliases)
        assertBooleanOptionEqual(parsedOptions, "NullAway:WarnOnGenericInferenceFailure", options.warnOnGenericInferenceFailure)

        assertThat(parsedOptions.flags.flagsMap.keys - ALL_NULLAWAY_OPTION_NAMES).isEmpty()
        assertThat(parsedOptions.remainingArgs).isEmpty()
    }

    private fun <T> assertDefault(
        parsedOptions: ErrorProneOptions,
        getter: ErrorProneOptions.() -> T,
    ) = assertThat(parsedOptions.getter()).isEqualTo(ErrorProneOptions.empty().getter())

    private fun assertListOptionEqual(
        parsedOptions: ErrorProneOptions,
        flagName: String,
        listProperty: ListProperty<String>,
    ) {
        parsedOptions.flags.getListOrEmpty(flagName).also {
            if (listProperty.orNull.isNullOrEmpty()) {
                assertThat(it).isEmpty()
            } else {
                assertThat(it).containsExactlyElementsIn(listProperty.get())
            }
        }
    }

    private fun assertBooleanOptionEqual(
        parsedOptions: ErrorProneOptions,
        flagName: String,
        booleanProperty: Property<Boolean>,
    ) {
        parsedOptions.flags.getBoolean(flagName).also {
            if (booleanProperty.isPresent) {
                assertThat(it).hasValue(booleanProperty.get())
            } else {
                assertThat(it).isEmpty()
            }
        }
    }

    private fun assertStringOptionEqual(
        parsedOptions: ErrorProneOptions,
        flagName: String,
        stringProperty: Property<String>,
    ) {
        parsedOptions.flags.get(flagName).also {
            if (stringProperty.isPresent) {
                assertThat(it).hasValue(stringProperty.get())
            } else {
                assertThat(it).isEmpty()
            }
        }
    }

    private fun CheckSeverity.toSeverity(): Severity =
        when (this) {
            CheckSeverity.DEFAULT -> Severity.DEFAULT
            CheckSeverity.OFF -> Severity.OFF
            CheckSeverity.WARN -> Severity.WARN
            CheckSeverity.ERROR -> Severity.ERROR
            else -> throw AssertionError()
        }
}
