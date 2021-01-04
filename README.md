# testfiles [![CI Status](https://github.com/jGleitz/spek-testfiles/workflows/CI/badge.svg)](https://github.com/jGleitz/spek-testfiles/actions)

A test helper to easily create files and directories for testing purposes.

* Organises test files according to the test structure
* Cleans up test files, but leaves them in place if the test fails, so you can examine them
* Generates random file names that are consistent each test run
* Supports [Spek](https://www.spekframework.org/) and [Kotest](https://kotest.io/)

## Dependencies

Framework | Dependency
--- | ---
[Spek](https://www.spekframework.org/) |  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.joshuagleitze/spek-testfiles/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.joshuagleitze/spek-testfiles) `de.joshuagleitze:spek-testfiles:<version>`
[Kotest](https://kotest.io/) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.joshuagleitze/kotest-files/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.joshuagleitze/kotest-files) `de.joshuagleitze:kotest-files:<version>`

## Usage

### Spek

Create a `testFiles` instance for your Spek by using
the  [`testFiles()`](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles.spek/test-files.html) function:

```kotlin
import DeletionMode.*
import de.joshuagleitze.testfiles.spek.testFiles
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
			testFiles.createFile("irrelevant", delete = Always)
			testFiles.createFile("default mode", delete = IfSuccessful)
			testFiles.createFile("output", delete = Never)
		}
	}
})

```

### Kotest

Use the global [`testFiles`](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles.kotest/test-files.html) property:

```kotlin
import DeletionMode.*
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
```

## Test File Directory

`testfiles` will automatically pick the most appropriate folder to create the files in. Concretely, it uses this logic (
see [DefaultTestFiles.determineTestFilesRootDirectory](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles/-default-test-files/-companion/determine-test-files-root-directory.html)):

Case | Test File Directory | Use Case
--- | --- | ---
`<pwd>/build` exists | `<pwd>/build/test-outputs` | Gradle users
`<pwd>/target` exists | `<pwd>/target/test-outputs` | Maven users
`<pwd>/test-outputs` exists | `<pwd>/test-outputs` | Other users wishing to have the files in the project directory
else | `<tmpdir>/spek-test-outputs` | Fallback 

The files in the test directory will be organized into directories according to the test structure.
For example, the “output” file from the example above would be created at `<test file directory>/[ExampleSpek]/[using test files]/[cleans up files]/output`. 

## Deletion Mode

Per default, `testiles` will delete created files and directories if the current test or group was successful and retain them if the test or
group failed. This allows you to examine test output after tests fails, but does not clutter your test output folder.

The behaviour can be changed by passing the
correct [`DeletionMode`](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles/-deletion-mode/index.html)
to [`createFile`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-test-files/create-file.html)
or [`createDirectory`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-test-files/create-directory.html):

[`DeletionMode`](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles/-deletion-mode/index.html) | behaviour
--- | ---
[`Always`](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles/-deletion-mode/-always/index.html) | Always delete the created file after the test or group has run.
[`IfSuccessful`](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles/-deletion-mode/-if-successful/index.html) | Delete the file if the test or group ran through successfully; retain the file if the test or group failed. The default value.
[`Never`](https://jgleitz.github.io/testfiles/testfiles/de.joshuagleitze.testfiles/-deletion-mode/-never/index.html) | always retain the file after the test run.

Retained files will be deleted once the group or test they were created for is executed again.

## File Names

If no file or directory name is provided, `testfiles` will create one. Names are constructed following the pattern `test-<number>`
where `<number>` is a randomly generated, non-negative integer. The random generator is seeded with the current test directory path to make
sure the same test will always get the same file names every execution.

For example, the first generated file from the example above might have the
path `<test file directory>/[ExampleSpek]/[using test files]/[generates file names]/test-162363182`.

If a file name is provided, the name must not be wrapped in angle brackets; i.e. either not start with `[` or not end with `]`. 
