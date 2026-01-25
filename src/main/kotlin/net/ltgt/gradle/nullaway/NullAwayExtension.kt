@file:Suppress("ktlint:standard:no-wildcard-imports")

package net.ltgt.gradle.nullaway

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class NullAwayExtension {
    /**
     * Indicates that the [annotatedPackages] flag has been deliberately omitted, and that NullAway can proceed with only treating `@NullMarked` code as annotated, in accordance with the JSpecify specification.
     */
    abstract val onlyNullMarked: Property<Boolean>

    /**
     * The list of packages that should be considered properly annotated according to the NullAway convention.
     */
    abstract val annotatedPackages: ListProperty<String>

    /** If set to true, enables new checks based on JSpecify (like checks for generic types). */
    abstract val jspecifyMode: Property<Boolean>
}
