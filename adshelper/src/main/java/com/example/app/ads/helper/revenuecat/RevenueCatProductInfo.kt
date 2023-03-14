package com.example.app.ads.helper.revenuecat

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.revenuecat.purchases.PackageType
import kotlinx.android.parcel.Parcelize

@Keep
    @Parcelize
    data class RevenueCatProductInfo(
    @SerializedName("id")
        @Expose
        val id: String,
    @SerializedName("formatted_price")
        @Expose
        val formattedPrice: String,
    @SerializedName("packageType")
        @Expose
        val packageType: PackageType,
    @SerializedName("price_amount_micros")
        @Expose
        val priceAmountMicros: Long,
    @SerializedName("price_currency_code")
        @Expose
        val priceCurrencyCode: String,
    @SerializedName("billing_period")
        @Expose
        val billingPeriod: String,
    @SerializedName("free_trial_period")
        @Expose
        var freeTrialPeriod: String,
    @SerializedName("product_detail")
        @Expose
        val productDetail: com.revenuecat.purchases.Package
    ) : Parcelable
