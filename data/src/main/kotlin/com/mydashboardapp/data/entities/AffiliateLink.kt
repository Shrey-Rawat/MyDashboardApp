package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "affiliate_links",
    indices = [Index("itemId"), Index("merchant"), Index("isActive")],
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AffiliateLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val merchant: String, // e.g., "Amazon", "Best Buy", "Target"
    val originalUrl: String, // direct product URL
    val affiliateUrl: String, // URL with affiliate tracking
    val price: Double?,
    val currency: String = "USD",
    val availability: String?, // e.g., "In Stock", "Out of Stock", "Limited"
    val rating: Float?, // product rating (0.0 to 5.0)
    val reviewCount: Int?, // number of reviews
    val description: String?, // product description from merchant
    val imageUrl: String?, // product image from merchant
    val isActive: Boolean = true, // is this link still valid/active
    val isPrimary: Boolean = false, // is this the primary/recommended link
    val commission: Float?, // commission rate (0.0 to 100.0)
    val clickCount: Int = 0, // how many times this link was clicked
    val lastClickAt: Long?, // when this link was last clicked
    val lastPriceUpdate: Long?, // when price was last updated
    val priceHistory: String?, // JSON array of price changes
    val notes: String?,
    val tags: String?, // comma-separated tags
    val trackingId: String?, // affiliate tracking ID
    val campaignId: String?, // marketing campaign ID
    val couponCode: String?, // associated coupon/discount code
    val expiresAt: Long?, // when this link expires
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
