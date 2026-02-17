plugins {
    // Plugin standard per Kotlin e Applicazione
    alias(libs.plugins.kotlin.jvm)
    application
    idea
}

repositories {
    mavenCentral()
}

dependencies {
    // JUnit Jupiter - Versione definita esplicitamente per stabilità
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Guava (se lo usi nel codice)
    implementation(libs.guava)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "it.contia4zampe.simulator.AppKt"
}

// Configurazione specifica per i TEST
tasks.withType<Test> {
    useJUnitPlatform()

    // Questa è la parte magica per vedere i tuoi println ("LOG: ...")
    testLogging {
        // Mostra l'output dei println direttamente nel terminale se true
        showStandardStreams = false // Cambia a true se vuoi vedere tutto, ma potrebbe essere troppo verboso
        // Mostra i nomi dei test mentre vengono eseguiti
        //events("passed", "skipped", "failed", "standardOut", "standardError")
        // Formato dettagliato per gli errori
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}