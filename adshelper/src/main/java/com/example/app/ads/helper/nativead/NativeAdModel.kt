package com.example.app.ads.helper.nativead

import com.example.app.ads.helper.AdMobAdsListener
import com.google.android.gms.ads.nativead.NativeAd

data class NativeAdModel(
    var nativeAd: NativeAd? = null,
    var adsID: String = "",
    var listener: AdMobAdsListener? = null,
    var isAdLoadingRunning: Boolean = false
)
