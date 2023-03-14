package com.example.app.ads.helper.reward

import android.app.Activity
import android.content.Context
import com.example.app.ads.helper.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 02 Dec 2022
 *
 * RewardedVideoAdHelper.kt - Simple object which has load and handle your Interstitial Reward AD data
 */
object RewardedVideoAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var mListener: AdMobAdsListener? = null

    //<editor-fold desc="This Ads Related Flag">
    private var isThisAdShowing: Boolean = false
    private var isAnyIndexLoaded = false
    private var isStartToLoadAnyIndex = false
    private var isAnyIndexAlreadyLoaded = false
    //</editor-fold>

    internal var isNeedToLoadMultipleRequest: Boolean = false
    private var mAdIdPosition: Int = -1

    private var mOnAdLoaded: () -> Unit = {}
    private var mOnStartToLoadAd: () -> Unit = {}

    private fun getRewardedVideoAdModel(
        onFindModel: (index: Int, rewardedVideoAdModel: RewardedVideoAdModel) -> Unit
    ) {
        mAdIdPosition = if (mAdIdPosition < admob_rewarded_video_ad_model_list.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        logE(TAG, "getRewardedVideoAdModel: AdIdPosition -> $mAdIdPosition")

        if (mAdIdPosition >= 0 && mAdIdPosition < admob_rewarded_video_ad_model_list.size) {
            onFindModel.invoke(mAdIdPosition, admob_rewarded_video_ad_model_list[mAdIdPosition])
        } else {
            mAdIdPosition = -1
        }
    }

    // TODO: Load Single Ad Using Model Class
    private fun loadNewAd(
        fContext: Context,
        fModel: RewardedVideoAdModel,
        fIndex: Int
    ) {

        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdsID -> ${fModel.adsID}")

        fModel.isAdLoadingRunning = true
        fModel.listener?.onStartToLoadRewardVideoAd()

        RewardedAd.load(
            fContext,
            fModel.adsID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    super.onAdLoaded(rewardedAd)
                    fModel.isAdLoadingRunning = false

                    rewardedAd.apply {
                        fullScreenContentCallback = object : FullScreenContentCallback() {

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                logI(tag = TAG, message = "loadNewAd: onAdShowedFullScreenContent: Index -> $fIndex")
                                isAnyAdShowing = true
                                isThisAdShowing = true
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                logE(tag = TAG, message = "loadNewAd: onAdFailedToShowFullScreenContent: Index -> $fIndex\nErrorMessage::${adError.message}\nErrorCode::${adError.code}")
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                logI(tag = TAG, message = "loadNewAd: onAdDismissedFullScreenContent: Index -> $fIndex")
                                fModel.rewardedAd?.fullScreenContentCallback = null
                                fModel.rewardedAd = null

                                isAnyAdShowing = false
                                isAnyAdOpen = false
                                isThisAdShowing = false

                                fModel.listener?.onAdClosed()
                            }
                        }
                    }.also {
                        logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex")
                        fModel.rewardedAd = it
                        fModel.listener?.onRewardVideoAdLoaded(it)
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    logE(tag = TAG, message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}")
                    fModel.isAdLoadingRunning = false
                    fModel.rewardedAd = null
                    fModel.listener?.onAdFailed()
                }
            }
        )
    }

    private fun requestWithIndex(
        fContext: Context,
        rewardedVideoAdModel: RewardedVideoAdModel,
        index: Int,
        onStartToLoadAd: () -> Unit,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (fContext.isOnline
            && rewardedVideoAdModel.rewardedAd == null
            && !rewardedVideoAdModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = rewardedVideoAdModel.apply {
                    this.listener = object : AdMobAdsListener {

                        override fun onStartToLoadRewardVideoAd() {
                            super.onStartToLoadRewardVideoAd()
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

                        override fun onRewardVideoAdLoaded(rewardedAd: RewardedAd) {
                            super.onRewardVideoAdLoaded(rewardedAd)
                            mAdIdPosition = -1
                            logI(tag = TAG, message = "requestWithIndex: onRewardVideoAdLoaded: Index -> $index")
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
                            loadAd(fContext = fContext, onStartToLoadAd = onStartToLoadAd, onAdLoaded = onAdLoaded)
                        }

                        override fun onAdFailed() {
                            super.onAdFailed()
                            onAdFailed.invoke()
                        }
                    }
                },
                fIndex = index
            )
        } else if (fContext.isOnline
            && rewardedVideoAdModel.rewardedAd != null
        ) {
            if (!isAnyIndexAlreadyLoaded) {
                logI(tag = TAG, message = "requestWithIndex: already loaded ad Index -> $index")
                isAnyIndexAlreadyLoaded = true
                onAdLoaded.invoke()
                if (onAdLoaded != mOnAdLoaded) {
                    mOnAdLoaded.invoke()
                }
            }
        }
    }

    /**
     * Call this method when you need to load your Reward Video AD
     * you need to call this method only once in any activity or fragment
     *
     *
     * Use of this Method
     * loadAd(
     *      fContext = reference of your activity or fragment context
     *      onStartToLoadAd = {[show progress when start to load Reward Video AD]},
     *      onAdLoaded = {[hide progress after successfully load Reward Video AD]},
     * )
     *
     * @param fContext this is a reference to your activity or fragment context
     * @param onStartToLoadAd @see [AdMobAdsListener.onStartToLoadRewardVideoAd]
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

        if (admob_rewarded_video_ad_model_list.isNotEmpty()) {

            if (isNeedToLoadMultipleRequest) {
                logI(tag = TAG, message = "loadAd: Request Ad From All ID at Same Time")
                admob_rewarded_video_ad_model_list.forEachIndexed { index, rewardedVideoAdModel ->
                    requestWithIndex(
                        fContext = fContext,
                        rewardedVideoAdModel = rewardedVideoAdModel,
                        index = index,
                        onStartToLoadAd = onStartToLoadAd,
                        onAdLoaded = onAdLoaded,
                        onAdFailed = {},
                    )
                }
            } else {
                logI(tag = TAG, message = "loadAd: Request Ad After Failed Previous Index Ad")
                getRewardedVideoAdModel { index, rewardedVideoAdModel ->
                    logI(tag = TAG, message = "loadAd: getRewardedInterstitialAdModel: Index -> $index")
                    requestWithIndex(
                        fContext = fContext,
                        rewardedVideoAdModel = rewardedVideoAdModel,
                        index = index,
                        onStartToLoadAd = onStartToLoadAd,
                        onAdLoaded = onAdLoaded,
                        onAdFailed = {
                            if ((mAdIdPosition + 1) >= admob_rewarded_video_ad_model_list.size) {
                                mAdIdPosition = -1
                            } else {
                                loadAd(fContext = fContext, onStartToLoadAd = onStartToLoadAd, onAdLoaded = mOnAdLoaded)
                            }
                        },
                    )
                }
            }
        } else {
            throw RuntimeException("set RewardedVideo Ad Id First")
        }
    }

    /**
     * Call this method when you need to show your Reward Video AD
     * you need to call this method with your Activity Context
     *
     *
     * Use of this Method
     * activity.showRewardedVideoAd(
     *      onUserEarnedReward = { isUserEarnedReward -> [by Default value = false, it's true when user successfully earned reward]},
     * )
     *
     * @param onUserEarnedReward @see [AdMobAdsListener.onUserEarnedReward]
     */
    fun Activity.showRewardedVideoAd(
        onUserEarnedReward: (isUserEarnedReward: Boolean) -> Unit
    ) {
        if (!isThisAdShowing) {
            var isUserEarnedReward = false

            mListener = object : AdMobAdsListener {
                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    if (isAppForeground) {
                        onUserEarnedReward.invoke(isUserEarnedReward)
                    }
                    logI(tag = TAG, message = "showRewardedVideoAd: onAdClosed: Load New Ad after ad close")
                }
            }

            if (admob_rewarded_video_ad_model_list.isNotEmpty()) {

                val loadedAdModel: RewardedVideoAdModel? = admob_rewarded_video_ad_model_list.find { it.rewardedAd != null }

                loadedAdModel?.let {
                    val lIndex: Int = admob_rewarded_video_ad_model_list.indexOf(it)

                    if (isNeedToShowAds && !isThisAdShowing) {
                        if (it.rewardedAd != null && isOnline && !this.isFinishing) {
                            if (!isAnyAdShowing) {
                                isAnyAdShowing = true
                                isAnyAdOpen = true
                                isThisAdShowing = true

                                it.rewardedAd?.show(this) {
                                    isUserEarnedReward = true
                                    logI(tag = TAG, message = "showRewardedVideoAd: Show RewardedVideo Ad Index -> $lIndex")
                                }
                            }
                        }
                    }
                }
            } else {
                throw RuntimeException("set RewardedVideo Ad Id First")
            }
        }
    }

    fun destroy() {
        mListener = null
        isThisAdShowing = false
        isAnyIndexLoaded = false
        isStartToLoadAnyIndex = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1

        for (data in admob_rewarded_video_ad_model_list) {
            data.rewardedAd?.fullScreenContentCallback = null
            data.rewardedAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}