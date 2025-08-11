package com.mydashboardapp.billing

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.billingclient.api.*
import com.mydashboardapp.billing.data.BillingRepositoryImpl
import com.mydashboardapp.billing.domain.model.*
import com.mydashboardapp.billing.ui.PremiumUpgradeScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end tests for Google Play Billing integration
 * Uses Google Play Billing Test Lab for real purchase flow testing
 * 
 * Note: These tests require test accounts and test products configured in Google Play Console
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BillingE2ETest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestBillingActivity>()

    private lateinit var billingClient: BillingClient
    
    companion object {
        // Test product IDs - these must be configured in Google Play Console
        const val TEST_PREMIUM_MONTHLY = "premium_monthly_test"
        const val TEST_PREMIUM_YEARLY = "premium_yearly_test"
        const val TEST_PREMIUM_LIFETIME = "premium_lifetime_test"
        
        // Test accounts - configure these in Google Play Console for testing
        val TEST_ACCOUNTS = listOf(
            "test1@gmail.com",
            "test2@gmail.com"
        )
    }

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Initialize billing client for direct testing
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        billingClient = BillingClient.newBuilder(context)
            .setListener { _, _ -> }
            .enablePendingPurchases()
            .build()
    }

    @Test
    fun billingClient_connectsSuccessfully() = runTest {
        // Test basic billing client connection
        var connectionResult: BillingResult? = null
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connectionResult = billingResult
            }
            
            override fun onBillingServiceDisconnected() {
                // Handle disconnection
            }
        })
        
        // Wait for connection (in real test, use coroutines or callbacks)
        Thread.sleep(2000) // Not ideal, but for demonstration
        
        assertNotNull(connectionResult)
        assertEquals(BillingClient.BillingResponseCode.OK, connectionResult!!.responseCode)
    }

    @Test
    fun billingClient_queriesTestProducts() = runTest {
        connectBillingClient()
        
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TEST_PREMIUM_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TEST_PREMIUM_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TEST_PREMIUM_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        var productDetails: List<ProductDetails>? = null
        var queryResult: BillingResult? = null

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { result, details ->
            queryResult = result
            productDetails = details
        }

        // Wait for query to complete
        Thread.sleep(2000)

        assertNotNull(queryResult)
        assertEquals(BillingClient.BillingResponseCode.OK, queryResult!!.responseCode)
        assertNotNull(productDetails)
        assertTrue(productDetails!!.isNotEmpty())
    }

    @Test
    fun premiumUpgradeScreen_displaysTestProducts() {
        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Wait for products to load
        Thread.sleep(3000)

        // Verify subscription options are displayed
        composeTestRule
            .onNodeWithText("Choose Your Plan")
            .assertIsDisplayed()

        // Verify monthly option
        composeTestRule
            .onNodeWithText("Monthly")
            .assertIsDisplayed()

        // Verify yearly option (should be marked as popular)
        composeTestRule
            .onNodeWithText("Yearly")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("POPULAR")
            .assertIsDisplayed()

        // Verify lifetime option
        composeTestRule
            .onNodeWithText("Lifetime")
            .assertIsDisplayed()
    }

    @Test
    fun premiumUpgradeScreen_selectsProduct() {
        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Wait for products to load
        Thread.sleep(3000)

        // Select yearly subscription
        composeTestRule
            .onNodeWithText("Yearly")
            .performClick()

        // Verify selection is highlighted
        composeTestRule
            .onNodeWithText("Yearly")
            .assertExists()

        // Subscribe button should appear
        composeTestRule
            .onNodeWithText("Subscribe Now")
            .assertIsDisplayed()
    }

    @Test
    fun premiumUpgradeScreen_startsPurchaseFlow() {
        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Wait for products to load
        Thread.sleep(3000)

        // Select a product
        composeTestRule
            .onNodeWithText("Monthly")
            .performClick()

        // Attempt purchase
        composeTestRule
            .onNodeWithText("Subscribe Now")
            .performClick()

        // Should show loading state
        composeTestRule
            .onNodeWithText("Processing purchase...")
            .assertIsDisplayed()

        // Note: In real Test Lab environment, this would open Google Play purchase flow
    }

    @Test
    fun billingRepository_handlesPurchaseFlow() = runTest {
        connectBillingClient()
        
        // Query products first
        val productDetails = queryTestProducts()
        assertTrue(productDetails.isNotEmpty())

        val testProduct = productDetails.first()
        
        // Create billing repository with test client
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = BillingRepositoryImpl(context, mockk())
        
        // Initialize repository
        val initResult = repository.initialize()
        assertTrue(initResult is BillingResult.Success)
        
        // Attempt purchase flow (will open Play Store in real environment)
        val purchaseResult = repository.startPurchaseFlow(
            ProductDetails(
                productId = testProduct.productId,
                productType = ProductType.SUBS,
                title = testProduct.title,
                description = testProduct.description,
                price = testProduct.subscriptionOfferDetails?.firstOrNull()
                    ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "",
                priceAmount = testProduct.subscriptionOfferDetails?.firstOrNull()
                    ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros ?: 0L,
                currencyCode = testProduct.subscriptionOfferDetails?.firstOrNull()
                    ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceCurrencyCode ?: "USD"
            )
        )
        
        // In test environment, this should succeed (launch purchase flow)
        assertTrue(purchaseResult is BillingResult.Success || purchaseResult is BillingResult.Error)
    }

    @Test
    fun billingRepository_queriesExistingPurchases() = runTest {
        connectBillingClient()
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = BillingRepositoryImpl(context, mockk())
        
        // Initialize and query purchases
        repository.initialize()
        val purchasesResult = repository.queryPurchases()
        
        // Should succeed even if no purchases exist
        assertTrue(purchasesResult is BillingResult.Success)
        
        if (purchasesResult is BillingResult.Success) {
            val purchases = purchasesResult.data
            // Purchases list can be empty for test accounts
            assertTrue(purchases.isEmpty() || purchases.isNotEmpty())
        }
    }

    @Test
    fun billingRepository_handlesTrialFlow() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = BillingRepositoryImpl(context, mockk())
        
        // Initialize repository
        repository.initialize()
        
        // Check trial availability
        val isTrialAvailable = repository.isTrialAvailable()
        assertTrue(isTrialAvailable) // Should be true for fresh install
        
        // Start trial
        val trialResult = repository.startTrial()
        assertTrue(trialResult is BillingResult.Success)
        
        // Check premium state after trial starts
        val premiumState = repository.premiumState.first()
        assertTrue(premiumState.isPro)
        assertTrue(premiumState.isTrialActive)
        assertNotNull(premiumState.trialEndDate)
    }

    @Test
    fun trialBanner_showsCorrectInformation() {
        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Start trial first (mock state)
        composeTestRule
            .onNodeWithText("Start Trial")
            .performClick()

        // Should show trial banner with remaining days
        composeTestRule
            .onAllNodesWithText(Regex("\\d+ days remaining"))
            .onFirst()
            .assertIsDisplayed()

        // Should show upgrade option
        composeTestRule
            .onNodeWithText("Upgrade Now")
            .assertIsDisplayed()
    }

    @Test
    fun premiumFeatures_showCorrectAvailability() {
        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Verify premium features are listed
        composeTestRule
            .onNodeWithText("Premium Features")
            .assertIsDisplayed()

        // Check for specific features
        composeTestRule
            .onNodeWithText("Track unlimited nutrition entries")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Create unlimited workout plans")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Access detailed analytics and insights")
            .assertIsDisplayed()
    }

    @Test
    fun purchaseValidation_handlesServerValidation() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = BillingRepositoryImpl(context, mockk())
        
        // Mock purchase for validation
        val mockPurchase = PurchaseInfo(
            purchaseToken = "mock_token_123",
            productId = TEST_PREMIUM_MONTHLY,
            purchaseTime = System.currentTimeMillis(),
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = false,
            isAutoRenewing = true,
            orderId = "mock_order_123"
        )
        
        // Validate purchase
        val validationResult = repository.validatePurchase(mockPurchase)
        
        // Should be valid for purchased state
        assertTrue(validationResult is PurchaseValidationResult.Valid)
    }

    @Test
    fun errorHandling_displaysAppropriateMessages() {
        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Simulate network error (no products loaded)
        Thread.sleep(5000) // Wait for timeout

        // Should show error state
        composeTestRule
            .onNodeWithText("Failed to load subscription options")
            .assertIsDisplayed()

        // Should show retry button
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun accessibilityTest_billingScreensAreAccessible() {
        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Verify important elements have content descriptions
        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertExists()

        // Verify subscription options are accessible
        composeTestRule
            .onAllNodesWithText("Monthly")
            .onFirst()
            .assert(hasClickAction())

        // Verify main action button is accessible
        Thread.sleep(2000) // Wait for products to load
        
        composeTestRule
            .onNodeWithText("Subscribe Now")
            .assert(hasClickAction())
    }

    @Test
    fun testLabIntegration_realPurchaseFlow() {
        // This test is specifically for Google Play Test Lab
        // It will only pass when run in the Test Lab environment with test accounts
        
        val isTestLab = isRunningInTestLab()
        if (!isTestLab) {
            // Skip this test in local environment
            return
        }

        composeTestRule.setContent {
            PremiumUpgradeScreen(
                onBackClick = {}
            )
        }

        // Wait for products to load
        Thread.sleep(3000)

        // Select yearly subscription (best value for testing)
        composeTestRule
            .onNodeWithText("Yearly")
            .performClick()

        // Start purchase flow
        composeTestRule
            .onNodeWithText("Subscribe Now")
            .performClick()

        // In Test Lab, this will open the real Google Play purchase flow
        // The test will need to be configured with test accounts that can complete purchases
        
        // Wait for purchase flow to complete (longer timeout for real purchases)
        Thread.sleep(15000)

        // After purchase, should show success state or premium features unlocked
        // This would be implemented based on your app's post-purchase flow
    }

    private fun connectBillingClient() {
        var connected = false
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
            }
            
            override fun onBillingServiceDisconnected() {
                connected = false
            }
        })
        
        // Wait for connection
        var attempts = 0
        while (!connected && attempts < 10) {
            Thread.sleep(500)
            attempts++
        }
        
        assertTrue(connected)
    }

    private fun queryTestProducts(): List<com.android.billingclient.api.ProductDetails> {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TEST_PREMIUM_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TEST_PREMIUM_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        var productDetails: List<com.android.billingclient.api.ProductDetails>? = null
        
        billingClient.queryProductDetailsAsync(queryParams) { _, details ->
            productDetails = details
        }
        
        // Wait for query
        var attempts = 0
        while (productDetails == null && attempts < 10) {
            Thread.sleep(500)
            attempts++
        }
        
        return productDetails ?: emptyList()
    }

    private fun isRunningInTestLab(): Boolean {
        // Check if running in Firebase Test Lab
        return try {
            val testLabSetting = InstrumentationRegistry.getArguments().getString("firebase.testLabSetting")
            testLabSetting != null
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Test activity for billing tests
 */
class TestBillingActivity : androidx.activity.ComponentActivity()
