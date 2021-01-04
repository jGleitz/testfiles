package de.joshuagleitze.testfiles.kotest.samples

import de.joshuagleitze.testfiles.DeletionMode.Always
import de.joshuagleitze.testfiles.DeletionMode.IfSuccessful
import de.joshuagleitze.testfiles.DeletionMode.Never
import de.joshuagleitze.testfiles.kotest.testFiles
import io.kotest.core.spec.style.DescribeSpec

class ExampleSpek: DescribeSpec({
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
