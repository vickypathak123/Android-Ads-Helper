@file:Suppress("unused")

package com.example.app.ads.helper.reward

import android.app.Activity
import android.content.Context
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.AdStatusModel
import com.example.app.ads.helper.adRequestBuilder
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isAppNotPurchased
import com.example.app.ads.helper.is_enable_rewarded_interstitial_ad_from_remote_config
import com.example.app.ads.helper.is_need_to_load_multiple_rewarded_interstitial_ad_request
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.list_of_admob_rewarded_interstitial_ads
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 02 Dec 2022
 * @updated 25 Jun 2024
 *
 * RewardedInterstitialAdHelper.kt - Simple object which has load and handle your Interstitial Reward AD data
 */
object RewardedInterstitialAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var isThisAdShowing: Boolean = false
    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false
    private var isStartToLoadAnyIndex = false
    private var mAdIdPosition: Int = -1

    private var mOnAdLoaded: () -> Unit = {}
    private var mOnStartToLoadAd: () -> Unit = {}
    private var mListener: AdMobAdsListener<RewardedInterstitialAd>? = null

    private val isLastIndex: Boolean get() = (mAdIdPosition + 1) >= list_of_admob_rewarded_interstitial_ads.size

    private val isAdsIDSated: Boolean
        get() {
            if (list_of_admob_rewarded_interstitial_ads.isNotEmpty()) {
                return true
            } else {
                throw RuntimeException("set RewardedInterstitial Ad Id First")
            }
        }

    private fun getRewardedInterstitialAdModel(
        isNeedToLoadMultipleRequest: Boolean,
        onFindModel: (index: Int, fAdModel: AdStatusModel<RewardedInterstitialAd>) -> Unit
    ) {
        if (isAdsIDSated) {
            if (isNeedToLoadMultipleRequest) {
                logE(TAG, "getRewardedInterstitialAdModel: Load Multiple Request")
                list_of_admob_rewarded_interstitial_ads.forEachIndexed { index, adStatusModel ->
                    onFindModel.invoke(index, adStatusModel)
                }
            } else {
                if (list_of_admob_rewarded_interstitial_ads.any { it.isAdLoadingRunning }) {
                    logE(tag = TAG, message = "getRewardedInterstitialAdModel: list_of_admob_rewarded_interstitial_ads.any { it.isAdLoadingRunning } == true, $mAdIdPosition")
                } else {
                    logE(tag = TAG, message = "getRewardedInterstitialAdModel: list_of_admob_rewarded_interstitial_ads.any { it.isAdLoadingRunning } == false, $mAdIdPosition")
                    mAdIdPosition = if (mAdIdPosition < list_of_admob_rewarded_interstitial_ads.size) {
                        if (mAdIdPosition == -1) {
                            0
                        } else {
                            (mAdIdPosition + 1)
                        }
                    } else {
                        0
                    }

                    logE(TAG, "getRewardedInterstitialAdModel: AdIdPosition -> $mAdIdPosition")

                    if (mAdIdPosition >= 0 && mAdIdPosition < list_of_admob_rewarded_interstitial_ads.size) {
                        onFindModel.invoke(mAdIdPosition, list_of_admob_rewarded_interstitial_ads[mAdIdPosition])
                    } else {
                        mAdIdPosition = -1
                    }
                }
            }
        }
    }

    private fun loadNewAd(
        fContext: Context,
        fModel: AdStatusModel<RewardedInterstitialAd>,
        fIndex: Int
    ) {

        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdID -> ${fModel.adID}")

        fModel.isAdLoadingRunning = true
        fModel.listener?.onStartToLoadRewardAd()

        RewardedInterstitialAd.load(
            fContext,
            fModel.adID,
            adRequestBuilder,
            object : RewardedInterstitialAdLoadCallback() {

                override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    super.onAdLoaded(rewardedInterstitialAd)
                    fModel.isAdLoadingRunning = false

                    rewardedInterstitialAd.apply {
                        fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                logI(
                                    tag = TAG,
                                    message = "loadNewAd: onAdShowedFullScreenContent: Index -> $fIndex"
                                )
                                isAnyAdOpen = true
                                isThisAdShowing = true
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                logE(
                                    tag = TAG,
                                    message = "loadNewAd: onAdFailedToShowFullScreenContent: Index -> $fIndex\nErrorMessage::${adError.message}\nErrorCode::${adError.code}"
                                )
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                logI(
                                    tag = TAG,
                                    message = "loadNewAd: onAdDismissedFullScreenContent: Index -> $fIndex"
                                )
                                fModel.apply {
                                    this.loadedAd?.fullScreenContentCallback = null
                                    this.loadedAd = null
                                    isAnyAdOpen = false
                                    isThisAdShowing = false
                                    this.listener?.onAdClosed()
                                }
                            }
                        }
                    }.also {
                        logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex")
                        fModel.apply {
                            this.loadedAd = it
                            this.listener?.onAdLoaded(it)
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    logE(
                        tag = TAG,
                        message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}"
                    )
                    fModel.apply {
                        this.isAdLoadingRunning = false
                        this.loadedAd = null
                        this.listener?.onAdFailed()
                    }
                }
            }
        )
    }

    private fun requestWithIndex(
        fContext: Context,
        fModel: AdStatusModel<RewardedInterstitialAd>,
        fIndex: Int,
        onStartToLoadAd: () -> Unit,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (isOnline
            && fModel.loadedAd == null
            && !fModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = fModel.apply {
                    this.listener = object : AdMobAdsListener<RewardedInterstitialAd> {

                        override fun onStartToLoadRewardAd() {
                            super.onStartToLoadRewardAd()
                            if (!isAnyIndexAlreadyLoaded) {
                                if (!isStartToLoadAnyIndex) {
                                    isStartToLoadAnyIndex = true
                                    onStartToLoadAd.invoke()
                                    if (onStartToLoadAd != mOnStartToLoadAd) {
                                        mOnStartToLoadAd.invoke()
                                    }
                                }
                            }
                        }

                        override fun onAdLoaded(fLoadedAd: RewardedInterstitialAd) {
                            super.onAdLoaded(fLoadedAd)
                            mAdIdPosition = -1
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onAdLoaded: Index -> $fIndex"
                            )
                            if (!isAnyIndexLoaded) {
                                isAnyIndexLoaded = true
                                onAdLoaded.invoke()
                                if (onAdLoaded != mOnAdLoaded) {
                                    mOnAdLoaded.invoke()
                                }
                            }
                        }

                        override fun onAdClosed(isShowFullScreenAd: Boolean) {
                            super.onAdClosed(isShowFullScreenAd)
                            mListener?.onAdClosed(isShowFullScreenAd)
                            loadAd(
                                fContext = fContext,
                                onStartToLoadAd = onStartToLoadAd,
                                onAdLoaded = onAdLoaded
                            )
                        }

                        override fun onAdFailed() {
                            super.onAdFailed()
                            onAdFailed.invoke()
                        }
                    }
                },
                fIndex = fIndex
            )
        } else if (isOnline && fModel.loadedAd != null) {
            if (!isAnyIndexAlreadyLoaded) {
                logI(tag = TAG, message = "requestWithIndex: already loaded ad Index -> $fIndex")
                isAnyIndexAlreadyLoaded = true
                onAdLoaded.invoke()
                if (onAdLoaded != mOnAdLoaded) {
                    mOnAdLoaded.invoke()
                }
            }
        }
    }

    /**
     * Call this method when you need to load your Reward Interstitial AD
     * you need to call this method only once in any activity or fragment
     *
     *
     * Use of this Method
     * loadAd(
     *      fContext = reference of your activity or fragment context
     *      onStartToLoadAd = {[show progress when start to load Reward Interstitial AD]},
     *      onAdLoaded = {[hide progress after successfully load Reward Interstitial AD]},
     * )
     *
     * @param fContext this is a reference to your activity or fragment context
     * @param onStartToLoadAd @see [AdMobAdsListener.onStartToLoadRewardAd]
     * @param onAdLoaded @see [AdMobAdsListener.onAdLoaded]
     */
    fun loadAd(
        fContext: Context,
        onStartToLoadAd: () -> Unit,
        onAdLoaded: () -> Unit,
    ) {
        mOnAdLoaded = onAdLoaded
        mOnStartToLoadAd = onStartToLoadAd
        isAnyIndexLoaded = false
        isStartToLoadAnyIndex = false
        isAnyIndexAlreadyLoaded = false

        if (isRewardedInterstitialAdEnable() && isOnline) {
            getRewardedInterstitialAdModel(isNeedToLoadMultipleRequest = is_need_to_load_multiple_rewarded_interstitial_ad_request) { index, fAdModel ->
                logI(tag = TAG, message = "loadAd: getRewardedInterstitialAdModel: Index -> $index")
                requestWithIndex(
                    fContext = fContext,
                    fModel = fAdModel,
                    fIndex = index,
                    onStartToLoadAd = onStartToLoadAd,
                    onAdLoaded = onAdLoaded,
                    onAdFailed = {
                        if (!is_need_to_load_multiple_rewarded_interstitial_ad_request) {
                            if (isLastIndex) {
                                mAdIdPosition = -1
                            } else {
                                loadAd(
                                    fContext = fContext,
                                    onStartToLoadAd = onStartToLoadAd,
                                    onAdLoaded = mOnAdLoaded
                                )
                            }
                        }
                    },
                )
            }
        } else {
            onStartToLoadAd.invoke()
//            onAdLoaded.invoke()
        }
    }

    internal fun isRewardedInterstitialAdEnable(): Boolean = isAppNotPurchased && is_enable_rewarded_interstitial_ad_from_remote_config

    /**
     * this method will check Reward Interstitial Ad is Available or not
     */
    private fun isRewardedInterstitialAvailable(): Boolean {
        return isRewardedInterstitialAdEnable() && list_of_admob_rewarded_interstitial_ads.any { it.loadedAd != null }
    }


    /**
     * Call this method when you need to show your Reward Interstitial AD
     * you need to call this method with your Activity Context
     *
     *
     * Use of this Method
     * activity.showRewardedInterstitialAd(
     *      onUserEarnedReward = { isUserEarnedReward -> [by Default value = false, it's true when user successfully earned reward]},
     * )
     *
     * @param onUserEarnedReward @see [AdMobAdsListener.onUserEarnedReward]
     */
    fun Activity.showRewardedInterstitialAd(
        onUserEarnedReward: (isUserEarnedReward: Boolean) -> Unit,
    ) {
        if (!isThisAdShowing && isRewardedInterstitialAvailable() && isOnline) {
            var isUserEarnedReward = false

            mListener = object : AdMobAdsListener<RewardedInterstitialAd> {
                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    if (isAppForeground) {
                        onUserEarnedReward.invoke(isUserEarnedReward)
                    }
                    logI(
                        tag = TAG,
                        message = "showRewardedInterstitialAd: onAdClosed: Load New Ad after ad close"
                    )
                }
            }

            if (isAdsIDSated) {
                list_of_admob_rewarded_interstitial_ads.find { it.loadedAd != null }?.let { loadedAdModel ->
                    val lIndex: Int = list_of_admob_rewarded_interstitial_ads.indexOf(loadedAdModel)

                    if (isRewardedInterstitialAvailable() && loadedAdModel.loadedAd != null && isOnline && !this.isFinishing && !isAnyAdOpen && !isThisAdShowing) {
                        isAnyAdOpen = true
                        isThisAdShowing = true
                        loadedAdModel.loadedAd?.show(this) {
                            isUserEarnedReward = true
                            logI(
                                tag = TAG,
                                message = "showRewardedInterstitialAd: Show RewardedInterstitial Ad Index -> $lIndex"
                            )
                        }
                    }
                }
            }
        } else {
            onUserEarnedReward.invoke(false)
        }
    }

    fun destroy() {
        mListener = null
        isThisAdShowing = false
        isAnyIndexLoaded = false
        isStartToLoadAnyIndex = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1

        for (data in list_of_admob_rewarded_interstitial_ads) {
            data.loadedAd?.fullScreenContentCallback = null
            data.loadedAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}