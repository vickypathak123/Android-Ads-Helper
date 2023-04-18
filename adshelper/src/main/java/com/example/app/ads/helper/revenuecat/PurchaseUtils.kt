package com.example.app.ads.helper.revenuecat




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


/*
val PackageType.getRevenueCatProductInfo: RevenueCatProductInfo?
    get() {
        return revenueCatProductList.find { it.packageType == this }
    }
*/


/*
fun initRevenueCatProductList(onProductListInit: () -> Unit) {
    val TAG = "initRevenueCatProductList"
    Purchases.sharedInstance.getOfferingsWith({ error ->
        // An error occurred
        logE(tag = TAG, "onCreate:  error -->$error")
    }) { offerings ->
        offerings.current?.availablePackages?.let { listOfProducts ->
            // Display packages for sale
            if (revenueCatProductList.isEmpty()) {
                revenueCatProductList.clear()
                listOfProducts.forEach {
                    logD(TAG, "onCreate:  purchase -->$it")
                    val freeTrialPeriod = when (it.packageType) {
                        PackageType.LIFETIME -> {
                            "Not Found"
                        }
                        PackageType.MONTHLY, PackageType.ANNUAL, PackageType.WEEKLY -> {
                            val pricingList = it.product.freeTrialPeriod
                            if (customerInfo?.entitlements?.all?.any { entitle -> entitle?.value?.productIdentifier.toString() == it.product.sku } == true) {
                                "Not Found"
                            } else if (pricingList?.isNotEmpty() == true) {
                                pricingList.getFullBillingPeriod() ?: "Not Found"
                            } else {
                                "Not Found"
                            }
                        }
                        else -> {
                            "Not Found"
                        }
                    }
                    val billingPeriod = when (it.packageType) {
                        PackageType.LIFETIME -> {
                            "Not Found"
                        }
                        PackageType.MONTHLY, PackageType.ANNUAL, PackageType.WEEKLY -> {
                            val pricingList = it.product.subscriptionPeriod
                            if (pricingList?.isNotEmpty() == true) {
                                pricingList.getFullBillingPeriod() ?: "Not Found"
                            } else {
                                "Not Found"
                            }
                        }
                        else -> {
                            "Not Found"
                        }
                    }



                    revenueCatProductList.add(
                        RevenueCatProductInfo(
                            id = it.product.sku,
                            formattedPrice = it.product.price,
                            packageType = it.packageType,
                            priceAmountMicros = it.product.priceAmountMicros,
                            priceCurrencyCode = it.product.priceCurrencyCode,
                            billingPeriod = billingPeriod,
                            freeTrialPeriod = freeTrialPeriod,
                            productDetail = it
                        )
                    )
                }
                logD(TAG, "onCreate: PRODUCT_LIST -->" + Gson().toJson(revenueCatProductList))
                onProductListInit()
            } else {
                onProductListInit()
            }
        }
    }
}*/
