import cn.lalaki.pub.BaseCentralPortalPlusExtension

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("cn.lalaki.central") version "1.2.5"
    id("signing")
}

group = libs.versions.group.get()
version = libs.versions.version.get()

repositories {
    mavenCentral()
    maven("https://repository.voinearadu.dev/repository/maven-releases/")
}

dependencies {
    // Dependencies
    api(libs.gson)
    compileOnly(libs.jedis)
    testImplementation(libs.jedis)

    // Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    compileOnly(libs.jetbrains.annotations)
    annotationProcessor(libs.jetbrains.annotations)
    testCompileOnly(libs.jetbrains.annotations)
    testAnnotationProcessor(libs.jetbrains.annotations)

    // Tests
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.register("javadocJar", Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

tasks.register("sourcesJar", Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val localRepo = uri("local-repo")

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))

            pom{
                name.set("Utils Library")
                description.set("A utility library for various purposes.")
                url.set("https://github.com/Voinea-Radu/Utils")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/Voinea-Radu/Utils/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("voinearadu")
                        name.set("Voinea Radu-Mihai")
                        email.set("contact@voinearadu.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Voinea-Radu/Utils.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Voinea-Radu/Utils.git")
                    url.set("https://github.com/Voinea-Radu/Utils")
                }
            }
        }
    }

    repositories {
        if (project.properties["com.voinearadu.publish"] == "true") {
            maven(url = (project.findProperty("com.voinearadu.url") ?: "") as String) {
                name = "VoineaRaduRepository"
                credentials(PasswordCredentials::class) {
                    username = (project.findProperty("com.voinearadu.auth.username") ?: "") as String
                    password = (project.findProperty("com.voinearadu.auth.password") ?: "") as String
                }
            }
        }

        if (project.properties["generic.publish"] == "true") {
            maven(url = (project.findProperty("generic.url") ?: "") as String) {
                name = "GenericRepository"
                credentials(PasswordCredentials::class) {
                    username = (project.findProperty("generic.auth.username") ?: "") as String
                    password = (project.findProperty("generic.auth.password") ?: "") as String
                }
            }
        }

        maven {
            url = localRepo
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

centralPortalPlus {
    if (project.properties["maven.publish"] == "true") {
        url = localRepo
        username = project.findProperty("maven.auth.username") as String
        password = project.findProperty("maven.auth.password") as String
        publishingType = BaseCentralPortalPlusExtension.PublishingType.AUTOMATIC
    }
}