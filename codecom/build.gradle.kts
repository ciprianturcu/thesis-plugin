plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.21"
  id("org.jetbrains.intellij") version "1.16.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2023.3.6")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("com.intellij.java"))
}

dependencies {
  // SLF4J API
  implementation ("org.slf4j:slf4j-api:2.0.13")
  // SLF4J Implementation: Logback
  implementation ("ch.qos.logback:logback-classic:1.5.6")
  implementation ("org.json:json:20240303")

  // JUnit 5
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")

  // Mockito
  testImplementation("org.mockito:mockito-core:4.8.0")
  testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")

  // IntelliJ Testing Framework
  testImplementation("com.jetbrains:ideaIC:2023.3")
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  test {
    useJUnitPlatform()
  }

  patchPluginXml {
    sinceBuild.set("231")
    untilBuild.set("241.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }

  // Custom JAR task
  register<Jar>("customJar") {
    archiveBaseName.set("CodeCom")
    destinationDirectory.set(file("${layout.buildDirectory}/libs"))
    from(sourceSets.main.get().output)
    from({
      configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  }

  buildPlugin {
    dependsOn("customJar")
  }
}


