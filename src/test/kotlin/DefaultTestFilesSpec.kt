import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.niok.deleteRecursively
import de.joshuagleitze.test.spek.testfiles.DefaultTestFiles
import de.joshuagleitze.test.spek.testfiles.DeletionMode.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.ExecutionResult.Failure
import org.spekframework.spek2.lifecycle.ExecutionResult.Success
import org.spekframework.spek2.runtime.scope.GroupScopeImpl
import org.spekframework.spek2.runtime.scope.TestScopeImpl
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Files.createDirectories
import java.nio.file.Files.createDirectory
import java.nio.file.Files.createFile
import java.nio.file.Paths

object DefaultTestFilesSpec : Spek({
    val expectedRootFolder = Paths.get("build/test-outputs")
    lateinit var testFiles: DefaultTestFiles

    beforeGroup {
        expectedRootFolder.deleteRecursively()
    }

    beforeEachTest {
        testFiles = DefaultTestFiles()
    }

    describe("DefaultTestFiles") {
        describe("Housekeeping") {
            it("clears pre-existing files when entering a group") {
                val mockGroup = mockScope<GroupScopeImpl>("delete pre-existing group")

                val scopeDir = createDirectories(expectedRootFolder.resolve("[delete pre-existing group]"))
                val testDir = createDirectory(scopeDir.resolve("test"))
                val subTestFile = createFile(testDir.resolve("sub"))
                val testFile = createFile(scopeDir.resolve("test file"))

                testFiles.beforeExecuteGroup(mockGroup)

                expect(scopeDir).isDirectory()
                expect(testDir).existsNot()
                expect(subTestFile).existsNot()
                expect(testFile).existsNot()

                testFiles.afterExecuteGroup(mockGroup, Success)
            }

            it("clears pre-existing files when entering a test") {
                val mockTest = mockScope<TestScopeImpl>("delete pre-existing test")

                val scopeDir = createDirectories(expectedRootFolder.resolve("[delete pre-existing test]"))
                val testDir = createDirectory(scopeDir.resolve("test"))
                val subTestFile = createFile(testDir.resolve("sub"))
                val testFile = createFile(scopeDir.resolve("test file"))

                testFiles.beforeExecuteTest(mockTest)

                expect(scopeDir).isDirectory()
                expect(testDir).existsNot()
                expect(subTestFile).existsNot()
                expect(testFile).existsNot()

                testFiles.afterExecuteTest(mockTest, Success)
            }

            it("retains existing group directories when entering a group") {
                val mockGroup = mockScope<GroupScopeImpl>("retain pre-existing group")

                val scopeDir = createDirectories(expectedRootFolder.resolve("[retain pre-existing group]"))
                val groupTestDir = createDirectory(scopeDir.resolve("[test]"))
                val subTestFile = createFile(groupTestDir.resolve("sub"))
                val testFile = createFile(scopeDir.resolve("test file"))

                testFiles.beforeExecuteGroup(mockGroup)

                expect(scopeDir).isDirectory()
                expect(groupTestDir).isDirectory()
                expect(subTestFile).isRegularFile()
                expect(testFile).existsNot()

                testFiles.afterExecuteGroup(mockGroup, Success)
            }

            it("retains existing group directories when entering a test") {
                val mockTest = mockScope<TestScopeImpl>("retain pre-existing test")

                val scopeDir = createDirectories(expectedRootFolder.resolve("[retain pre-existing test]"))
                val groupTestDir = createDirectory(scopeDir.resolve("[test]"))
                val subTestFile = createFile(groupTestDir.resolve("sub"))
                val testFile = createFile(scopeDir.resolve("test file"))

                testFiles.beforeExecuteTest(mockTest)

                expect(scopeDir).isDirectory()
                expect(groupTestDir).isDirectory()
                expect(subTestFile).isRegularFile()
                expect(testFile).existsNot()

                testFiles.afterExecuteTest(mockTest, Success)
            }

            it("does not create a group or test folder if not necessary") {
                val mockGroup = mockScope<GroupScopeImpl>("no premature creation group")
                val mockGroupTarget = expectedRootFolder.resolve("[no premature creation group]")
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
                val mockGroupTarget = expectedRootFolder.resolve("[named file creation group]")
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
                val mockGroupTarget = expectedRootFolder.resolve("[named directory creation group]")
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

            it("creates an empty file with a generated name") {
                val mockGroup = mockScope<GroupScopeImpl>("unnamed file creation group")
                val mockGroupTarget = expectedRootFolder.resolve("[unnamed file creation group]")
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
                val mockGroupTarget = expectedRootFolder.resolve("[unnamed directory creation group]")
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
        }

        describe("file cleanup") {
            it("deletes a file that has been marked to be deleted ALWAYS") {
                val mockTest = mockScope<TestScopeImpl>("test")

                testFiles.beforeExecuteTest(mockTest)
                val successTestfile = testFiles.createFile(delete = ALWAYS)
                expect(successTestfile).exists()
                testFiles.afterExecuteTest(mockTest, Success)
                expect(successTestfile).existsNot()

                testFiles.beforeExecuteTest(mockTest)
                val failureTestfile = testFiles.createFile(delete = ALWAYS)
                expect(failureTestfile).exists()
                testFiles.afterExecuteTest(mockTest, Failure(IllegalStateException()))
                expect(failureTestfile).existsNot()
            }

            it("deletes a file that has been marked to be deleted after success if appropriate") {
                val mockTest = mockScope<TestScopeImpl>("test")

                testFiles.beforeExecuteTest(mockTest)
                val successTestfile = testFiles.createFile(delete = IF_SUCCESSFUL)
                expect(successTestfile).exists()
                testFiles.afterExecuteTest(mockTest, Success)
                expect(successTestfile).existsNot()

                testFiles.beforeExecuteTest(mockTest)
                val failureTestfile = testFiles.createFile(delete = IF_SUCCESSFUL)
                expect(failureTestfile).exists()
                testFiles.afterExecuteTest(mockTest, Failure(IllegalStateException()))
                expect(failureTestfile).exists()
            }

            it("does not delete a file that has been marked to be deleted NEVER") {
                val mockTest = mockScope<TestScopeImpl>("test")

                testFiles.beforeExecuteTest(mockTest)
                val successTestfile = testFiles.createFile(delete = NEVER)
                expect(successTestfile).exists()
                testFiles.afterExecuteTest(mockTest, Success)
                expect(successTestfile).exists()

                testFiles.beforeExecuteTest(mockTest)
                val failureTestfile = testFiles.createFile(delete = NEVER)
                expect(failureTestfile).exists()
                testFiles.afterExecuteTest(mockTest, Failure(IllegalStateException()))
                expect(failureTestfile).exists()
            }
        }
    }
})
