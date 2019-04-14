package net.ltgt.gradle.nullaway

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.listProperty

open class NullAwayExtension internal constructor(
    objectFactory: ObjectFactory
) {
    val annotatedPackages = objectFactory.listProperty<String>()
}
