plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Firebase Google Services plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.qurandaily"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.qurandaily"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ------------------------
    // 🔥 Firebase (via BoM)
    // ------------------------
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Auth
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firestore (bookmarks)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Storage (if you need later)
    implementation("com.google.firebase:firebase-storage-ktx")

    // Cloud Messaging (if you use FCM)
    implementation("com.google.firebase:firebase-messaging-ktx")

    // ------------------------
    // Networking / Coroutines
    // ------------------------
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
