plugins {
    java
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}

dependencies {
    // Production code dependencies
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(files("libs/interactive-brokers-api-9.72.18.jar"))

    // Test code dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
}

tasks.test {
    useJUnitPlatform {
        // includeTags "integration", "feature-168"
        // excludeTags "integration"
    }
}

tasks.wrapper {
    gradleVersion = "6.4.1"
}

springBoot {
    buildInfo {
        properties {
            artifact = "theta-artifact"
            group = "theta-group"
            name = "theta-project"
            version = "0.4"
        }
    }
}
