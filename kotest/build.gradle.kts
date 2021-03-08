import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("kotest-files")
val description by extra("Manage test files and directories neatly when testing with Kotest!")

dependencies {
	val kotestVersion = "4.4.3"

	api(project(":base"))
	// Kotest is a peer dependency
	compileOnly(name = "kotest-framework-api", version = kotestVersion, group = "io.kotest")

	testImplementation(name = "kotest-runner-junit5", version = kotestVersion, group = "io.kotest")
	testImplementation(name = "atrium-fluent-en_GB", version = "0.15.0", group = "ch.tutteli.atrium")

	constraints {
		testImplementation(kotlin("reflect", version = KotlinCompilerVersion.VERSION))
	}
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
}

