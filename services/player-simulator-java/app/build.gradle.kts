plugins {
    application
    java
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

    testImplementation(libs.junit)
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    mainClass = "org.example.App"
}
