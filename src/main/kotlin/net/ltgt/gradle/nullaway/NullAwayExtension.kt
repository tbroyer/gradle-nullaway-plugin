package net.ltgt.gradle.nullaway

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.listProperty

open class NullAwayExtension internal constructor(
    objectFactory: ObjectFactory
) {
    /**
     * The list of packages that should be considered properly annotated according to the NullAway convention.
     */
    val annotatedPackages = objectFactory.listProperty<String>()
}
