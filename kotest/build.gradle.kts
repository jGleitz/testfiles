import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

	constraints {
		testImplementation(kotlin("reflect", version = KotlinCompilerVersion.VERSION))
	}
}

java {
	sourceCompatibility = VERSION_17
	targetCompatibility = VERSION_17
}

kotlin {
	explicitApi()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "17"
	}
}

tasks.compileTestKotlin {
	kotlinOptions {
		freeCompilerArgs += "-Xopt-in=kotlin.io.path.ExperimentalPathApi"
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

