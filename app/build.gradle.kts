import java.util.Properties

val properties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "1.9.25"
}

android {
    namespace = "com.example.passionDaily"
    compileSdk = 34

    buildFeatures {
        compose = true
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore/release-key.jks")
            storePassword = properties.getProperty("STORE_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = "com.example.passionDaily"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "GOOGLE_CLIENT_ID",
            "\"${properties.getProperty("GOOGLE_CLIENT_ID")}\"")
        buildConfigField("String", "FCM_URL",
            "\"${properties.getProperty("FCM_URL")}\"")
        buildConfigField("String", "FIREBASE_MESSAGING_URL",
            "\"${properties.getProperty("FIREBASE_MESSAGING_URL")}\"")
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")

            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
    kotlinOptions {
        jvmTarget = "19"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/*.kotlin_module"
        }
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all {
                it.systemProperty("robolectric.logging.enabled", "false")
            }
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    val lifecycle_version = "2.8.7"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore:25.1.1")

    // GSON
    implementation("com.google.code.gson:gson:2.11.0")

    // gcm
    implementation("com.google.android.gms:play-services-gcm:17.0.0")

    // coil
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // http
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

    // firebase config
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.6.3")

    // AndroidX Test
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test:core-ktx:1.5.0")

    // Mockk
    testImplementation("io.mockk:mockk:1.13.11")
    androidTestImplementation("io.mockk:mockk-android:1.13.11")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    // Truth
    testImplementation("com.google.truth:truth:1.4.3")
    androidTestImplementation("com.google.truth:truth:1.4.3")

    // Compose UI Testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // turbine
    testImplementation("app.cash.turbine:turbine:1.2.0")
    androidTestImplementation("app.cash.turbine:turbine:1.2.0")

    // truth
    androidTestImplementation("com.google.truth:truth:1.4.3")

    // json - 테스트환경
    testImplementation("org.json:json:20240303")

    testImplementation ("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test:core-ktx:1.6.0")
    androidTestImplementation("androidx.test:core-ktx:1.6.0")

    // splash
    implementation ("androidx.core:core-splashscreen:1.0.1")
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.12")
}
