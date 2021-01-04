package de.joshuagleitze.testfiles

import de.joshuagleitze.testfiles.DeletionMode.Always
import de.joshuagleitze.testfiles.DeletionMode.IfSuccessful
import de.joshuagleitze.testfiles.DeletionMode.Never
import java.io.IOException
import java.lang.Integer.MAX_VALUE
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.SKIP_SUBTREE
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumMap
import java.util.HashSet
import java.util.Random
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.isDirectory

public class DefaultTestFiles: TestFiles {
	private var currentScope = ROOT_SCOPE

	override fun createDirectory(name: String?, delete: DeletionMode): Path = currentScope.prepareNewPath(name, delete).createDirectory()

	override fun createFile(name: String?, delete: DeletionMode): Path = currentScope.prepareNewPath(name, delete).createFile()

	/**
	 * Reports that we have entered a new scope.
	 */
	public fun enterScope(name: String) {
		val nextScopeDirectory = currentScope.targetDirectory / "[${escapeScopeName(name)}]"
		nextScopeDirectory.clear()
		currentScope = ScopeFiles(currentScope, nextScopeDirectory)
	}

	/**
	 * Reports that we have left the scope that was entered most recently without being left yet. Also reports that the scope had the
	 * provided [result]. A [ScopeResult.Failure] will be applied to all currently entered scopes. That means that if any other scope that
	 * was entered during the current scope reported a [ScopeResult.Failure], this scope will be considered to have failed even if [result]
	 * is not [ScopeResult.Failure]
	 */
	public fun leaveScope(result: ScopeResult) {
		currentScope.report(result)
		currentScope.cleanup()
		currentScope = currentScope.parent
	}

	private fun escapeScopeName(name: String) = name.replace(invalidFileNameCharacters, "-")

	private class ScopeFiles(parent: ScopeFiles?, val targetDirectory: Path) {
		val parent = parent ?: this
		private var result: ScopeResult? = null
		private val toDelete = EnumMap<DeletionMode, MutableSet<Path>>(DeletionMode::class.java)
		private var created: Boolean = false
		private val idGenerator = Random(targetDirectory.hashCode().toLong())

		fun prepareNewPath(name: String?, delete: DeletionMode): Path {
			val targetName = name?.apply(Companion::checkFileName) ?: generateTestFileName()
			val target = this.ensureExistingTargetDirectory().resolve(targetName)
			toDelete.computeIfAbsent(delete) { HashSet() }.add(target)
			return target
		}

		// double checked locking does not suffer the “not fully initialized object” problem here.
		private fun ensureExistingTargetDirectory(): Path {
			if (!created) {
				synchronized(this) {
					if (!created) {
						targetDirectory.createDirectories()
						created = true
					}
				}
			}
			return targetDirectory
		}

		private fun requireResult() = result ?: error("No result has been reported for the scope $targetDirectory!")

		fun cleanup() {
			synchronized(this) {
				if (created) {
					val result = requireResult()
					toDelete.forEach { (deletionMode, files) ->
						if (result.shouldBeDeleted(deletionMode)) files.forEach { it.clear() }
					}
					targetDirectory.deleteIfExistsAndEmpty()
				}
			}
		}

		fun report(result: ScopeResult) {
			this.result = this.result?.combineWith(result) ?: result
			if (parent !== this) parent.report(result)
		}

		private fun generateTestFileName() = "test-" + idGenerator.nextInt(MAX_VALUE)
	}

	public companion object {
		private val ROOT_SCOPE by lazy { ScopeFiles(null, determineTestFilesRootDirectory()) }

		/**
		 * Pattern of directories that are created to group test files by their Spek group.
		 */
		public val SCOPE_DIRECTORY_PATTERN: Regex = Regex("^\\[.*]$")

		/**
		 * Determines the root directory within which all test files will be created.
		 */
		public fun determineTestFilesRootDirectory(): Path = when {
			Path("build").isDirectory() -> Path("build/test-outputs")
			Path("target").isDirectory() -> Path("target/test-outputs")
			Path("test-outputs").isDirectory() -> Path("test-outputs")
			else -> Path(System.getProperty("java.io.tmpdir")) / "test-outputs"
		}.toAbsolutePath()

		private fun checkFileName(name: String) {
			require(!name.matches(Regex("^\\[.*]$"))) { "A test file name must not start with '[' and end with ']'! was: '$name'" }
		}

		private fun Path.clear() = tolerateDoesNotExist {
			walkFileTree(this, object: SimpleFileVisitor<Path>() {
				override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
					checkNotNull(dir) { "dir was null" }
					return if (dir != this@clear && SCOPE_DIRECTORY_PATTERN.matches(dir.fileName.toString())) SKIP_SUBTREE
					else CONTINUE
				}

				override fun postVisitDirectory(dir: Path, exc: IOException?) =
					super.postVisitDirectory(dir, exc).also { dir.deleteIfExistsAndEmpty() }

				override fun visitFile(file: Path, attrs: BasicFileAttributes?) =
					super.visitFile(file, attrs).also { file.deleteIfExists() }
			})
		}

		private fun Path.deleteIfExistsAndEmpty() = try {
			deleteIfExists()
		} catch (notEmpty: DirectoryNotEmptyException) {
			// swallow
		}

		private inline fun Path.tolerateDoesNotExist(block: Path.() -> Unit) {
			try {
				block()
			} catch (noSuchFile: java.nio.file.NoSuchFileException) {
				// swallow
			}
		}

		private val unixInvalidCharacters get() = Regex("[/\u0000]")
		private val windowsInvalidCharacters get() = Regex("[/\\\\<>:\"|?*\u0000]")
		private val invalidFileNameCharacters by lazy(PUBLICATION) {
			val osName = System.getProperty("os.name").toLowerCase()
			if (setOf("nix", "nux", "aix", "mac").any { osName.contains(it) }) unixInvalidCharacters
			else windowsInvalidCharacters // default to windows because it is the most restrictive
		}
	}

	/**
	 * The outcomes of a test scope that are relevant to us.
	 *
	 * This does not include skipped scopes, as they should not be reported to [DefaultTestFiles] in the first place.
	 */
	public enum class ScopeResult {
		/**
		 * All tests in this scope were successful.
		 */
		Success {
			public override fun combineWith(otherResult: ScopeResult): ScopeResult = when (otherResult) {
				Success -> Success
				Failure -> Failure
			}

			public override fun shouldBeDeleted(deletionMode: DeletionMode): Boolean = when (deletionMode) {
				Always,
				IfSuccessful -> true
				Never -> false
			}
		},

		/**
		 * At least one test in the current scope failed in any way.
		 */
		Failure {
			public override fun combineWith(otherResult: ScopeResult): ScopeResult = Failure
			public override fun shouldBeDeleted(deletionMode: DeletionMode): Boolean = when (deletionMode) {
				Always -> true
				IfSuccessful, Never -> false
			}
		};

		/**
		 * Combines this result with [otherResult], such that if a scope had previously `this` result and [otherResult] occurred in the
		 * scope, the returned value is the new overall result of the scope.
		 */
		public abstract fun combineWith(otherResult: ScopeResult): ScopeResult

		/**
		 * Determines whether, if some file was created with the provided [deletionMode] for a scope that had `this` result, the file should
		 * now be deleted.
		 */
		public abstract fun shouldBeDeleted(deletionMode: DeletionMode): Boolean
	}
}
