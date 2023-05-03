import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    application
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.jpa") version "1.7.10"
    kotlin("plugin.allopen") version "1.7.10"
    kotlin("plugin.noarg") version "1.7.10"
    id("org.jetbrains.compose") version "1.3.0"
    id("org.jetbrains.dokka") version "1.8.10"
}

application {
    mainClass.set("net.apcsimple.controlapplication.ControlServerKt")
}

group = "net.apcsimple"
version = "0.3.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}


repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://maven.google.com/")
    }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.xerial:sqlite-jdbc")
    implementation("com.github.nmalkin:sklog:0.0.1")
    implementation("com.intelligt.modbus:jlibmodbus:1.2.9.7")
    implementation("com.digitalpetri.modbus:modbus-master-tcp:1.2.0")
    implementation("com.digitalpetri.modbus:modbus-slave-tcp:1.2.0")
    implementation("org.ejml:ejml-simple:0.41")
    testImplementation("com.google.android.things:androidthings:1.0")
    compileOnly("com.github.purejavacomm:purejavacomm:1.0.2.RELEASE")
    //    implementation("org.testng:testng:7.1.0")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("com.google.code.gson:gson")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(compose.desktop.currentOs)
    implementation("com.apple:AppleJavaExtensions:1.4")
}

allOpen {
    annotations("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embeddable")
}

noArg {
    annotation("net.apcsimple.controlapplication.annotations.NoArgs")
//	invokeInitializers = true
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
