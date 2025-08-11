plugins {
    id("mydashboardapp.android.library")
    id("mydashboardapp.android.hilt")
    id("kotlinx-serialization")
}

android {
    namespace = "com.mydashboardapp.core"
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.hilt.navigation.compose)
    api("androidx.compose.material:material-icons-extended")
    
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.serialization.json)
    api(libs.androidx.datastore.preferences)
    
    // Sentry - temporarily disabled due to version issues
    // api(libs.sentry.android)
    // api(libs.sentry.compose)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
