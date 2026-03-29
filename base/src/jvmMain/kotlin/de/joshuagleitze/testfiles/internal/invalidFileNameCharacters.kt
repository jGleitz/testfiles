package de.joshuagleitze.testfiles.internal

private val unixInvalidCharacters get() = Regex("[/\u0000]")
private val windowsInvalidCharacters get() = Regex("[/\\\\<>:\"|?*\u0000]")

internal actual val invalidFileNameCharacters: Regex by lazy {
	val osName = System.getProperty("os.name").lowercase()
	if (setOf("nix", "nux", "aix", "mac").any { osName.contains(it) }) unixInvalidCharacters
	else windowsInvalidCharacters // default to windows because it is the most restrictive
}
