plugins {
    application
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.openjfx.javafxplugin") version "0.1.0" // ✅ Updated from 0.0.14
}

group = "org.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Database
    runtimeOnly("org.postgresql:postgresql:42.7.1")
    runtimeOnly("com.h2database:h2:2.2.224")

    // Kafka + Avro + Schema Registry
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("io.confluent:kafka-avro-serializer:7.5.3")
    implementation("org.apache.avro:avro:1.11.3")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Jackson Avro support
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-avro:2.15.3")

    // Local dependency to Avro schemas
    implementation(files("../../../common/avro-schemas/build/libs/avro-schemas-1.0.0.jar"))

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // ❌ REMOVE JavaFX if not needed for backend service
    // If you need JavaFX, keep version 21 to match Java 21:
    // implementation("org.openjfx:javafx-controls:21")
    // implementation("org.openjfx:javafx-fxml:21")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // ✅ Keep Java 21 for stability
    }
}

application {
    mainClass = "org.example.Main"
}

// ✅ Only include javafx block if you actually use JavaFX
javafx {
    version = "21" // ✅ Match Java version
    modules = listOf(
        "javafx.controls",
        "javafx.fxml"
    )
}