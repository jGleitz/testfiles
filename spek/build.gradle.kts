import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(buildLibs.plugins.kotlin.jvm)
	alias(buildLibs.plugins.dokka)
}

val artifactId by extra("spek-testfiles")
val description by extra("Manage test files and directories neatly when testing with Spek!")

dependencies {
	// Spek is a peer dependency
	compileOnly(libs.spek.dsl.jvm)
	compileOnly(libs.spek.runtime.jvm)

	api(project(":base"))
	testImplementation(libs.spek.dsl.jvm)
	testImplementation(testLibs.atrium.fluent.en.gb)
	testRuntimeOnly(testLibs.spek.runner.junit5)
	testRuntimeOnly(testLibs.junit.platform.launcher)

	constraints {
		testImplementation(testLibs.kotlin.reflect)
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
