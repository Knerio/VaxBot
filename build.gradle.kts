plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "de.derioo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.derioo.de/releases")
}

dependencies {
    /** Annotations **/
    implementation("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.jetbrains:annotations:24.1.0")

    /** JavaUtils **/
    implementation("de.derioo.javautils:common:2.7.0")
    implementation("de.derioo.javautils:discord:2.7.0")

    /** JDA **/
    implementation("net.dv8tion:JDA:5.0.0-beta.24")


    /** Test dependencies **/
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.0")
    testImplementation("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.test {
    useJUnitPlatform()
}