package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "budget_envelopes")
data class BudgetEnvelope(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g., "Groceries", "Entertainment", "Gas"
    val category: String, // e.g., "Food", "Transportation", "Entertainment"
    val monthlyLimit: Double, // monthly budget allocation
    val currentBalance: Double, // current available balance in envelope
    val spent: Double = 0.0, // amount spent this month
    val rolloverEnabled: Boolean = true, // whether unused budget rolls over
    val rolloverLimit: Double? = null, // max amount that can rollover (null = no limit)
    val priority: Int = 0, // for ordering envelopes (higher = more important)
    val color: String? = null, // hex color for UI
    val icon: String? = null, // icon identifier
    val isActive: Boolean = true,
    val notes: String? = null,
    val alertThreshold: Double? = null, // alert when spending reaches this percentage (0.0-1.0)
    val autoRefill: Boolean = true, // automatically refill to monthlyLimit each month
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_periods")
data class BudgetPeriod(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val year: Int,
    val month: Int, // 1-12
    val startDate: Long, // first day of month timestamp
    val endDate: Long, // last day of month timestamp
    val totalIncome: Double = 0.0,
    val totalAllocated: Double = 0.0,
    val totalSpent: Double = 0.0,
    val unallocated: Double = 0.0, // income not assigned to envelopes
    val isActive: Boolean = false, // true for current month
    val isClosed: Boolean = false, // true when month is finalized
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "envelope_allocations",
    indices = [Index("envelopeId"), Index("budgetPeriodId")],
    foreignKeys = [
        ForeignKey(
            entity = BudgetEnvelope::class,
            parentColumns = ["id"],
            childColumns = ["envelopeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BudgetPeriod::class,
            parentColumns = ["id"],
            childColumns = ["budgetPeriodId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EnvelopeAllocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val envelopeId: Long,
    val budgetPeriodId: Long,
    val allocatedAmount: Double, // amount allocated to envelope for this period
    val spentAmount: Double = 0.0, // amount spent from envelope this period
    val rolledOverFrom: Double = 0.0, // amount rolled over from previous period
    val rolledOverTo: Double = 0.0, // amount that will roll over to next period
    val remainingBalance: Double, // current remaining balance
    val transactionCount: Int = 0, // number of transactions in this period
    val lastTransactionDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "envelope_transactions",
    indices = [Index("envelopeId"), Index("transactionId"), Index("budgetPeriodId")],
    foreignKeys = [
        ForeignKey(
            entity = BudgetEnvelope::class,
            parentColumns = ["id"],
            childColumns = ["envelopeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BudgetPeriod::class,
            parentColumns = ["id"],
            childColumns = ["budgetPeriodId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EnvelopeTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val envelopeId: Long,
    val transactionId: Long, // reference to main transaction
    val budgetPeriodId: Long,
    val amount: Double, // amount allocated from envelope
    val type: String, // "EXPENSE", "ALLOCATION", "ROLLOVER", "ADJUSTMENT"
    val description: String? = null,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
