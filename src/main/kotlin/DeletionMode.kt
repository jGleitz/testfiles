package de.joshuagleitze.testfiles

public enum class DeletionMode {
	/**
	 * Always delete the created file after the test has run.
	 */
	ALWAYS,

	/**
	 * Delete the file if the test ran through successfully; retain the file if the test failed. Retained files will be
	 * deleted the next time the test is executed.
	 */
	IF_SUCCESSFUL,

	/**
	 * Retain the file after the test ran. Retained files will be deleted the next time the test is executed.
	 */
	NEVER
}
