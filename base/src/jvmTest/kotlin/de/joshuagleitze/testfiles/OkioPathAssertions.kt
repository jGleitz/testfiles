package de.joshuagleitze.testfiles

import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isDirectory
import ch.tutteli.atrium.api.fluent.en_GB.isReadable
import ch.tutteli.atrium.api.fluent.en_GB.isRegularFile
import ch.tutteli.atrium.api.fluent.en_GB.isWritable
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.creating.Expect
import okio.Path

val Expect<Path>.content
	get() = feature("content") { toNioPath().toFile().readText() }

val Expect<Path>.fileName
	get() = feature("name") { name }

val Expect<Path>.parent
	get() = feature("parent") { parent!! }

fun Expect<Path>.isRegularFile() = feature("as NIO path") { toNioPath() }.isRegularFile()
fun Expect<Path>.isDirectory() = feature("as NIO path") { toNioPath() }.isDirectory()
fun Expect<Path>.isReadable() = feature("as NIO path") { toNioPath() }.isReadable()
fun Expect<Path>.isWritable() = feature("as NIO path") { toNioPath() }.isWritable()
fun Expect<Path>.isAbsolute() = feature("isAbsolute") { isAbsolute }.toBe(true)
