package de.joshuagleitze.test.spek.testfiles

import de.joshuagleitze.test.spek.testfiles.DefaultTestFiles.Companion.SCOPE_DIRECTORY_PATTERN
import de.joshuagleitze.test.spek.testfiles.DeletionMode.ALWAYS
import de.joshuagleitze.test.spek.testfiles.DeletionMode.IF_SUCCESSFUL
import de.joshuagleitze.test.spek.testfiles.DeletionMode.NEVER
import org.spekframework.spek2.dsl.Root
import org.spekframework.spek2.lifecycle.ExecutionResult
import org.spekframework.spek2.lifecycle.ExecutionResult.Success
import org.spekframework.spek2.lifecycle.GroupScope
import org.spekframework.spek2.lifecycle.LifecycleListener
import org.spekframework.spek2.lifecycle.Scope
import org.spekframework.spek2.lifecycle.TestScope
import org.spekframework.spek2.runtime.scope.ScopeImpl
import java.io.IOException
import java.lang.Integer.MAX_VALUE
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.SKIP_SUBTREE
import java.nio.file.Files.createDirectories
import java.nio.file.Files.createDirectory
import java.nio.file.Files.createFile
import java.nio.file.Files.delete
import java.nio.file.Files.exists
import java.nio.file.Files.isDirectory
import java.nio.file.Files.list
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

class DefaultTestFiles internal constructor(): LifecycleListener, TestFiles {
	private val scopeContext = Stack<TestFilesScopeContext>().also { it.push(ROOT_SCOPE) }

	override fun createDirectory(name: String?, delete: DeletionMode): Path = with(scopeContext.peek()) {
		createDirectory(prepareTarget(name, delete))
	}

	override fun createFile(name: String?, delete: DeletionMode): Path = with(scopeContext.peek()) {
		createFile(prepareTarget(name, delete))
	}

	override fun beforeExecuteTest(test: TestScope) = enter(test)
	override fun beforeExecuteGroup(group: GroupScope) = enter(group)

	override fun afterExecuteTest(test: TestScope, result: ExecutionResult) = leave(result)
	override fun afterExecuteGroup(group: GroupScope, result: ExecutionResult) = leave(result)

	private fun enter(target: Scope) {
		val currentContext = scopeContext.peek()
		val nextScopeDirectoryName = "[${target.name}]"
		val nextScopeDirectory = currentContext.targetDirectory.resolve(nextScopeDirectoryName)
		clear(nextScopeDirectory)
		scopeContext.push(TestFilesScopeContext(nextScopeDirectory))
	}

	private fun leave(result: ExecutionResult) {
		val oldContext = scopeContext.pop()

		if (oldContext.created) {
			synchronized(oldContext) {
				oldContext.toDelete.forEach(::clear)
				if (result == Success) {
					oldContext.toDeleteIfSuccess.forEach(::clear)
				}
				deleteIfEmpty(oldContext.targetDirectory)
			}
		}
	}

	private val Scope.name: String
		get() = when (this) {
            is ScopeImpl -> this.id.name
            is GroupScope -> "unknown group"
            is TestScope -> "unknown test"
			else -> "unknown scope"
		}

	private class TestFilesScopeContext(
        val targetDirectory: Path,
        val toDeleteIfSuccess: MutableSet<Path> = HashSet(),
        val toDelete: MutableSet<Path> = HashSet(),
        var created: Boolean = false
    ) {
		private val idGenerator = Random(targetDirectory.hashCode().toLong())

		fun prepareTarget(name: String?, delete: DeletionMode): Path {
			val targetName = name?.apply(::checkFileName) ?: generateTestFileName()
			val target = this.ensureExistingTargetDirectory().resolve(targetName)
			when (delete) {
                ALWAYS -> toDelete.add(target)
                IF_SUCCESSFUL -> toDeleteIfSuccess.add(target)
                NEVER -> Unit
			}
			return target
		}

		// double checked locking does not suffer the “not fully initialized object” problem here.
		private fun ensureExistingTargetDirectory(): Path {
			if (!this.created) {
				synchronized(this) {
					if (!this.created) {
						createDirectories(targetDirectory)
						this.created = true
					}
				}
			}
			return targetDirectory
		}

		private fun generateTestFileName() = "test-" + idGenerator.nextInt(MAX_VALUE)
	}

	companion object {
		private val ROOT_SCOPE by lazy {
			TestFilesScopeContext(testFilesDirectory)
		}

		/**
		 * Pattern of directories that are created to group test files by their Spek group.
		 */
		val SCOPE_DIRECTORY_PATTERN = Regex("^\\[.*]$")

		/**
		 * The root directory within which all test files will be created. Accessing this property may create the
		 * directory if it did not exist before.
		 */
		val testFilesDirectory: Path by lazy {
			when {
				isDirectory(Path.of("build")) -> createDirectories(Path.of("build/test-outputs"))
				isDirectory(Path.of("target")) -> createDirectories(Path.of("target/test-outputs"))
				isDirectory(Path.of("test-outputs")) -> createDirectories(Path.of("test-outputs"))
				else -> createDirectories(Path.of(System.getProperty("java.io.tmpdir")).resolve("spek-test-outputs"))
			}
		}

		private fun checkFileName(name: String) {
			require(!name.matches(Regex("^\\[.*]$"))) { "A test file name must not start with '[' and end with ']'! was: '$name'" }
		}
	}
}

/**
 * Creates a new [TestFiles] instance.
 */
fun Root.testFiles(): TestFiles = DefaultTestFiles().also { this.registerListener(it) }

private fun clear(path: Path) {
	when {
		isDirectory(path) -> deleteNonGroupFilesRecursively(path)
		exists(path) -> delete(path)
	}
}

private fun deleteNonGroupFilesRecursively(rootDirectory: Path) {
	walkFileTree(rootDirectory, object: SimpleFileVisitor<Path>() {
        override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
            checkNotNull(dir) { "dir was null" }
            return if (dir != rootDirectory && SCOPE_DIRECTORY_PATTERN.matches(dir.fileName.toString())) SKIP_SUBTREE
            else CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?) =
            super.postVisitDirectory(dir, exc).also { if (dir != rootDirectory) deleteIfEmpty(dir) }

        override fun visitFile(file: Path, attrs: BasicFileAttributes?) =
            super.visitFile(file, attrs).also { delete(file) }
    })
}

private fun deleteIfEmpty(path: Path) {
	if (!list(path).findAny().isPresent) {
		delete(path)
	}
}
