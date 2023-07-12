import java.nio.charset.Charset

plugins {
    id("java")
//    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "team.idealstate.network"
version = "1.0.4"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.compileJava {
    dependsOn(tasks.clean)
    options.encoding = "utf-8"
}

tasks.create("process-scripts") {
    doFirst {
        val reg = Regex(
            "JAR\\_FILE\\=\\./redirect\\-[0-9]{1,}\\.[0-9]{1,}\\.[0-9]{1,}(-SNAPSHOT){0,1}\\.jar"
        )
        val GBK = Charset.forName("GBK")
        val text = "JAR_FILE=./redirect-${rootProject.version}.jar"
        var script = file("${rootDir}/scripts/start-redirect.bat")
        var content = script.readText(GBK)
        reg.find(content)?.apply {
            script.writeText(script.readText(GBK).replace(this.value, text), GBK)
        }
        script = file("${rootDir}/scripts/start-redirect.sh")
        content = script.readText(Charsets.UTF_8)
        reg.find(content)?.apply {
            script.writeText(script.readText(Charsets.UTF_8).replace(this.value, text), Charsets.UTF_8)
        }
    }
}

tasks.create<Copy>("copy-zip-to-libs") {
    from("${buildDir}/distributions/${rootProject.name}-${rootProject.version}.zip")
    into("${buildDir}/libs")
}

tasks.create<Zip>("copy-to-zip") {
    finalizedBy(tasks.named("copy-zip-to-libs"))
    from("${buildDir}/libs")
    from("${rootDir}/scripts")
}

tasks.shadowJar {
    dependsOn("process-scripts")
    finalizedBy(tasks.named("copy-to-zip"))
    archiveClassifier.set("")
    manifest {
        attributes(
            "Main-Class" to "team.idealstate.network.redirect.Redirect",
            "Multi-Release" to true
        )
    }
}

repositories {
    mavenLocal()
    maven {
        name = "aliyun-public"
        url = uri("https://maven.aliyun.com/repository/public")
    }
    mavenCentral()
}

dependencies {
    val log4jVersion = "2.20.0"
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
}
