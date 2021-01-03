package de.joshuagleitze.testfiles.spek

import de.joshuagleitze.testfiles.DefaultTestFiles
import de.joshuagleitze.testfiles.TestFiles
import org.spekframework.spek2.dsl.Root

/**
 * Creates a [TestFiles] instance that will listen to `this` [Root] to capture the structure of the tests.
 *
 * This function _must_ be called from the root of a Spek before any test is created. That means, for example, that it _must not_ be called
 * from the initializer of a `memoized` value. The returned instance should be used throughout the Spek.
 *
 * @sample de.joshuagleitze.testfiles.spek.ExampleSpek
 */
public fun Root.testFiles(): TestFiles = DefaultTestFiles().also { registerListener(SpekTestFilesAdapter(it)) }
