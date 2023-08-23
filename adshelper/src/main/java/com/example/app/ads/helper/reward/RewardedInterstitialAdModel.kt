package com.example.app.ads.helper.reward

import com.example.app.ads.helper.AdMobAdsListener
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

data class RewardedInterstitialAdModel(
    var rewardedInterstitialAd: RewardedInterstitialAd? = null,
    var adsID: String = "",
    var listener: AdMobAdsListener? = null,
    var isAdLoadingRunning: Boolean = false
)
