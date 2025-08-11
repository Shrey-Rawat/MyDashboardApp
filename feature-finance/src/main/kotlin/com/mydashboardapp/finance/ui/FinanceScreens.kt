package com.mydashboardapp.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var subcategory by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedAccountId by remember { mutableLongStateOf(0L) }
    var showAccountDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAccounts()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isExpense) "Log Expense" else "Log Income") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (amount.isNotEmpty() && description.isNotEmpty() && selectedAccountId != 0L) {
                                val amountValue = amount.toDoubleOrNull() ?: 0.0
                                if (isExpense) {
                                    viewModel.logExpense(
                                        accountId = selectedAccountId,
                                        amount = amountValue,
                                        description = description,
                                        category = category.ifEmpty { "Other" },
                                        subcategory = subcategory.takeIf { it.isNotEmpty() },
                                        merchant = merchant.takeIf { it.isNotEmpty() },
                                        notes = notes.takeIf { it.isNotEmpty() }
                                    )
                                } else {
                                    viewModel.logIncome(
                                        accountId = selectedAccountId,
                                        amount = amountValue,
                                        description = description,
                                        category = category.ifEmpty { "Income" },
                                        source = merchant.takeIf { it.isNotEmpty() },
                                        notes = notes.takeIf { it.isNotEmpty() }
                                    )
                                }
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilterChip(
                            selected = isExpense,
                            onClick = { isExpense = true },
                            label = { Text("Expense") },
                            leadingIcon = { Icon(Icons.Default.TrendingDown, contentDescription = null) }
                        )
                        FilterChip(
                            selected = !isExpense,
                            onClick = { isExpense = false },
                            label = { Text("Income") },
                            leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) }
                        )
                    }
                }
            }
            
            // Amount
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Account selection
            item {
                ExposedDropdownMenuBox(
                    expanded = showAccountDropdown,
                    onExpandedChange = { showAccountDropdown = it }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAccountDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showAccountDropdown,
                        onDismissRequest = { showAccountDropdown = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.name} - ${account.type}") },
                                onClick = {
                                    selectedAccountId = account.id
                                    showAccountDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Category
            item {
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        val categories = if (isExpense) {
                            listOf("Food & Dining", "Transportation", "Shopping", "Entertainment", 
                                   "Bills & Utilities", "Healthcare", "Education", "Travel", "Other")
                        } else {
                            listOf("Salary", "Freelance", "Business Income", "Investment Returns", 
                                   "Interest", "Dividends", "Other Income")
                        }
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Subcategory
            item {
                OutlinedTextField(
                    value = subcategory,
                    onValueChange = { subcategory = it },
                    label = { Text("Subcategory (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Merchant/Source
            item {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text(if (isExpense) "Merchant (Optional)" else "Source (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEnvelopesScreen(
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val envelopes by viewModel.envelopes.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadEnvelopes()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Envelopes") },
                actions = {
                    IconButton(onClick = { /* Add envelope */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Envelope")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(envelopes) { envelope ->
                EnvelopeCard(envelope = envelope)
            }
        }
    }
}

@Composable
private fun EnvelopeCard(
    envelope: BudgetEnvelope
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = envelope.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = envelope.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: $${String.format("%.2f", envelope.spent)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Budget: $${String.format("%.2f", envelope.monthlyLimit)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val progress = if (envelope.monthlyLimit > 0) {
                envelope.spent / envelope.monthlyLimit
            } else 0.0
            
            LinearProgressIndicator(
                progress = progress.toFloat().coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    progress >= 1.0 -> MaterialTheme.colorScheme.error
                    progress >= 0.8 -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Remaining: $${String.format("%.2f", envelope.currentBalance)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentPortfolioScreen(
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val investments by viewModel.investments.collectAsStateWithLifecycle()
    val portfolioValue by viewModel.portfolioValue.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadInvestments()
        viewModel.loadPortfolioValue()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Investment Portfolio") },
                actions = {
                    IconButton(onClick = { viewModel.refreshPrices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Prices")
                    }
                    IconButton(onClick = { /* Add investment */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Investment")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Portfolio summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Portfolio Value",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "$${String.format("%.2f", portfolioValue.currentValue)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Cost Basis: $${String.format("%.2f", portfolioValue.totalCost)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Gain/Loss: $${String.format("%.2f", portfolioValue.totalGainLoss)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (portfolioValue.totalGainLoss >= 0) 
                                    Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
            
            // Investments
            items(investments) { investment ->
                InvestmentCard(investment = investment)
            }
        }
    }
}

@Composable
private fun InvestmentCard(
    investment: Investment
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = investment.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = investment.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    val currentPrice = investment.currentPrice ?: investment.purchasePrice
                    Text(
                        text = "$${String.format("%.2f", currentPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    investment.currentPrice?.let { current ->
                        val change = current - investment.purchasePrice
                        val changePercent = (change / investment.purchasePrice) * 100
                        Text(
                            text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)} (${String.format("%.1f", changePercent)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shares: ${String.format("%.4f", investment.quantity)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                val currentValue = investment.quantity * (investment.currentPrice ?: investment.purchasePrice)
                Text(
                    text = "Value: $${String.format("%.2f", currentValue)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = "Cost Basis: $${String.format("%.2f", investment.purchasePrice)} per share",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
