@file:Suppress("unused")

package com.example.app.ads.helper

/**
 * @author Akshay Harsoda
 * @since 24 Jun 2024
 */

data class AdStatusModel<T>(
    var loadedAd: T? = null,
    var adID: String = "",
    var listener: AdMobAdsListener<T>? = null,
    var isAdLoadingRunning: Boolean = false,
    var defaultAdListener: com.google.android.gms.ads.AdListener? = null
)