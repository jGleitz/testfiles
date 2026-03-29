package de.joshuagleitze.testfiles.internal

import okio.FileSystem
import okio.Path

internal actual val platformFileSystem: FileSystem get() = FileSystem.SYSTEM
internal actual val platformTemporaryDirectory: Path get() = FileSystem.SYSTEM_TEMPORARY_DIRECTORY
