plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.pestdetector"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pestdetector"
        minSdk = 24
        targetSdk = 34
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

    // КРОК 1: Вимикаємо стиснення TFLite (Це вирішує помилку "модель не знайдена")
    androidResources {
        noCompress += "tflite"
    }

    buildFeatures {
        mlModelBinding = false
    }
}

dependencies {
    // КРОК 2: Бібліотеки TensorFlow Lite
    // Оновлюємо до 2.16.1 (найновіша версія з підтримкою нових опкодів)
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Якщо ти залишив ці бібліотеки, теж онови їх:
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")

    // Решта твоїх стандартних бібліотек...
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}