package de.joshuagleitze.testfiles.internal

internal expect inline fun <T> synchronizedAccess(lock: Any, block: () -> T): T
