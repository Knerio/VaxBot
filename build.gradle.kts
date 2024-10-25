plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "de.derioo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.derioo.de/releases")
    maven("https://repo.panda-lang.org/releases")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

dependencies {

    /** Annotations **/
    implementation("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    implementation("org.jetbrains:annotations:26.0.1")
    implementation("org.jetbrains:annotations:26.0.1")

    /** Jackson **/
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")

    /** JavaUtils **/
    implementation("de.derioo.javautils:common:2.7.2")

    /** JDA **/
    implementation("net.dv8tion:JDA:5.0.0-beta.17")
    implementation("com.github.walkyst:lavaplayer-fork:1.4.3")

    /** CommandFramework **/
    implementation("dev.rollczi:litecommands-jda:3.4.2")

    /** Mongo DB **/
    implementation("org.mongodb:mongodb-driver-sync:5.1.2")

    /** En2do **/
    implementation("eu.koboo:en2do:1.0.2")

    implementation("io.javalin:javalin:6.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("commons-io:commons-io:2.17.0")
    implementation("com.github.twitch4j:twitch4j:1.8.0")
    implementation("com.google.api-client:google-api-client:1.35.2")
    implementation("com.google.apis:google-api-services-youtube:v3-rev20241022-2.0.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "de.derioo.Main"
    }
}