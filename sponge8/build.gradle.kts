import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "me.vaan.bibleread.sponge8"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    implementation(project(":api"))
    implementation(files("./libs/acf-sponge10.jar"))
    //implementation("co.aikar:acf-sponge10:0.5.1-SNAPSHOT")
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("co.aikar.commands", "me.vaan.bibleread.shaded.acf")
    relocate("co.aikar.locales", "me.vaan.bibleread.shaded.locales")
    relocate("com.google.gson", "me.vaan.bibleread.shaded.gson")
}

tasks.named("build") {
    dependsOn("shadowJar")
}


sponge {
    apiVersion("8.3.0-SNAPSHOT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("bible-read-plugin") {
        displayName("BibleReadPlugin")
        license("GPL-3")
        entrypoint("me.vaan.bibleread.sponge8.BibleReadPlugin")
        description("My plugin description")
        links {
            // homepageLink("https://spongepowered.org")
            // sourceLink("https://spongepowered.org/source")
            // issuesLink("https://spongepowered.org/issues")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
