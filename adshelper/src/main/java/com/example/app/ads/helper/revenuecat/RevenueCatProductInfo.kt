//package com.example.app.ads.helper.revenuecat
//
//import android.os.Parcel
//import android.os.Parcelable
//import androidx.annotation.Keep
//import com.google.gson.annotations.Expose
//import com.google.gson.annotations.SerializedName
//import com.revenuecat.purchases.Package
//import com.revenuecat.purchases.PackageType
//import kotlinx.android.parcel.Parcelize
//
//data class RevenueCatProductInfo(
//    @SerializedName("id")
//    val id: String,
//    @SerializedName("formatted_price")
//    val formattedPrice: String,
//    @SerializedName("packageType")
//    val packageType: PackageType,
//    @SerializedName("price_amount_micros")
//    val priceAmountMicros: Long,
//    @SerializedName("price_currency_code")
//    val priceCurrencyCode: String,
//    @SerializedName("billing_period")
//    val billingPeriod: String,
//    @SerializedName("free_trial_period")
//    var freeTrialPeriod: String,
//    @SerializedName("product_detail")
//    val productDetail: Package
//)
