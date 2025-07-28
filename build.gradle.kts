plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.ben-manes.versions") version("0.51.0")
}

group = "net.justlime"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.spigot)
    compileOnly(libs.authlib)
    implementation(libs.kotlin)


    compileOnly(libs.annotation)
}

tasks {
  runServer {
    minecraftVersion("1.21")
  }
}

val targetJavaVersion = 8
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// === SHADOW COPY TASK ===
tasks.register<Copy>("shadowJarCopy") {
    group = "build"
    description = "Copy shadowJar (non-obfuscated) jar to local test server"
    dependsOn("shadowJar")
    from(tasks.shadowJar.get().outputs.files.singleFile)
    into("E:/Minecraft/servers/PaperMC-1.21.4/plugins")
}

// === PROGUARD COPY TASK ===
tasks.register<Copy>("obfCopy") {
    group = "build"
    description = "Copy obfuscated jar to local test server"
    dependsOn("proguardJar")
    from("$buildDir/libs/${project.name}-${project.version}-obf.jar")
    into("E:/Minecraft/servers/PaperMC-1.21.4/plugins")
}

