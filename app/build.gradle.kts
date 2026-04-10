plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.final_project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.final_project"
        minSdk = 29
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
    buildFeatures {
        mlModelBinding = true
        viewBinding = true
    }
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    // Change 1.12.3 to 1.9.3
    implementation("androidx.activity:activity:1.9.3")
    // Change 1.17.0 to 1.15.0
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.core:core-ktx:1.15.0")
    // Change 1.0.2 to 1.0.0 (or remove this line if you aren't using Navigation Events)
    implementation("androidx.navigationevent:navigationevent-android:1.0.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)



    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.3")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4") //
    implementation("org.pytorch:pytorch_android_lite:1.13.1")
    implementation("org.pytorch:pytorch_android_torchvision_lite:1.13.1")
    implementation("com.google.mlkit:face-detection:16.1.6")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
    implementation(libs.vosk.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}