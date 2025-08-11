/**
 * JaCoCo Test Coverage Configuration
 * Configured to achieve 90% coverage target for repositories
 */

apply(plugin = "jacoco")

configure<JacocoPluginExtension> {
    toolVersion = "0.8.8"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

android {
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            it.finalizedBy("jacocoTestReport")
        }
    }
    
    buildTypes {
        debug {
            isTestCoverageEnabled = true
        }
    }
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

task<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/*\$WhenMappings.*",
        "**/*\$serializer.*",
        "**/database/**",
        "**/di/**",
        "**/*\$\$serializer.*",
        "**/*Module.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*",
        "**/*MembersInjector*.*",
        "**/*_Factory*.*",
        "**/*_Provide*Factory*.*"
    )
    
    val debugTree = fileTree("${buildDir}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
    }
    val kotlinDebugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    
    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
    
    val sourceDirs = listOf(
        "${project.projectDir}/src/main/java",
        "${project.projectDir}/src/main/kotlin"
    )
    
    sourceDirectories.setFrom(files(sourceDirs))
    executionData.setFrom(fileTree("${buildDir}/jacoco").include("**/*.exec"))
}

task<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn("jacocoTestReport")
    
    violationRules {
        rule {
            element = "CLASS"
            
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
            
            excludes = listOf(
                "*.R",
                "*.R$*",
                "*.BuildConfig",
                "*.*Test*",
                "*.di.*",
                "*.*Module",
                "*.*_Factory*",
                "*.*Dagger*",
                "*.*Hilt*"
            )
        }
        
        rule {
            element = "PACKAGE"
            includes = listOf("com.mydashboardapp.data.repository.*")
            
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

// Repository-specific coverage targets
task("repositoryCoverageReport") {
    group = "verification"
    description = "Generate coverage report specifically for repository classes"
    
    doLast {
        val reportFile = file("${buildDir}/reports/jacoco/jacocoTestReport/html/index.html")
        if (reportFile.exists()) {
            println("Repository Coverage Report generated at: ${reportFile.absolutePath}")
        }
    }
}

// Task to validate 90% coverage target
task("validateCoverage") {
    group = "verification"
    description = "Validate that repository coverage meets 90% target"
    dependsOn("jacocoTestReport")
    
    doLast {
        val reportFile = file("${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        if (reportFile.exists()) {
            val report = reportFile.readText()
            val coveragePattern = Regex("""missed="(\d+)" covered="(\d+)"""")
            val matches = coveragePattern.findAll(report)
            
            var totalMissed = 0
            var totalCovered = 0
            
            matches.forEach { match ->
                totalMissed += match.groupValues[1].toInt()
                totalCovered += match.groupValues[2].toInt()
            }
            
            val totalLines = totalMissed + totalCovered
            val coverage = if (totalLines > 0) {
                (totalCovered.toDouble() / totalLines.toDouble()) * 100
            } else {
                0.0
            }
            
            println("Repository Coverage: ${String.format("%.1f", coverage)}%")
            
            if (coverage < 90.0) {
                throw GradleException("Repository coverage (${String.format("%.1f", coverage)}%) is below 90% target")
            } else {
                println("✅ Repository coverage target (90%) achieved!")
            }
        }
    }
}
