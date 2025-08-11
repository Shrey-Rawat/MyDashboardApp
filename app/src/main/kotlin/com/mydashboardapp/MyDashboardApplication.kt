package com.mydashboardapp

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyDashboardApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d("MyDashboardApp", "Application onCreate() called")
        
        // Initialize other components here
        // Sentry initialization removed temporarily
        
        Log.d("MyDashboardApp", "Application initialization completed")
    }
}
