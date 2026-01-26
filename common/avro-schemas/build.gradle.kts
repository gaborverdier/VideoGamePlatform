plugins {
    id("java")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "com.gaming.common"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation("org.apache.avro:avro:1.11.3")
}

// Configuration to generate Java classes from Avro schemas
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

// Task to display generated classes
tasks.register("showGeneratedClasses") {
    doLast {
        println("Generated Avro classes:")
        fileTree("build/generated-main-avro-java").forEach { file ->
            if (file.name.endsWith(".java")) {
                println("  - ${file.relativeTo(projectDir)}")
            }
        }
    }
}
