package de.joshuagleitze.testfiles.kotest.internal

import kotlin.reflect.KClass

// KClass.qualifiedName is not supported in Kotlin/JS; use simpleName instead
internal actual fun KClass<*>.testScopeName(): String = simpleName ?: "<anonymous>"
