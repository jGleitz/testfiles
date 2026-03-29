package de.joshuagleitze.testfiles.internal

internal actual inline fun <T> synchronizedAccess(lock: Any, block: () -> T): T = synchronized(lock, block)
