import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
kotlin("multiplatform")
id("org.jetbrains.dokka")
}

val artifactId by extra("kotest-files")
val description by extra("Manage test files and directories neatly when testing with Kotest!")

kotlin {
jvm()
js(IR) {
nodejs()
}

sourceSets {
val commonMain by getting {
dependencies {
val kotestVersion = "4.6.2"
api(project(":base"))
// Kotest is a peer dependency
compileOnly("io.kotest:kotest-framework-api:$kotestVersion")
}
}

val jvmTest by getting {
dependencies {
val kotestVersion = "4.6.2"
implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
implementation("ch.tutteli.atrium:atrium-fluent-en_GB:0.16.0")
implementation(kotlin("reflect"))
}
}

val jsTest by getting {
dependencies {
val kotestVersion = "4.6.2"
implementation("io.kotest:kotest-framework-engine:$kotestVersion")
implementation("io.kotest:kotest-assertions-core:$kotestVersion")
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
}
