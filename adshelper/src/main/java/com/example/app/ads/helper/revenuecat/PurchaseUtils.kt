package com.example.app.ads.helper.revenuecat

import com.example.app.ads.helper.purchase.ProductPurchaseHelper
import com.example.app.ads.helper.revenueCatProductList
import com.revenuecat.purchases.PackageType


fun String.getFullBillingPeriod(): String {
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


val PackageType.getRevenueCatProductInfo: RevenueCatProductInfo?
    get() {
        return revenueCatProductList.find { it.packageType == this }
    }