package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "investments",
    indices = [Index("accountId"), Index("symbol")],
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Investment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val symbol: String, // stock ticker symbol, fund symbol, etc.
    val name: String, // full name of the investment
    val type: String, // e.g., "Stock", "ETF", "Mutual Fund", "Bond", "Crypto"
    val quantity: Double, // number of shares/units owned
    val purchasePrice: Double, // average cost basis per share
    val currentPrice: Double?, // current market price per share
    val currency: String = "USD",
    val sector: String?, // e.g., "Technology", "Healthcare", "Finance"
    val industry: String?, // more specific than sector
    val exchange: String?, // e.g., "NYSE", "NASDAQ", "TSX"
    val purchaseDate: Long?, // when first purchased
    val lastPriceUpdate: Long?, // when price was last updated
    val dividendYield: Double?, // annual dividend yield percentage
    val expenseRatio: Double?, // for funds
    val beta: Double?, // volatility measure
    val peRatio: Double?, // price to earnings ratio
    val marketCap: Long?, // market capitalization
    val notes: String?,
    val tags: String?, // comma-separated tags
    val isWatchlist: Boolean = false, // tracking without owning
    val alertPriceHigh: Double?, // alert when price goes above this
    val alertPriceLow: Double?, // alert when price goes below this
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
