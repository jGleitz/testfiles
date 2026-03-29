package de.joshuagleitze.testfiles

import de.joshuagleitze.testfiles.DefaultTestFiles.Companion.determineTestFilesRootDirectory
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.nio.file.Files.walk
import java.util.stream.Stream
import kotlin.io.path.deleteExisting

private fun String.toAbsoluteOkioPath() = java.nio.file.Paths.get(this).toAbsolutePath().toString().toPath()

val buildDir = "build".toAbsoluteOkioPath()
val targetDir = "target".toAbsoluteOkioPath()
val testOutputsDir = "test-outputs".toAbsoluteOkioPath()
val tmpDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY

fun deletePotentialTargetDirectories() {
listOf(buildDir, targetDir, testOutputsDir).forEach { it.deleteRecursivelyIfExists() }
determineTestFilesRootDirectory().deleteRecursivelyIfExists()
}

private fun Path.deleteRecursivelyIfExists() {
val nioPath = java.nio.file.Paths.get(toString())
walkNioIfExists(nioPath).sorted(reverseOrder()).forEach { it.deleteExisting() }
}

private fun walkNioIfExists(path: java.nio.file.Path) = try {
walk(path)
} catch (e: java.nio.file.NoSuchFileException) {
Stream.empty()
}

private val fileRoot by lazy {
deletePotentialTargetDirectories()
FileSystem.SYSTEM.createDirectories(testOutputsDir)
val fileRoot = determineTestFilesRootDirectory()
DefaultTestFiles() // call constructor to freeze output directory
fileRoot
}

fun freezeFileRoot() = fileRoot
