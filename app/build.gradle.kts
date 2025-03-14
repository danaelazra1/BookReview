plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id ("kotlin-kapt")

}

android {
    namespace = "com.idz.bookreview"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.idz.bookreview"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.findProperty("CLOUDINARY_CLOUD_NAME")}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.findProperty("CLOUDINARY_API_KEY")}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${project.findProperty("CLOUDINARY_API_SECRET")}\"")
            buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${project.findProperty("CLOUDINARY_UPLOAD_PRESET")}\"")
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
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.picasso:picasso:2.71828")

    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation("com.google.firebase:firebase-firestore-ktx:24.11.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.8.9")

    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

    implementation (libs.cloudinary.android)

    implementation (libs.retrofit)
    implementation (libs.gson)
    implementation (libs.converter.gson)

    implementation (libs.glide)


}