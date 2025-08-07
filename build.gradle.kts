plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.0"
    `maven-publish`
}

group = "net.justlime"
version = "1.0.0"

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
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.spigot)
    compileOnly(libs.authlib)
    compileOnly(libs.kotlin)
    compileOnly(libs.kotlin.reflection)
    compileOnly(libs.annotation)
    compileOnly(libs.adventure.platform)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.papi)

}


val targetJavaVersion = 8
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.shadowJar{
    minimize()
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "LimeFrameGUI"
            version = project.version.toString()
        }

    }
    //use kotlin 2.1.0

}

// === SHADOW COPY TASK ===
tasks.register<Copy>("shadowJarCopy") {
    group = "build"
    description = "Copy shadowJar jar to local test server"
    dependsOn("shadowJar")
    from(tasks.shadowJar.get().outputs.files.singleFile)
    into("E:/Minecraft/servers/PaperMC-1.21.4/plugins")
}





