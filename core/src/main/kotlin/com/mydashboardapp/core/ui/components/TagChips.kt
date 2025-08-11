package com.mydashboardapp.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class Tag(
    val id: String,
    val label: String,
    val color: Color? = null,
    val icon: ImageVector? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableTagChip(
    tag: Tag,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) {
            tag.color ?: MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "container_color_animation"
    )
    
    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "content_color_animation"
    )
    
    FilterChip(
        onClick = { onSelectionChanged(!isSelected) },
        label = { 
            Text(
                text = tag.label,
                color = animatedContentColor
            )
        },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = animatedContentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else if (tag.icon != null) {
            {
                Icon(
                    imageVector = tag.icon,
                    contentDescription = null,
                    tint = animatedContentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = animatedContainerColor,
            labelColor = animatedContentColor,
            selectedContainerColor = animatedContainerColor,
            selectedLabelColor = animatedContentColor
        ),
        enabled = enabled,
        modifier = modifier.semantics {
            contentDescription = if (isSelected) {
                "${tag.label} tag selected"
            } else {
                "${tag.label} tag not selected"
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissibleTagChip(
    tag: Tag,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    InputChip(
        onClick = { /* Optional click action */ },
        label = { 
            Text(
                text = tag.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        selected = false,
        leadingIcon = if (tag.icon != null) {
            {
                Icon(
                    imageVector = tag.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        trailingIcon = {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove ${tag.label}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = tag.color ?: MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = enabled,
        modifier = modifier.semantics {
            contentDescription = "${tag.label} tag, swipe or tap close to remove"
        }
    )
}

@Composable
fun TagChipGroup(
    tags: List<Tag>,
    selectedTags: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = true,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tags) { tag ->
            SelectableTagChip(
                tag = tag,
                isSelected = selectedTags.contains(tag.id),
                onSelectionChanged = { isSelected ->
                    if (multiSelect) {
                        val newSelection = if (isSelected) {
                            selectedTags + tag.id
                        } else {
                            selectedTags - tag.id
                        }
                        onSelectionChanged(newSelection)
                    } else {
                        val newSelection = if (isSelected) {
                            setOf(tag.id)
                        } else {
                            emptySet()
                        }
                        onSelectionChanged(newSelection)
                    }
                },
                enabled = enabled
            )
        }
    }
}

@Composable
fun DismissibleTagChipGroup(
    tags: List<Tag>,
    onTagDismiss: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tags) { tag ->
            DismissibleTagChip(
                tag = tag,
                onDismiss = { onTagDismiss(tag) },
                enabled = enabled
            )
        }
    }
}

@Composable
fun FlowTagChipGroup(
    tags: List<Tag>,
    selectedTags: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = true,
    enabled: Boolean = true
) {
    // Flow layout for tags that wrap to multiple rows
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var currentRowTags = mutableListOf<Tag>()
        var currentRowWidth = 0
        val maxWidth = 300 // Approximate max width in dp
        
        tags.forEach { tag ->
            val tagWidth = tag.label.length * 8 + 50 // Approximate tag width
            
            if (currentRowWidth + tagWidth > maxWidth && currentRowTags.isNotEmpty()) {
                // Start new row
                TagRow(
                    tags = currentRowTags.toList(),
                    selectedTags = selectedTags,
                    onSelectionChanged = onSelectionChanged,
                    multiSelect = multiSelect,
                    enabled = enabled
                )
                currentRowTags.clear()
                currentRowWidth = 0
            }
            
            currentRowTags.add(tag)
            currentRowWidth += tagWidth
        }
        
        // Add remaining tags
        if (currentRowTags.isNotEmpty()) {
            TagRow(
                tags = currentRowTags.toList(),
                selectedTags = selectedTags,
                onSelectionChanged = onSelectionChanged,
                multiSelect = multiSelect,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun TagRow(
    tags: List<Tag>,
    selectedTags: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    multiSelect: Boolean,
    enabled: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tags.forEach { tag ->
            SelectableTagChip(
                tag = tag,
                isSelected = selectedTags.contains(tag.id),
                onSelectionChanged = { isSelected ->
                    if (multiSelect) {
                        val newSelection = if (isSelected) {
                            selectedTags + tag.id
                        } else {
                            selectedTags - tag.id
                        }
                        onSelectionChanged(newSelection)
                    } else {
                        val newSelection = if (isSelected) {
                            setOf(tag.id)
                        } else {
                            emptySet()
                        }
                        onSelectionChanged(newSelection)
                    }
                },
                enabled = enabled
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagChipsPreview() {
    var selectedTags by remember { mutableStateOf(setOf("1", "3")) }
    var dismissibleTags by remember { 
        mutableStateOf(listOf(
            Tag("d1", "Work"),
            Tag("d2", "Personal"),
            Tag("d3", "Important")
        ))
    }
    
    val sampleTags = listOf(
        Tag("1", "Technology", Color.Blue),
        Tag("2", "Design", Color.Green),
        Tag("3", "Business", Color.Red),
        Tag("4", "Health", Color.Yellow),
        Tag("5", "Education", Color.Magenta)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Selectable Tags", style = MaterialTheme.typography.headlineSmall)
        TagChipGroup(
            tags = sampleTags,
            selectedTags = selectedTags,
            onSelectionChanged = { selectedTags = it }
        )
        
        Text("Dismissible Tags", style = MaterialTheme.typography.headlineSmall)
        DismissibleTagChipGroup(
            tags = dismissibleTags,
            onTagDismiss = { tag ->
                dismissibleTags = dismissibleTags - tag
            }
        )
        
        Text("Flow Layout Tags", style = MaterialTheme.typography.headlineSmall)
        FlowTagChipGroup(
            tags = sampleTags,
            selectedTags = selectedTags,
            onSelectionChanged = { selectedTags = it }
        )
    }
}
