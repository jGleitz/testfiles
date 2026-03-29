package de.joshuagleitze.testfiles.internal

// JavaScript is single-threaded, so no synchronization is needed.
internal actual inline fun <T> synchronizedAccess(lock: Any, block: () -> T): T = block()
