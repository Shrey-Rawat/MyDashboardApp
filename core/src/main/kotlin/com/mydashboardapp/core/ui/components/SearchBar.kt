package com.mydashboardapp.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class SearchSuggestion(
    val text: String,
    val description: String? = null,
    val icon: ImageVector? = null,
    val isHistory: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    enabled: Boolean = true,
    suggestions: List<SearchSuggestion> = emptyList(),
    recentSearches: List<String> = emptyList(),
    onSuggestionClick: ((SearchSuggestion) -> Unit)? = null,
    onClearRecentSearches: (() -> Unit)? = null,
    maxSuggestions: Int = 5,
    showSuggestionsOnFocus: Boolean = true,
    leadingIcon: ImageVector = Icons.Default.Search,
    trailingContent: (@Composable () -> Unit)? = null
) {
    var isActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Filter suggestions based on query
    val filteredSuggestions = remember(query, suggestions, recentSearches) {
        val querySuggestions = if (query.isNotBlank()) {
            suggestions.filter { 
                it.text.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true
            }
        } else {
            suggestions
        }.take(maxSuggestions)
        
        val recentItems = if (query.isBlank() && recentSearches.isNotEmpty()) {
            recentSearches.take(maxSuggestions - querySuggestions.size).map {
                SearchSuggestion(
                    text = it,
                    icon = Icons.Default.AccessTime,
                    isHistory = true
                )
            }
        } else {
            emptyList()
        }
        
        querySuggestions + recentItems
    }
    
    Column(modifier = modifier) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { searchQuery ->
                onSearch(searchQuery)
                isActive = false
                focusManager.clearFocus()
            },
            active = isActive,
            onActiveChange = { active ->
                isActive = active
                if (!active) {
                    focusManager.clearFocus()
                }
            },
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                onQueryChange("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    trailingContent?.invoke()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (showSuggestionsOnFocus && focusState.isFocused) {
                        isActive = true
                    }
                }
                .semantics {
                    contentDescription = "Search input field, $placeholder"
                }
        ) {
            // Suggestions content
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (recentSearches.isNotEmpty() && query.isBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            onClearRecentSearches?.let { clearAction ->
                                TextButton(onClick = clearAction) {
                                    Text("Clear")
                                }
                            }
                        }
                    }
                }
                
                items(filteredSuggestions) { suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        onItemClick = {
                            onSuggestionClick?.invoke(suggestion)
                            onQueryChange(suggestion.text)
                            onSearch(suggestion.text)
                            isActive = false
                            focusManager.clearFocus()
                        }
                    )
                }
                
                if (filteredSuggestions.isEmpty() && query.isNotBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No suggestions found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: SearchSuggestion,
    onItemClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (suggestion.isHistory) FontWeight.Normal else FontWeight.Medium
            )
        },
        supportingContent = suggestion.description?.let { desc ->
            {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = suggestion.icon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (suggestion.isHistory) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .semantics {
                contentDescription = if (suggestion.isHistory) {
                    "Recent search: ${suggestion.text}"
                } else {
                    "Search suggestion: ${suggestion.text}"
                }
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Compact search field, $placeholder"
            }
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    var query by remember { mutableStateOf("") }
    val sampleSuggestions = listOf(
        SearchSuggestion("Productivity Tips", "Learn how to be more productive"),
        SearchSuggestion("Time Management", "Master your time"),
        SearchSuggestion("Goal Setting", "Set and achieve your goals"),
        SearchSuggestion("Habit Tracking", "Build better habits")
    )
    val sampleRecentSearches = listOf("workout", "nutrition", "finance")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Full Search Bar", style = MaterialTheme.typography.headlineSmall)
        CustomSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { /* Handle search */ },
            placeholder = "Search productivity tips...",
            suggestions = sampleSuggestions,
            recentSearches = sampleRecentSearches,
            onClearRecentSearches = { /* Clear recent searches */ }
        )
        
        Text("Compact Search Bar", style = MaterialTheme.typography.headlineSmall)
        CompactSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { /* Handle search */ },
            placeholder = "Quick search..."
        )
    }
}
