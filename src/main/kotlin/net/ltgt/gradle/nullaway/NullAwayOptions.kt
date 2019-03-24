package net.ltgt.gradle.nullaway

import net.ltgt.gradle.errorprone.CheckSeverity
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class NullAwayOptions internal constructor(
    objectFactory: ObjectFactory,
    nullawayExtension: NullAwayExtension
) {
    companion object {
        const val NAME = "nullaway"
    }

    @get:Input
    val severity = objectFactory.property<CheckSeverity>().convention(CheckSeverity.DEFAULT)
    @get:Input
    val annotatedPackages = objectFactory.listProperty<String>().convention(nullawayExtension.annotatedPackages)
    @get:Input
    @get:Optional
    val unannotatedSubPackages = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val unannotatedClasses = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val knownInitializers = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val excludedClassAnnotations = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val excludedClasses = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val excludedFieldAnnotations = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val customInitializerAnnotations = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val externalInitAnnotations = objectFactory.listProperty<String>()
    @get:Input
    @get:Optional
    val treatGeneratedAsUnannotated = objectFactory.property<Boolean>()
    @get:Input
    @get:Optional
    val acknowledgeRestrictiveAnnotations = objectFactory.property<Boolean>()
    @get:Input
    @get:Optional
    val checkOptionalEmptiness = objectFactory.property<Boolean>()

    internal fun asArguments(): Iterable<String> = sequenceOf(
        "-Xep:NullAway${severity.getOrElse(CheckSeverity.DEFAULT).asArg}",
        listOption("AnnotatedPackages", annotatedPackages),
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
        booleanOption("CheckOptionalEmptiness", checkOptionalEmptiness)
    )
        .filterNotNull()
        .asIterable()

    private fun listOption(name: String, value: Provider<List<String>>) =
        value.orNull.takeUnless { it.isNullOrEmpty() }?.let { "-XepOpt:NullAway:$name=${it.joinToString(separator = ",")}" }

    private fun booleanOption(name: String, value: Provider<Boolean>): String? =
        value.orNull?.let { "-XepOpt:NullAway:$name=$it" }

    private fun stringOption(name: String, value: Provider<String>): String? =
        value.orNull?.let { "-XepOpt:NullAway:$name=$it" }
}

private val CheckSeverity.asArg: String
    get() = when (this) {
        CheckSeverity.DEFAULT -> ""
        else -> ":$name"
    }
