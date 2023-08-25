package com.example.app.ads.helper.reward

import android.app.Activity
import android.content.Context
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.admob_rewarded_interstitial_ad_model_list
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAnyAdShowing
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isNeedToShowAds
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 02 Dec 2022
 *
 * RewardedInterstitialAdHelper.kt - Simple object which has load and handle your Interstitial Reward AD data
 */
object RewardedInterstitialAdHelper {

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

    private fun getRewardedInterstitialAdModel(
        onFindModel: (index: Int, rewardedInterstitialAdModel: RewardedInterstitialAdModel) -> Unit
    ) {
        mAdIdPosition = if (mAdIdPosition < admob_rewarded_interstitial_ad_model_list.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        logE(TAG, "getRewardedInterstitialAdModel: AdIdPosition -> $mAdIdPosition")

        if (mAdIdPosition >= 0 && mAdIdPosition < admob_rewarded_interstitial_ad_model_list.size) {
            onFindModel.invoke(
                mAdIdPosition,
                admob_rewarded_interstitial_ad_model_list[mAdIdPosition]
            )
        } else {
            mAdIdPosition = -1
        }
    }

    // TODO: Load Single Ad Using Model Class
    private fun loadNewAd(
        fContext: Context,
        fModel: RewardedInterstitialAdModel,
        fIndex: Int
    ) {

        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdsID -> ${fModel.adsID}")

        fModel.isAdLoadingRunning = true
        fModel.listener?.onStartToLoadRewardedInterstitialAd()

        RewardedInterstitialAd.load(
            fContext,
            fModel.adsID,
            AdRequest.Builder().build(),
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
                                isAnyAdShowing = true
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
                                fModel.rewardedInterstitialAd?.fullScreenContentCallback = null
                                fModel.rewardedInterstitialAd = null

                                isAnyAdShowing = false
                                isAnyAdOpen = false
                                isThisAdShowing = false

                                fModel.listener?.onAdClosed()
                            }
                        }
                    }.also {
                        logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex")
                        fModel.rewardedInterstitialAd = it
                        fModel.listener?.onRewardInterstitialAdLoaded(it)
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    logE(
                        tag = TAG,
                        message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}"
                    )
                    fModel.isAdLoadingRunning = false
                    fModel.rewardedInterstitialAd = null
                    fModel.listener?.onAdFailed()
                }
            }
        )
    }

    private fun requestWithIndex(
        fContext: Context,
        rewardedInterstitialAdModel: RewardedInterstitialAdModel,
        index: Int,
        onStartToLoadAd: () -> Unit,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (fContext.isOnline
            && rewardedInterstitialAdModel.rewardedInterstitialAd == null
            && !rewardedInterstitialAdModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = rewardedInterstitialAdModel.apply {
                    this.listener = object : AdMobAdsListener {

                        override fun onStartToLoadRewardedInterstitialAd() {
                            super.onStartToLoadRewardedInterstitialAd()

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

                        override fun onRewardInterstitialAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                            super.onRewardInterstitialAdLoaded(rewardedInterstitialAd)
                            mAdIdPosition = -1
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onRewardInterstitialAdLoaded: Index -> $index"
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
                fIndex = index
            )
        } else if (fContext.isOnline
            && rewardedInterstitialAdModel.rewardedInterstitialAd != null
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
     * @param onStartToLoadAd @see [AdMobAdsListener.onStartToLoadRewardedInterstitialAd]
     * @param onAdLoaded @see [AdMobAdsListener.onAdLoaded]
     * @param isNeedToShow check if Subscribe is done then ads will not show
     * @param remoteConfig check remote Config parameters is if true ads will show else false ads will not show
     */
    fun loadAd(
        fContext: Context,
        isNeedToShow: Boolean = true,
        onStartToLoadAd: () -> Unit,
        onAdLoaded: () -> Unit,

        ) {
        if (isNeedToShow && VasuAdsConfig.with(fContext).remoteConfigInterstitialRewardAds && fContext.isOnline) {
            mOnAdLoaded = onAdLoaded
            mOnStartToLoadAd = onStartToLoadAd
            isAnyIndexLoaded = false
            isStartToLoadAnyIndex = false
            isAnyIndexAlreadyLoaded = false

            if (admob_rewarded_interstitial_ad_model_list.isNotEmpty()) {

                if (isNeedToLoadMultipleRequest) {
                    logI(tag = TAG, message = "loadAd: Request Ad From All ID at Same Time")
                    admob_rewarded_interstitial_ad_model_list.forEachIndexed { index, rewardedInterstitialAdModel ->
                        requestWithIndex(
                            fContext = fContext,
                            rewardedInterstitialAdModel = rewardedInterstitialAdModel,
                            index = index,
                            onStartToLoadAd = onStartToLoadAd,
                            onAdLoaded = onAdLoaded,
                            onAdFailed = {},
                        )
                    }
                } else {
                    logI(tag = TAG, message = "loadAd: Request Ad After Failed Previous Index Ad")
                    getRewardedInterstitialAdModel { index, rewardedInterstitialAdModel ->
                        logI(
                            tag = TAG,
                            message = "loadAd: getRewardedInterstitialAdModel: Index -> $index"
                        )
                        requestWithIndex(
                            fContext = fContext,
                            rewardedInterstitialAdModel = rewardedInterstitialAdModel,
                            index = index,
                            onStartToLoadAd = onStartToLoadAd,
                            onAdLoaded = onAdLoaded,
                            onAdFailed = {
                                if ((mAdIdPosition + 1) >= admob_rewarded_interstitial_ad_model_list.size) {
                                    mAdIdPosition = -1
                                } else {
                                    loadAd(
                                        fContext = fContext,
                                        onStartToLoadAd = onStartToLoadAd,
                                        onAdLoaded = mOnAdLoaded
                                    )
                                }
                            },
                        )
                    }
                }
            } else {
                throw RuntimeException("set RewardedInterstitial Ad Id First")
            }
        } else {
            onStartToLoadAd.invoke()
            onAdLoaded.invoke()
        }
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
     * @param isNeedToShow check if Subscribe is done then ads will not show
     * @param remoteConfig check remote Config parameters is if true ads will show else false ads will not show
     */
    fun Activity.showRewardedInterstitialAd(
        isNeedToShow: Boolean = true,
        onUserEarnedReward: (isUserEarnedReward: Boolean) -> Unit,
    ) {
        if (isNeedToShow && VasuAdsConfig.with(this).remoteConfigInterstitialRewardAds) {
            if (!isThisAdShowing) {
                var isUserEarnedReward = false

                mListener = object : AdMobAdsListener {
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

                if (admob_rewarded_interstitial_ad_model_list.isNotEmpty()) {

                    val loadedAdModel: RewardedInterstitialAdModel? =
                        admob_rewarded_interstitial_ad_model_list.find { it.rewardedInterstitialAd != null }

                    loadedAdModel?.let {
                        val lIndex: Int = admob_rewarded_interstitial_ad_model_list.indexOf(it)

                        if (!isThisAdShowing) {
                            if (it.rewardedInterstitialAd != null && isOnline && !this.isFinishing) {
                                if (!isAnyAdShowing) {
                                    isAnyAdShowing = true
                                    isAnyAdOpen = true
                                    isThisAdShowing = true

                                    it.rewardedInterstitialAd?.show(this) {
                                        isUserEarnedReward = true
                                        logI(
                                            tag = TAG,
                                            message = "showRewardedInterstitialAd: Show RewardedInterstitial Ad Index -> $lIndex"
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    throw RuntimeException("set RewardedInterstitial Ad Id First")
                }
            }
        } else {
            onUserEarnedReward.invoke(true)
        }
    }

    fun destroy() {
        mListener = null
        isThisAdShowing = false
        isAnyIndexLoaded = false
        isStartToLoadAnyIndex = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1

        for (data in admob_rewarded_interstitial_ad_model_list) {
            data.rewardedInterstitialAd?.fullScreenContentCallback = null
            data.rewardedInterstitialAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}