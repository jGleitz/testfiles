package de.joshuagleitze.test.spek.testfiles

import de.joshuagleitze.test.spek.testfiles.DeletionMode.IF_SUCCESSFUL
import java.nio.file.Path

/**
 * Helper that creates test files and directories for use in tests. The helper manages a directory structures that reflects the structure of
 * the Spek tests. Created files and directories will reside in the part of the directory structure that corresponds to the current Spek
 * group or test. The helper also manages when to delete the created files.
 *
 * @see DeletionMode
 */
interface TestFiles {
	/**
	 * Creates a test directory in the directory of the current Spek group or test.
	 *
	 * @param name The name of the directory. If omitted or `null`, the name will be generated.
	 * @param delete When to delete the created directory, see [DeletionMode].
	 * @return The absolute [Path] to the created directory.
	 */
	fun createDirectory(name: String? = null, delete: DeletionMode = IF_SUCCESSFUL): Path

	/**
	 * Creates a test file in the directory of the current Spek group or test.
	 *
	 * @param name The name of the file. If omitted or `null`, the name will be generated.
	 * @param delete When to delete the created file, see [DeletionMode].
	 * @return The absolute [Path] to the created file.
	 */
	fun createFile(name: String? = null, delete: DeletionMode = IF_SUCCESSFUL): Path
}
