package de.joshuagleitze.testfiles

import de.joshuagleitze.testfiles.DeletionMode.Always
import de.joshuagleitze.testfiles.DeletionMode.IfSuccessful
import de.joshuagleitze.testfiles.DeletionMode.Never
import de.joshuagleitze.testfiles.internal.absolutize
import de.joshuagleitze.testfiles.internal.invalidFileNameCharacters
import de.joshuagleitze.testfiles.internal.platformFileSystem
import de.joshuagleitze.testfiles.internal.platformTemporaryDirectory
import de.joshuagleitze.testfiles.internal.synchronizedAccess
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import kotlin.random.Random

public class DefaultTestFiles : TestFiles {
	private var currentScope = ROOT_SCOPE

	override fun createDirectory(name: String?, delete: DeletionMode): Path =
		currentScope.prepareNewPath(name, delete).also { platformFileSystem.createDirectories(it) }

	override fun createFile(name: String?, delete: DeletionMode): Path =
		currentScope.prepareNewPath(name, delete).also { platformFileSystem.write(it) {} }

	/**
	 * Reports that we have entered a new scope.
	 */
	public fun enterScope(name: String) {
		val nextScopeDirectory = currentScope.targetDirectory / "[${escapeScopeName(name)}]"
		nextScopeDirectory.clearScopeDirectory(isRoot = true)
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
		private val toDelete = HashMap<DeletionMode, MutableSet<Path>>()
		private var created: Boolean = false
		private val idGenerator = Random(targetDirectory.hashCode())

		fun prepareNewPath(name: String?, delete: DeletionMode): Path {
			val targetName = name?.apply(Companion::checkFileName) ?: generateTestFileName()
			val target = ensureExistingTargetDirectory() / targetName
			toDelete.getOrPut(delete) { mutableSetOf() }.add(target)
			return target
		}

		// double checked locking does not suffer the "not fully initialized object" problem here.
		private fun ensureExistingTargetDirectory(): Path {
			if (!created) {
				synchronizedAccess(this) {
					if (!created) {
						platformFileSystem.createDirectories(targetDirectory)
						created = true
					}
				}
			}
			return targetDirectory
		}

		private fun requireResult() = result ?: error("No result has been reported for the scope $targetDirectory!")

		fun cleanup() {
			synchronizedAccess(this) {
				if (created) {
					val result = requireResult()
					toDelete.forEach { (deletionMode, files) ->
						if (result.shouldBeDeleted(deletionMode)) files.forEach { it.clearScopeDirectory(isRoot = true) }
					}
					try {
						platformFileSystem.delete(targetDirectory)
					} catch (_: IOException) {
						// directory not empty, leave it
					}
				}
			}
		}

		fun report(result: ScopeResult) {
			this.result = this.result?.combineWith(result) ?: result
			if (parent !== this) parent.report(result)
		}

		private fun generateTestFileName() = "test-" + idGenerator.nextInt(Int.MAX_VALUE)
	}

	public companion object {
		private val ROOT_SCOPE by lazy { ScopeFiles(null, determineTestFilesRootDirectory()) }

		/**
		 * Pattern of directories that are created to group test files by their scope.
		 */
		public val SCOPE_DIRECTORY_PATTERN: Regex = Regex("^\\[.*]$")

		/**
		 * Determines the root directory within which all test files will be created.
		 */
		public fun determineTestFilesRootDirectory(): Path = absolutize(when {
			platformFileSystem.metadataOrNull("build".toPath())?.isDirectory == true -> "build/test-outputs".toPath()
			platformFileSystem.metadataOrNull("target".toPath())?.isDirectory == true -> "target/test-outputs".toPath()
			platformFileSystem.metadataOrNull("test-outputs".toPath())?.isDirectory == true -> "test-outputs".toPath()
			else -> platformTemporaryDirectory / "test-outputs"
		})

		private fun checkFileName(name: String) {
			require(!name.matches(SCOPE_DIRECTORY_PATTERN)) {
				"A test file name must not start with '[' and end with ']'! was: '$name'"
			}
		}

		private fun Path.clearScopeDirectory(isRoot: Boolean) {
			val fs = platformFileSystem
			if (!fs.exists(this)) return
			val metadata = fs.metadataOrNull(this) ?: return
			if (metadata.isRegularFile) {
				try {
					fs.delete(this)
				} catch (_: IOException) {
					// swallow
				}
				return
			}
			if (metadata.isDirectory) {
				try {
					fs.list(this).forEach { child ->
						val childIsDir = fs.metadataOrNull(child)?.isDirectory == true
						val isNestedScope = !isRoot && childIsDir && SCOPE_DIRECTORY_PATTERN.matches(child.name)
						if (!isNestedScope) {
							child.clearScopeDirectory(isRoot = false)
						}
					}
				} catch (_: IOException) {
					// swallow listing errors
				}
				try {
					fs.delete(this)
				} catch (_: IOException) {
					// directory not empty, leave it
				}
			}
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
