import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }
            
            // Configure Kotlin compiler options for JVM projects
            configureKotlinJvm()
            
            // Configure common test dependencies for all jvm modules
            dependencies {
                add("testImplementation", kotlin("test"))
            }
        }
    }
}
