package de.joshuagleitze.testfiles.kotest.samples

import de.joshuagleitze.testfiles.DeletionMode.ALWAYS
import de.joshuagleitze.testfiles.DeletionMode.IF_SUCCESSFUL
import de.joshuagleitze.testfiles.DeletionMode.NEVER
import de.joshuagleitze.testfiles.kotest.testFiles
import io.kotest.core.spec.style.DescribeSpec

class ExampleSpek: DescribeSpec({
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
