plugins {
    application
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    // Kafka Streams
    implementation("org.apache.kafka:kafka-streams:7.8.3-ce")
    implementation("io.confluent:kafka-streams-avro-serde:7.8.3")

    // Avro + Schema Registry
    implementation("io.confluent:kafka-avro-serializer:7.8.3")
    implementation("org.apache.avro:avro:1.11.3")

    // Common Avro Schemas (generated classes)
    implementation("com.gaming:avro-schemas")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.example.AppKt")
}
