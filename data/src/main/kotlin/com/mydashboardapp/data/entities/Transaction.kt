package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index("accountId"), Index("date"), Index("category")],
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val amount: Double, // positive for income, negative for expenses
    val currency: String = "USD",
    val description: String,
    val category: String?, // e.g., "Food", "Transport", "Entertainment", "Income"
    val subcategory: String?, // e.g., "Groceries", "Gas", "Movies"
    val date: Long, // transaction date timestamp
    val type: String, // e.g., "Debit", "Credit", "Transfer", "ATM", "Fee"
    val status: String = "Posted", // e.g., "Pending", "Posted", "Canceled"
    val merchant: String?, // merchant/payee name
    val location: String?, // transaction location
    val reference: String?, // check number, confirmation number, etc.
    val tags: String?, // comma-separated tags
    val isRecurring: Boolean = false,
    val recurringGroupId: String?, // to group recurring transactions
    val notes: String?,
    val receiptUrl: String?, // link to receipt image
    val isManual: Boolean = true, // false if imported from bank
    val externalId: String?, // ID from bank/import source
    val balanceAfter: Double?, // account balance after this transaction
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
