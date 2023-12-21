import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    id("nu.studer.jooq") version "8.2"
    id("org.flywaydb.flyway") version "9.22.1" // Major version 10 has some issues with postgres migration
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jooq:3.2.0")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql:42.7.1")

    //flyway
    implementation("org.flywaydb:flyway-core:10.3.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
// jooq generation using postgres db

dependencies {
    jooqGenerator("org.postgresql:postgresql:42.5.1")
}

val dbUrl = "jdbc:postgresql://localhost:5432/postgres"
val dbUser = "postgres"
val dbPassword = "pass123"
val dbDriver = "org.postgresql.Driver"

flyway {
    driver = dbDriver
    url = dbUrl
    user = dbUser
    password = dbPassword
    schemas = arrayOf("public")
    baselineOnMigrate = true
    table = "flyway_schema_history"
    locations = arrayOf("filesystem:$projectDir/src/main/resources/db/migration")
}

jooq {
    version.set("3.18.7")  // the default (can be omitted)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)  // the default (can be omitted)
    configurations {
        create("main") {  // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)

            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    driver = dbDriver
                    url = dbUrl
                    user = dbUser
                    password = dbPassword
                    properties.add(Property().apply {
                        key = "ssl"
                        value = "false"
                    })
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.addAll(listOf(
                            ForcedType().apply {
                                name = "varchar"
                                includeExpression = ".*"
                                includeTypes = "JSONB?"
                            },
                            ForcedType().apply {
                                name = "varchar"
                                includeExpression = ".*"
                                includeTypes = "INET"
                            }
                        ))
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.example.generated"
                        directory = "src/main/jooq"  // default (can be omitted)
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }

}