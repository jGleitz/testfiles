package de.joshuagleitze.testfiles.internal

import okio.Path
import okio.Path.Companion.toPath

internal actual fun absolutize(path: Path): Path {
if (path.isAbsolute) return path
return java.nio.file.Paths.get(path.toString()).toAbsolutePath().toString().toPath()
}
