import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.niok.deleteRecursively
import de.joshuagleitze.test.spek.testfiles.DefaultTestFiles
import de.joshuagleitze.test.spek.testfiles.DefaultTestFiles.Companion.determineTestFilesRootDirectory
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.runtime.scope.ScopeId
import org.spekframework.spek2.runtime.scope.ScopeImpl
import org.spekframework.spek2.runtime.scope.ScopeType
import java.nio.file.Files.createDirectories
import java.nio.file.Files.readAllBytes
import java.nio.file.Path
import java.nio.file.Paths

internal inline fun <reified S: ScopeImpl> mockScope(name: String) = mockk<S>().also {
    every { it.id } returns ScopeId(ScopeType.Scope, name)
}

internal val Expect<Path>.content get() = feature("string content") { String(readAllBytes(this)) }

val buildDir = Paths.get("build")
val targetDir = Paths.get("target")
val testOutputsDir = Paths.get("test-outputs")
val tmpDir = Paths.get(System.getProperty("java.io.tmpdir"))

fun deletePotentialTargetDirectories() {
    listOf(buildDir, targetDir, testOutputsDir).forEach { it.deleteRecursively() }
    determineTestFilesRootDirectory().deleteRecursively()
}

private val fileRoot by lazy {
    deletePotentialTargetDirectories()
    createDirectories(testOutputsDir)
    val fileRoot = determineTestFilesRootDirectory()
    DefaultTestFiles() // call constructor to freeze output directory
    fileRoot
}

fun freezeFileRoot() = fileRoot
