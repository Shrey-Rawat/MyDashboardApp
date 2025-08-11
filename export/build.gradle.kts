plugins {
    id("mydashboardapp.android.library")
    id("mydashboardapp.android.hilt")
}

android {
    namespace = "com.mydashboardapp.export"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    
    // CSV/PDF generation dependencies - add specific libraries as needed
    // implementation("com.opencsv:opencsv:5.7.1")  // For CSV export
    // implementation("com.itextpdf:itextpdf:5.5.13.3")  // For PDF export
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
