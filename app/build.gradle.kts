plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

android {
    namespace = "com.ecosphere.partner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ecosphere.partner"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {

        }
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    ndkVersion = "29.0.14206865"

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.android.material:material:1.12.0")

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.gson)
    implementation(libs.gson.converter)

    implementation(libs.sdp)
    implementation(libs.ssp)

    implementation(libs.play.services.location)

    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation(libs.play.services.maps)
    implementation(libs.google.maps.utils)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.androidx.paging.runtime.ktx)

    implementation(libs.shimmer)

    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.camera.mlkit)

    implementation(libs.mlkit.barcode)

    implementation(libs.guava)
    implementation(libs.lottie)

    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
}