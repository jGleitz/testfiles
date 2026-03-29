package de.joshuagleitze.testfiles.internal

import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.blackholeSink

/**
 * A minimal [FileSystem] implementation for Kotlin/JS Node.js using the built-in `fs`, `os`, and `path` modules.
 * Only the operations needed by [de.joshuagleitze.testfiles.DefaultTestFiles] are implemented.
 */
internal object JsNodeFileSystem : FileSystem() {
override fun canonicalize(path: Path): Path {
val str = path.toString()
return js("require('path').resolve(str)").unsafeCast<String>().toPath()
}

override fun metadataOrNull(path: Path): FileMetadata? {
val str = path.toString()
return try {
val stats: dynamic = js("require('fs').statSync(str)")
FileMetadata(
isRegularFile = stats.isFile().unsafeCast<Boolean>(),
isDirectory = stats.isDirectory().unsafeCast<Boolean>(),
)
} catch (e: Throwable) {
null
}
}

override fun list(dir: Path): List<Path> {
val str = dir.toString()
return try {
val entries = js("require('fs').readdirSync(str)").unsafeCast<Array<String>>()
entries.map { dir / it }
} catch (e: Throwable) {
throw IOException("Failed to list $dir", e)
}
}

override fun listOrNull(dir: Path): List<Path>? {
val str = dir.toString()
return try {
val entries = js("require('fs').readdirSync(str)").unsafeCast<Array<String>>()
entries.map { dir / it }
} catch (e: Throwable) {
null
}
}

override fun createDirectory(dir: Path, mustCreate: Boolean) {
val str = dir.toString()
try {
js("require('fs').mkdirSync(str)")
} catch (e: Throwable) {
if (!exists(dir)) throw IOException("Failed to create directory $dir", e)
}
}

override fun appendingSink(file: Path, mustExist: Boolean): Sink {
throw UnsupportedOperationException("appendingSink is not supported by JsNodeFileSystem")
}

/**
 * Creates an empty file and returns a [blackholeSink] that discards any written data.
 * This is sufficient for [de.joshuagleitze.testfiles.DefaultTestFiles] which only creates empty files.
 */
override fun sink(file: Path, mustCreate: Boolean): Sink {
val str = file.toString()
if (mustCreate && exists(file)) throw IOException("$file already exists")
try {
js("require('fs').writeFileSync(str, '')")
} catch (e: Throwable) {
throw IOException("Failed to create file $file", e)
}
return blackholeSink()
}

override fun delete(path: Path, mustExist: Boolean) {
val meta = metadataOrNull(path)
if (meta == null) {
if (mustExist) throw IOException("$path does not exist")
return
}
val str = path.toString()
try {
if (meta.isDirectory) {
js("require('fs').rmdirSync(str)")
} else {
js("require('fs').unlinkSync(str)")
}
} catch (e: Throwable) {
throw IOException("Failed to delete $path", e)
}
}

override fun atomicMove(source: Path, target: Path) {
val src = source.toString()
val dst = target.toString()
try {
js("require('fs').renameSync(src, dst)")
} catch (e: Throwable) {
throw IOException("Failed to move $source to $target", e)
}
}

override fun createSymlink(source: Path, target: Path) {
throw UnsupportedOperationException("Symlinks are not supported by JsNodeFileSystem")
}

override fun openReadOnly(file: Path): FileHandle =
throw UnsupportedOperationException("openReadOnly is not supported by JsNodeFileSystem")

override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle =
throw UnsupportedOperationException("openReadWrite is not supported by JsNodeFileSystem")

override fun source(file: Path): Source =
throw UnsupportedOperationException("source is not supported by JsNodeFileSystem")

val TMPDIR: Path
get() = js("require('os').tmpdir()").unsafeCast<String>().toPath()
}
