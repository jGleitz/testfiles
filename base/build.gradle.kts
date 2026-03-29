import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
kotlin("multiplatform")
id("org.jetbrains.dokka")
}

val artifactId by extra("testfiles")
val description by extra("Manage test files and directories neatly!")

kotlin {
jvm()
js(IR) {
nodejs()
}

sourceSets {
val commonMain by getting {
dependencies {
api("com.squareup.okio:okio:3.9.0")
}
}

val jvmTest by getting {
dependencies {
val spekVersion = "2.0.17"
implementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
implementation("ch.tutteli.atrium:atrium-fluent-en_GB:0.16.0")
runtimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
implementation(kotlin("reflect"))
}
}
}
}

tasks.withType<KotlinCompile> {
kotlinOptions {
jvmTarget = "1.8"
}
}

tasks.withType<Test> {
useJUnitPlatform()

val testPwd = buildDir.resolve("test-pwd")
doFirst {
testPwd.mkdirs()
}
workingDir = testPwd
}
