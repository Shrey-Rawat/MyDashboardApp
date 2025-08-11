package com.mydashboardapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.mydashboardapp.core.ui.theme.MyDashboardAppTheme
import com.mydashboardapp.onboarding.OnboardingScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MyDashboardAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    
    @Composable
    private fun MainScreen() {
        var isOnboardingCompleted by remember { 
            mutableStateOf(
                sharedPreferences.getBoolean("onboarding_completed", false)
            )
        }
        
        if (!isOnboardingCompleted) {
            OnboardingScreen(
                onOnboardingComplete = {
                    // Mark onboarding as completed
                    lifecycleScope.launch {
                        sharedPreferences.edit()
                            .putBoolean("onboarding_completed", true)
                            .apply()
                        isOnboardingCompleted = true
                    }
                }
            )
        } else {
            MainAppScreen()
        }
    }
}
