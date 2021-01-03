package de.joshuagleitze.testfiles

import ch.tutteli.atrium.api.fluent.en_GB.and
import ch.tutteli.atrium.api.fluent.en_GB.exists
import ch.tutteli.atrium.api.fluent.en_GB.existsNot
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.fileName
import ch.tutteli.atrium.api.fluent.en_GB.isAbsolute
import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isReadable
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.isWritable
import ch.tutteli.atrium.api.fluent.en_GB.matches
import ch.tutteli.atrium.api.fluent.en_GB.messageContains
import ch.tutteli.atrium.api.fluent.en_GB.notToBe
import ch.tutteli.atrium.api.fluent.en_GB.notToThrow
import ch.tutteli.atrium.api.fluent.en_GB.parent
import ch.tutteli.atrium.api.fluent.en_GB.startsWith
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.creating.Expect
import de.joshuagleitze.testfiles.DefaultTestFiles.Companion.determineTestFilesRootDirectory
import de.joshuagleitze.testfiles.DefaultTestFiles.TestResult.FAILURE
import de.joshuagleitze.testfiles.DefaultTestFiles.TestResult.SUCCESS
import de.joshuagleitze.testfiles.DefaultTestFilesSpec.content
import de.joshuagleitze.testfiles.DeletionMode.ALWAYS
import de.joshuagleitze.testfiles.DeletionMode.IF_SUCCESSFUL
import de.joshuagleitze.testfiles.DeletionMode.NEVER
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Files.delete
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.readText

