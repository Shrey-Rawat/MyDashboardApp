package com.mydashboardapp.core.ui.accessibility

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Provides accessibility-enhanced content descriptions for UI components
 */
object AccessibilityStrings {
    const val BUTTON_PRESS = "Double tap to activate"
    const val LOADING = "Loading content, please wait"
    const val EXPANDABLE = "Double tap to expand"
    const val COLLAPSIBLE = "Double tap to collapse"
    const val SELECTED = "Selected"
    const val UNSELECTED = "Not selected"
    const val REQUIRED_FIELD = "Required field"
    const val OPTIONAL_FIELD = "Optional field"
    
    fun describePagination(currentPage: Int, totalPages: Int) = 
        "Page $currentPage of $totalPages"
    
    fun describeProgress(progress: Float) = 
        "Progress: ${(progress * 100).toInt()} percent complete"
    
    fun describeList(itemCount: Int, itemType: String = "items") = 
        "List with $itemCount $itemType"
    
    fun describeTime(hours: Int, minutes: Int) = 
        "$hours hours and $minutes minutes"
    
    fun describeDate(day: String, month: String, year: String) = 
        "$day $month $year"
}

/**
 * Calculates appropriate text scaling based on system font scale preferences
 */
@Composable
fun getAccessibleTextStyle(
    baseStyle: TextStyle,
    scaleFactor: Float = getSystemFontScale()
): TextStyle {
    return baseStyle.copy(
        fontSize = (baseStyle.fontSize.value * scaleFactor).sp,
        lineHeight = (baseStyle.lineHeight.value * scaleFactor).sp
    )
}

/**
 * Gets the current system font scale setting
 */
@Composable
fun getSystemFontScale(): Float {
    val configuration = LocalConfiguration.current
    return configuration.fontScale.coerceIn(0.85f, 2.0f)
}

/**
 * Creates a modifier with proper TalkBack support and semantic properties
 */
@Composable
fun Modifier.accessibleClickable(
    contentDescription: String,
    role: Role? = Role.Button,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true
): Modifier {
    val hapticFeedback = LocalHapticFeedback.current
    
    return this
        .semantics {
            this.contentDescription = contentDescription
            if (role != null) this.role = role
            if (!enabled) this.disabled()
        }
        .then(
            if (onLongClick != null) {
                @OptIn(ExperimentalFoundationApi::class)
                Modifier.combinedClickable(
                    enabled = enabled,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                )
            } else {
                Modifier.clickable(enabled = enabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            }
        )
}

/**
 * Provides appropriate contrast colors for better accessibility
 */
@Composable
fun getAccessibleColors(
    backgroundColor: Color = MaterialTheme.colorScheme.surface
): AccessibleColorPalette {
    val isLightBackground = backgroundColor.luminance() > 0.5f
    
    return AccessibleColorPalette(
        background = backgroundColor,
        onBackground = if (isLightBackground) Color.Black else Color.White,
        highContrast = if (isLightBackground) Color.Black else Color.White,
        mediumContrast = if (isLightBackground) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
        lowContrast = if (isLightBackground) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.6f)
    )
}

data class AccessibleColorPalette(
    val background: Color,
    val onBackground: Color,
    val highContrast: Color,
    val mediumContrast: Color,
    val lowContrast: Color
)

/**
 * Modifier to announce state changes to screen readers
 */
@Composable
fun Modifier.announceStateChange(
    stateDescription: String,
    shouldAnnounce: Boolean = true
): Modifier {
    return if (shouldAnnounce) {
        this.semantics {
            liveRegion = LiveRegionMode.Polite
            contentDescription = stateDescription
        }
    } else this
}

/**
 * Creates semantic properties for form fields
 */
@Composable
fun Modifier.formFieldSemantics(
    label: String,
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    helpText: String? = null
): Modifier {
    return this.semantics {
        contentDescription = buildString {
            append(label)
            if (isRequired) append(", required")
            if (isError && errorMessage != null) {
                append(", error: $errorMessage")
            }
            if (helpText != null) append(", $helpText")
        }
        
        if (isError) {
            error(errorMessage ?: "Invalid input")
        }
    }
}

/**
 * Creates semantic properties for lists and grids
 */
