plugins {
    id("com.android.application")
}

android {
    namespace = "com.egormit.hdmiswitch"
    buildToolsVersion = "37.0.0"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        targetSdk {
            version = release(37)
        }
        versionCode = 4
        versionName = "1.3"
    }

    flavorDimensions += "target"
    productFlavors {
        create("ps5") {
            dimension = "target"
            applicationId = "com.egormit.hdmiswitch.ps5"
        }
        create("appletv") {
            dimension = "target"
            applicationId = "com.egormit.hdmiswitch.appletv"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
