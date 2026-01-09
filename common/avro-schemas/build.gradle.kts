plugins {
    id("java")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.0"
}

group = "com.gaming.common"
version = "1.0.0"

repositories {
    mavenCentral()
    // Ajout du dépôt Confluent pour Kafka Avro Serializer
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation("org.apache.avro:avro:1.11.3")
}

// Configuration pour générer les classes Java depuis les schémas Avro
avro {
    isCreateSetters.set(true)
    isEnableDecimalLogicalType.set(true)
    fieldVisibility.set("PRIVATE")
}

sourceSets {
    main {
        java {
            srcDir("build/generated-main-avro-java")
        }
    }
}
