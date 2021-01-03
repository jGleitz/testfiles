package de.joshuagleitze.testfiles.spek

import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isReadable
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.isWritable
import ch.tutteli.atrium.api.fluent.en_GB.parent
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.core.polyfills.fullName
import de.joshuagleitze.testfiles.DefaultTestFiles
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.io.path.div

object SpekTestFilesIntegrationSpec: Spek({
	val fileRoot = DefaultTestFiles.determineTestFilesRootDirectory()
	val testFiles = testFiles()

	describe("testFiles") {
		val expectedGroupFolder = fileRoot / "[${SpekTestFilesIntegrationSpec::class.fullName}]" / "[testFiles]"

		it("creates a test file with the appropriate name") {
			expect(testFiles.createFile()) {
				isRegularFile()
				isReadable()
				isWritable()
				parent.toBe(expectedGroupFolder / "[creates a test file with the appropriate name]")
			}
		}

		it("creates a test directory with the appropriate name") {
			expect(testFiles.createDirectory()) {
				isDirectory()
				isReadable()
				isWritable()
				parent.toBe(expectedGroupFolder / "[creates a test directory with the appropriate name]")
			}
		}
	}
})
