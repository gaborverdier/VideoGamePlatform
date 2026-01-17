plugins {
    application
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.gaming.platform"
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
    runtimeOnly("com.h2database:h2:2.2.224") // For tests and local development

    // Kafka + Avro + Schema Registry
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("io.confluent:kafka-avro-serializer:7.5.3")
    implementation("org.apache.avro:avro:1.11.3")

    // Lombok (boilerplate reduction)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Local dependency to Avro schemas
    implementation(files("../../../common/avro-schemas/build/libs/avro-schemas-1.0.0.jar"))

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Actuator (monitoring)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Security (optional - for authentication)
    // implementation("org.springframework.boot:spring-boot-starter-security")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.junit)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.gaming.platform.PlatformServiceApplication"
}