plugins {
    id("mydashboardapp.android.library")
    id("mydashboardapp.android.hilt")
}

android {
    namespace = "com.mydashboardapp.billing"
}

dependencies {
    implementation(project(":core"))
    
    // Google Play Billing dependencies
    implementation(libs.billing)
    
    // Unit Testing
    testImplementation(libs.bundles.junit5)
    testImplementation(libs.bundles.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    
    // Instrumented Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.hilt.navigation.compose)
    androidTestImplementation(libs.hilt.android.testing)
}
