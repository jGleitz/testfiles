package de.joshuagleitze.testfiles

import ch.tutteli.atrium.api.fluent.en_GB.and
import ch.tutteli.atrium.api.fluent.en_GB.exists
import ch.tutteli.atrium.api.fluent.en_GB.existsNot
import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.matches
import ch.tutteli.atrium.api.fluent.en_GB.messageContains
import ch.tutteli.atrium.api.fluent.en_GB.notToBe
import ch.tutteli.atrium.api.fluent.en_GB.notToThrow
import ch.tutteli.atrium.api.fluent.en_GB.startsWith
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import de.joshuagleitze.testfiles.DefaultTestFiles.Companion.determineTestFilesRootDirectory
import de.joshuagleitze.testfiles.DefaultTestFiles.ScopeResult.Failure
import de.joshuagleitze.testfiles.DefaultTestFiles.ScopeResult.Success
import de.joshuagleitze.testfiles.DeletionMode.Always
import de.joshuagleitze.testfiles.DeletionMode.IfSuccessful
import de.joshuagleitze.testfiles.DeletionMode.Never
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object DefaultTestFilesSpec : Spek({
	val fileRoot = freezeFileRoot()
	lateinit var testFiles: DefaultTestFiles

	beforeEachTest {
		testFiles = DefaultTestFiles()
	}

	describe("DefaultTestFiles") {
		describe("root folder") {
			beforeEachTest { deletePotentialTargetDirectories() }

			it("uses the build directory if present") {
				FileSystem.SYSTEM.createDirectories(buildDir)
				FileSystem.SYSTEM.createDirectories(targetDir)
				FileSystem.SYSTEM.createDirectories(testOutputsDir)
				expect(determineTestFilesRootDirectory()).toBe(buildDir / "test-outputs")
			}

			it("uses the target directory if present") {
				FileSystem.SYSTEM.createDirectories(targetDir)
				FileSystem.SYSTEM.createDirectories(testOutputsDir)
				expect(determineTestFilesRootDirectory()).toBe(targetDir / "test-outputs")
			}

			it("uses the test-outputs directory if present") {
				FileSystem.SYSTEM.createDirectories(testOutputsDir)
				expect(determineTestFilesRootDirectory()).toBe(testOutputsDir)
			}

			it("falls back to the tmpdir") {
				expect(determineTestFilesRootDirectory()).toBe(tmpDir / "test-outputs")
			}
		}

		describe("housekeeping") {
			it("clears pre-existing files when entering a scope") {
				val scopeDir = fileRoot / "[delete pre-existing group]"
				FileSystem.SYSTEM.createDirectories(scopeDir)
				val testDir = scopeDir / "pre-existing dir"
				FileSystem.SYSTEM.createDirectory(testDir)
				val subTestFile = testDir / "sub"
				FileSystem.SYSTEM.write(subTestFile) {}
				val testFile = scopeDir / "pre-existing file"
				FileSystem.SYSTEM.write(testFile) {}

				testFiles.enterScope("delete pre-existing group")

				expect(testDir.toNioPath()).existsNot()
				expect(subTestFile.toNioPath()).existsNot()
				expect(testFile.toNioPath()).existsNot()
			}

			it("retains existing scope directories when entering a scope") {
				val scopeDir = fileRoot / "[retain pre-existing group]"
				FileSystem.SYSTEM.createDirectories(scopeDir)
				val groupTestDir = scopeDir / "[test]"
				FileSystem.SYSTEM.createDirectory(groupTestDir)
				val subTestFile = groupTestDir / "deeper pre-existing file"
				FileSystem.SYSTEM.write(subTestFile) {}
				val testFile = scopeDir / "pre-existing file"
				FileSystem.SYSTEM.write(testFile) {}

				testFiles.enterScope("retain pre-existing group")

				expect(scopeDir.toNioPath()).isDirectory()
				expect(groupTestDir.toNioPath()).isDirectory()
				expect(subTestFile.toNioPath()).isRegularFile()
				expect(testFile.toNioPath()).existsNot()
			}

			it("does not create a scope folder if not necessary") {
				val outerScopeTarget = fileRoot / "[no premature creation]"
				val innerScopeTarget = outerScopeTarget / "[sub]"

				testFiles.enterScope("no premature creation")
				expect(outerScopeTarget.toNioPath()).existsNot()

				testFiles.enterScope("sub")
				expect(outerScopeTarget.toNioPath()).existsNot()
				expect(innerScopeTarget.toNioPath()).existsNot()

				testFiles.createFile()
				expect(innerScopeTarget.toNioPath()).exists()
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
			testFiles.leaveScope(Success)

			testFiles.enterScope("consistency")
			val firstFileSecondTime = testFiles.createFile()
			val secondFileSecondTime = testFiles.createFile()
			val thirdFileSecondTime = testFiles.createFile()
			testFiles.leaveScope(Success)

			expect(firstFileFirstTime).toBe(firstFileSecondTime)
			expect(secondFileFirstTime).toBe(secondFileSecondTime)
			expect(thirdFileFirstTime).toBe(thirdFileSecondTime)
		}
	}

	describe("file cleanup") {
		listOf(
			Always to Success,
			Always to Failure,
			IfSuccessful to Success
		).forEach { (deletionMode, result) ->
			it("deletes a file that has been marked to be deleted $deletionMode after $result") {
				testFiles.enterScope("delete after $result")
				val testfile = testFiles.createFile(delete = deletionMode)
				val testdir = testFiles.createDirectory(delete = deletionMode)
				expect(testfile.toNioPath()).exists()
				expect(testdir.toNioPath()).exists()

				testFiles.leaveScope(result)
				expect(testfile.toNioPath()).existsNot()
				expect(testdir.toNioPath()).existsNot()
			}
		}

		listOf(
			IfSuccessful to Failure,
			Never to Success,
			Never to Failure
		).forEach { (deletionMode, result) ->
			it("retains a file that has been marked to be deleted $deletionMode after $result") {
				testFiles.enterScope("retain after $result")
				val testfile = testFiles.createFile(delete = deletionMode)
				val testdir = testFiles.createDirectory(delete = deletionMode)
				expect(testfile.toNioPath()).exists()
				expect(testdir.toNioPath()).exists()

				testFiles.leaveScope(result)
				expect(testfile.toNioPath()).exists()
				expect(testdir.toNioPath()).exists()
			}
		}

		it("retains a file that has been marked to be deleted IF_SUCCESSFUL if only one inner scope reported FAILURE") {
			testFiles.enterScope("outer")
			val testfile = testFiles.createFile(delete = IfSuccessful)
			val testdir = testFiles.createDirectory(delete = IfSuccessful)

			testFiles.enterScope("first inner (successful)")
			testFiles.createFile(delete = IfSuccessful)
			testFiles.leaveScope(Success)

			testFiles.enterScope("second inner (failing)")
			testFiles.createFile(delete = IfSuccessful)
			testFiles.leaveScope(Failure)

			testFiles.enterScope("third inner (successful)")
			testFiles.createFile(delete = IfSuccessful)
			testFiles.leaveScope(Success)

			testFiles.leaveScope(Success)

			expect(testfile.toNioPath()).exists()
			expect(testdir.toNioPath()).exists()
		}

		it("tolerates deletion of created files") {
			testFiles.enterScope("tolerate deletion")
			FileSystem.SYSTEM.delete(testFiles.createFile(delete = Always))
			FileSystem.SYSTEM.delete(testFiles.createDirectory(delete = Always))

			expect {
				testFiles.leaveScope(Success)
			}.notToThrow()
		}
	}
})
