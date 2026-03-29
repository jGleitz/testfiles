package de.joshuagleitze.testfiles.kotest

import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isReadable
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.isWritable
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.core.polyfills.fullName
import de.joshuagleitze.testfiles.DefaultTestFiles
import io.kotest.core.spec.style.DescribeSpec

class KotestTestFilesIntegrationSpec : DescribeSpec({
	val fileRoot = DefaultTestFiles.determineTestFilesRootDirectory()

	describe("testFiles") {
		val expectedGroupFolder = fileRoot / "[${KotestTestFilesIntegrationSpec::class.fullName}]" / "[testFiles]"

		it("creates a test file with the appropriate name") {
			val file = testFiles.createFile()
			expect(file.toNioPath()) {
				isRegularFile()
				isReadable()
				isWritable()
			}
			expect(file.parent?.toNioPath()).toBe((expectedGroupFolder / "[creates a test file with the appropriate name]").toNioPath())
		}

		it("creates a test directory with the appropriate name") {
			val dir = testFiles.createDirectory()
			expect(dir.toNioPath()) {
				isDirectory()
				isReadable()
				isWritable()
			}
			expect(dir.parent?.toNioPath()).toBe((expectedGroupFolder / "[creates a test directory with the appropriate name]").toNioPath())
		}
	}
})
