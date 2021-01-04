package de.joshuagleitze.testfiles.kotest

import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isReadable
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.isWritable
import ch.tutteli.atrium.api.fluent.en_GB.parent
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.core.polyfills.fullName
import de.joshuagleitze.testfiles.DefaultTestFiles
import io.kotest.core.spec.style.DescribeSpec
import kotlin.io.path.div

class KotestTestFilesIntegrationSpec: DescribeSpec({
	val fileRoot = DefaultTestFiles.determineTestFilesRootDirectory()

	describe("testFiles") {
		val expectedGroupFolder = fileRoot / "[${KotestTestFilesIntegrationSpec::class.fullName}]" / "[testFiles]"

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
