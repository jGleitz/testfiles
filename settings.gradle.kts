rootProject.name = "testfiles"

plugins {
  id("com.gradle.develocity") version "4.3.2"
}

include("base", "kotest", "spek")

develocity {
  buildScan {
    publishing.onlyIf { false }
  }
}
