object Dependencies {

    object Android {
        const val coreKtx = "androidx.core:core-ktx:1.8.0"
        const val appCompat = "androidx.appcompat:appcompat:1.4.2"
        const val material = "com.google.android.material:material:1.6.1"
    }

    object Coroutines {
        private const val version = "1.3.9"

        const val lib = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object Lifecycle {
        private const val version = "2.4.0"
        const val lifecycleKtx = "androidx.lifecycle:lifecycle-runtime-ktx:2.3.1"
        const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"
        const val activityCompose = "androidx.activity:activity-compose:1.3.1"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
        const val kapt = "androidx.lifecycle:lifecycle-compiler:$version"
        const val java8 = "androidx.lifecycle:lifecycle-common-java8:$version"
    }

    object CameraX {
        private const val version = "1.1.0"

        const val core = "androidx.camera:camera-core:$version"
        const val camera2 = "androidx.camera:camera-camera2:$version"
        const val lifecycle = "androidx.camera:camera-lifecycle:$version"
        const val view = "androidx.camera:camera-view:$version"
        const val extensions = "androidx.camera:camera-extensions:$version"
        const val video = "androidx.camera:camera-video:$version"
    }

    object Compose {
        private const val version = "1.1.1"

        const val ui = "androidx.compose.ui:ui:$version"
        const val material = "androidx.compose.material:material:$version"
        const val tooling = "androidx.compose.ui:ui-tooling-preview:$version"
        const val livedata = "androidx.compose.runtime:runtime-livedata:$version"

        const val toolingTest = "androidx.compose.ui:ui-tooling:$version"
        const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"

        const val accompanist = "com.google.accompanist:accompanist-systemuicontroller:0.18.0"
    }

    object Network {

        object OkHttp {
            private const val version = "4.10.0"

            const val lib = "com.squareup.okhttp3:okhttp:$version"
        }

        object Retrofit {
            private const val version = "2.9.0"

            const val lib = "com.squareup.retrofit2:retrofit:$version"
        }
    }

    object Dagger {
        private const val version = "2.43.1"

        const val lib = "com.google.dagger:dagger:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
    }

    object Log {
        object Timber {
            private const val version = "5.0.1"

            const val lib = "com.jakewharton.timber:timber:$version"
        }
    }

    object Test {
        const val jUnit = "junit:junit:4.+"
        const val androidJUnit = "androidx.test.ext:junit:1.1.2"
        const val espresso = "androidx.test.espresso:espresso-core:3.3.0"
    }
}