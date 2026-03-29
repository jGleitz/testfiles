package de.joshuagleitze.testfiles.kotest

import de.joshuagleitze.testfiles.DefaultTestFiles
import de.joshuagleitze.testfiles.TestFiles

internal val internalTestFiles: DefaultTestFiles = DefaultTestFiles()

/**
 * A [TestFiles] instance that will use the structure of the Kotest specs in this project when creating files.
 *
 * @sample de.joshuagleitze.testfiles.kotest.samples.ExampleSpec
 */
public val testFiles: TestFiles get() = internalTestFiles
