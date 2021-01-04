package de.joshuagleitze.testfiles

import de.joshuagleitze.testfiles.DefaultTestFiles.Companion.determineTestFilesRootDirectory
import java.nio.file.Files.walk
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting

val buildDir = Path("build").toAbsolutePath()
val targetDir = Path("target").toAbsolutePath()
val testOutputsDir = Path("test-outputs").toAbsolutePath()
val tmpDir = Path(System.getProperty("java.io.tmpdir")).toAbsolutePath()

fun deletePotentialTargetDirectories() {
	listOf(buildDir, targetDir, testOutputsDir).forEach { it.deleteRecursivelyIfExists() }
	determineTestFilesRootDirectory().deleteRecursivelyIfExists()
}

private fun Path.deleteRecursivelyIfExists() = walkIfExists().sorted(reverseOrder()).forEach { it.deleteExisting() }

private fun Path.walkIfExists() = try {
	walk(this)
} catch (e: java.nio.file.NoSuchFileException) {
	Stream.empty()
}

private val fileRoot by lazy {
	deletePotentialTargetDirectories()
	testOutputsDir.createDirectories()
	val fileRoot = determineTestFilesRootDirectory()
	DefaultTestFiles() // call constructor to freeze output directory
	fileRoot
}

fun freezeFileRoot() = fileRoot
