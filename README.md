# spek-testfiles

[Spek](https://www.spekframework.org/) extension to easily create files and directories for testing purposes.

 * Organises test files according to the test structure
 * Cleans up test files but can leave them in place if the test fails so you can examine them
 * Generates random file names that are the same each test run
 

## Example

```kotlin
import de.joshuagleitze.test.spek.testfiles.DeletionMode.*
import de.joshuagleitze.test.spek.testfiles.testFiles
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ExampleSpek : Spek({
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
```

## Test File Directory

`spek-testfiles` will pick the folder it creates the testfiles in automatically according to this logic:

Case | Test File Directory | Use Case  
--- | --- | ---
`<pwd>/build` exists | `<pwd>/build/test-outputs` | Gradle users
`<pwd>/target` exists | `<pwd>/target/test-outputs` | Maven users
`<pwd>/test-outputs` exists | `<pwd>/test-outputs` | Other users wishing to have the files in the project directory
else | `<tmpdir>/spek-test-outputs` | Fallback 

The files in the test directory will be organized into directories according to the test structure.
For example, the “output” file from the example above would be created at `<test file directory>/[ExampleSpek]/[using test files]/[cleans up files]/output`. 

## Deletion Mode

Per default, `spek-testiles` will delete created files and directories if the current test or group was successful and retain them if the test or group failed.
This allows you to examine test output after tests fails, but does not clutter your test output folder.

The behaviour can be changed by passing the correct [`DeletionMode`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-deletion-mode/index.html) to [`createFile`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-test-files/create-file.html) or [`createDirectory`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-test-files/create-directory.html):

[`DeletionMode`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-deletion-mode/index.html) | behaviour
--- | ---
[`ALWAYS`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-deletion-mode/-a-l-w-a-y-s.html) | Always delete the created file after the test or group has run.
[`IF_SUCCESSFUL`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-deletion-mode/-i-f_-s-u-c-c-e-s-s-f-u-l.html) | Delete the file if the test or group ran through successfully; retain the file if the test or group failed. The default value.
[`NEVER`](https://jgleitz.github.io/spek-testfiles/spek-testfiles/de.joshuagleitze.test.spek.testfiles/-deletion-mode/-n-e-v-e-r.html) | always retain the file after the test run.

Retained files will be deleted once the group or test they were created for is executed again.

## File Names

If no file or directory name is provided, `spek-testfiles` will create one.
Names are constructed following the pattern `test-<number>` where `<number>` is a randomly generated non-negative integer.
The random generator is seeded with the current test directory path to make sure the same test will always get the same file names every execution.

For example, the first generated file from the example above might have the path `<test file directory>/[ExampleSpek]/[using test files]/[generates file names]/test-162363182`.

If a file name is provided, the name must not be wrapped in angle brackets; i.e. either not start with `[` or not end with `]`. 
