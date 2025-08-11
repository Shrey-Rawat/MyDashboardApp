package com.mydashboardapp.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    isExpandedInitially: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    animationDuration: Int = 300,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isExpandedInitially) }
    
    // Notify parent of expansion state changes
    LaunchedEffect(isExpanded) {
        onExpandedChange?.invoke(isExpanded)
    }
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "expand_icon_rotation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (isExpanded) {
                    "$title card expanded, tap to collapse"
                } else {
                    "$title card collapsed, tap to expand"
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { 
                        isExpanded = !isExpanded 
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(animationDuration)) + 
                        expandVertically(animationSpec = tween(animationDuration)),
                exit = fadeOut(animationSpec = tween(animationDuration)) + 
                       shrinkVertically(animationSpec = tween(animationDuration))
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
fun ExpandableCardGroup(
    cards: List<ExpandableCardData>,
    modifier: Modifier = Modifier,
    multipleExpansion: Boolean = true,
    animationDuration: Int = 300,
    verticalSpacing: Dp = 8.dp
) {
    var expandedStates by remember {
        mutableStateOf(
            cards.associate { it.id to it.isExpandedInitially }
        )
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        cards.forEach { cardData ->
            ExpandableCard(
                title = cardData.title,
                subtitle = cardData.subtitle,
                icon = cardData.icon,
                isExpandedInitially = expandedStates[cardData.id] ?: false,
                onExpandedChange = { isExpanded ->
                    if (!multipleExpansion && isExpanded) {
                        // Collapse all other cards
                        expandedStates = expandedStates.mapValues { false } + 
                                        (cardData.id to true)
                    } else {
                        expandedStates = expandedStates + (cardData.id to isExpanded)
                    }
                },
                enabled = cardData.enabled,
                animationDuration = animationDuration,
                content = cardData.content
            )
        }
    }
}

data class ExpandableCardData(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val isExpandedInitially: Boolean = false,
    val enabled: Boolean = true,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccordionExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    isExpandedInitially: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    animationDuration: Int = 300,
    headerContent: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isExpandedInitially) }
    
    LaunchedEffect(isExpanded) {
        onExpandedChange?.invoke(isExpanded)
    }
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "expand_icon_rotation"
    )
    
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (isExpanded) {
                    "$title accordion expanded"
                } else {
                    "$title accordion collapsed"
                }
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header with custom content support
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { 
                        isExpanded = !isExpanded 
                    },
                color = if (isExpanded) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    
                    // Custom header content
                    headerContent?.invoke(this)
                    
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(animationDuration),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(animationDuration)),
                exit = shrinkVertically(
                    animationSpec = tween(animationDuration),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(animationDuration))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableCardPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Basic Expandable Card", style = MaterialTheme.typography.headlineSmall)
        ExpandableCard(
            title = "Settings",
            subtitle = "App configuration options",
            icon = Icons.Default.ExpandMore
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dark mode: Enabled")
                Text("Notifications: On")
                Text("Auto-sync: Every hour")
            }
        }
        
        Text("Accordion Style", style = MaterialTheme.typography.headlineSmall)
        AccordionExpandableCard(
            title = "Account Details",
            subtitle = "Manage your account settings",
            headerContent = {
                Badge { Text("Pro") }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Email: user@example.com")
                Text("Plan: Professional")
                Text("Storage: 50GB used of 100GB")
                Button(onClick = {}) {
                    Text("Manage Account")
                }
            }
        }
    }
}
