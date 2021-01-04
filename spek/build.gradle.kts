import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka")
}

val artifactId by extra("spek-testfiles")

dependencies {
	val spekVersion = "2.0.15"

	// Spek is a peer dependency
	compileOnly(name = "spek-dsl-jvm", group = "org.spekframework.spek2", version = spekVersion)
	compileOnly(name = "spek-runtime-jvm", group = "org.spekframework.spek2", version = spekVersion)

	api(rootProject)
	testImplementation(name = "spek-dsl-jvm", version = spekVersion, group = "org.spekframework.spek2")
	testImplementation(name = "atrium-fluent-en_GB", version = "0.15.0", group = "ch.tutteli.atrium")
	testRuntimeOnly(name = "spek-runner-junit5", version = spekVersion, group = "org.spekframework.spek2")

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
	reports.junitXml.isEnabled = true
}
