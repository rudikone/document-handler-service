plugins {
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("org.cyclonedx.bom") version "1.7.4"
}

group = "ru.rudikov"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    //spring
    implementation("org.springframework.boot:spring-boot-starter-web")

    //kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    //detekt
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")

    //openapi
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

    //db
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.0.6")

    //test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
}


tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    cyclonedxBom {
        setIncludeConfigs(listOf("runtimeClasspath"))
        setSkipConfigs(listOf("compileClasspath", "testCompileClasspath"))
        setProjectType("application")
        setSchemaVersion("1.4")
        setDestination(project.file("build/reports"))
        setOutputName("CycloneDX-Sbom")
        setOutputFormat("json")
        setIncludeBomSerialNumber(false)
        setIncludeLicenseText(false)
        setComponentVersion("2.0.0")
    }

    build {
        finalizedBy("cyclonedxBom")
    }

    test {
        useJUnitPlatform()
    }
}
