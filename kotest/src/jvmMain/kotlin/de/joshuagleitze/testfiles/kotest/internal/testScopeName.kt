package de.joshuagleitze.testfiles.kotest.internal

import kotlin.reflect.KClass

internal actual fun KClass<*>.testScopeName(): String = qualifiedName ?: simpleName ?: "<anonymous>"
