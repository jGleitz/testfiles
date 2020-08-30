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
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.HashSet
import java.util.Random
import java.util.Stack

class DefaultTestFiles internal constructor(): LifecycleListener, TestFiles {
	private val scopeFiles = Stack<ScopeFiles>().also { it.push(ROOT_SCOPE_FILES) }

	override fun createDirectory(name: String?, delete: DeletionMode): Path = with(scopeFiles.peek()) {
		createDirectory(prepareNewPath(name, delete))
	}

	override fun createFile(name: String?, delete: DeletionMode): Path = with(scopeFiles.peek()) {
		createFile(prepareNewPath(name, delete))
	}

	override fun beforeExecuteTest(test: TestScope) = enter(test)
	override fun beforeExecuteGroup(group: GroupScope) = enter(group)

	override fun afterExecuteTest(test: TestScope, result: ExecutionResult) = leave(result)
	override fun afterExecuteGroup(group: GroupScope, result: ExecutionResult) = leave(result)

	private fun enter(target: Scope) {
		val currentScopeFiles = scopeFiles.peek()
		val nextScopeDirectory = currentScopeFiles.targetDirectory.resolve("[${target.name}]")
		clear(nextScopeDirectory)
		scopeFiles.push(ScopeFiles(nextScopeDirectory))
	}

	private fun leave(result: ExecutionResult) {
		scopeFiles.pop().cleanup(wasSuccess = result is Success)
	}

	private val Scope.name: String
		get() = when (this) {
			is ScopeImpl -> this.id.name
			is GroupScope -> "unknown group"
			is TestScope -> "unknown test"
			else -> "unknown scope"
		}

	private class ScopeFiles(
		val targetDirectory: Path,
		private val toDeleteIfSuccess: MutableSet<Path> = HashSet(),
		private val toDelete: MutableSet<Path> = HashSet(),
		private var created: Boolean = false
	) {
		private val idGenerator = Random(targetDirectory.hashCode().toLong())

		fun prepareNewPath(name: String?, delete: DeletionMode): Path {
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

		fun cleanup(wasSuccess: Boolean) {
			if (created) {
				synchronized(this) {
					toDelete.forEach(::clear)
					if (wasSuccess) {
						toDeleteIfSuccess.forEach(::clear)
					}
					deleteIfEmpty(targetDirectory)
				}
			}
		}

		private fun generateTestFileName() = "test-" + idGenerator.nextInt(MAX_VALUE)
	}

	companion object {
		private val ROOT_SCOPE_FILES by lazy {
			ScopeFiles(testFilesRootDirectory)
		}

		/**
		 * Pattern of directories that are created to group test files by their Spek group.
		 */
		val SCOPE_DIRECTORY_PATTERN = Regex("^\\[.*]$")

		/**
		 * The root directory within which all test files will be created. Accessing this property may create the
		 * directory if it did not exist before.
		 */
		val testFilesRootDirectory: Path by lazy {
			when {
				isDirectory(Paths.get("build")) -> createDirectories(Paths.get("build/test-outputs"))
				isDirectory(Paths.get("target")) -> createDirectories(Paths.get("target/test-outputs"))
				isDirectory(Paths.get("test-outputs")) -> createDirectories(Paths.get("test-outputs"))
				else -> createDirectories(Paths.get(System.getProperty("java.io.tmpdir")).resolve("spek-test-outputs"))
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
