plugins {
    id("mydashboardapp.android.feature")
}

android {
    namespace = "com.mydashboardapp.feature.inventory"
    
    buildFeatures {
        buildConfig = true
    }
    
    // Add product flavors to match the app module
    flavorDimensions += listOf("version", "auth")
    productFlavors {
        create("free") {
            dimension = "version"
            buildConfigField("boolean", "IS_PRO_VERSION", "false")
        }
        create("pro") {
            dimension = "version"
            buildConfigField("boolean", "IS_PRO_VERSION", "true")
        }
        
        create("firebase") {
            dimension = "auth"
            isDefault = true
        }
        create("stub") {
            dimension = "auth"
        }
    }
}

dependencies {
    implementation(project(":data"))
    
    // Camera and ML Kit (for barcode scanning in pro version)
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // Work Manager for background tasks
    implementation(libs.androidx.work.runtime.ktx)
    
    // Permissions
    implementation(libs.accompanist.permissions)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
