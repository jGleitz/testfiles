package de.joshuagleitze.testfiles.internal

import okio.Path
import okio.Path.Companion.toPath

internal actual fun absolutize(path: Path): Path {
if (path.isAbsolute) return path
return JsNodeFileSystem.canonicalize(path)
}
