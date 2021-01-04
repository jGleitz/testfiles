package de.joshuagleitze.testfiles

import de.joshuagleitze.testfiles.DeletionMode.IfSuccessful
import java.nio.file.Path

/**
 * Helper that creates test files and directories for use in tests. The helper manages a directory structure that reflects the structure of
 * the tests. Created files and directories will reside in the part of the directory structure that corresponds to the current test scope.
 * The helper also manages when to delete the created files.
 *
 * @see DeletionMode
 */
public interface TestFiles {
	/**
	 * Creates a test directory in the directory of the current test scope.
	 *
	 * @param name The name of the directory. If omitted or `null`, the name will be generated.
	 * @param delete When to delete the created directory, see [DeletionMode].
	 * @return The absolute [Path] to the created directory.
	 */
	public fun createDirectory(name: String? = null, delete: DeletionMode = IfSuccessful): Path

	/**
	 * Creates a test file in the directory of the current test scope.
	 *
	 * @param name The name of the file. If omitted or `null`, the name will be generated.
	 * @param delete When to delete the created file, see [DeletionMode].
	 * @return The absolute [Path] to the created file.
	 */
	public fun createFile(name: String? = null, delete: DeletionMode = IfSuccessful): Path
}
