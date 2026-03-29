package de.joshuagleitze.testfiles.internal

import okio.FileSystem
import okio.Path

internal actual val platformFileSystem: FileSystem get() = JsNodeFileSystem
internal actual val platformTemporaryDirectory: Path get() = JsNodeFileSystem.TMPDIR
