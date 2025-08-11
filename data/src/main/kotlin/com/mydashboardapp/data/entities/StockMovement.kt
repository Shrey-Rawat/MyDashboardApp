package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    indices = [Index("itemId"), Index("fromLocationId"), Index("toLocationId"), Index("timestamp")],
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["fromLocationId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["toLocationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val type: String, // e.g., "In", "Out", "Move", "Adjustment", "Consumed", "Lost", "Found"
    val quantity: Int,
    val fromLocationId: Long?, // source location (null for new items)
    val toLocationId: Long?, // destination location (null for items going out)
    val timestamp: Long,
    val reason: String?, // why this movement happened
    val reference: String?, // invoice number, order number, etc.
    val cost: Double?, // cost per unit for this movement
    val totalCost: Double?, // total cost for this quantity
    val currency: String = "USD",
    val supplier: String?, // who we got it from (for incoming items)
    val recipient: String?, // who we gave it to (for outgoing items)
    val batchNumber: String?, // batch/lot number for tracking
    val expiryDate: Long?, // for perishable items
    val condition: String?, // condition of items in this movement
    val notes: String?,
    val documentUrl: String?, // link to related document (invoice, receipt, etc.)
    val isVerified: Boolean = false, // has this movement been verified/confirmed
    val verifiedBy: String?, // who verified this movement
    val verifiedAt: Long?, // when it was verified
    val createdBy: String?, // who created this record
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
