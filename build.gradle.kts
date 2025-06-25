plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.hibernate.orm") version "6.6.15.Final"
    id("org.graalvm.buildtools.native") version "0.10.6"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "2.2.0"
}

group = "com.tianqueal"

version = System.getenv("APP_VERSION") ?: "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

configurations { compileOnly { extendsFrom(configurations.annotationProcessor.get()) } }

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    implementation("org.mapstruct:mapstruct:1.6.3")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.icegreen:greenmail:2.1.3") {
        exclude(group = "com.sun.mail", module = "jakarta.mail")
    }
    testImplementation("com.icegreen:greenmail-junit5:2.1.3") {
        exclude(group = "com.sun.mail", module = "jakarta.mail")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    kapt("org.hibernate.orm:hibernate-jpamodelgen:6.6.15.Final")
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

hibernate { enhancement { enableAssociationManagement = true } }

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
}
