@file:Suppress("unused")

package com.example.app.ads.helper.purchase

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import com.android.billingclient.api.*
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.logW
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.RawValue
import org.jetbrains.annotations.NotNull
import java.util.*

object ProductPurchaseHelper {

    class ProductInfo(
        @SerializedName("id")
        val id: String,
        @SerializedName("formatted_price")
        val formattedPrice: String,
        @SerializedName("price_amount_micros")
        val priceAmountMicros: Long,
        @SerializedName("price_currency_code")
        val priceCurrencyCode: String,
        @SerializedName("billing_period")
        val billingPeriod: String,
        @SerializedName("free_trial_period")
        val freeTrialPeriod: String,
        @SerializedName("product_detail")
        val productDetail: ProductDetails
    )

    private val TAG: String = javaClass.simpleName

    private val lifeTimeProductKeyList: ArrayList<String> = ArrayList()
    private val subscriptionKeyList: ArrayList<String> = ArrayList()
    private val PRODUCT_LIST: ArrayList<ProductInfo> = ArrayList()

    private var mPurchaseListener: ProductPurchaseListener? =
        null // Callback for listen purchase states
    private var mBillingClient: BillingClient? = null // Object of BillingClient

    private var isConsumable: Boolean =
        false // Flag if purchase need to consume so user can buy again

    // variable to track event time
    private var mLastClickTime: Long = 0
    private const val mMinDuration = 1000

