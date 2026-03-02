import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(buildLibs.plugins.kotlin.jvm)
	alias(buildLibs.plugins.dokka)
}

val artifactId by extra("kotest-files")
val description by extra("Manage test files and directories neatly when testing with Kotest!")

dependencies {
	api(project(":base"))
	// Kotest is a peer dependency
	compileOnly(libs.kotest.framework.api)

	testImplementation(testLibs.kotest.runner.junit5)
	testImplementation(testLibs.atrium.fluent.en.gb)
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

