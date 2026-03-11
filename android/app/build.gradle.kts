import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.softpos_demo"
    compileSdk = flutter.compileSdkVersion

    defaultConfig {
        applicationId = "com.example.softpos_demo"
        minSdk = 28
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(files("libs/SoftPos-v1.3.66.35-Debug_Prod.aar"))

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.6.4")
    implementation("com.squareup.retrofit2:converter-scalars:2.6.4")
    implementation("androidx.room:room-runtime:2.2.5")
    implementation("com.google.android.gms:play-services-safetynet:17.0.0")
    implementation("com.flurry.android:analytics:14.0.0")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
    implementation("com.squareup.okhttp3:logging-interceptor:3.9.1")
}

flutter {
    source = "../.."
}