plugins {
    id("mydashboardapp.android.feature")
}

android {
    namespace = "com.mydashboardapp.feature.ai"
}

dependencies {
    implementation(project(":data"))
    
    // Network dependencies for AI API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // SSE support for streaming responses
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    
    // Security for encrypted API key storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
