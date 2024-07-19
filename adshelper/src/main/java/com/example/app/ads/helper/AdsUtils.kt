@file:Suppress("unused")

package com.example.app.ads.helper

import android.util.Log
import com.google.android.gms.ads.AdRequest

/**
 * @author Akshay Harsoda
 * @since 24 Jun 2024
 */

private const val TAG: String = "AdMob_AdsUtils"

var isAppForeground = false
var isAnyAdOpen = false

internal var need_to_block_open_ad_internally: Boolean = false
internal var isAppNotPurchased: Boolean = true
//internal var isAppNotPurchased: Boolean = false

//region Multiple Request Flag
internal var is_need_to_load_multiple_banner_ad_request: Boolean = false
internal var is_need_to_load_multiple_interstitial_ad_request: Boolean = false
internal var is_need_to_load_multiple_native_ad_request: Boolean = false
internal var is_need_to_load_multiple_app_open_ad_request: Boolean = false
internal var is_need_to_load_multiple_rewarded_interstitial_ad_request: Boolean = false
internal var is_need_to_load_multiple_rewarded_video_ad_request: Boolean = false
//endregion

//region Remote Config Flag
internal var is_enable_banner_ad_from_remote_config: Boolean = true
internal var is_enable_interstitial_ad_from_remote_config: Boolean = true
internal var is_enable_native_ad_from_remote_config: Boolean = true
internal var is_enable_app_open_ad_from_remote_config: Boolean = true
internal var is_enable_rewarded_interstitial_ad_from_remote_config: Boolean = true
internal var is_enable_rewarded_video_ad_from_remote_config: Boolean = true
//endregion

//region Remote Config Flag
internal var isBlockInterstitialAd: Boolean = false
internal var isEnableOpenAd: Boolean = false
//endregion

internal var admob_app_id: String = ""

//region Ads Model List of Admob Ads
internal var list_of_admob_splash_banner_ads: ArrayList<AdStatusModel<com.google.android.gms.ads.AdView>> = ArrayList()
internal var list_of_admob_banner_ads: ArrayList<AdStatusModel<com.google.android.gms.ads.AdView>> = ArrayList()
internal var list_of_admob_interstitial_ads: ArrayList<AdStatusModel<com.google.android.gms.ads.interstitial.InterstitialAd>> = ArrayList()
internal var list_of_admob_native_ads: ArrayList<AdStatusModel<com.google.android.gms.ads.nativead.NativeAd>> = ArrayList()
internal var list_of_admob_app_open_ads: ArrayList<AdStatusModel<com.google.android.gms.ads.appopen.AppOpenAd>> = ArrayList()
internal var list_of_admob_rewarded_interstitial_ads: ArrayList<AdStatusModel<com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd>> = ArrayList()
internal var list_of_admob_rewarded_video_ads: ArrayList<AdStatusModel<com.google.android.gms.ads.rewarded.RewardedAd>> = ArrayList()
//endregion

internal val adRequestBuilder: AdRequest get() = AdRequest.Builder().build()

fun stopShowingOpenAdInternally() {
    need_to_block_open_ad_internally = true
}

fun startShowingOpenAdInternally() {
    need_to_block_open_ad_internally = false
}

fun updateAppPurchasedStatusRemoveAds() {
    Log.e(TAG, "updateAppPurchasedStatusRemoveAds: AdsManager Before::$isAppNotPurchased")
    isAppNotPurchased = false
    Log.e(TAG, "updateAppPurchasedStatusRemoveAds: AdsManager After::$isAppNotPurchased")
}
