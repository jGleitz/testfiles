import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isReadable
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.isWritable
import ch.tutteli.atrium.api.fluent.en_GB.parent
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import de.joshuagleitze.test.spek.testfiles.testFiles
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Path

object TestFileIntegrationSpec: Spek({
    val expectedRootFolder: Path = Path.of("build/test-outputs")
    val testFiles = testFiles()

    describe("testFiles") {
        val expectedGroupFolder = expectedRootFolder
            .resolve("[TestFileIntegrationSpec]")
            .resolve("[testFiles]")

        it("creates a test file with the appropriate name") {
            expect(testFiles.createFile()) {
                isRegularFile()
                isReadable()
                isWritable()
                parent.toBe(
                    expectedGroupFolder
                        .resolve("[creates a test file with the appropriate name]")
                )
            }
        }

        it("creates a test directory with the appropriate name") {
            expect(testFiles.createDirectory()) {
                isDirectory()
                isReadable()
                isWritable()
                parent.toBe(
                    expectedGroupFolder
                        .resolve("[creates a test directory with the appropriate name]")
                )
            }
        }
    }
})
