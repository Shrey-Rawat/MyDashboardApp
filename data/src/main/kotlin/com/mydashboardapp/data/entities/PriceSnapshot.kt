package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_snapshots",
    indices = [Index("investmentId"), Index("timestamp"), Index("symbol")],
    foreignKeys = [
        ForeignKey(
            entity = Investment::class,
            parentColumns = ["id"],
            childColumns = ["investmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PriceSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val investmentId: Long?,
    val symbol: String, // allows tracking prices even without owning the investment
    val price: Double,
    val currency: String = "USD",
    val timestamp: Long, // when this price was recorded
    val volume: Long?, // trading volume
    val high: Double?, // high price for the period
    val low: Double?, // low price for the period
    val open: Double?, // opening price for the period
    val close: Double?, // closing price for the period (same as price for real-time)
    val change: Double?, // price change from previous period
    val changePercent: Double?, // percentage change from previous period
    val marketCap: Long?, // market capitalization at this time
    val peRatio: Double?, // price to earnings ratio
    val dividendYield: Double?, // dividend yield at this time
    val source: String?, // e.g., "Yahoo Finance", "Alpha Vantage", "Manual"
    val isRealTime: Boolean = false,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
)
