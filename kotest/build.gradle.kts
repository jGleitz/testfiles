import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("kotest-files")
val description by extra("Manage test files and directories neatly when testing with Kotest!")

dependencies {
	val kotestVersion = "4.6.2"

	api(project(":base"))
	// Kotest is a peer dependency
	compileOnly(name = "kotest-framework-api", version = kotestVersion, group = "io.kotest")

	testImplementation(name = "kotest-runner-junit5", version = kotestVersion, group = "io.kotest")
	testImplementation(name = "atrium-fluent-en_GB", version = "0.16.0", group = "ch.tutteli.atrium")
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
	explicitApi()
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

