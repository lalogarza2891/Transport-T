plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.transport_t"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.transport_t"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("org.slf4j:slf4j-simple:1.7.25")
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("com.google.android.libraries.places:places:2.4.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    testImplementation("junit:junit:4.13.2")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.fragment:fragment-ktx:1.6.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.maps:google-maps-services:2.1.2")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}
