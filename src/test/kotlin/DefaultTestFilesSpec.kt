import ch.tutteli.atrium.api.fluent.en_GB.exists
import ch.tutteli.atrium.api.fluent.en_GB.existsNot
import ch.tutteli.atrium.api.fluent.en_GB.fileName
import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isReadable
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.isWritable
import ch.tutteli.atrium.api.fluent.en_GB.messageContains
import ch.tutteli.atrium.api.fluent.en_GB.notToBe
import ch.tutteli.atrium.api.fluent.en_GB.notToThrow
import ch.tutteli.atrium.api.fluent.en_GB.parent
import ch.tutteli.atrium.api.fluent.en_GB.startsWith
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import de.joshuagleitze.test.spek.testfiles.DefaultTestFiles
import de.joshuagleitze.test.spek.testfiles.DefaultTestFiles.Companion.determineTestFilesRootDirectory
import de.joshuagleitze.test.spek.testfiles.DeletionMode.ALWAYS
import de.joshuagleitze.test.spek.testfiles.DeletionMode.IF_SUCCESSFUL
import de.joshuagleitze.test.spek.testfiles.DeletionMode.NEVER
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.ExecutionResult.Failure
import org.spekframework.spek2.lifecycle.ExecutionResult.Success
import org.spekframework.spek2.runtime.scope.GroupScopeImpl
import org.spekframework.spek2.runtime.scope.TestScopeImpl
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Files.createDirectories
import java.nio.file.Files.createDirectory
import java.nio.file.Files.createFile
import java.nio.file.Files.delete

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
                createDirectories(buildDir)
                createDirectories(targetDir)
                createDirectories(testOutputsDir)
                expect(determineTestFilesRootDirectory()).toBe(buildDir.resolve("test-outputs"))
            }

            it("uses the target directory if present") {
                createDirectories(targetDir)
                createDirectories(testOutputsDir)
                expect(determineTestFilesRootDirectory()).toBe(targetDir.resolve("test-outputs"))
            }

            it("uses the test-outputs directory if present") {
                createDirectories(testOutputsDir)
                expect(determineTestFilesRootDirectory()).toBe(testOutputsDir)
            }

            it("falls back to the tmpdir") {
                expect(determineTestFilesRootDirectory()).toBe(tmpDir.resolve("spek-test-outputs"))
            }
        }

        describe("housekeeping") {
            it("clears pre-existing files when entering a group") {
                val mockGroup = mockScope<GroupScopeImpl>("delete pre-existing group")

                val scopeDir = createDirectories(fileRoot.resolve("[delete pre-existing group]"))
                val testDir = createDirectory(scopeDir.resolve("pre-existing dir"))
                val subTestFile = createFile(testDir.resolve("sub"))
                val testFile = createFile(scopeDir.resolve("pre-existing file"))

                testFiles.beforeExecuteGroup(mockGroup)

                expect(testDir).existsNot()
                expect(subTestFile).existsNot()
                expect(testFile).existsNot()

                testFiles.afterExecuteGroup(mockGroup, Success)
            }

            it("clears pre-existing files when entering a test") {
                val mockTest = mockScope<TestScopeImpl>("delete pre-existing test")

                val scopeDir = createDirectories(fileRoot.resolve("[delete pre-existing test]"))
                val testDir = createDirectory(scopeDir.resolve("pre-existing dir"))
                val subTestFile = createFile(testDir.resolve("deeper pre-existing file"))
                val testFile = createFile(scopeDir.resolve("pre-existing file"))

                testFiles.beforeExecuteTest(mockTest)

                expect(testDir).existsNot()
                expect(subTestFile).existsNot()
                expect(testFile).existsNot()

                testFiles.afterExecuteTest(mockTest, Success)
            }

            it("retains existing group directories when entering a group") {
                val mockGroup = mockScope<GroupScopeImpl>("retain pre-existing group")

                val scopeDir = createDirectories(fileRoot.resolve("[retain pre-existing group]"))
                val groupTestDir = createDirectory(scopeDir.resolve("[test]"))
                val subTestFile = createFile(groupTestDir.resolve("deeper pre-existing file"))
                val testFile = createFile(scopeDir.resolve("pre-existing file"))

                testFiles.beforeExecuteGroup(mockGroup)

                expect(scopeDir).isDirectory()
                expect(groupTestDir).isDirectory()
                expect(subTestFile).isRegularFile()
                expect(testFile).existsNot()

                testFiles.afterExecuteGroup(mockGroup, Success)
            }

            it("retains existing group directories when entering a test") {
                val mockTest = mockScope<TestScopeImpl>("retain pre-existing test")

                val scopeDir = createDirectories(fileRoot.resolve("[retain pre-existing test]"))
                val groupTestDir = createDirectory(scopeDir.resolve("[test]"))
                val subTestFile = createFile(groupTestDir.resolve("deeper pre-existing file"))
                val testFile = createFile(scopeDir.resolve("pre-existing file"))

                testFiles.beforeExecuteTest(mockTest)

                expect(scopeDir).isDirectory()
                expect(groupTestDir).isDirectory()
                expect(subTestFile).isRegularFile()
                expect(testFile).existsNot()

                testFiles.afterExecuteTest(mockTest, Success)
            }

            it("does not create a group or test folder if not necessary") {
                val mockGroup = mockScope<GroupScopeImpl>("no premature creation group")
                val mockGroupTarget = fileRoot.resolve("[no premature creation group]")
                val mockSubGroup = mockScope<GroupScopeImpl>("sub")
                val mockSubGroupTarget = mockGroupTarget.resolve("[sub]")
                val mockTest = mockScope<TestScopeImpl>("test")
                val mockTestTarget = mockSubGroupTarget.resolve("[test]")

                testFiles.beforeExecuteGroup(mockGroup)
                expect(mockGroupTarget).existsNot()

                testFiles.beforeExecuteGroup(mockSubGroup)
                expect(mockGroupTarget).existsNot()
                expect(mockSubGroupTarget).existsNot()

                testFiles.beforeExecuteTest(mockTest)
                expect(mockGroupTarget).existsNot()
                expect(mockSubGroupTarget).existsNot()
                expect(mockTestTarget).existsNot()

                testFiles.createFile()
                expect(mockTestTarget).exists()
            }
        }

        describe("file creation") {
            it("creates an empty file with the provided name") {
                val mockGroup = mockScope<GroupScopeImpl>("named file creation group")
                val mockGroupTarget = fileRoot.resolve("[named file creation group]")
                val mockTest = mockScope<TestScopeImpl>("test")
                val mockTestTarget = mockGroupTarget.resolve("[test]")

                testFiles.beforeExecuteGroup(mockGroup)
                expect(testFiles.createFile("testfile")) {
                    isRegularFile()
                    isReadable()
                    isWritable()
                    content.toBe("")
                    fileName.toBe("testfile")
                    parent.toBe(mockGroupTarget)
                }

                testFiles.beforeExecuteTest(mockTest)
                expect(testFiles.createFile("testfile")) {
                    isRegularFile()
                    isReadable()
                    isWritable()
                    content.toBe("")
                    fileName.toBe("testfile")
                    parent.toBe(mockTestTarget)
                }
            }

            it("creates an empty directory with the provided name") {
                val mockGroup = mockScope<GroupScopeImpl>("named directory creation group")
                val mockGroupTarget = fileRoot.resolve("[named directory creation group]")
                val mockTest = mockScope<TestScopeImpl>("test")
                val mockTestTarget = mockGroupTarget.resolve("[test]")

                testFiles.beforeExecuteGroup(mockGroup)
                expect(testFiles.createDirectory("testdir")) {
                    isDirectory()
                    isReadable()
                    isWritable()
                    fileName.toBe("testdir")
                    parent.toBe(mockGroupTarget)
                }

                testFiles.beforeExecuteTest(mockTest)
                expect(testFiles.createDirectory("testdir")) {
                    isDirectory()
                    isReadable()
                    isWritable()
                    fileName.toBe("testdir")
                    parent.toBe(mockTestTarget)
                }
            }

            it("rejects file names that match the group directory pattern") {
                testFiles.beforeExecuteTest(mockScope<TestScopeImpl>("rejects bad file names"))

                expect { testFiles.createFile("[test") }.notToThrow()
                expect { testFiles.createFile("test]") }.notToThrow()
                expect { testFiles.createFile("[test]") }.toThrow<IllegalArgumentException> {
                    messageContains("[test]")
                }
            }

            it("rejects directory names that match the group directory pattern") {
                testFiles.beforeExecuteTest(mockScope<TestScopeImpl>("rejects bad directory names"))

                expect { testFiles.createDirectory("[test") }.notToThrow()
                expect { testFiles.createDirectory("test]") }.notToThrow()
                expect { testFiles.createDirectory("[test]") }.toThrow<IllegalArgumentException> {
                    messageContains("[test]")
                }
            }

            it("creates an empty file with a generated name") {
                val mockGroup = mockScope<GroupScopeImpl>("unnamed file creation group")
                val mockGroupTarget = fileRoot.resolve("[unnamed file creation group]")
                val mockTest = mockScope<TestScopeImpl>("test")
                val mockTestTarget = mockGroupTarget.resolve("[test]")

                testFiles.beforeExecuteGroup(mockGroup)
                expect(testFiles.createFile()) {
                    isRegularFile()
                    isReadable()
                    isWritable()
                    content.toBe("")
                    fileName.startsWith("test-")
                    parent.toBe(mockGroupTarget)
                }

                testFiles.beforeExecuteTest(mockTest)
                expect(testFiles.createFile()) {
                    isRegularFile()
                    isReadable()
                    isWritable()
                    content.toBe("")
                    fileName.startsWith("test-")
                    parent.toBe(mockTestTarget)
                }
            }

            it("creates an empty directory with a generated name") {
                val mockGroup = mockScope<GroupScopeImpl>("unnamed directory creation group")
                val mockGroupTarget = fileRoot.resolve("[unnamed directory creation group]")
                val mockTest = mockScope<TestScopeImpl>("test")
                val mockTestTarget = mockGroupTarget.resolve("[test]")

                testFiles.beforeExecuteGroup(mockGroup)
                expect(testFiles.createDirectory()) {
                    isDirectory()
                    isReadable()
                    isWritable()
                    fileName.startsWith("test-")
                    parent.toBe(mockGroupTarget)
                }

                testFiles.beforeExecuteTest(mockTest)
                expect(testFiles.createDirectory()) {
                    isDirectory()
                    isReadable()
                    isWritable()
                    fileName.startsWith("test-")
                    parent.toBe(mockTestTarget)
                }
            }

            it("generates different file names on subsequent creations") {
                testFiles.beforeExecuteTest(mockScope<TestScopeImpl>("different file names"))

                expect(testFiles.createFile()).notToBe(testFiles.createFile())
                expect(testFiles.createDirectory()).notToBe(testFiles.createDirectory())
            }

            it("generates the same file names for the same creations") {
                val mockTest = mockScope<TestScopeImpl>("consistent file names")

                testFiles.beforeExecuteTest(mockTest)
                val firstFile = testFiles.createFile()
                testFiles.afterExecuteTest(mockTest, Success)

                testFiles.beforeExecuteTest(mockTest)
                val secondFile = testFiles.createFile()
                testFiles.afterExecuteTest(mockTest, Success)

                expect(firstFile).toBe(secondFile)
            }
        }

        describe("file cleanup") {
            it("deletes a file that has been marked to be deleted ALWAYS") {
                val mockTest = mockScope<TestScopeImpl>("delete ALWAYS")

                testFiles.beforeExecuteTest(mockTest)
                val successTestfile = testFiles.createFile(delete = ALWAYS)
                val successTestdir = testFiles.createDirectory(delete = ALWAYS)
                expect(successTestfile).exists()
                expect(successTestdir).exists()
                testFiles.afterExecuteTest(mockTest, Success)
                expect(successTestfile).existsNot()
                expect(successTestdir).existsNot()

                testFiles.beforeExecuteTest(mockTest)
                val failureTestfile = testFiles.createFile(delete = ALWAYS)
                val failureTestdir = testFiles.createDirectory(delete = ALWAYS)
                expect(failureTestfile).exists()
                expect(failureTestdir).exists()
                testFiles.afterExecuteTest(mockTest, Failure(IllegalStateException()))
                expect(failureTestfile).existsNot()
                expect(failureTestdir).existsNot()
            }

            it("deletes a file that has been marked to be deleted after success if appropriate") {
                val mockTest = mockScope<TestScopeImpl>("delete IF_SUCCESSFUL")

                testFiles.beforeExecuteTest(mockTest)
                val successTestfile = testFiles.createFile(delete = IF_SUCCESSFUL)
                val successTestdir = testFiles.createDirectory(delete = IF_SUCCESSFUL)
                expect(successTestfile).exists()
                expect(successTestdir).exists()
                testFiles.afterExecuteTest(mockTest, Success)
                expect(successTestfile).existsNot()
                expect(successTestdir).existsNot()


                testFiles.beforeExecuteTest(mockTest)
                val failureTestfile = testFiles.createFile(delete = IF_SUCCESSFUL)
                val failureTestdir = testFiles.createDirectory(delete = IF_SUCCESSFUL)
                expect(failureTestfile).exists()
                expect(failureTestdir).exists()
                testFiles.afterExecuteTest(mockTest, Failure(IllegalStateException()))
                expect(failureTestfile).exists()
                expect(failureTestdir).exists()
            }

            it("does not delete a file that has been marked to be deleted NEVER") {
                val mockTest = mockScope<TestScopeImpl>("delete NEVER")

                testFiles.beforeExecuteTest(mockTest)
                val successTestfile = testFiles.createFile(delete = NEVER)
                val successTestdir = testFiles.createDirectory(delete = NEVER)
                expect(successTestfile).exists()
                expect(successTestdir).exists()
                testFiles.afterExecuteTest(mockTest, Success)
                expect(successTestfile).exists()
                expect(successTestdir).exists()

                testFiles.beforeExecuteTest(mockTest)
                val failureTestfile = testFiles.createFile(delete = NEVER)
                val failureTestdir = testFiles.createDirectory(delete = NEVER)
                expect(failureTestfile).exists()
                expect(failureTestdir).exists()
                testFiles.afterExecuteTest(mockTest, Failure(IllegalStateException()))
                expect(failureTestfile).exists()
                expect(failureTestdir).exists()
            }

            it("tolerates deletion of created files") {
                val mockTest = mockScope<TestScopeImpl>("deletion toleration")

                testFiles.beforeExecuteTest(mockTest)
                delete(testFiles.createFile(delete = ALWAYS))
                delete(testFiles.createDirectory(delete = ALWAYS))

                expect {
                    testFiles.afterExecuteTest(mockTest, Success)
                }.notToThrow()
            }
        }
    }
})
