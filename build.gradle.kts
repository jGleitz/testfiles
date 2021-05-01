import org.jetbrains.dokka.gradle.DokkaTask

plugins {
	kotlin("jvm") version "1.4.32"
	id("org.jetbrains.dokka") version "1.4.32"
	id("com.palantir.git-version") version "0.12.3"
	`maven-publish`
	signing
	id("de.marcphilipp.nexus-publish") version "0.4.0"
	id("io.codearte.nexus-staging") version "0.30.0"
}

group = "de.joshuagleitze"
version = if (isSnapshot) versionDetails.gitHash else versionDetails.lastTag.drop("v")
status = if (isSnapshot) "snapshot" else "release"
val gitRef = if (isSnapshot) versionDetails.gitHash else versionDetails.lastTag

subprojects {
	group = rootProject.group
	version = rootProject.version
	status = rootProject.status
}

allprojects {
	repositories {
		mavenCentral()
	}
}

tasks.withType<Test> {
	reports.junitXml.isEnabled = true
}

val ossrhUsername: String? by project
val ossrhPassword: String? by project
val githubRepository: String? by project
val githubOwner = githubRepository?.split("/")?.get(0)
val githubToken: String? by project

nexusStaging {
	username = ossrhUsername
	password = ossrhPassword
	numberOfRetries = 42
}

val closeAndReleaseRepository by project.tasks

subprojects {
	afterEvaluate {
		apply {
			plugin("org.jetbrains.dokka")
			plugin("de.marcphilipp.nexus-publish")
			plugin("org.gradle.maven-publish")
			plugin("org.gradle.signing")
		}

		val sourcesJar by tasks.registering(Jar::class) {
			group = "build"
			description = "Assembles the source code into a jar"
			archiveClassifier.set("sources")
			from(sourceSets.main.map { it.allSource })
		}

		tasks.withType<DokkaTask> {
			dokkaSourceSets.named("main") {
				this.DokkaSourceSetID(if (extra.has("artifactId")) extra["artifactId"] as String else project.name)
				sourceLink {
					val projectPath = projectDir.absoluteFile.relativeTo(rootProject.projectDir.absoluteFile)
					localDirectory.set(file("src/main/kotlin"))
					remoteUrl.set(uri("https://github.com/$githubRepository/blob/$gitRef/$projectPath/src/main/kotlin").toURL())
					remoteLineSuffix.set("#L")
				}
			}
		}

		val dokkaJar by tasks.registering(Jar::class) {
			group = "build"
			description = "Assembles the Kotlin docs with Dokka"
			archiveClassifier.set("javadoc")
			from(tasks.named("dokkaJavadoc"))
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

		val mavenCentral = nexusPublishing.repositories.sonatype {
			username.set(ossrhUsername)
			password.set(ossrhPassword)
		}

		val publishToGithub = tasks.named("publishAllPublicationsTo${githubPackages.name.capitalize()}Repository")
		val publishToMavenCentral = tasks.named("publishTo${mavenCentral.name.capitalize()}")

		tasks.register("release") {
			group = "release"
			description = "Releases the project to all remote repositories"
			dependsOn(publishToGithub, publishToMavenCentral, rootProject.tasks.closeAndReleaseRepository)
		}

		rootProject.tasks.closeAndReleaseRepository { mustRunAfter(publishToMavenCentral) }
	}
}

val Project.isSnapshot get() = versionDetails.commitDistance != 0
fun String.drop(prefix: String) = if (this.startsWith(prefix)) this.drop(prefix.length) else this
val Project.versionDetails get() = (this.extra["versionDetails"] as groovy.lang.Closure<*>)() as com.palantir.gradle.gitversion.VersionDetails
