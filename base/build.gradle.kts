import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("testfiles")
val description by extra("Manage test files and directories neatly!")

dependencies {
	testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.19")
	testImplementation("ch.tutteli.atrium:atrium-fluent-en_GB:0.18.0")
	testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.19")
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

tasks.withType<Test> {
	useJUnitPlatform()

	val testPwd = layout.buildDirectory.dir("test-pwd")
	doFirst {
		testPwd.get().asFile.mkdirs()
	}
	workingDir = testPwd.get().asFile
}


