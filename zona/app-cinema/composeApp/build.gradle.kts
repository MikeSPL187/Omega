import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
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
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
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
}

if (withAndroid) apply(from = "android.gradle")
