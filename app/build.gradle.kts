plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.test_app"
    compileSdk = 34  // Updated to 34 for compatibility

    defaultConfig {
        applicationId = "com.example.test_app"
        minSdk = 24
        targetSdk = 34  // Updated to match compileSdk
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
}

dependencies {
    // Core Android and UI dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.7.2")
    implementation("com.android.volley:volley:1.2.1")

    // Networking and database
    implementation(libs.volley)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.sqlite:sqlite:2.1.0")

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