object DefaultTestFilesSpec: Spek({
	val fileRoot = freezeFileRoot()
	lateinit var testFiles: DefaultTestFiles

	beforeEachTest {
		testFiles = DefaultTestFiles()
	}

	describe("DefaultTestFiles") {
		describe("root folder") {
			beforeEachTest { deletePotentialTargetDirectories() }

			it("uses the build directory if present") {
				buildDir.createDirectories()
				targetDir.createDirectories()
				testOutputsDir.createDirectories()
				expect(determineTestFilesRootDirectory()).toBe(buildDir / "test-outputs")
			}

			it("uses the target directory if present") {
				targetDir.createDirectories()
				testOutputsDir.createDirectories()
				expect(determineTestFilesRootDirectory()).toBe(targetDir / "test-outputs")
			}

			it("uses the test-outputs directory if present") {
				testOutputsDir.createDirectories()
				expect(determineTestFilesRootDirectory()).toBe(testOutputsDir)
			}

			it("falls back to the tmpdir") {
				expect(determineTestFilesRootDirectory()).toBe(tmpDir / "test-outputs")
			}
		}

		describe("housekeeping") {
			it("clears pre-existing files when entering a scope") {
				val scopeDir = (fileRoot / "[delete pre-existing group]").createDirectories()
				val testDir = (scopeDir / "pre-existing dir").createDirectory()
				val subTestFile = (testDir / "sub").createFile()
				val testFile = (scopeDir / "pre-existing file").createFile()

				testFiles.enterScope("delete pre-existing group")

				expect(testDir).existsNot()
				expect(subTestFile).existsNot()
				expect(testFile).existsNot()
			}

			it("retains existing scope directories when entering a scope") {
				val scopeDir = (fileRoot / "[retain pre-existing group]").createDirectories()
				val groupTestDir = (scopeDir / "[test]").createDirectory()
				val subTestFile = (groupTestDir / "deeper pre-existing file").createFile()
				val testFile = (scopeDir / "pre-existing file").createFile()

				testFiles.enterScope("retain pre-existing group")

				expect(scopeDir).isDirectory()
				expect(groupTestDir).isDirectory()
				expect(subTestFile).isRegularFile()
				expect(testFile).existsNot()
			}

			it("does not create a scope folder if not necessary") {
				val outerScopeTarget = fileRoot / "[no premature creation]"
				val innerScopeTarget = outerScopeTarget / "[sub]"

				testFiles.enterScope("no premature creation")
				expect(outerScopeTarget).existsNot()

				testFiles.enterScope("sub")
				expect(outerScopeTarget).existsNot()
				expect(innerScopeTarget).existsNot()

				testFiles.createFile()
				expect(innerScopeTarget).exists()
			}
		}

		describe("file name checks") {
			it("rejects file names that match the group directory pattern") {
				testFiles.enterScope("rejects bad file names")

				expect { testFiles.createFile("[test") }.notToThrow()
				expect { testFiles.createFile("test]") }.notToThrow()
				expect { testFiles.createFile("[test]") }.toThrow<IllegalArgumentException> {
					messageContains("[test]")
				}
			}

			it("rejects directory names that match the group directory pattern") {
				testFiles.enterScope("rejects bad directory names")

				expect { testFiles.createDirectory("[test") }.notToThrow()
				expect { testFiles.createDirectory("test]") }.notToThrow()
				expect { testFiles.createDirectory("[test]") }.toThrow<IllegalArgumentException> {
					messageContains("[test]")
				}
			}

			listOf('/', '\\', '<', '>', ':', '\"', '|', '?', '*', '\u0000').forEach { badCharacter ->
				it("escapes '$badCharacter' in a scope name if necessary") {
					testFiles.enterScope("test with -$badCharacter- in it")

					expect { testFiles.createFile("test") }.notToThrow()
						// check that / \ is not messing up the directory structure
						.and.parent.fileName.matches(Regex(".test with -.- in it."))
				}
			}
		}
	}

	describe("file creation") {
		it("creates an empty file with the provided name") {
			testFiles.enterScope("named file creation")
			expect(testFiles.createFile("testfile")) {
				isRegularFile()
				isReadable()
				isWritable()
				content.toBe("")
				fileName.toBe("testfile")
				parent.toBe(fileRoot / "[named file creation]")
			}

			testFiles.enterScope("inner")
			expect(testFiles.createFile("testfile")) {
				isRegularFile()
				isReadable()
				isWritable()
				content.toBe("")
				fileName.toBe("testfile")
				parent.toBe(fileRoot / "[named file creation]" / "[inner]")
			}
		}

		it("creates an empty directory with the provided name") {
			testFiles.enterScope("named directory creation")
			expect(testFiles.createDirectory("testdir")) {
				isDirectory()
				isReadable()
				isWritable()
				fileName.toBe("testdir")
				parent.toBe(fileRoot / "[named directory creation]")
			}

			testFiles.enterScope("inner")
			expect(testFiles.createDirectory("testdir")) {
				isDirectory()
				isReadable()
				isWritable()
				fileName.toBe("testdir")
				parent.toBe(fileRoot / "[named directory creation]" / "[inner]")
			}
		}

		it("hands out absolute paths") {
			testFiles.enterScope("hands out absolute paths")

			expect(testFiles.createFile("testFile")).isAbsolute()
			expect(testFiles.createDirectory("testDir")).isAbsolute()
		}

		it("creates an empty file with a generated name") {
			testFiles.enterScope("unnamed file creation")
			expect(testFiles.createFile()) {
				isRegularFile()
				isReadable()
				isWritable()
				content.toBe("")
				fileName.startsWith("test-")
				parent.toBe(fileRoot / "[unnamed file creation]")
			}

			testFiles.enterScope("inner")
			expect(testFiles.createFile()) {
				isRegularFile()
				isReadable()
				isWritable()
				content.toBe("")
				fileName.startsWith("test-")
				parent.toBe(fileRoot / "[unnamed file creation]" / "[inner]")
			}
		}

		it("creates an empty directory with a generated name") {
			testFiles.enterScope("unnamed directory creation")
			expect(testFiles.createDirectory()) {
				isDirectory()
				isReadable()
				isWritable()
				fileName.startsWith("test-")
				parent.toBe(fileRoot / "[unnamed directory creation]")
			}

			testFiles.enterScope("inner")
			expect(testFiles.createDirectory()) {
				isDirectory()
				isReadable()
				isWritable()
				fileName.startsWith("test-")
				parent.toBe(fileRoot / "[unnamed directory creation]" / "[inner]")
			}
		}

		it("generates different file names on subsequent creations") {
			testFiles.enterScope("different file names")

			expect(testFiles.createFile()).notToBe(testFiles.createFile())
			expect(testFiles.createDirectory()).notToBe(testFiles.createDirectory())
		}

		it("generates the same file names for the same creations") {
			testFiles.enterScope("consistency")
			val firstFileFirstTime = testFiles.createFile()
			val secondFileFirstTime = testFiles.createFile()
			val thirdFileFirstTime = testFiles.createFile()
			testFiles.leaveScope(SUCCESS)

			testFiles.enterScope("consistency")
			val firstFileSecondTime = testFiles.createFile()
			val secondFileSecondTime = testFiles.createFile()
			val thirdFileSecondTime = testFiles.createFile()
			testFiles.leaveScope(SUCCESS)

			expect(firstFileFirstTime).toBe(firstFileSecondTime)
			expect(secondFileFirstTime).toBe(secondFileSecondTime)
			expect(thirdFileFirstTime).toBe(thirdFileSecondTime)
		}
	}

	describe("file cleanup") {
		listOf(
			ALWAYS to SUCCESS,
			ALWAYS to FAILURE,
			IF_SUCCESSFUL to SUCCESS
		).forEach { (deletionMode, result) ->
			it("deletes a file that has been marked to be deleted $deletionMode after $result") {
				testFiles.enterScope("delete after $result")
				val testfile = testFiles.createFile(delete = deletionMode)
				val testdir = testFiles.createDirectory(delete = deletionMode)
				expect(testfile).exists()
				expect(testdir).exists()

				testFiles.leaveScope(result)
				expect(testfile).existsNot()
				expect(testdir).existsNot()
			}
		}

		listOf(
			IF_SUCCESSFUL to FAILURE,
			NEVER to SUCCESS,
			NEVER to FAILURE
		).forEach { (deletionMode, result) ->
			it("retains a file that has been marked to be deleted $deletionMode after $result") {
				testFiles.enterScope("retain after $result")
				val testfile = testFiles.createFile(delete = deletionMode)
				val testdir = testFiles.createDirectory(delete = deletionMode)
				expect(testfile).exists()
				expect(testdir).exists()

				testFiles.leaveScope(result)
				expect(testfile).exists()
				expect(testdir).exists()
			}
		}

		it("retains a file that has been marked to be deleted IF_SUCCESSFUL if only one inner scope reported FAILURE") {
			testFiles.enterScope("outer")
			val testfile = testFiles.createFile(delete = IF_SUCCESSFUL)
			val testdir = testFiles.createDirectory(delete = IF_SUCCESSFUL)

			testFiles.enterScope("first inner (successful)")
			testFiles.createFile(delete = IF_SUCCESSFUL)
			testFiles.leaveScope(SUCCESS)

			testFiles.enterScope("second inner (failing)")
			testFiles.createFile(delete = IF_SUCCESSFUL)
			testFiles.leaveScope(FAILURE)

			testFiles.enterScope("third inner (successful)")
			testFiles.createFile(delete = IF_SUCCESSFUL)
			testFiles.leaveScope(SUCCESS)

			testFiles.leaveScope(SUCCESS)

			expect(testfile).exists()
			expect(testdir).exists()
		}

		it("tolerates deletion of created files") {
			testFiles.enterScope("tolerate deletion")
			delete(testFiles.createFile(delete = ALWAYS))
			delete(testFiles.createDirectory(delete = ALWAYS))

			expect {
				testFiles.leaveScope(SUCCESS)
			}.notToThrow()
		}
	}
}) {
	val Expect<Path>.content get() = feature("content") { readText() }
}
