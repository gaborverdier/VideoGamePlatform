tasks.test {
    ignoreFailures = true
}
plugins {
    application
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com"
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

    // Kafka + Avro + Schema Registry
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("io.confluent:kafka-avro-serializer:7.5.3")
    implementation("org.apache.avro:avro:1.11.3")

    // CSV Parser (pour VGSales)
    implementation("com.opencsv:opencsv:5.9")

    // Lombok (réduction du boilerplate)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Dépendance locale aux schémas Avro
    implementation(files("../../../common/avro-schemas/build/libs/avro-schemas-1.0.0.jar"))

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Actuator (monitoring)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

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
    mainClass = "com.PublisherServiceApplication"
}
