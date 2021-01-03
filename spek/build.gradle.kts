import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

dependencies {
	val spekVersion = "2.0.15"

	// Spek is a peer dependency which is not declared by this module
	compileOnly(name = "spek-dsl-jvm", group = "org.spekframework.spek2", version = spekVersion)
	compileOnly(name = "spek-runtime-jvm", group = "org.spekframework.spek2", version = spekVersion)
}

java {
	sourceCompatibility = VERSION_1_8
	targetCompatibility = VERSION_1_8
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.8"
	}
}
