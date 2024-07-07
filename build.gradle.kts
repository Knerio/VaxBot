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
    maven("https://reposilite.koboo.eu/releases")
}

dependencies {
    /** Annotations **/
    implementation("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.jetbrains:annotations:24.1.0")

    /** Jackson **/
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")

    /** JavaUtils **/
    implementation("de.derioo.javautils:common:2.7.1")
    implementation("de.derioo.javautils:discord:2.7.1")

    /** JDA **/
    implementation("net.dv8tion:JDA:5.0.0-beta.17")

    /** CommandFramework **/
    implementation("dev.rollczi:litecommands-jda:3.4.2")

    /** Mongo DB **/
    implementation("org.mongodb:mongodb-driver-sync:5.1.1")

    /** En2do **/
    implementation("eu.koboo:en2do:2.3.9")

    /** Test dependencies **/
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.0")
    testImplementation("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
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