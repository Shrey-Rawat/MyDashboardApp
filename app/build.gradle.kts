plugins {
    id("mydashboardapp.android.application")
    id("mydashboardapp.android.hilt")
    // id("io.sentry.android.gradle") // Temporarily disabled
}

android {
    namespace = "com.mydashboardapp"
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    defaultConfig {
        applicationId = "com.mydashboardapp"
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    
    // Note: composeOptions is no longer needed with Compose compiler plugin
    // The plugin automatically configures the compiler settings
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.useJUnitPlatform()
            }
        }
    }

    signingConfigs {
        create("release") {
            // These will be provided via command line during CI/CD
            storeFile = file(project.findProperty("android.injected.signing.store.file") ?: "debug.keystore")
            storePassword = project.findProperty("android.injected.signing.store.password") as String? ?: "android"
            keyAlias = project.findProperty("android.injected.signing.key.alias") as String? ?: "androiddebugkey"
            keyPassword = project.findProperty("android.injected.signing.key.password") as String? ?: "android"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Use release signing config if available, otherwise fall back to debug
            signingConfig = if (project.hasProperty("android.injected.signing.store.file")) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            
            // Additional release optimizations
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
        }
    }

    flavorDimensions += listOf("version", "auth")
    productFlavors {
        create("free") {
            dimension = "version"
            applicationIdSuffix = ".free"
            versionNameSuffix = "-free"
            
            buildConfigField("boolean", "IS_PRO_VERSION", "false")
            buildConfigField("String", "SENTRY_DSN", "\"https://a72855998b3e174755611c78ee92d6af@o4509820996419584.ingest.de.sentry.io/4509821671047248\"")
            resValue("string", "app_name", "Best Productivity App Free")
        }
        create("pro") {
            dimension = "version"
            applicationIdSuffix = ".pro"
            versionNameSuffix = "-pro"
            
            buildConfigField("boolean", "IS_PRO_VERSION", "true")
            buildConfigField("String", "SENTRY_DSN", "\"https://a72855998b3e174755611c78ee92d6af@o4509820996419584.ingest.de.sentry.io/4509821671047248\"")
            resValue("string", "app_name", "Best Productivity App Pro")
        }
        
        // Auth implementation flavors - use Firebase by default
        create("firebase") {
            dimension = "auth"
            isDefault = true
        }
        create("stub") {
            dimension = "auth"
        }
    }
}

// Sentry config - temporarily disabled
/*
sentry {
    // Organization and project
    org.set("shray")
    projectName.set("mydashboardapp")
    authToken.set(System.getenv("SENTRY_AUTH_TOKEN"))

    // Source context
    includeSourceContext.set(true)
    
    // ProGuard mapping
    includeProguardMapping.set(true)
    autoUploadProguardMapping.set(true)
    
    // Auto installation for releases only
    autoInstallation {
        enabled.set(true)
    }
}
*/

dependencies {
    implementation(project(":core"))
    // Temporarily disabled until compilation issues are fixed  
    // implementation(project(":auth"))
    // implementation(project(":billing"))
    // implementation(project(":feature-nutrition"))
    // implementation(project(":feature-training"))
    // implementation(project(":feature-productivity"))
    // implementation(project(":feature-finance"))
    // implementation(project(":feature-inventory"))
    // implementation(project(":feature-ai"))
    
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Core libraries - Room temporarily disabled
    // implementation(libs.bundles.room)
    // kapt(libs.room.compiler)
    implementation(libs.bundles.coroutines)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.bundles.accompanist)
    
    // Sentry - temporarily disabled due to version issues  
    // implementation(libs.sentry.android)
    // implementation(libs.sentry.compose)
    
    // Testing - Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.junit5)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    
    // Testing - Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    
    // Debug
    debugImplementation(libs.bundles.compose.debug)
}
