plugins {
    alias(libs.plugins.android.application)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    viewBinding{
        enable = true
    }
}

dependencies {
    implementation (libs.github.glide)
    implementation (libs.circleimageview)
    implementation (libs.circleindicator)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.firebase.database)
    annotationProcessor (libs.androidx.room.room.compiler)
    implementation (libs.androidx.room.runtime)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}