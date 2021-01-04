import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("kotest-files")

dependencies {
	val kotestVersion = "4.3.2"

	implementation(rootProject)
	// Kotest is a peer dependency
	compileOnly(name = "kotest-framework-api", version = kotestVersion, group = "io.kotest")

	testImplementation(name = "kotest-runner-junit5", version = kotestVersion, group = "io.kotest")
	testImplementation(name = "atrium-fluent-en_GB", version = "0.15.0", group = "ch.tutteli.atrium")
}

java {
	sourceCompatibility = VERSION_1_8
	targetCompatibility = VERSION_1_8
}

kotlin {
	explicitApi()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.8"
	}
}

tasks.compileTestKotlin {
	kotlinOptions {
		freeCompilerArgs += "-Xopt-in=kotlin.io.path.ExperimentalPathApi"
	}
}

tasks.withType<DokkaTask> {
	dokkaSourceSets.named("main") {
		samples.from("src/test/kotlin/samples/ExampleSpek.kt")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	reports.junitXml.isEnabled = true
}

