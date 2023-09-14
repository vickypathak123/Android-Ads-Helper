//package com.example.app.ads.helper.revenuecat
//
//import com.example.app.ads.helper.customerInfo
//import com.example.app.ads.helper.logD
//import com.example.app.ads.helper.logE
//import com.example.app.ads.helper.purchase.ProductPurchaseHelper
//import com.example.app.ads.helper.revenueCatProductList
//import com.google.gson.Gson
//import com.revenuecat.purchases.PackageType
//import com.revenuecat.purchases.Purchases
//import com.revenuecat.purchases.getOfferingsWith
//
//
//fun String.getFullBillingPeriod(): String {
//    return try {
//        val size = this.length
//        val period = this.substring(1, size - 1)
//        when (this.substring(size - 1, size)) {
//            "D", "d" -> "$period Day"
//            "W", "w" -> "$period Week"
//            "M", "m" -> "$period Month"
//            "Y", "y" -> "$period Year"
//            else -> "Not Found"
//        }
//    } catch (e: Exception) {
//        "Not Found"
//    }
//}
//
//
//val PackageType.getRevenueCatProductInfo: RevenueCatProductInfo?
//    get() {
//        return revenueCatProductList.find { it.packageType == this }
//    }
//
//
//fun initRevenueCatProductList(onProductListInit: () -> Unit) {
//    val TAG = "initRevenueCatProductList"
//    Purchases.sharedInstance.getOfferingsWith({ error ->
//        // An error occurred
//        logE(tag = TAG, "onCreate:  error -->$error")
//    }) { offerings ->
//        offerings.current?.availablePackages?.let { listOfProducts ->
//            // Display packages for sale
//            if (revenueCatProductList.isEmpty()) {
//                revenueCatProductList.clear()
//                listOfProducts.forEach {
//                    logD(TAG, "onCreate:  purchase -->$it")
//                    ProductPurchaseHelper.addOtherSubscriptionKey(it.product.sku)
//                    val freeTrialPeriod = when (it.packageType) {
//                        PackageType.LIFETIME -> {
//                            "Not Found"
//                        }
//
//                        PackageType.MONTHLY, PackageType.ANNUAL, PackageType.WEEKLY -> {
//                            val pricingList = it.product.subscriptionOptions!!.freeTrial
//                            if (customerInfo?.entitlements?.all?.any { entitle -> entitle?.value?.productIdentifier.toString() == it.product.sku } == true) {
//                                "Not Found"
//                            } else if (pricingList != null) {
//                                pricingList!!.billingPeriod ?: "Not Found"
//                            } else {
//                                "Not Found"
//                            }
//                        }
//
//                        else -> {
//                            "Not Found"
//                        }
//                    }
//                    val billingPeriod = when (it.packageType) {
//                        PackageType.LIFETIME -> {
//                            "Not Found"
//                        }
//
//                        PackageType.MONTHLY, PackageType.ANNUAL, PackageType.WEEKLY -> {
//                            val pricingList = it.product.subscriptionOptions
//                            if (pricingList?.isNotEmpty() == true) {
//                                pricingList.basePlan!!.billingPeriod ?: "Not Found"
//                            } else {
//                                "Not Found"
//                            }
//                        }
//
//                        else -> {
//                            "Not Found"
//                        }
//                    }
//
//
//
//                    revenueCatProductList.add(
//                        RevenueCatProductInfo(
//                            id = it.product.sku,
//                            formattedPrice = it.product.price.formatted.toString(),
//                            packageType = it.packageType,
//                            priceAmountMicros = it.product.price.amountMicros,
//                            priceCurrencyCode = it.product.price.currencyCode,
//                            billingPeriod = billingPeriod.toString(),
//                            freeTrialPeriod = freeTrialPeriod.toString(),
//                            productDetail = it
//                        )
//                    )
//                }
//                logD(TAG, "onCreate: PRODUCT_LIST -->" + Gson().toJson(revenueCatProductList))
//                onProductListInit()
//            } else {
//                onProductListInit()
//            }
//        }
//    }
//}
