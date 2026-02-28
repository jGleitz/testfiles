import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("spek-testfiles")
val description by extra("Manage test files and directories neatly when testing with Spek!")

dependencies {
	val spekVersion = "2.0.17"

	// Spek is a peer dependency
	compileOnly(name = "spek-dsl-jvm", group = "org.spekframework.spek2", version = spekVersion)
	compileOnly(name = "spek-runtime-jvm", group = "org.spekframework.spek2", version = spekVersion)

	api(project(":base"))
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
	}
}

tasks.compileTestKotlin {
	compilerOptions {
		freeCompilerArgs.add("-opt-in=kotlin.io.path.ExperimentalPathApi")
	}
}

dokka {
	dokkaSourceSets.main {
		samples.from("src/test/kotlin/samples/ExampleSpek.kt")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
