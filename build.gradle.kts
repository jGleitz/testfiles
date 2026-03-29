import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
kotlin("jvm") version "1.9.23"
kotlin("multiplatform") version "1.9.23" apply false
id("org.jetbrains.dokka") version "1.9.20"
id("com.palantir.git-version") version "3.0.0"
`maven-publish`
signing
id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
idea
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
plugin("org.gradle.maven-publish")
plugin("org.gradle.signing")
}

val isMultiplatform = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

val dokkaJar by tasks.registering(Jar::class) {
group = "build"
description = "Assembles the Kotlin docs with Dokka"
archiveClassifier.set("javadoc")
from(tasks.named("dokkaJavadoc"))
}

signing {
val signingKey: String? by project
val signingKeyPassword: String? by project
useInMemoryPgpKeys(signingKey, signingKeyPassword)
}

if (isMultiplatform) {
val kotlin = extensions.getByType<KotlinMultiplatformExtension>()

tasks.withType<DokkaTask> {
dokkaSourceSets.named("commonMain") {
this.DokkaSourceSetID(if (extra.has("artifactId")) extra["artifactId"] as String else project.name)
sourceLink {
val projectPath = projectDir.absoluteFile.relativeTo(rootProject.projectDir.absoluteFile)
localDirectory.set(file("src/commonMain/kotlin"))
remoteUrl.set(uri("https://github.com/$githubRepository/blob/$gitRef/$projectPath/src/commonMain/kotlin").toURL())
remoteLineSuffix.set("#L")
}
}
}

// KMP plugin creates publications for each target; configure POM metadata on all of them
publishing.publications.withType<MavenPublication>().configureEach {
val targetArtifactId = if (extra.has("artifactId")) extra["artifactId"] as String else project.name
// KMP root publication uses the base artifactId; platform publications get a suffix
if (name == "kotlinMultiplatform") {
artifactId = targetArtifactId
artifact(dokkaJar)
} else if (name == "jvm") {
artifactId = "$targetArtifactId-jvm"
} else if (name == "js") {
artifactId = "$targetArtifactId-js"
} else if (name == "metadata") {
artifactId = "$targetArtifactId-metadata"
}

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
} else {
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

artifacts {
archives(sourcesJar)
archives(dokkaJar)
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
dependsOn(publishToGithub, publishToMavenCentral, rootProject.tasks.closeAndReleaseStagingRepository)
}

rootProject.tasks.closeAndReleaseStagingRepository { mustRunAfter(publishToMavenCentral) }
}
}

val Project.isSnapshot get() = versionDetails.commitDistance != 0
fun String.drop(prefix: String) = if (this.startsWith(prefix)) this.drop(prefix.length) else this
fun String.firstUpper() = this.replaceFirstChar { it.titlecase() }
val Project.versionDetails get() = (this.extra["versionDetails"] as groovy.lang.Closure<*>)() as com.palantir.gradle.gitversion.VersionDetails
