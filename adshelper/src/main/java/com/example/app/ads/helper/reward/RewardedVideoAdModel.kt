package com.example.app.ads.helper.reward

import com.example.app.ads.helper.AdMobAdsListener
import com.google.android.gms.ads.rewarded.RewardedAd

data class RewardedVideoAdModel(
    var rewardedAd: RewardedAd? = null,
    var adsID: String = "",
    var listener: AdMobAdsListener? = null,
    var isAdLoadingRunning: Boolean = false
)
