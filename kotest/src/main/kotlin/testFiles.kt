package de.joshuagleitze.testfiles.kotest

import de.joshuagleitze.testfiles.DefaultTestFiles
import de.joshuagleitze.testfiles.TestFiles

internal val internalTestFiles: DefaultTestFiles = DefaultTestFiles()

/**
 * A [TestFiles] instance that will use the structure of the Kotest tests in this project to create files.
 *
 * @sample de.joshuagleitze.testfiles.kotest.samples.ExampleSpek
 */
public val testFiles: TestFiles get() = internalTestFiles
