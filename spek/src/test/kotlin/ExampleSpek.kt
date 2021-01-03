package de.joshuagleitze.testfiles.spek

import de.joshuagleitze.testfiles.DeletionMode.ALWAYS
import de.joshuagleitze.testfiles.DeletionMode.IF_SUCCESSFUL
import de.joshuagleitze.testfiles.DeletionMode.NEVER
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ExampleSpek: Spek({
	val testFiles = testFiles()

	describe("using test files") {
		it("generates file names") {
			testFiles.createFile()
			testFiles.createDirectory()
		}

		it("cleans up files") {
			testFiles.createFile("irrelevant", delete = ALWAYS)
			testFiles.createFile("default mode", delete = IF_SUCCESSFUL)
			testFiles.createFile("output", delete = NEVER)
		}
	}
})
