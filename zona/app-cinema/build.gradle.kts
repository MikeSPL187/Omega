// Plugins live on the root buildscript classpath so the Android Gradle Plugin can be
// added only when the Android target is enabled (it is served from Google Maven only).
buildscript {
    val withAndroid = gradle.startParameter.projectProperties["zona.android"] == "true" ||
        providers.gradleProperty("zona.android").orNull == "true"
    repositories {
        gradlePluginPortal()
        mavenCentral()
        if (withAndroid) google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.21")
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.9.3")
        if (withAndroid) classpath("com.android.tools.build:gradle:8.11.1")
    }
}
