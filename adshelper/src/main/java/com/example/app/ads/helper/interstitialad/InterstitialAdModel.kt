package com.example.app.ads.helper.interstitialad

import com.example.app.ads.helper.AdMobAdsListener
import com.google.android.gms.ads.interstitial.InterstitialAd

data class InterstitialAdModel(
    var interstitialAd: InterstitialAd? = null,
    var adsID: String = "",
    var listener: AdMobAdsListener? = null,
    var isAdLoadingRunning: Boolean = false
)
