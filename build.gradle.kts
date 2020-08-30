import com.moowork.gradle.node.yarn.YarnTask

plugins {
    kotlin("jvm") version "1.4.0"
    id("com.palantir.git-version") version "0.12.3"
    id("com.github.node-gradle.node") version "2.2.4"
}

group = "de.joshuagleitze"
version = if (isSnapshot) versionDetails.gitHash else versionDetails.lastTag.drop("v")
status = if (isSnapshot) "snapshot" else "release"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

node {
    download = true
    version = "12.18.3"
}

val yarnInstall by tasks.registering(YarnTask::class) {
    args = listOf("install")
}

val prepare by tasks.registering {
    group = "build setup"
    dependsOn(yarnInstall)
}

val yarnInstallCi by tasks.registering(YarnTask::class) {
    args = listOf("install", "--immutable")
}

val prepareCi by tasks.registering {
    group = "build setup"
    dependsOn(yarnInstallCi)
}

val checkCommits by tasks.creating(YarnTask::class) {
    group = "verification"
    args = listOf("commitlint")
}

val release by tasks.creating(YarnTask::class) {
    group = "publishing"
    args = listOf("semantic-release")
}

val Project.isSnapshot get() = versionDetails.commitDistance != 0
fun String.drop(prefix: String) = if (this.startsWith(prefix)) this.drop(prefix.length) else this
val Project.versionDetails get() = (this.extra["versionDetails"] as groovy.lang.Closure<*>)() as com.palantir.gradle.gitversion.VersionDetails
