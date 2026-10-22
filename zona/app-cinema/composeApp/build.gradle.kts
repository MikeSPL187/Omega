import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val withAndroid = (findProperty("zona.android") as String?) == "true"
if (withAndroid) apply(plugin = "com.android.application")

kotlin {
    jvm("desktop") {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    if (withAndroid) androidTarget()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("io.ktor:ktor-client-core:3.3.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-okhttp:3.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
            }
        }
    }
}

compose.resources {
    packageOfResClass = "ru.zona.cinema.res"
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "ru.zona.cinema.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb)
            packageName = "zona-cinema"
            packageVersion = "0.1.0"
        }
    }
}

// Renders every screen to PNG (build/screens) without a display; used to review the UI.
tasks.register<JavaExec>("renderScreens") {
    group = "verification"
    val desktop = kotlin.targets.getByName("desktop") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val main = desktop.compilations.getByName("main")
    dependsOn(main.compileTaskProvider)
    classpath = files(main.output.allOutputs, main.runtimeDependencyFiles)
    mainClass.set("ru.zona.cinema.RenderScreensKt")
    args(layout.buildDirectory.dir("screens").get().asFile.absolutePath)
    systemProperty("java.awt.headless", "true")
    systemProperty("skiko.renderApi", "SOFTWARE")
    maxHeapSize = "1536m"
}

// Calls the live Zona API through the app's Catalog and prints what came back and how fast.
tasks.register<JavaExec>("checkApi") {
    group = "verification"
    val desktop = kotlin.targets.getByName("desktop") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val main = desktop.compilations.getByName("main")
    dependsOn(main.compileTaskProvider)
    classpath = files(main.output.allOutputs, main.runtimeDependencyFiles)
    mainClass.set("ru.zona.cinema.CheckApiKt")
}

if (withAndroid) apply(from = "android.gradle")
