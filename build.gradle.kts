plugins {
    java
    war
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.util"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("org.modelmapper:modelmapper:3.1.1")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("com.h2database:h2")
    annotationProcessor("org.projectlombok:lombok")
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
}
sourceSets {
    val profile = try {
        if (project.hasProperty("profile")) project.property("profile").toString()
        else "dev"
    } catch (e: Exception) {"dev"}
    main {
        java.srcDirs("src/main/java")
        resources {
            srcDirs(listOf("src/main/resources", "src/main/resources-$profile"))
        }
    }
}
tasks {
    processResources {
        duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}
