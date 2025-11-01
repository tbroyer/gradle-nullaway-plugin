@file:Suppress("ktlint:standard:no-wildcard-imports")

package net.ltgt.gradle.nullaway

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.*

open class NullAwayExtension internal constructor(
    objectFactory: ObjectFactory,
) {
    /**
     * Indicates that the [annotatedPackages] flag has been deliberately omitted, and that NullAway can proceed with only treating `@NullMarked` code as annotated, in accordance with the JSpecify specification.
     */
    val onlyNullMarked = objectFactory.property<Boolean>()

    /**
     * The list of packages that should be considered properly annotated according to the NullAway convention.
     */
    val annotatedPackages = objectFactory.listProperty<String>()

    /** If set to true, enables new checks based on JSpecify (like checks for generic types). */
    val jspecifyMode = objectFactory.property<Boolean>()
}
