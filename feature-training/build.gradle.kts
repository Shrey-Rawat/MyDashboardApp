plugins {
    id("mydashboardapp.android.feature")
}

android {
    namespace = "com.mydashboardapp.feature.training"
}

dependencies {
    implementation(project(":data"))
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
