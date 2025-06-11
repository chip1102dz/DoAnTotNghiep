plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.doantotnghiep"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.doantotnghiep"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    viewBinding{
        enable = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/INDEX.LIST",
                "mozilla/public-suffix-list.txt"
            )
        }
    }
}

dependencies {
    implementation ("com.google.cloud:google-cloud-dialogflow:4.8.0")
    implementation ("io.grpc:grpc-okhttp:1.53.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.18.0")
    // Để tránh xung đột
    implementation ("androidx.annotation:annotation:1.6.0")

    // JSON parsing cho Dialogflow
    implementation ("com.google.code.gson:gson:2.10.1")

    // Async processing
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("org.greenrobot:eventbus:3.3.1")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation (libs.github.glide)
    implementation (libs.circleimageview)
    implementation (libs.circleindicator)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics.buildtools)
    annotationProcessor (libs.androidx.room.room.compiler)
    implementation (libs.androidx.room.runtime)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.material.dialogs)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}