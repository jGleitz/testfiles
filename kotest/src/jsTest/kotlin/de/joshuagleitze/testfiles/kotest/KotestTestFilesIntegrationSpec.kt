package de.joshuagleitze.testfiles.kotest

import de.joshuagleitze.testfiles.DefaultTestFiles
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class KotestTestFilesIntegrationSpec : DescribeSpec({
val fileRoot = DefaultTestFiles.determineTestFilesRootDirectory()

describe("testFiles") {
val expectedGroupFolder = fileRoot /
"[${KotestTestFilesIntegrationSpec::class.simpleName}]" /
"[testFiles]"

it("creates a test file with the appropriate name") {
val file = testFiles.createFile()
file.parent shouldBe expectedGroupFolder / "[creates a test file with the appropriate name]"
}

it("creates a test directory with the appropriate name") {
val dir = testFiles.createDirectory()
dir.parent shouldBe expectedGroupFolder / "[creates a test directory with the appropriate name]"
}
}
})
