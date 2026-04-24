/* ------------------------------ Plugins ------------------------------ */
plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "8.1.0"
    id("com.gradleup.shadow") version "8.3.9"
    id("checkstyle")
    eclipse
}

/* --------------------------- JDK ------------------------------------- */
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

/* ----------------------------- Metadata ------------------------------ */
group = "at.helpch.placeholderapi.expansion"

version = "2.7.3"

/* ----------------------------- Resources ----------------------------- */
tasks.named<ProcessResources>("processResources") { from("LICENSE") { into("/") } }

/* ---------------------------- Repos ---------------------------------- */
repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
}

/* ---------------------- Java project deps ---------------------------- */
dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

apply(from = "eclipse.gradle.kts")

/* ---------------------- Reproducible jars ---------------------------- */
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

/* ----------------------------- Jar ----------------------------------- */
tasks.jar {
    archiveClassifier.set("part")
    manifest {
        attributes["Implementation-Title"] = "Server-PAPI"
        attributes["Implementation-Version"] = project.version
    }
}

/* ----------------------------- Shadow -------------------------------- */
tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Server-PAPI-${project.version}.jar")
    manifest {
        attributes["Implementation-Title"] = "Server-PAPI"
        attributes["Implementation-Version"] = project.version
    }
    minimize()
}

tasks.build { dependsOn(tasks.spotlessApply, tasks.shadowJar) }

/* --------------------------- Javac opts ------------------------------- */
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
    options.isFork = true
}

/* ----------------------------- Auto Formatting ------------------------ */
spotless {
    java {
        eclipse().configFile("config/formatter/eclipse-java-formatter.xml")
        leadingTabsToSpaces()
        removeUnusedImports()
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}

checkstyle {
    toolVersion = "10.18.1"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = true
    isShowViolations = true
}

tasks.named("compileJava") { dependsOn("spotlessApply") }

tasks.named("spotlessCheck") { dependsOn("spotlessApply") }