@Composable
fun Modifier.collectionSemantics(
    itemCount: Int,
    itemPosition: Int? = null,
    collectionType: String = "list"
): Modifier {
    return this.semantics {
        contentDescription = if (itemPosition != null) {
            "Item ${itemPosition + 1} of $itemCount in $collectionType"
        } else {
            "$collectionType with $itemCount items"
        }
    }
}

/**
 * Creates semantic properties for progress indicators
 */
@Composable
fun Modifier.progressSemantics(
    progress: Float,
    label: String = "Progress"
): Modifier {
    val progressPercent = (progress * 100).toInt()
    return this.semantics {
        contentDescription = "$label: $progressPercent percent complete"
        progressBarRangeInfo = ProgressBarRangeInfo(
            current = progress,
            range = 0f..1f
        )
    }
}

/**
 * Font scaling utilities for different text hierarchies
 */
@Composable
fun getScaledTextStyles(): ScaledTextStyles {
    val scaleFactor = getSystemFontScale()
    val baseTypography = MaterialTheme.typography
    
    return ScaledTextStyles(
        displayLarge = getAccessibleTextStyle(baseTypography.displayLarge, scaleFactor),
        displayMedium = getAccessibleTextStyle(baseTypography.displayMedium, scaleFactor),
        displaySmall = getAccessibleTextStyle(baseTypography.displaySmall, scaleFactor),
        headlineLarge = getAccessibleTextStyle(baseTypography.headlineLarge, scaleFactor),
        headlineMedium = getAccessibleTextStyle(baseTypography.headlineMedium, scaleFactor),
        headlineSmall = getAccessibleTextStyle(baseTypography.headlineSmall, scaleFactor),
        titleLarge = getAccessibleTextStyle(baseTypography.titleLarge, scaleFactor),
        titleMedium = getAccessibleTextStyle(baseTypography.titleMedium, scaleFactor),
        titleSmall = getAccessibleTextStyle(baseTypography.titleSmall, scaleFactor),
        bodyLarge = getAccessibleTextStyle(baseTypography.bodyLarge, scaleFactor),
        bodyMedium = getAccessibleTextStyle(baseTypography.bodyMedium, scaleFactor),
        bodySmall = getAccessibleTextStyle(baseTypography.bodySmall, scaleFactor),
        labelLarge = getAccessibleTextStyle(baseTypography.labelLarge, scaleFactor),
        labelMedium = getAccessibleTextStyle(baseTypography.labelMedium, scaleFactor),
        labelSmall = getAccessibleTextStyle(baseTypography.labelSmall, scaleFactor)
    )
}

data class ScaledTextStyles(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle
)

/**
 * Creates appropriate touch target sizes for accessibility
 */
@Composable
fun Modifier.accessibleTouchTarget(
    minSize: androidx.compose.ui.unit.Dp = 48.dp
): Modifier {
    return this.sizeIn(minWidth = minSize, minHeight = minSize)
}

/**
 * Modifier for announcing content changes to assistive technologies
 */
@Composable
fun Modifier.announceContent(
    announcement: String,
    priority: LiveRegionMode = LiveRegionMode.Polite
): Modifier {
    return this.semantics {
        liveRegion = priority
        contentDescription = announcement
    }
}

/**
 * Helper to check if accessibility services are enabled
 */
@Composable
fun isAccessibilityServiceEnabled(): Boolean {
    val context = LocalContext.current
    val accessibilityManager = remember {
        context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    }
    
    return accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled
}

/**
 * Helper to create appropriate scaling for UI elements based on accessibility settings
 */
@Composable
fun getAccessibilityScale(): Float {
    val fontScale = getSystemFontScale()
    val isAccessibilityEnabled = isAccessibilityServiceEnabled()
    
    return when {
        isAccessibilityEnabled && fontScale > 1.3f -> 1.2f
        fontScale > 1.5f -> 1.1f
        else -> 1.0f
    }
}

/**
 * Modifier that scales UI elements appropriately for accessibility
 */
@Composable
fun Modifier.accessibilityScale(): Modifier {
    val scale = getAccessibilityScale()
    return if (scale != 1.0f) this.scale(scale) else this
}
