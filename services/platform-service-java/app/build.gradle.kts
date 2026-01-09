plugins {
    application
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:7.8.3-ce")

    // Avro + Schema Registry
    implementation("io.confluent:kafka-avro-serializer:7.8.3")
    implementation("org.apache.avro:avro:1.11.3")

    //API - Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    
    // Base de données (H2 pour dev/test, commentez si vous utilisez une autre DB)
    runtimeOnly("com.h2database:h2")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.junit)
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    mainClass = "com.vgp.App"
}
