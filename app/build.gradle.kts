plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.fireemu"
    compileSdk = 34
    ndkVersion = "28.2.13676358"

    lint {
        // Fire OS target; this check enforces Google Play submission policy.
        // Keep all correctness and security lint checks enabled.
        disable += "ExpiredTargetSdkVersion"
    }

    defaultConfig {
        buildConfigField("boolean", "BUNDLED_CORE", "false")
        applicationId = "com.fireemu"
        minSdk = 28
        targetSdk = 30
        versionCode = 3
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
            if (providers.gradleProperty("includeX86").orNull == "true") abiFilters += "x86_64"
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") { dimension = "distribution" }
        create("demo") {
            dimension = "distribution"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            buildConfigField("boolean", "BUNDLED_CORE", "true")
        }
    }

    signingConfigs {
        if(System.getenv("FIRESPOT_KEYSTORE")!=null) create("distribution") {
            storeFile=file(System.getenv("FIRESPOT_KEYSTORE"))
            storePassword=System.getenv("FIRESPOT_STORE_PASSWORD")
            keyAlias="firespot"
            keyPassword=System.getenv("FIRESPOT_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig=signingConfigs.findByName("distribution")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            isJniDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.tv.material3.ExperimentalTvMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("../emulator/_shared/src/main/jni/Android.mk")
        }
    }

    packaging.jniLibs.useLegacyPackaging = true

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    // AndroidX Core & Lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose for TV & Material3 TV
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore (Settings)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coil for Cover Art
    implementation("io.coil-kt:coil-compose:2.5.0")

    // WorkManager (Background ROM Scanner)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
