package com.mydashboardapp.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onOnboardingComplete
            ) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(onboardingPages[page])
        }
        
        // Bottom section with indicators and navigation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(
                                width = if (isSelected) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            .animateContentSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }
                
                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < onboardingPages.size - 1) "Next" else "Get Started",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon container
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = page.backgroundColor.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = page.iconColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
        )
    }
}

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Smart Task Management",
        description = "Organize your daily tasks with intelligent prioritization, deadlines, and progress tracking. Get things done efficiently with our AI-powered task recommendations.",
        icon = Icons.Default.Checklist,
        backgroundColor = Color(0xFF6366F1),
        iconColor = Color(0xFF6366F1)
    ),
    OnboardingPage(
        title = "Nutrition Tracking",
        description = "Monitor your daily nutrition intake, track calories, macronutrients, and stay healthy. Get personalized meal recommendations based on your goals and preferences.",
        icon = Icons.Default.Restaurant,
        backgroundColor = Color(0xFF10B981),
        iconColor = Color(0xFF10B981)
    ),
    OnboardingPage(
        title = "Training Programs",
        description = "Create custom workout routines, track your fitness progress, and achieve your health goals. Access a library of exercises with proper form guidance.",
        icon = Icons.Default.FitnessCenter,
        backgroundColor = Color(0xFFF59E0B),
        iconColor = Color(0xFFF59E0B)
    ),
    OnboardingPage(
        title = "Financial Management",
        description = "Track expenses, manage budgets, and gain insights into your spending patterns. Set financial goals and receive smart saving recommendations.",
        icon = Icons.Default.AccountBalance,
        backgroundColor = Color(0xFF8B5CF6),
        iconColor = Color(0xFF8B5CF6)
    ),
    OnboardingPage(
        title = "Reporting & Analysis",
        description = "Get comprehensive insights about your productivity, health, and financial habits. Visualize your progress with beautiful charts and detailed analytics.",
        icon = Icons.Default.Analytics,
        backgroundColor = Color(0xFF06B6D4),
        iconColor = Color(0xFF06B6D4)
    ),
    OnboardingPage(
        title = "AI Integration",
        description = "Leverage the power of AI to get personalized recommendations, smart insights, and automated suggestions across all areas of your life. Your intelligent productivity assistant.",
        icon = Icons.Default.Psychology,
        backgroundColor = Color(0xFFEC4899),
        iconColor = Color(0xFFEC4899)
    )
)
