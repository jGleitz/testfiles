package de.joshuagleitze.testfiles.spek.samples

import de.joshuagleitze.testfiles.DeletionMode.Always
import de.joshuagleitze.testfiles.DeletionMode.IfSuccessful
import de.joshuagleitze.testfiles.DeletionMode.Never
import de.joshuagleitze.testfiles.spek.testFiles
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ExampleSpec: Spek({
	val testFiles = testFiles()

	describe("using test files") {
		it("generates file names") {
			testFiles.createFile()
			testFiles.createDirectory()
		}

		it("cleans up files") {
			testFiles.createFile("irrelevant", delete = Always)
			testFiles.createFile("default mode", delete = IfSuccessful)
			testFiles.createFile("output", delete = Never)
		}
	}
})
