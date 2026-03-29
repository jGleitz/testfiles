package de.joshuagleitze.testfiles.kotest.internal

import kotlin.reflect.KClass

internal expect fun KClass<*>.testScopeName(): String
