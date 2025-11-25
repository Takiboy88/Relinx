import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

// Load API keys from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.takitareq.linkbox"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.takitareq.linkbox"
        minSdk = 24
        targetSdk = 35
        versionCode = 18
        versionName = "1.8.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add Safe Browsing API key to BuildConfig
        buildConfigField("String", "SAFE_BROWSING_API_KEY", 
            "\"${localProperties.getProperty("safeBrowsingApiKey", "")}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../relinx-release-key.keystore")
            storePassword = "relinx2025"
            keyAlias = "relinx"
            keyPassword = "relinx2025"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    lint {
        checkReleaseBuilds = false
        // Or ignore this specific warning
        disable.add("NotificationPermission")
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("io.coil-kt:coil:2.6.0")
    
    // Room database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Image loading for favicons
    // Lightweight image loading (no external storage access)
    // Temporarily removed Glide to test Google Play permission detection
    // implementation("com.github.bumptech.glide:glide:4.15.1")
    // kapt("com.github.bumptech.glide:compiler:4.15.1")
    
    // JSON handling for export/import
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Google AdMob
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    
    // FlexboxLayoutManager for flexible layouts
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    
    // Retrofit for Safe Browsing API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}

