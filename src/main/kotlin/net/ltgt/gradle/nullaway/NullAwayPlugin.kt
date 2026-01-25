package net.ltgt.gradle.nullaway

import net.ltgt.gradle.errorprone.ErrorProneOptions
import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByName

val ErrorProneOptions.nullaway
    get() = (this as ExtensionAware).extensions.getByName<NullAwayOptions>(NullAwayPlugin.EXTENSION_NAME)

fun ErrorProneOptions.nullaway(action: Action<in NullAwayOptions>) =
    (this as ExtensionAware).extensions.configure(NullAwayPlugin.EXTENSION_NAME, action)
