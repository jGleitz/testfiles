import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("testfiles")
val description by extra("Manage test files and directories neatly!")

dependencies {
	val spekVersion = "2.0.17"

	testImplementation(name = "spek-dsl-jvm", version = spekVersion, group = "org.spekframework.spek2")
	testImplementation(name = "atrium-fluent-en_GB", version = "0.16.0", group = "ch.tutteli.atrium")
	testRuntimeOnly(name = "spek-runner-junit5", version = spekVersion, group = "org.spekframework.spek2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	constraints {
		testImplementation(kotlin("reflect"))
	}
}

java {
	sourceCompatibility = VERSION_17
	targetCompatibility = VERSION_17
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_17
		freeCompilerArgs.add("-opt-in=kotlin.io.path.ExperimentalPathApi")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()

	val testPwd = layout.buildDirectory.dir("test-pwd")
	doFirst {
		testPwd.get().asFile.mkdirs()
	}
	workingDir = testPwd.get().asFile
}


