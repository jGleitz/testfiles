import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
	alias(buildLibs.plugins.kotlin.jvm)
	alias(buildLibs.plugins.dokka)
	alias(buildLibs.plugins.dokka.javadoc) apply false
	`maven-publish`
	signing
	alias(buildLibs.plugins.nexus.publish)
	idea
}

group = "de.joshuagleitze"
version = if (version == "unspecified") "local" else version.toString().removePrefix("v")
status = if (version == "local") "snapshot" else "release"

subprojects {
	group = rootProject.group
	version = rootProject.version
	status = rootProject.status
}

allprojects {
	plugins.apply("org.gradle.idea")
	repositories {
		mavenCentral()
	}
	idea {
		module {
			isDownloadJavadoc = true
			isDownloadSources = true
		}
	}
}

tasks.withType<Test> {
	reports.junitXml.required.set(true)
}

val ossrhUsername: String? by project
val ossrhPassword: String? by project
val githubRepository: String? by project
val githubOwner = githubRepository?.split("/")?.get(0)
val githubToken: String? by project

val mavenCentral = nexusPublishing.repositories.sonatype {
	username.set(ossrhUsername)
	password.set(ossrhPassword)
}

subprojects {
	afterEvaluate {
		apply {
			plugin("org.jetbrains.dokka")
			plugin("org.jetbrains.dokka-javadoc")
			plugin("org.gradle.maven-publish")
			plugin("org.gradle.signing")
		}

		val sourcesJar by tasks.registering(Jar::class) {
			group = "build"
			description = "Assembles the source code into a jar"
			archiveClassifier.set("sources")
			from(sourceSets.main.map { it.allSource })
		}

		val projectPath = projectDir.absoluteFile.relativeTo(rootProject.projectDir.absoluteFile)
		configure<DokkaExtension> {
			dokkaSourceSets.named("main") {
				sourceLink {
					localDirectory = file("src/main/kotlin")
					remoteUrl = uri("https://github.com/$githubRepository/blob/main/$projectPath/src/main/kotlin")
					remoteLineSuffix = "#L"
				}
			}
		}

		val dokkaJar by tasks.registering(Jar::class) {
			group = "build"
			description = "Assembles the Kotlin docs with Dokka"
			archiveClassifier.set("javadoc")
			from(tasks.named("dokkaGeneratePublicationJavadoc"))
		}

		artifacts {
			archives(sourcesJar)
			archives(dokkaJar)
		}

		signing {
			val signingKey: String? by project
			val signingKeyPassword: String? by project
			useInMemoryPgpKeys(signingKey, signingKeyPassword)
		}

		publishing.publications.create<MavenPublication>("maven") {
			artifactId = if (extra.has("artifactId")) extra["artifactId"] as String else project.name

			from(components["java"])
			artifact(sourcesJar)
			artifact(dokkaJar)

			signing.sign(this)

			pom {
				name.set("$groupId:$artifactId")
				if (extra.has("description")) description.set(extra["description"] as String)
				inceptionYear.set("2020")
				url.set("https://github.com/$githubRepository")
				ciManagement {
					system.set("GitHub Actions")
					url.set("https://github.com/$githubRepository/actions")
				}
				issueManagement {
					system.set("GitHub Issues")
					url.set("https://github.com/$githubRepository/issues")
				}
				developers {
					developer {
						name.set("Joshua Gleitze")
						email.set("dev@joshuagleitze.de")
					}
				}
				scm {
					connection.set("scm:git:https://github.com/$githubRepository.git")
					developerConnection.set("scm:git:git://git@github.com:$githubRepository.git")
					url.set("https://github.com/$githubRepository")
				}
				licenses {
					license {
						name.set("MIT")
						url.set("https://opensource.org/licenses/MIT")
						distribution.set("repo")
					}
				}
			}
		}

		val githubPackages = publishing.repositories.maven("https://maven.pkg.github.com/$githubRepository") {
			name = "GitHubPackages"
			credentials {
				username = githubOwner
				password = githubToken
			}
		}

		val publishToGithub = tasks.named("publishAllPublicationsTo${githubPackages.name.firstUpper()}Repository")
		val publishToMavenCentral = tasks.named("publishTo${mavenCentral.name.firstUpper()}")

		tasks.register("release") {
			group = "release"
			description = "Releases the project to all remote repositories"
			dependsOn(publishToGithub, publishToMavenCentral, rootProject.tasks.closeAndReleaseStagingRepositories)
		}

		rootProject.tasks.closeAndReleaseStagingRepositories { mustRunAfter(publishToMavenCentral) }
	}
}

fun String.firstUpper() = this.replaceFirstChar { it.titlecase() }
