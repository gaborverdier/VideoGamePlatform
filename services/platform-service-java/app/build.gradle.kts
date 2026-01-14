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
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Kafka + Avro + Schema Registry (commented out due to network restrictions - uncomment when deploying)
    // implementation("org.apache.kafka:kafka-clients:3.6.1")
    // implementation("io.confluent:kafka-avro-serializer:7.5.3")
    implementation("org.apache.avro:avro:1.11.3")
    
    // Database (H2 for development)
    runtimeOnly("com.h2database:h2")
    
    // Avro Schemas project dependency (commented out - uncomment when deploying with Kafka)
    // implementation(files("../../../common/avro-schemas/build/libs/avro-schemas-1.0.0.jar"))
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.junit)
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "com.vgp.PlatformServiceApplication"
}
