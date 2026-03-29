package de.joshuagleitze.testfiles.internal

import okio.FileSystem
import okio.Path

internal expect val platformFileSystem: FileSystem
internal expect val platformTemporaryDirectory: Path
