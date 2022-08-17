package com.example.app.ads.helper

import android.content.Context
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 05 Aug 2021
 *
 * RewardVideoHelper.kt - Simple object which has load and handle your multiple size Reward Video AD data
 */
object RewardVideoHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var mRewardedAd: RewardedAd? = null

    private var isUserEarnedReward: Boolean = false

    private val isRewardVideoAdLoaded: Boolean
        get() {
            return mRewardedAd != null
        }

    private var mListener: AdMobAdsListener? = null

    private var mAdIdPosition: Int = -1

    private fun getRewardVideoAdID(): String? {

        mAdIdPosition = if (mAdIdPosition < admob_reward_video_ad_id.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        return if (mAdIdPosition >= 0 && mAdIdPosition < admob_reward_video_ad_id.size) {
            admob_reward_video_ad_id[mAdIdPosition]
        } else {
            mAdIdPosition = -1
            null
        }
    }

    internal fun loadRewardVideoAd(
        @NonNull fContext: Context,
        @NonNull fListener: AdMobAdsListener
    ) {
        fListener.onStartToLoadRewardVideoAd()

        getRewardVideoAdID()?.let { adsID ->

            var lRewardedAd: RewardedAd?

            if (BuildConfig.DEBUG) {
                logI(tag = TAG, message = "loadRewardVideoAd: AdsID -> $adsID")
            }

            RewardedAd.load(
                fContext,
                adsID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        logI(tag = TAG, message = "onAdLoaded: ")
                        mAdIdPosition = -1
                        lRewardedAd = rewardedAd
                        fListener.onRewardVideoAdLoaded(rewardedAd = rewardedAd)

                        lRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                logI(tag = TAG, message = "onAdDismissedFullScreenContent: ")
                                isInterstitialAdShow = false
                                isAnyAdShowing = false
                                fListener.onAdClosed()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                logI(tag = TAG, message = "onAdShowedFullScreenContent: ")
                                lRewardedAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                logE(tag = TAG, message = "onAdFailedToShowFullScreenContent: \nErrorMessage::${adError.message}\nErrorCode::${adError.code}")
                            }

                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        logE(tag = TAG, message = "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}")

                        lRewardedAd = null
                        if ((mAdIdPosition + 1) >= admob_reward_video_ad_id.size) {
                            mAdIdPosition = -1
                            fListener.onAdFailed()
                        } else {
                            loadRewardVideoAd(fContext, fListener)
                        }
                    }
                }
            )
        }
    }

    /**
     * Call this method when you need to load your Reward Video AD
     * you need to call this method only once in any activity or fragment
     *
     * @param fContext this is a reference to your activity or fragment context
     */
    fun loadRewardVideoAd(@NonNull fContext: Context) {

        if (mRewardedAd == null) {
            loadRewardVideoAd(fContext, object : AdMobAdsListener {

                override fun onStartToLoadRewardVideoAd() {
                    super.onStartToLoadRewardVideoAd()
                    mListener?.onStartToLoadRewardVideoAd()
                }

                override fun onRewardVideoAdLoaded(rewardedAd: RewardedAd) {
                    super.onRewardVideoAdLoaded(rewardedAd)
                    mRewardedAd = rewardedAd
                    mListener?.onAdLoaded()
                }

                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    super.onAdClosed(isShowFullScreenAd)
                    isAnyAdOpen = false
                    mRewardedAd?.fullScreenContentCallback = null
                    mRewardedAd = null
                    mListener?.onUserEarnedReward(isUserEarnedReward = isUserEarnedReward)
                }
            })
        } else {
            mListener?.onAdLoaded()
        }
    }

    /**
     * Call this method in your onCreate Method of activity or fragment
     *
     * Use of this Method
     * activity.isShowRewardVideoAd(
     *      onStartToLoadRewardVideoAd = {[show progress when start to load reward video AD]},
     *      onUserEarnedReward = { isUserEarnedReward -> [by Default value = false, it's true when user successfully earned reward]},
     *      onAdLoaded = {[hide progress after successfully load reward video AD]}
     * )
     *
     * @param onStartToLoadRewardVideoAd @see [AdMobAdsListener.onStartToLoadRewardVideoAd]
     * @param onUserEarnedReward @see [AdMobAdsListener.onUserEarnedReward]
     * @param onAdLoaded @see [AdMobAdsListener.onAdLoaded]
     */
    fun FragmentActivity.isShowRewardVideoAd(
        onStartToLoadRewardVideoAd: () -> Unit,
        onUserEarnedReward: (isUserEarnedReward: Boolean) -> Unit,
        onAdLoaded: () -> Unit
    ) {
        isUserEarnedReward = false

        mListener = object : AdMobAdsListener {

            override fun onStartToLoadRewardVideoAd() {
                super.onStartToLoadRewardVideoAd()
                onStartToLoadRewardVideoAd.invoke()
            }

            override fun onUserEarnedReward(isUserEarnedReward: Boolean) {
                super.onUserEarnedReward(isUserEarnedReward)
                isInterstitialAdShow = false
                onUserEarnedReward.invoke(isUserEarnedReward)
                loadRewardVideoAd(this@isShowRewardVideoAd)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                onAdLoaded.invoke()
            }
        }
    }

    /**
     * Call this method in when you need to show in your activity or fragment
     *
     * Use of this Method
     * activity.showRewardVideoAd()
     *
     * @param isAdShow call when ad showing
     */
    fun FragmentActivity.showRewardVideoAd(isAdShow: () -> Unit = {}) {
        isUserEarnedReward = false

        if (isNeedToShowAds && isRewardVideoAdLoaded) {
            if (!isAnyAdShowing) {
                isAnyAdShowing = true
                isAdShow.invoke()
                isAnyAdOpen = true
                mRewardedAd?.show(this) {
                    isUserEarnedReward = true
                }
                isInterstitialAdShow = true
            }
        }
    }

    fun destroy() {
        mAdIdPosition = -1
        mListener = null
        isUserEarnedReward = false
        mRewardedAd = null
    }
}