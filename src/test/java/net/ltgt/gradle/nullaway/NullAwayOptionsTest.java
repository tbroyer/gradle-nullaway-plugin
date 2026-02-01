package net.ltgt.gradle.nullaway;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Sets;
import com.google.errorprone.ErrorProneOptions;
import com.google.errorprone.ErrorProneOptions.Severity;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.ltgt.gradle.errorprone.CheckSeverity;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NullAwayOptionsTest {
  private static final Set<String> ALL_NULLAWAY_OPTION_NAMES =
      Set.of(
          "NullAway:OnlyNullMarked",
          "NullAway:AnnotatedPackages",
          "NullAway:UnannotatedSubPackages",
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
          "NullAway:WarnOnGenericInferenceFailure");

  ObjectFactory objects;
  ProviderFactory providers;

  @BeforeAll
  void setup(@TempDir File projectDir) {
    var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
    objects = project.getObjects();
    providers = project.getProviders();
  }

  @Test
  void generatesCorrectErrorProneOptions() {
    doTestOptions(options -> options.getSeverity().set(CheckSeverity.DEFAULT));
    doTestOptions(options -> options.enable());
    doTestOptions(options -> options.disable());
    doTestOptions(options -> options.warn());
    doTestOptions(options -> options.error());
    doTestOptions(options -> options.getOnlyNullMarked().set(true));
    doTestOptions(options -> options.getAnnotatedPackages().add("test"));
    doTestOptions(options -> options.getUnannotatedSubPackages().add("test.dummy"));
    doTestOptions(options -> options.getUnannotatedClasses().add("test"));
    doTestOptions(options -> options.getKnownInitializers().add("com.foo.Bar.method"));
    doTestOptions(
        options -> options.getExcludedClassAnnotations().add("com.example.NullAwayExcluded"));
    doTestOptions(options -> options.getExcludedClasses().add("test.Excluded"));
    doTestOptions(options -> options.getExcludedFieldAnnotations().add("javax.ws.rs.core.Context"));
    doTestOptions(options -> options.getCustomInitializerAnnotations().add("com.foo.Initializer"));
    doTestOptions(options -> options.getExternalInitAnnotations().add("com.example.ExternalInit"));
    doTestOptions(options -> options.getTreatGeneratedAsUnannotated().set(true));
    doTestOptions(options -> options.getAcknowledgeRestrictiveAnnotations().set(true));
    doTestOptions(options -> options.getCheckOptionalEmptiness().set(true));
    doTestOptions(options -> options.getSuggestSuppressions().set(true));
    doTestOptions(options -> options.getAssertsEnabled().set(true));
    doTestOptions(options -> options.getExhaustiveOverride().set(true));
    doTestOptions(options -> options.getCastToNonNullMethod().set("com.foo.Bar.castToNonNull"));
    doTestOptions(
        options -> options.getCheckOptionalEmptinessCustomClasses().add("com.foo.Optional"));
    doTestOptions(
        options -> options.getAutoFixSuppressionComment().set("Auto-fix\\u0020suppression"));
    doTestOptions(options -> options.getHandleTestAssertionLibraries().set(true));
    doTestOptions(options -> options.getAcknowledgeAndroidRecent().set(true));
    doTestOptions(options -> options.getCheckContracts().set(true));
    doTestOptions(options -> options.getCustomContractAnnotations().add("com.example.Contract"));
    doTestOptions(options -> options.getCustomNullableAnnotations().add("com.example.CouldBeNull"));
    doTestOptions(
        options -> options.getCustomNonnullAnnotations().add("com.example.MustNotBeNull"));
    doTestOptions(
        options -> options.getCustomGeneratedCodeAnnotations().add("com.example.Generated"));
    doTestOptions(options -> options.getJspecifyMode().set(true));
    doTestOptions(options -> options.getExtraFuturesClasses().add("com.example.Future"));
    doTestOptions(options -> options.getSuppressionNameAliases().add("NullIssue"));
    doTestOptions(options -> options.getWarnOnGenericInferenceFailure().set(true));

    doTestOptions(
        options -> {
          options.enable();
          options.getAnnotatedPackages().add("test");
          options.getUnannotatedSubPackages().add("test.dummy");
          options.getUnannotatedClasses().add("test.Unannotated");
          options.getKnownInitializers().add("com.foo.Bar.method");
          options.getExcludedClassAnnotations().add("com.example.NullAwayExcluded");
          options.getExcludedClasses().add("test.Excluded");
          options.getExcludedFieldAnnotations().add("javax.ws.rs.core.Context");
          options.getCustomInitializerAnnotations().add("com.foo.Initializer");
          options.getExternalInitAnnotations().add("com.example.ExternalInit");
          options.getTreatGeneratedAsUnannotated().set(true);
          options.getAcknowledgeRestrictiveAnnotations().set(true);
          options.getCheckOptionalEmptiness().set(true);
          options.getSuggestSuppressions().set(true);
          options.getAssertsEnabled().set(true);
          options.getExhaustiveOverride().set(true);
          options.getCastToNonNullMethod().set("com.foo.Bar.castToNonNull");
          options.getCheckOptionalEmptinessCustomClasses().add("com.foo.Optional");
          options.getAutoFixSuppressionComment().set("Auto-fix\\u0020suppression");
          options.getHandleTestAssertionLibraries().set(true);
          options.getAcknowledgeAndroidRecent().set(true);
          options.getCheckContracts().set(true);
          options.getCustomContractAnnotations().add("com.example.Contract");
          options.getCustomNullableAnnotations().add("com.example.CouldBeNull");
          options.getCustomNonnullAnnotations().add("com.example.MustNotBeNull");
          options.getCustomGeneratedCodeAnnotations().add("com.example.Generated");
          options.getJspecifyMode().set(true);
          options.getExtraFuturesClasses().add("com.example.Future");
          options.getSuppressionNameAliases().add("NullIssue");
          options.getWarnOnGenericInferenceFailure().set(true);
        });
  }

  private void doTestOptions(Consumer<NullAwayOptions> configure) {
    var options =
        objects.newInstance(NullAwayOptions.class, objects.newInstance(NullAwayExtension.class));
    configure.accept(options);
    var parsedOptions = parseOptions(options);
    assertOptionsEqual(options, parsedOptions);
  }

  @Test
  void configuresConventionsFromExtension() {
    doTestOptions(
        extension -> extension.getAnnotatedPackages().add("test"),
        options -> options.getAnnotatedPackages().add("test"));
  }

  private void doTestOptions(
      Consumer<NullAwayExtension> configureExtension, Consumer<NullAwayOptions> configureOptions) {
    var extension = objects.newInstance(NullAwayExtension.class);
    configureExtension.accept(extension);
    var options = objects.newInstance(NullAwayOptions.class, extension);

    var expectedOptions =
        objects.newInstance(NullAwayOptions.class, objects.newInstance(NullAwayExtension.class));
    configureOptions.accept(expectedOptions);

    assertThat(options.getAnnotatedPackages().get())
        .containsExactlyElementsIn(expectedOptions.getAnnotatedPackages().get())
        .inOrder();
    assertThat(options.getOnlyNullMarked().getOrNull())
        .isEqualTo(expectedOptions.getOnlyNullMarked().getOrNull());
    assertThat(options.getJspecifyMode().getOrNull())
        .isEqualTo(expectedOptions.getJspecifyMode().getOrNull());

    var parsedOptions = parseOptions(options);
    assertOptionsEqual(expectedOptions, parsedOptions);
  }

  private ErrorProneOptions parseOptions(NullAwayOptions options) {
    return ErrorProneOptions.processArgs(splitArgs(String.join(" ", options.asArguments())));
  }

  // This is how JavaC "parses" the -Xplugin: values: https://git.io/vx8yI
  private String[] splitArgs(String args) {
    return args.split("\\s+");
  }

  private void assertOptionsEqual(NullAwayOptions options, ErrorProneOptions parsedOptions) {
    assertDefault(parsedOptions, ErrorProneOptions::isDisableAllChecks);
    assertDefault(parsedOptions, ErrorProneOptions::isDisableAllWarnings);
    assertDefault(parsedOptions, ErrorProneOptions::isDropErrorsToWarnings);
    assertDefault(parsedOptions, ErrorProneOptions::isEnableAllChecksAsWarnings);
    assertDefault(parsedOptions, ErrorProneOptions::disableWarningsInGeneratedCode);
    assertDefault(parsedOptions, ErrorProneOptions::ignoreUnknownChecks);
    assertDefault(parsedOptions, ErrorProneOptions::isIgnoreSuppressionAnnotations);
    assertDefault(parsedOptions, ErrorProneOptions::isTestOnlyTarget);
    assertDefault(parsedOptions, ErrorProneOptions::getExcludedPattern);
    assertDefault(parsedOptions, ErrorProneOptions::isPubliclyVisibleTarget);
    assertDefault(parsedOptions, ErrorProneOptions::isSuggestionsAsWarnings);

    assertThat(parsedOptions.getSeverityMap())
        .containsExactly("NullAway", toSeverity(options.getSeverity().get()));
    assertBooleanOptionEqual(parsedOptions, "NullAway:OnlyNullMarked", options.getOnlyNullMarked());
    assertListOptionEqual(
        parsedOptions, "NullAway:AnnotatedPackages", options.getAnnotatedPackages());
    assertListOptionEqual(
        parsedOptions, "NullAway:UnannotatedSubPackages", options.getUnannotatedSubPackages());
    assertListOptionEqual(
        parsedOptions, "NullAway:UnannotatedClasses", options.getUnannotatedClasses());
    assertListOptionEqual(
        parsedOptions, "NullAway:KnownInitializers", options.getKnownInitializers());
    assertListOptionEqual(
        parsedOptions, "NullAway:ExcludedClassAnnotations", options.getExcludedClassAnnotations());
    assertListOptionEqual(parsedOptions, "NullAway:ExcludedClasses", options.getExcludedClasses());
    assertListOptionEqual(
        parsedOptions, "NullAway:ExcludedFieldAnnotations", options.getExcludedFieldAnnotations());
    assertListOptionEqual(
        parsedOptions,
        "NullAway:CustomInitializerAnnotations",
        options.getCustomInitializerAnnotations());
    assertListOptionEqual(
        parsedOptions, "NullAway:ExternalInitAnnotations", options.getExternalInitAnnotations());
    assertBooleanOptionEqual(
        parsedOptions,
        "NullAway:TreatGeneratedAsUnannotated",
        options.getTreatGeneratedAsUnannotated());
    assertBooleanOptionEqual(
        parsedOptions,
        "NullAway:AcknowledgeRestrictiveAnnotations",
        options.getAcknowledgeRestrictiveAnnotations());
    assertBooleanOptionEqual(
        parsedOptions, "NullAway:CheckOptionalEmptiness", options.getCheckOptionalEmptiness());
    assertBooleanOptionEqual(
        parsedOptions, "NullAway:SuggestSuppressions", options.getSuggestSuppressions());
    assertBooleanOptionEqual(parsedOptions, "NullAway:AssertsEnabled", options.getAssertsEnabled());
    assertBooleanOptionEqual(
        parsedOptions, "NullAway:ExhaustiveOverride", options.getExhaustiveOverride());
    assertStringOptionEqual(
        parsedOptions, "NullAway:CastToNonNullMethod", options.getCastToNonNullMethod());
    assertListOptionEqual(
        parsedOptions,
        "NullAway:CheckOptionalEmptinessCustomClasses",
        options.getCheckOptionalEmptinessCustomClasses());
    assertStringOptionEqual(
        parsedOptions,
        "NullAway:AutoFixSuppressionComment",
        options.getAutoFixSuppressionComment());
    assertBooleanOptionEqual(
        parsedOptions,
        "NullAway:HandleTestAssertionLibraries",
        options.getHandleTestAssertionLibraries());
    assertBooleanOptionEqual(
        parsedOptions, "NullAway:AcknowledgeAndroidRecent", options.getAcknowledgeAndroidRecent());
    assertBooleanOptionEqual(parsedOptions, "NullAway:CheckContracts", options.getCheckContracts());
    assertListOptionEqual(
        parsedOptions,
        "NullAway:CustomContractAnnotations",
        options.getCustomContractAnnotations());
    assertListOptionEqual(
        parsedOptions,
        "NullAway:CustomNullableAnnotations",
        options.getCustomNullableAnnotations());
    assertListOptionEqual(
        parsedOptions, "NullAway:CustomNonnullAnnotations", options.getCustomNonnullAnnotations());
    assertListOptionEqual(
        parsedOptions,
        "NullAway:CustomGeneratedCodeAnnotations",
        options.getCustomGeneratedCodeAnnotations());
    assertBooleanOptionEqual(parsedOptions, "NullAway:JSpecifyMode", options.getJspecifyMode());
    assertListOptionEqual(
        parsedOptions, "NullAway:ExtraFuturesClasses", options.getExtraFuturesClasses());
    assertListOptionEqual(
        parsedOptions, "NullAway:SuppressionNameAliases", options.getSuppressionNameAliases());
    assertBooleanOptionEqual(
        parsedOptions,
        "NullAway:WarnOnGenericInferenceFailure",
        options.getWarnOnGenericInferenceFailure());

    assertThat(
            Sets.difference(
                parsedOptions.getFlags().getFlagsMap().keySet(), ALL_NULLAWAY_OPTION_NAMES))
        .isEmpty();
    assertThat(parsedOptions.getRemainingArgs()).isEmpty();
  }

  private <T> void assertDefault(
      ErrorProneOptions parsedOptions, Function<ErrorProneOptions, T> getter) {
    assertThat(getter.apply(parsedOptions)).isEqualTo(getter.apply(ErrorProneOptions.empty()));
  }

  private void assertListOptionEqual(
      ErrorProneOptions parsedOptions, String flagName, Provider<List<String>> listProperty) {
    assertThat(parsedOptions.getFlags().getListOrEmpty(flagName))
        .containsExactlyElementsIn(listProperty.getOrElse(List.of()));
  }

  private void assertBooleanOptionEqual(
      ErrorProneOptions parsedOptions, String flagName, Provider<Boolean> booleanProperty) {
    var flagValue = parsedOptions.getFlags().getBoolean(flagName);
    if (booleanProperty.isPresent()) {
      assertThat(flagValue).hasValue(booleanProperty.get());
    } else {
      assertThat(flagValue).isEmpty();
    }
  }

  private void assertStringOptionEqual(
      ErrorProneOptions parsedOptions, String flagName, Provider<String> stringProperty) {
    var flagValue = parsedOptions.getFlags().get(flagName);
    if (stringProperty.isPresent()) {
      assertThat(flagValue).hasValue(stringProperty.get());
    } else {
      assertThat(flagValue).isEmpty();
    }
  }

  private Severity toSeverity(CheckSeverity checkSeverity) {
    return switch (checkSeverity) {
      case DEFAULT -> Severity.DEFAULT;
      case OFF -> Severity.OFF;
      case WARN -> Severity.WARN;
      case ERROR -> Severity.ERROR;
    };
  }
}
