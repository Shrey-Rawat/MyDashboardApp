plugins {
    id("mydashboardapp.android.library")
    id("mydashboardapp.android.hilt")
    id("kotlinx-serialization")
    id("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "com.mydashboardapp.sync"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.4"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc") {
                    option("lite")
                }
                create("grpckt") {
                    option("lite")
                }
            }
            task.builtins {
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    
    // Network dependencies for cloud sync
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // gRPC and Protobuf dependencies
    implementation("io.grpc:grpc-okhttp:1.58.0")
    implementation("io.grpc:grpc-protobuf-lite:1.58.0")
    implementation("io.grpc:grpc-stub:1.58.0")
    implementation("io.grpc:grpc-kotlin-stub:1.4.0")
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.24.4")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
