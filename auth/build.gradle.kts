plugins {
    id("mydashboardapp.android.library")
    id("mydashboardapp.android.hilt")
}

android {
    namespace = "com.mydashboardapp.auth"
    
    buildFeatures {
        buildConfig = true
    }
    
    // Support for version flavors (free/pro) and auth flavors (firebase/stub)
    flavorDimensions += listOf("version", "auth")
    productFlavors {
        // Version flavors
        create("free") {
            dimension = "version"
        }
        create("pro") {
            dimension = "version"
        }
        
        // Auth implementation flavors
        create("firebase") {
            dimension = "auth"
        }
        create("stub") {
            dimension = "auth"
        }
    }
}

dependencies {
    implementation(project(":core"))
    
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.coroutines)
    
    // Firebase dependencies - only for firebase flavor
    "firebaseImplementation"(platform(libs.firebase.bom))
    "firebaseImplementation"(libs.firebase.auth.ktx)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.junit5)
    testImplementation(libs.mockk)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)
}