    private val Int.getPurchaseState: String
        get() {
            return when (this) {
                Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                    "UNSPECIFIED_STATE"
                }

                Purchase.PurchaseState.PURCHASED -> {
                    "PURCHASED"
                }

                Purchase.PurchaseState.PENDING -> {
                    "PENDING"
                }

                else -> {
                    "Unknown"
                }
            }
        }

    val String.getProductInfo: ProductInfo?
        get() {
            return PRODUCT_LIST.find { it.id.equals(this, true) }
        }

    private val String.getPriceInDouble: Double
        get() {
            return if (this.isNotEmpty() && !(this.equals("Not Found", ignoreCase = false))) {
                this.replace("""[^0-9.]""".toRegex(), "").toDouble()
            } else {
                0.0
            }
        }

    //<editor-fold desc="set Product Keys">
    internal fun setLifeTimeProductKey(vararg keys: String) {
        lifeTimeProductKeyList.removeAll(lifeTimeProductKeyList.toSet())
        lifeTimeProductKeyList.clear()
        lifeTimeProductKeyList.addAll(keys.filter { it.isNotEmpty() })
    }

    internal fun setSubscriptionKey(vararg keys: String) {
        subscriptionKeyList.removeAll(subscriptionKeyList.toSet())
        subscriptionKeyList.clear()
        subscriptionKeyList.addAll(keys.filter { it.isNotEmpty() })
    }

    internal fun addOtherSubscriptionKey(keys: String) {
        if (!subscriptionKeyList.contains(keys))
            subscriptionKeyList.add(keys)
    }
    //</editor-fold>

    //<editor-fold desc="init Billing Related Data">
    fun initBillingClient(context: Context, purchaseListener: ProductPurchaseListener) {
        mPurchaseListener = purchaseListener

        mBillingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                if (SystemClock.elapsedRealtime() - mLastClickTime < mMinDuration) {
                    return@setListener
                } else {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            if (purchases != null) {
                                for (purchase in purchases) {
                                    purchase?.let {
                                        handlePurchase(context, it)
                                    }
                                }
                            } else {
                                logI(
                                    tag = TAG,
                                    message = "onPurchasesUpdated: =>> Response OK But Purchase List Null Found"
                                )
                            }
                        }

                        else -> {
                            when (billingResult.responseCode) {
                                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
                                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                                BillingClient.BillingResponseCode.USER_CANCELED -> {
                                    if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                                        Toast.makeText(
                                            context,
                                            "You've cancelled the Google play billing process",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                else -> {
                                    Toast.makeText(
                                        context,
                                        "Item not found or Google play billing error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            logResponseCode(
                                responseMsg = "onPurchasesUpdated: ",
                                billingResult = billingResult
                            )
                        }
                    }
                }
            }
            .build()

        mBillingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                logI(tag = TAG, message = "onBillingServiceDisconnected: =>> DISCONNECTED")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                logResponseCode("onBillingSetupFinished: ", billingResult)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    || billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
                ) {
                    mPurchaseListener?.onBillingSetupFinished(billingResult)
                }
            }
        })
    }

    fun initProductsKeys(context: Context, onInitializationComplete: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            initProducts(
                context = context,
                onComplete = {
                    CoroutineScope(Dispatchers.Main).launch {
                        initSubscription(
                            context = context,
                            onComplete = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    onInitializationComplete.invoke()
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    private suspend fun initProducts(context: Context, onComplete: () -> Unit) {
        if (lifeTimeProductKeyList.isNotEmpty()) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    lifeTimeProductKeyList.map { productId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    }
                )
                .build()

            initProductParams(
                context = context,
                methodName = "initProducts",
                params = params,
                productType = BillingClient.ProductType.INAPP,
                onComplete = onComplete
            )
        } else {
            onComplete.invoke()
        }
    }

    private suspend fun initSubscription(context: Context, onComplete: () -> Unit) {
        if (subscriptionKeyList.isNotEmpty()) {
            val historyParams = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            if (mBillingClient != null && mBillingClient?.isReady == true) {
                mBillingClient?.let { billingClient ->
                    val purchasesHistoryResult = billingClient.queryPurchaseHistory(historyParams)
                    if (purchasesHistoryResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchasesHistoryResult.purchaseHistoryRecordList?.let { listOfHistoryProducts ->
                            val idList =
                                listOfHistoryProducts.flatMap { it.products } as ArrayList<String>
                            initSubscription(
                                context = context,
                                historyList = idList,
                                onComplete = onComplete
                            )
                        } ?: initSubscription(
                            context = context,
                            historyList = ArrayList(),
                            onComplete = onComplete
                        )
                    } else {
                        initSubscription(
                            context = context,
                            historyList = ArrayList(),
                            onComplete = onComplete
                        )
                    }
                }
            } else {
                Log.e(TAG, "initSubscription: =>> The billing client is not ready")
                onComplete.invoke()
            }
        } else {
            onComplete.invoke()
        }
    }

    private suspend fun initSubscription(
        context: Context,
        historyList: ArrayList<String>,
        onComplete: () -> Unit
    ) {
        if (subscriptionKeyList.isNotEmpty()) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    subscriptionKeyList.map { productId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    }
                )
                .build()

            initProductParams(
                context = context,
                methodName = "initSubscription",
                params = params,
                productType = BillingClient.ProductType.SUBS,
                historyList = historyList,
                onComplete = onComplete
            )
        } else {
            onComplete.invoke()
        }
    }

    private suspend fun initProductParams(
        context: Context,
        methodName: String,
        @NotNull params: QueryProductDetailsParams,
        @NonNull productType: String,
        historyList: ArrayList<String> = ArrayList(),
        onComplete: () -> Unit
    ) {
        if (mBillingClient != null && mBillingClient?.isReady == true) {
            mBillingClient?.let { billingClient ->
                val productDetails = billingClient.queryProductDetails(params = params)

                if (productDetails.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    productDetails.productDetailsList?.let { listOfProducts ->
                        for (productDetail in listOfProducts) {
                            val productID = productDetail.productId

                            val formattedPrice = when (productType) {
                                BillingClient.ProductType.INAPP -> {
                                    productDetail.oneTimePurchaseOfferDetails?.formattedPrice
                                        ?: "Not Found"
                                }

                                BillingClient.ProductType.SUBS -> {
                                    val pricingList =
                                        productDetail.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.filter {
                                            !it.formattedPrice.equals(
                                                "Free",
                                                ignoreCase = true
                                            )
                                        }
                                    if (pricingList?.isNotEmpty() == true) {
                                        pricingList[0]?.formattedPrice ?: "Not Found"
                                    } else {
                                        "Not Found"
                                    }
                                }

                                else -> {
                                    "Not Found"
                                }
                            }

                            val priceAmountMicros = when (productType) {
                                BillingClient.ProductType.INAPP -> {
                                    productDetail.oneTimePurchaseOfferDetails?.priceAmountMicros
                                        ?: 0
                                }

                                BillingClient.ProductType.SUBS -> {
                                    val pricingList =
                                        productDetail.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.filter {
                                            !it.formattedPrice.equals(
                                                "Free",
                                                ignoreCase = true
                                            )
                                        }
                                    if (pricingList?.isNotEmpty() == true) {
                                        pricingList[0]?.priceAmountMicros ?: 0
                                    } else {
                                        0
                                    }
                                }

                                else -> {
                                    0
                                }
                            }

                            val priceCurrencyCode = when (productType) {
                                BillingClient.ProductType.INAPP -> {
                                    productDetail.oneTimePurchaseOfferDetails?.priceCurrencyCode
                                        ?: "Not Found"
                                }

                                BillingClient.ProductType.SUBS -> {
                                    val pricingList =
                                        productDetail.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.filter {
                                            !it.formattedPrice.equals(
                                                "Free",
                                                ignoreCase = true
                                            )
                                        }
                                    if (pricingList?.isNotEmpty() == true) {
                                        pricingList[0]?.priceCurrencyCode ?: "Not Found"
                                    } else {
                                        "Not Found"
                                    }
                                }

                                else -> {
                                    "Not Found"
                                }
                            }

                            val billingPeriod = when (productType) {
                                BillingClient.ProductType.INAPP -> {
                                    "One Time Purchase"
                                }

                                BillingClient.ProductType.SUBS -> {
                                    val pricingList =
                                        productDetail.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.filter {
                                            !it.formattedPrice.equals(
                                                "Free",
                                                ignoreCase = true
                                            )
                                        }
                                    if (pricingList?.isNotEmpty() == true) {
                                        pricingList[0]?.billingPeriod?.getFullBillingPeriod()
                                            ?: "Not Found"
                                    } else {
                                        "Not Found"
                                    }
                                }

                                else -> {
                                    "Not Found"
                                }
                            }

                            val freeTrialPeriod = when (productType) {
                                BillingClient.ProductType.INAPP -> {
                                    "Not Found"
                                }

                                BillingClient.ProductType.SUBS -> {
                                    val index = if (historyList.contains(productID)) {
                                        (productDetail.subscriptionOfferDetails?.size ?: 1) - 1
                                    } else {
                                        0
                                    }
                                    val pricingList =
                                        productDetail.subscriptionOfferDetails?.get(index)?.pricingPhases?.pricingPhaseList?.filter {
                                            it.formattedPrice.equals(
                                                "Free",
                                                ignoreCase = true
                                            )
                                        }
                                    if (pricingList?.isNotEmpty() == true) {
                                        pricingList[0]?.billingPeriod?.getFullBillingPeriod()
                                            ?: "Not Found"
                                    } else {
                                        "Not Found"
                                    }
                                }

                                else -> {
                                    "Not Found"
                                }
                            }

                            PRODUCT_LIST.add(
                                ProductInfo(
                                    id = productID,
                                    formattedPrice = formattedPrice,
                                    priceAmountMicros = priceAmountMicros,
                                    priceCurrencyCode = priceCurrencyCode,
                                    billingPeriod = billingPeriod,
                                    freeTrialPeriod = freeTrialPeriod,
                                    productDetail = productDetail
                                )
                            )

                            logProductDetail(
                                fMethodName = methodName,
                                fProductDetail = productDetail
                            )
                        }
                    }
                } else {
                    logResponseCode(
                        responseMsg = "$methodName: ",
                        billingResult = productDetails.billingResult
                    )
                }

                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(productType)
                        .build()
                ) { billingResult, purchaseList ->
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK,
                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                            if (purchaseList.isNotEmpty()) {
                                onPurchased(context, productType)
                            } else {
                                onExpired(context, productType)
                                logI(
                                    tag = TAG,
                                    message = "$methodName: =>> Purchases History Not Found"
                                )
                            }
                        }

                        else -> {
                            onExpired(context, productType)
                            logResponseCode(
                                responseMsg = "$methodName: ",
                                billingResult = billingResult
                            )
                        }
                    }
                    onComplete.invoke()
                }
            }
        } else {
            logE(tag = TAG, message = "$methodName: =>> The billing client is not ready")
            onComplete.invoke()
        }
    }

    private fun onPurchased(context: Context, @NonNull productType: String) {
        logE(tag = TAG, message = "onPurchased: Purchase Success")
        if (productType == BillingClient.ProductType.INAPP) {
            AdsManager(context).onProductPurchased()
        } else if (productType == BillingClient.ProductType.SUBS) {
            AdsManager(context).onProductSubscribed()
        }
    }

    private fun onExpired(context: Context, @NonNull productType: String) {
        logE(tag = TAG, message = "onExpired: Purchase Expired")
        if (productType == BillingClient.ProductType.INAPP) {
            AdsManager(context).onProductExpired()
        } else if (productType == BillingClient.ProductType.SUBS) {
            AdsManager(context).onSubscribeExpired()
        }
    }
    //</editor-fold>

    //<editor-fold desc="Handle Purchase">
    private fun handlePurchase(context: Context, purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            for (product in purchase.products) {
                if (lifeTimeProductKeyList.contains(product)) {
                    logI(tag = TAG, message = "handlePurchase: =>> productId -> $product")
                    AdsManager(context).onProductPurchased()
                } else if (subscriptionKeyList.contains(product)) {
                    logI(tag = TAG, message = "handlePurchase: =>> productId -> $product")
                    AdsManager(context).onProductSubscribed()
                }
            }

            mPurchaseListener?.onPurchasedSuccess(purchase = purchase)
        }

        CoroutineScope(Dispatchers.IO).launch {
            acknowledgePurchase(purchase = purchase)
            if (isConsumable) {
                consumePurchase(purchase = purchase)
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)

            val ackPurchaseResult = withContext(Dispatchers.IO) {
                mBillingClient?.acknowledgePurchase(acknowledgePurchaseParams.build())
            }

            if (ackPurchaseResult != null) {
                logResponseCode("acknowledgePurchase: ", ackPurchaseResult)
            } else {
                logE(tag = TAG, message = "acknowledgePurchase: =>> Not Found Any Purchase Result")
            }
        }

        logPurchaseItem(purchase = purchase)
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        mBillingClient?.let {
            val consumeResult = it.consumePurchase(consumeParams)
            logResponseCode("consumePurchase: ", consumeResult.billingResult)
        }
    }
    //</editor-fold>

    //<editor-fold desc="Purchase Product & Subscribe">
    fun purchaseProduct(
        activity: Activity,
        @NotNull productId: String,
        fIsConsumable: Boolean = false
    ) {
        isConsumable = fIsConsumable
        CoroutineScope(Dispatchers.Main).launch {
            purchaseSelectedProduct(
                methodName = "purchaseProduct",
                activity = activity,
                productId = productId,
                productKeyList = lifeTimeProductKeyList,
                productType = BillingClient.ProductType.INAPP
            )
        }
    }

    fun subscribeProduct(
        activity: Activity,
        @NotNull productId: String,
        fIsConsumable: Boolean = false
    ) {
        isConsumable = fIsConsumable
        CoroutineScope(Dispatchers.Main).launch {
            purchaseSelectedProduct(
                methodName = "subscribeProduct",
                activity = activity,
                productId = productId,
                productKeyList = subscriptionKeyList,
                productType = BillingClient.ProductType.SUBS
            )
        }
    }

    private suspend fun purchaseSelectedProduct(
        methodName: String,
        activity: Activity,
        @NotNull productId: String,
        @NotNull productKeyList: ArrayList<String>,
        @NonNull productType: String
    ) {
        if (mBillingClient != null && mBillingClient?.isReady == true) {
            mBillingClient?.let { billingClient ->
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        productKeyList.map { keyId ->
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(keyId)
                                .setProductType(productType)
                                .build()
                        }
                    )
                    .build()

                val productDetailsResult = withContext(Dispatchers.IO) {
                    billingClient.queryProductDetails(params)
                }

                val productDetail = getProductDetails(
                    productId = productId,
                    productDetailsList = productDetailsResult.productDetailsList
                )

                if (productDetail != null) {

                    val offerToken = if (productType == BillingClient.ProductType.SUBS) {
                        productDetail.subscriptionOfferDetails?.get(0)?.offerToken
                    } else {
                        null
                    }

                    if (offerToken == null && productType == BillingClient.ProductType.SUBS) {
                        return
                    }

                    val lBuilder: BillingFlowParams.ProductDetailsParams.Builder =
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                    lBuilder.apply {
                        setProductDetails(productDetail)
                        offerToken?.let {
                            setOfferToken(it)
                        }
                    }

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(mutableListOf(lBuilder.build()))
                        .build()

                    val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                            logE(tag = TAG, message = "$methodName: =>> ITEM_ALREADY_OWNED")

                            onPurchased(context = activity, productType = productType)
                            mPurchaseListener?.onProductAlreadyOwn()
                            logProductDetail(
                                fMethodName = methodName,
                                fProductDetail = productDetail
                            )
                        }

                        BillingClient.BillingResponseCode.OK -> {
                            logE(tag = TAG, message = "$methodName: =>> Purchase in Progress")
                        }

                        else -> {
                            logResponseCode(methodName, billingResult)
                        }
                    }

                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        mPurchaseListener?.onBillingKeyNotFound(productId)
                    }
                    logE(
                        tag = TAG,
                        message = "$methodName: =>> Product Detail not found for product id:: $productId"
                    )
                }
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(activity, "The Billing Client Is Not Ready", Toast.LENGTH_SHORT)
                    .show()
            }
            logE(tag = TAG, message = "$methodName: =>> The billing client is not ready")
        }
    }
    //</editor-fold>

    //<editor-fold desc="other get function">
    private fun getProductDetails(
        @NotNull productId: String,
        productDetailsList: List<ProductDetails>?
    ): ProductDetails? {
        return productDetailsList?.find { it.productId.equals(productId, true) }
    }

    private fun String.getFullBillingPeriod(): String {
        return try {
            val size = this.length
            val period = this.substring(1, size - 1)
            when (this.substring(size - 1, size)) {
                "D", "d" -> "$period Day"
                "W", "w" -> "$period Week"
                "M", "m" -> "$period Month"
                "Y", "y" -> "$period Year"
                else -> "Not Found"
            }
        } catch (e: Exception) {
            "Not Found"
        }
    }
    //</editor-fold>

    //<editor-fold desc="Price Discount">
    fun getWeekBaseYearlyDiscount(
        weekPrice: String,
        yearPrice: String,
        onDiscountCalculated: (yearlyDiscountPercentage: Double, yearlyWeekBaseDiscountPrice: String) -> Unit
    ) {
        weekPrice.getPriceInDouble.let { lWeekNumber ->
            yearPrice.getPriceInDouble.let { lYearNumber ->
                val lWeekPrize: Double = (lWeekNumber * 52) - lYearNumber
                val lYearPrizeBaseOfWeek = (lWeekNumber * 52)
                var lDiscountPercentage: Double = (lWeekPrize / lYearPrizeBaseOfWeek) * 100

                lDiscountPercentage *= 100
                lDiscountPercentage = lDiscountPercentage.toInt().toDouble()
                lDiscountPercentage /= 100

                val lDiscountPrice = weekPrice.replace(
                    String.format(Locale.ENGLISH, "%.2f", lWeekNumber),
                    String.format(Locale.ENGLISH, "%.2f", (lYearNumber / 52)),
                    false
                )

                onDiscountCalculated.invoke(lDiscountPercentage, lDiscountPrice)
            }
        }
    }

    fun getWeekBaseMonthlyDiscount(
        weekPrice: String,
        monthPrice: String,
        onDiscountCalculated: (monthlyDiscountPercentage: Double, monthlyWeekBaseDiscountPrice: String) -> Unit
    ) {
        weekPrice.getPriceInDouble.let { lWeekNumber ->
            monthPrice.getPriceInDouble.let { lMonthNumber ->
                val lWeekPrize: Double = (lWeekNumber * 4) - lMonthNumber
                val lMonthPrizeBaseOfWeek = (lWeekNumber * 4)
                var lDiscountPercentage: Double = (lWeekPrize / lMonthPrizeBaseOfWeek) * 100

                lDiscountPercentage *= 100
                lDiscountPercentage = lDiscountPercentage.toInt().toDouble()
                lDiscountPercentage /= 100

                val lDiscountPrice = weekPrice.replace(",", "").replace(
                    String.format(Locale.ENGLISH, "%.2f", lWeekNumber),
                    String.format(Locale.ENGLISH, "%.2f", (lMonthNumber / 4)),
                    false
                )

                onDiscountCalculated.invoke(lDiscountPercentage, lDiscountPrice)
            }
        }
    }

    fun getMonthBaseYearlyDiscount(
        monthPrice: String,
        yearPrice: String,
        onDiscountCalculated: (yearlyDiscountPercentage: Double, yearlyMonthBaseDiscountPrice: String) -> Unit
    ) {
        monthPrice.getPriceInDouble.let { lMonthNumber ->
            yearPrice.getPriceInDouble.let { lYearNumber ->
                val lMonthPrize: Double = (lMonthNumber * 12) - lYearNumber
                val lYearPrizeBaseOfMonth = (lMonthNumber * 12)
                var lDiscountPercentage: Double = (lMonthPrize / lYearPrizeBaseOfMonth) * 100

                lDiscountPercentage *= 100
                lDiscountPercentage = lDiscountPercentage.toInt().toDouble()
                lDiscountPercentage /= 100

                val lDiscountPrice = monthPrice.replace(",", "").replace(
                    String.format(Locale.ENGLISH, "%.2f", lMonthNumber),
                    String.format(Locale.ENGLISH, "%.2f", (lYearNumber / 12)),
                    false
                )

                onDiscountCalculated.invoke(lDiscountPercentage, lDiscountPrice)
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="All Log Related Purchase">
    internal fun logResponseCode(responseMsg: String, @NotNull billingResult: BillingResult) {
        val errorCode = when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> "RESULT OK"
            BillingClient.BillingResponseCode.ERROR -> "ERROR"
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "SERVICE_TIMEOUT"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
            BillingClient.BillingResponseCode.USER_CANCELED -> "USER_CANCELED"
            else -> "unDefined Error"
        }
        logE(
            tag = TAG,
            message = "$responseMsg :: \nerrorCode::$errorCode,\nMessage::${billingResult.debugMessage}"
        )
    }

    private fun logPurchaseItem(purchase: Purchase) {
        with(purchase) {
            logW(
                tag = TAG,
                message = "<<<-----------------   Purchase Details   ----------------->>>"
            )
            logW(tag = TAG, message = "Order Id: $orderId")
            logW(tag = TAG, message = "Original Json: $originalJson")
            logW(tag = TAG, message = "Package Name: $packageName")
            logW(tag = TAG, message = "Purchase Token: $purchaseToken")
            logW(tag = TAG, message = "Signature: $signature")
            products.forEach {
                logW(tag = TAG, message = "Products: $it")
                logW(tag = TAG, message = "Price: ${it.getProductInfo?.formattedPrice}")
            }
            logW(tag = TAG, message = "Purchase State: ${purchaseState.getPurchaseState}")
            logW(tag = TAG, message = "Quantity: $quantity")
            logW(tag = TAG, message = "Purchase Time: $purchaseTime")
            logW(tag = TAG, message = "Acknowledged: $isAcknowledged")
            logW(tag = TAG, message = "AutoRenewing: $isAutoRenewing")
            logW(
                tag = TAG,
                message = "<<<-----------------   End of Purchase Details   ----------------->>>"
            )
        }
    }

    private fun logProductDetail(fMethodName: String, @NotNull fProductDetail: ProductDetails) {
        with(fProductDetail) {
            logW(tag = TAG, message = "\n")
            logW(
                tag = TAG,
                message = "$fMethodName: <<<-----------------   \"$productId\" Product Details   ----------------->>>"
            )
            logW(tag = TAG, message = "$fMethodName: Product Id:: $productId")
            logW(tag = TAG, message = "$fMethodName: Name:: $name")
            logW(tag = TAG, message = "$fMethodName: Title:: $title")
            logW(tag = TAG, message = "$fMethodName: Description:: $description")
            logW(tag = TAG, message = "$fMethodName: Product Type:: $productType")
            oneTimePurchaseOfferDetails?.let { details ->
                with(details) {
                    logW(tag = TAG, message = "\n")
                    logW(
                        tag = TAG,
                        message = "$fMethodName: <<<-----------------   Life-Time Purchase Product Price Details   ----------------->>>"
                    )
                    logW(
                        tag = TAG,
                        message = "$fMethodName: Price Amount Micros:: $priceAmountMicros"
                    )
                    logW(tag = TAG, message = "$fMethodName: Formatted Price:: $formattedPrice")
                    logW(
                        tag = TAG,
                        message = "$fMethodName: Price Currency Code:: $priceCurrencyCode"
                    )
                    logW(
                        tag = TAG,
                        message = "$fMethodName: <<<-----------------   End of Life-Time Purchase Product Price Details   ----------------->>>"
                    )
                }
            }
            subscriptionOfferDetails?.let { details ->
                if (details.isNotEmpty()) {
                    details.forEachIndexed { index, subscriptionOfferDetails ->
                        subscriptionOfferDetails?.let { offerDetails ->
                            with(offerDetails) {
                                logW(tag = "", message = "\n")
                                logW(
                                    tag = TAG,
                                    message = "$fMethodName: <<<-----------------   Product Offer Details of Index:: $index   ----------------->>>"
                                )
                                logW(tag = TAG, message = "$fMethodName: Offer Token:: $offerToken")
                                logW(tag = TAG, message = "$fMethodName: Offer Tags:: $offerTags")
//                                logW(tag = TAG, message = "$fMethodName: Installment Plan Details:: $installmentPlanDetails")

                                if (pricingPhases.pricingPhaseList.isNotEmpty()) {
                                    pricingPhases.pricingPhaseList.forEachIndexed { index, pricingPhase1 ->
                                        pricingPhase1?.let { pricingPhase ->
                                            with(pricingPhase) {
                                                logW(tag = "", message = "\n")
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: <<<-----------------   Product Offer Price Details of Index:: $index   ----------------->>>"
                                                )
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: Billing Period:: $billingPeriod"
                                                )
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: Formatted Price:: $formattedPrice"
                                                )
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: Price Amount Micros:: $priceAmountMicros"
                                                )
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: Price Currency Code:: $priceCurrencyCode"
                                                )
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: Recurrence Mode:: $recurrenceMode"
                                                )
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: Billing Cycle Count:: $billingCycleCount"
                                                )
                                                logW(
                                                    tag = TAG,
                                                    message = "$fMethodName: <<<-----------------   End of Product Offer Price Details of Index:: $index   ----------------->>>"
                                                )
                                            }
                                        }
                                    }
                                }

                                logW(
                                    tag = TAG,
                                    message = "$fMethodName: <<<-----------------   End of Product Offer Details of Index:: $index   ----------------->>>"
                                )
                            }
                        }
                    }
                }
            }
            logW(
                tag = TAG,
                message = "$fMethodName: <<<-----------------   End of \"$productId\" Product Details   ----------------->>>"
            )
        }
    }
    //</editor-fold>

    interface ProductPurchaseListener {
        fun onPurchasedSuccess(purchase: Purchase)
        fun onProductAlreadyOwn()
        fun onBillingSetupFinished(billingResult: BillingResult)
        fun onBillingKeyNotFound(productId: String)
    }
}