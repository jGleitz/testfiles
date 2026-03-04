import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("kotest-files")
val description by extra("Manage test files and directories neatly when testing with Kotest!")

dependencies {
	api(project(":base"))
	// Kotest is a peer dependency
	compileOnly("io.kotest:kotest-framework-api:5.9.1")

	testImplementation("io.kotest:kotest-runner-junit5:4.6.4")
	testImplementation("ch.tutteli.atrium:atrium-fluent-en_GB:0.18.0")
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

dokka {
	dokkaSourceSets.main {
		samples.from("src/test/kotlin/samples/ExampleSpec.kt")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

