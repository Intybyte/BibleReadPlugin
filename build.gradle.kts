plugins {
    id("java")
    id("io.freefair.lombok") version "8.14.2"
}

group = "me.vaan.bibleread"
version = "1.0.0"

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "io.freefair.lombok")
}