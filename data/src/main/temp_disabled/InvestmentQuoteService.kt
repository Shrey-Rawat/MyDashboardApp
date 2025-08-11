package com.mydashboardapp.data.service

import com.mydashboardapp.data.entities.Investment
import com.mydashboardapp.data.entities.PriceSnapshot
import com.mydashboardapp.data.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentQuoteService @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    
    /**
     * Update investment prices from user-supplied REST endpoint
     */
    suspend fun updatePricesFromEndpoint(
        endpointUrl: String,
        symbols: List<String>,
        apiKey: String? = null,
        headers: Map<String, String> = emptyMap()
    ): QuoteUpdateResult = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<QuoteResult>()
            val errors = mutableListOf<String>()
            
            for (symbol in symbols) {
                try {
                    val quote = fetchQuoteFromEndpoint(endpointUrl, symbol, apiKey, headers)
                    quote?.let {
                        financeRepository.updateInvestmentPrice(
                            symbol = symbol,
                            price = it.price,
                            source = it.source
                        )
                        results.add(QuoteResult(symbol, it.price, true))
                    } ?: run {
                        errors.add("No quote data found for $symbol")
                        results.add(QuoteResult(symbol, 0.0, false))
                    }
                } catch (e: Exception) {
                    errors.add("Failed to update $symbol: ${e.message}")
                    results.add(QuoteResult(symbol, 0.0, false))
                }
            }
            
            QuoteUpdateResult(
                success = errors.isEmpty(),
                updatedCount = results.count { it.success },
                totalCount = symbols.size,
                results = results,
                errors = errors
            )
            
        } catch (e: Exception) {
            QuoteUpdateResult(
                success = false,
                errors = listOf("Failed to update prices: ${e.message}")
            )
        }
    }
    
    /**
     * Fetch single quote from REST endpoint
     */
    private suspend fun fetchQuoteFromEndpoint(
        endpointUrl: String,
        symbol: String,
        apiKey: String? = null,
        headers: Map<String, String> = emptyMap()
    ): Quote? = withContext(Dispatchers.IO) {
        try {
            val url = buildQuoteUrl(endpointUrl, symbol, apiKey)
            val connection = URL(url).openConnection() as HttpURLConnection
            
            // Set headers
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseQuoteResponse(response, symbol)
            } else {
                null
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Build quote URL based on endpoint format
     */
    private fun buildQuoteUrl(endpointUrl: String, symbol: String, apiKey: String?): String {
        var url = endpointUrl
        
        // Replace common placeholders
        url = url.replace("{symbol}", symbol)
        url = url.replace("{SYMBOL}", symbol.uppercase())
        
        // Add API key if provided
        apiKey?.let { key ->
            val separator = if (url.contains("?")) "&" else "?"
            url += "${separator}apikey=$key"
        }
        
        return url
    }
    
    /**
     * Parse quote response from various common formats
     */
    private fun parseQuoteResponse(response: String, symbol: String): Quote? {
        return try {
            val jsonObject = JSONObject(response)
            
            // Try different response formats
            val price = when {
                // Alpha Vantage format
                jsonObject.has("Global Quote") -> {
                    val globalQuote = jsonObject.getJSONObject("Global Quote")
                    globalQuote.optString("05. price", "0").toDoubleOrNull()
                }
                // Yahoo Finance format
                jsonObject.has("quoteSummary") -> {
                    val quoteSummary = jsonObject.getJSONObject("quoteSummary")
                    val result = quoteSummary.getJSONArray("result").getJSONObject(0)
                    val price = result.getJSONObject("price")
                    price.optDouble("regularMarketPrice", 0.0)
                }
                // IEX Cloud format
                jsonObject.has("latestPrice") -> {
                    jsonObject.optDouble("latestPrice", 0.0)
                }
                // Finnhub format
                jsonObject.has("c") -> {
                    jsonObject.optDouble("c", 0.0) // Current price
                }
                // Generic format
                jsonObject.has("price") -> {
                    jsonObject.optDouble("price", 0.0)
                }
                // Array format (first element)
                response.trim().startsWith("[") -> {
                    val array = org.json.JSONArray(response)
                    if (array.length() > 0) {
                        val firstItem = array.getJSONObject(0)
                        firstItem.optDouble("price", firstItem.optDouble("close", 0.0))
                    } else 0.0
                }
                else -> {
                    // Try to find any numeric field that might be price
                    findPriceInJson(jsonObject)
                }
            }
            
            price?.let { p ->
                if (p > 0) {
                    Quote(
                        symbol = symbol,
                        price = p,
                        timestamp = System.currentTimeMillis(),
                        source = "REST Endpoint"
                    )
                } else null
            }
            
        } catch (e: Exception) {
            // Try parsing as simple numeric value
            response.trim().toDoubleOrNull()?.let { price ->
                if (price > 0) {
                    Quote(
                        symbol = symbol,
                        price = price,
                        timestamp = System.currentTimeMillis(),
                        source = "REST Endpoint"
                    )
                } else null
            }
        }
    }
    
    /**
     * Try to find price in JSON by looking for common field names
     */
    private fun findPriceInJson(jsonObject: JSONObject): Double? {
        val priceFields = listOf(
            "price", "last", "lastPrice", "close", "regularMarketPrice",
            "current", "currentPrice", "marketPrice", "quote", "value"
        )
        
        priceFields.forEach { field ->
            if (jsonObject.has(field)) {
                val value = jsonObject.optDouble(field, -1.0)
                if (value > 0) return value
            }
        }
        
        return null
    }
    
    /**
     * Get supported endpoint templates
     */
    fun getSupportedEndpointTemplates(): List<EndpointTemplate> {
        return listOf(
            EndpointTemplate(
                name = "Alpha Vantage",
                url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apikey}",
                requiresApiKey = true,
                description = "Real-time stock quotes from Alpha Vantage"
            ),
            EndpointTemplate(
                name = "IEX Cloud", 
                url = "https://cloud.iexapis.com/stable/stock/{symbol}/quote?token={apikey}",
                requiresApiKey = true,
                description = "Real-time and historical stock data"
            ),
            EndpointTemplate(
                name = "Finnhub",
                url = "https://finnhub.io/api/v1/quote?symbol={symbol}&token={apikey}",
                requiresApiKey = true,
                description = "Real-time stock price updates"
            ),
            EndpointTemplate(
                name = "Custom JSON",
                url = "https://your-api.com/quote/{symbol}",
                requiresApiKey = false,
                description = "Custom endpoint returning JSON with price field"
            ),
            EndpointTemplate(
                name = "Simple Number",
                url = "https://your-api.com/price/{symbol}",
                requiresApiKey = false,
                description = "Custom endpoint returning plain number price"
            )
        )
    }
    
    /**
     * Test endpoint connectivity
     */
    suspend fun testEndpoint(
        endpointUrl: String,
        testSymbol: String = "AAPL",
        apiKey: String? = null,
        headers: Map<String, String> = emptyMap()
    ): TestResult = withContext(Dispatchers.IO) {
        try {
            val quote = fetchQuoteFromEndpoint(endpointUrl, testSymbol, apiKey, headers)
            TestResult(
                success = quote != null,
                price = quote?.price,
                message = if (quote != null) "Successfully fetched quote for $testSymbol" else "No quote data received"
            )
        } catch (e: Exception) {
            TestResult(
                success = false,
                message = "Failed to connect: ${e.message}"
            )
        }
    }
}

data class Quote(
    val symbol: String,
    val price: Double,
    val timestamp: Long,
    val source: String,
    val high: Double? = null,
    val low: Double? = null,
    val open: Double? = null,
    val volume: Long? = null,
    val change: Double? = null,
    val changePercent: Double? = null
)

data class QuoteResult(
    val symbol: String,
    val price: Double,
    val success: Boolean
)

data class QuoteUpdateResult(
    val success: Boolean,
    val updatedCount: Int = 0,
    val totalCount: Int = 0,
    val results: List<QuoteResult> = emptyList(),
    val errors: List<String> = emptyList()
)

data class EndpointTemplate(
    val name: String,
    val url: String,
    val requiresApiKey: Boolean,
    val description: String
)

data class TestResult(
    val success: Boolean,
    val price: Double? = null,
    val message: String
)
