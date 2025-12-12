plugins {
    id("java")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.8.0"
}

group = "com.gaming.common"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.avro:avro:1.11.3")
}

sourceSets {
    main {
        resources {
            srcDir("src/main/avro")
        }
    }
}
