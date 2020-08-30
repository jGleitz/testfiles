package de.joshuagleitze.test.spek.testfiles

import de.joshuagleitze.test.spek.testfiles.DeletionMode.IF_SUCCESSFUL
import java.nio.file.Path

interface TestFiles {
	fun createDirectory(name: String? = null, delete: DeletionMode = IF_SUCCESSFUL): Path
	fun createFile(name: String? = null, delete: DeletionMode = IF_SUCCESSFUL): Path
}
