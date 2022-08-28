plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.xannanov.musicplayer"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(Dependencies.Android.material)
    implementation(Dependencies.Android.coreKtx)
    implementation(Dependencies.Android.appCompat)

    // Compose
//    implementation(Dependencies.Compose.ui)
//    implementation(Dependencies.Compose.material)
//    implementation(Dependencies.Compose.accompanist)
//    implementation(Dependencies.Compose.livedata)
//    implementation(Dependencies.Compose.tooling)

    // CameraX
    implementation(Dependencies.CameraX.core)
    implementation(Dependencies.CameraX.camera2)
    implementation(Dependencies.CameraX.view)
    implementation(Dependencies.CameraX.lifecycle)
    implementation(Dependencies.CameraX.extensions)
    implementation(Dependencies.CameraX.video)

    // Network
//    implementation(Dependencies.Network.OkHttp.lib)
//    implementation(Dependencies.Network.Retrofit.lib)

    // Lifecycle
    implementation(Dependencies.Lifecycle.lifecycleKtx)
//    implementation(Dependencies.Lifecycle.activityCompose)
//    implementation(Dependencies.Lifecycle.viewModelCompose)

    // Dagger
//    implementation(Dependencies.Dagger.lib)
//    kapt(Dependencies.Dagger.compiler)

    // Log
    implementation(Dependencies.Log.Timber.lib)
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation(Dependencies.Test.jUnit)
    androidTestImplementation(Dependencies.Test.androidJUnit)
    androidTestImplementation(Dependencies.Test.espresso)
}