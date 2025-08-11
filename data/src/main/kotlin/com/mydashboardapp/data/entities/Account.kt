package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // e.g., "Checking", "Savings", "Credit", "Investment", "Cash"
    val subtype: String?, // e.g., "High Yield Savings", "Rewards Credit Card"
    val institution: String?, // bank or financial institution name
    val accountNumber: String?, // encrypted/masked account number
    val currency: String = "USD",
    val currentBalance: Double,
    val availableBalance: Double?,
    val creditLimit: Double?, // for credit accounts
    val interestRate: Double?, // APY for savings, APR for credit
    val minimumBalance: Double?,
    val monthlyFee: Double?,
    val isActive: Boolean = true,
    val isHidden: Boolean = false,
    val color: String?, // hex color for UI
    val icon: String?, // icon identifier
    val notes: String?,
    val openedAt: Long?,
    val lastSyncAt: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
