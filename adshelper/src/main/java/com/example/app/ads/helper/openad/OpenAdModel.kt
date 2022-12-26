package com.example.app.ads.helper.openad

import com.example.app.ads.helper.AdMobAdsListener
import com.google.android.gms.ads.appopen.AppOpenAd

data class OpenAdModel(
    var appOpenAd: AppOpenAd? = null,
    var adsID: String = "",
    var listener: AdMobAdsListener? = null,
    var isAdLoadingRunning: Boolean = false
)
