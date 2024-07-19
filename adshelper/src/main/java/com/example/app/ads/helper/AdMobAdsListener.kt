@file:Suppress("unused")

package com.example.app.ads.helper

/**
 * @author Akshay Harsoda
 * @since 05 Aug 2021
 * @updated 24 Jun 2024
 *
 * AdMobAdsListener.kt - Simple interface which has notified your AD process
 */
interface AdMobAdsListener<T> {

    /**
     * This method is called when your AD data was loaded successfully
     */
    fun onAdLoaded(fLoadedAd: T) {}

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
     * This method is called when your Reward Video AD or Reward Interstitial AD was closed after successfully showing to the user
     * And it will notify you if user earned any reward
     *
     * @param isUserEarnedReward [by Default value = false] it's true when user successfully earned reward
     */
    fun onUserEarnedReward(isUserEarnedReward: Boolean) {}

    /**
     * This method is called when your Reward Video AD or Reward Interstitial AD was start to load new AD
     */
    fun onStartToLoadRewardAd() {}

}