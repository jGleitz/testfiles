package de.joshuagleitze.testfiles.internal

private val unixInvalidCharacters = Regex("[/\u0000]")
private val windowsInvalidCharacters = Regex("[/\\\\<>:\"|?*\u0000]")

// Check process.platform to determine the OS (Node.js environment)
@Suppress("UNUSED_VARIABLE")
private external val process: dynamic

internal actual val invalidFileNameCharacters: Regex
get() = if (js("process.platform") == "win32") windowsInvalidCharacters else unixInvalidCharacters
