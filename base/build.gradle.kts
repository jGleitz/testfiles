import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(buildLibs.plugins.kotlin.jvm)
	alias(buildLibs.plugins.dokka)
}

val artifactId by extra("testfiles")
val description by extra("Manage test files and directories neatly!")

dependencies {
	testImplementation(libs.spek.dsl.jvm)
	testImplementation(testLibs.atrium.fluent.en.gb)
	testRuntimeOnly(testLibs.spek.runner.junit5)
	testRuntimeOnly(testLibs.junit.platform.launcher)
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

tasks.withType<Test> {
	useJUnitPlatform()

	val testPwd = layout.buildDirectory.dir("test-pwd")
	doFirst {
		testPwd.get().asFile.mkdirs()
	}
	workingDir = testPwd.get().asFile
}


