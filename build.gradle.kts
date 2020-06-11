import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.2.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.71"
	kotlin("plugin.spring") version "1.3.71"
}

group = "com.projdgroep3"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	google()
	jcenter()
	mavenCentral()
}

dependencies {

	//Spring
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-autoconfigure")
	implementation("org.springframework.security:spring-security-core")
	implementation("org.springframework.security:spring-security-web")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.data:spring-data-commons")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	compileOnly("org.springframework.boot:spring-boot-starter-tomcat")

	//Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	//DB
        // PostgreSQL
        implementation("org.postgresql:postgresql:42.2.12")
        //Exposed
        implementation("org.jetbrains.exposed", "exposed-core", "0.23.1")
        implementation("org.jetbrains.exposed", "exposed-dao", "0.23.1")
        implementation("org.jetbrains.exposed", "exposed-jdbc", "0.23.1")

	//Crypto
	implementation("commons-codec:commons-codec:1.12")

	//Configuration
	implementation ("com.natpryce", "konfig", "1.6.10.0")

	//Swagger
	implementation("io.springfox", "springfox-swagger2", "2.9.2")
	implementation("io.springfox", "springfox-swagger-ui", "2.9.2")

	//MapBox
	implementation("androidx.annotation:annotation:1.0.0")
	implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:5.2.1")

	//Test
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
