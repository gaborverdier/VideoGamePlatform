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
    implementation("org.apache.kafka:kafka-streams:7.8.3-ce")

    // Avro + Schema Registry
    implementation("io.confluent:kafka-avro-serializer:7.8.3")
    implementation("org.apache.avro:avro:1.11.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.example.AppKt")
}
