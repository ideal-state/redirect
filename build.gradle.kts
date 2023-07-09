plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "pers.ketikai.network"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.compileJava {
    options.encoding = "utf-8"
}

tasks.create<Copy>("copy-start-script") {
    from("${rootDir}/start-redirect.bat")
    into("${buildDir}/libs")
}

tasks.shadowJar {
    dependsOn(tasks.named("copy-start-script"))
    archiveClassifier.set("")
    manifest {
        attributes(
            "Main-Class" to "pers.ketikai.network.redirect.Redirect",
            "Multi-Release" to true
        )
    }
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
}
