@file:Suppress("unused")

package com.example.app.ads.helper

import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

/**
 * @author Akshay Harsoda
 * @since 05 Aug 2021
 *
 * AdMobAdsListener.kt - Simple interface which has notified your AD process
 */
interface AdMobAdsListener {

    /**
     * This method is called when your AD data was loaded successfully
     */
    fun onAdLoaded() {}

    /**
     * This method is called when your AD was failed to load
     */
    fun onAdFailed() {}

    /**
     * This method is called when your AD was closed after successfully showing to the user
     * @param isShowFullScreenAd [by Default value = false] it's true when fullscreen native ad show and close
     */
    fun onAdClosed(isShowFullScreenAd: Boolean = false) {}

    /**
     * This method is called when your Adaptive Banner AD data was loaded successfully
     *
     * @param adView it's a reference to your Adaptive Banner Ad View
     */
    fun onAdaptiveBannerLoaded(adView: AdView) {}

    /**
     * This method is called when your Native AD data was loaded successfully
     *
     * @param nativeAd it's a reference to your Native Ad
     */
    fun onNativeAdLoaded(nativeAd: NativeAd) {}

    /**
     * This method is called when your Interstitial AD data was loaded successfully
     *
     * @param interstitialAd it's a reference to your Interstitial Ad
     */
    fun onInterstitialAdLoaded(interstitialAd: InterstitialAd) {}

    /**
     * This method is called when your Reward Video AD data was loaded successfully
     *
     * @param rewardedAd it's a reference to your Rewarded Video Ad
     */
    fun onRewardVideoAdLoaded(rewardedAd: RewardedAd) {}

    /**
     * This method is called when your Reward Interstitial AD data was loaded successfully
     *
     * @param rewardedInterstitialAd it's a reference to your Rewarded Interstitial Ad
     */
    fun onRewardInterstitialAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {}

    /**
     * This method is called when your AppOpenAd data was loaded successfully
     *
     * @param appOpenAd it's a reference to your AppOpenAd
     */
    fun onAppOpenAdLoaded(appOpenAd: AppOpenAd) {}

    /**
     * This method is called when your Reward Video AD was closed after successfully showing to the user
     * And it will notify you if user earned any reward
     *
     * @param isUserEarnedReward [by Default value = false] it's true when user successfully earned reward
     */
    fun onUserEarnedReward(isUserEarnedReward: Boolean) {}

    /**
     * This method is called when your Reward Video AD was start to load new AD
     */
    fun onStartToLoadRewardVideoAd() {}

    /**
     * This method is called when your Reward Interstitial AD was start to load new AD
     */
    fun onStartToLoadRewardedInterstitialAd() {}

}