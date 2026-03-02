rootProject.name = "testfiles"

dependencyResolutionManagement {
  versionCatalogs {
    create("testLibs") {
      from(files("gradle/testLibs.versions.toml"))
    }
    create("buildLibs") {
      from(files("gradle/buildLibs.versions.toml"))
    }
  }
}

plugins {
  id("com.gradle.develocity") version "4.3.2"
}

include("base", "kotest", "spek")

develocity {
  buildScan {
    publishing.onlyIf { false }
  }
}
