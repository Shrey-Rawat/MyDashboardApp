package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val category: String?, // e.g., "Electronics", "Books", "Clothing", "Food"
    val subcategory: String?, // more specific categorization
    val brand: String?,
    val model: String?,
    val sku: String?, // Stock Keeping Unit
    val barcode: String?, // UPC, EAN, etc.
    val color: String?,
    val size: String?,
    val weight: Double?, // in grams
    val dimensions: String?, // e.g., "10x5x2 cm"
    val material: String?,
    val purchasePrice: Double?,
    val currentValue: Double?,
    val currency: String = "USD",
    val purchaseDate: Long?,
    val warrantyExpiry: Long?,
    val condition: String?, // e.g., "New", "Good", "Fair", "Poor"
    val serialNumber: String?,
    val notes: String?,
    val tags: String?, // comma-separated tags
    val imageUrl: String?,
    val manualUrl: String?, // link to manual/documentation
    val isConsumable: Boolean = false, // true for items that get used up
    val minimumStock: Int?, // alert when quantity goes below this
    val maximumStock: Int?, // optimal maximum quantity
    val reorderPoint: Int?, // when to reorder
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
