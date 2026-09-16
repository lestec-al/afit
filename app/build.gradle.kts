plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.yurhel.alex.afit"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.yurhel.alex.afit"
        minSdk = 26
        targetSdk = 37
        versionCode = 37
        versionName = "37"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    packaging {
        // Need for Google/Drive auth to work ?
        resources.excludes.add("META-INF/*")
    }
    // Trying to add all languages to the bundle version of app ?
    androidResources {
        generateLocaleConfig = true
    }
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    // Graph
    implementation(libs.graphview)
    // Google/Drive auth
    implementation(libs.google.api.services.drive)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.api.client.android)
    // Test
    //testImplementation(libs.junit)
    //androidTestImplementation(libs.androidx.junit)
    //androidTestImplementation(libs.androidx.espresso.core)
    //androidTestImplementation(platform(libs.androidx.compose.bom))
    //androidTestImplementation(libs.androidx.ui.test.junit4)
    //debugImplementation(libs.androidx.ui.tooling)
    //debugImplementation(libs.androidx.ui.test.manifest)
}