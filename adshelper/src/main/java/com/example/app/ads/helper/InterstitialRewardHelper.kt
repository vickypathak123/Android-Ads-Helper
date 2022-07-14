package com.example.app.ads.helper

import android.content.Context
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 05 Aug 2021
 *
 * InterstitialRewardHelper.kt - Simple object which has load and handle your Interstitial Reward AD data
 */
object InterstitialRewardHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    private var isUserEarnedReward: Boolean = false

    private val isRewardedInterstitialAdLoaded: Boolean
        get() {
            return mRewardedInterstitialAd != null
        }

    private var mListener: AdMobAdsListener? = null

    private var mAdIdPosition: Int = -1

    private fun getRewardedInterstitialAdID(): String? {

        mAdIdPosition = if (mAdIdPosition < admob_interstitial_ad_reward_id.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        return if (mAdIdPosition >= 0 && mAdIdPosition < admob_interstitial_ad_reward_id.size) {
            admob_interstitial_ad_reward_id[mAdIdPosition]
        } else {
            mAdIdPosition = -1
            null
        }
    }

    internal fun loadRewardedInterstitialAd(@NonNull fContext: Context, @NonNull fListener: AdMobAdsListener) {
        fListener.onStartToLoadRewardedInterstitialAd()

        getRewardedInterstitialAdID()?.let { adsID ->
            var lRewardedInterstitialAd: RewardedInterstitialAd?

            logI(tag = TAG, message = "loadRewardedInterstitialAd: AdsID -> $adsID")

            RewardedInterstitialAd.load(
                fContext,
                adsID,
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {

                    override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                        super.onAdLoaded(rewardedInterstitialAd)
                        mAdIdPosition = -1
                        logI(tag = TAG, message = "onAdLoaded: ")

                        lRewardedInterstitialAd = rewardedInterstitialAd
                        fListener.onRewardInterstitialAdLoaded(rewardedInterstitialAd = rewardedInterstitialAd)

                        lRewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

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
                                lRewardedInterstitialAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                logE(tag = TAG, message = "onAdFailedToShowFullScreenContent: \nErrorMessage::${adError.message}\nErrorCode::${adError.code}")
                            }

                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        logE(tag = TAG, message = "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}")

                        lRewardedInterstitialAd = null
                        if ((mAdIdPosition + 1) >= admob_interstitial_ad_reward_id.size) {
                            mAdIdPosition = -1
                            fListener.onAdFailed()
                        } else {
                            loadRewardedInterstitialAd(fContext, fListener)
                        }
                    }
                }
            )
        }
    }

    /**
     * Call this method when you need to load your Reward Interstitial AD
     * you need to call this method only once in any activity or fragment
     *
     * @param fContext this is a reference to your activity or fragment context
     */
    fun loadRewardedInterstitialAd(@NonNull fContext: Context) {

        if (mRewardedInterstitialAd == null) {

            loadRewardedInterstitialAd(fContext, object : AdMobAdsListener {

                override fun onStartToLoadRewardedInterstitialAd() {
                    super.onStartToLoadRewardedInterstitialAd()
                    mListener?.onStartToLoadRewardedInterstitialAd()
                }

                override fun onRewardInterstitialAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    super.onRewardInterstitialAdLoaded(rewardedInterstitialAd)
                    mRewardedInterstitialAd = rewardedInterstitialAd
                    mListener?.onAdLoaded()
                }

                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    super.onAdClosed(isShowFullScreenAd)
                    mRewardedInterstitialAd?.fullScreenContentCallback = null
                    mRewardedInterstitialAd = null
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
     * activity.isShowRewardedInterstitialAd(
     *      onStartToLoadRewardedInterstitialAd = {[show progress when start to load Reward Interstitial AD]},
     *      onUserEarnedReward = { isUserEarnedReward -> [by Default value = false, it's true when user successfully earned reward]},
     *      onAdLoaded = {[hide progress after successfully load Reward Interstitial AD]}
     * )
     *
     * @param onStartToLoadRewardedInterstitialAd @see [AdMobAdsListener.onStartToLoadRewardedInterstitialAd]
     * @param onUserEarnedReward @see [AdMobAdsListener.onUserEarnedReward]
     * @param onAdLoaded @see [AdMobAdsListener.onAdLoaded]
     */
    fun FragmentActivity.isShowRewardedInterstitialAd(
        onStartToLoadRewardedInterstitialAd: () -> Unit,
        onUserEarnedReward: (isUserEarnedReward: Boolean) -> Unit,
        onAdLoaded: () -> Unit
    ) {
        isUserEarnedReward = false

        mListener = object : AdMobAdsListener {

            override fun onStartToLoadRewardedInterstitialAd() {
                super.onStartToLoadRewardedInterstitialAd()
                onStartToLoadRewardedInterstitialAd.invoke()
            }

            override fun onUserEarnedReward(isUserEarnedReward: Boolean) {
                super.onUserEarnedReward(isUserEarnedReward)
                isInterstitialAdShow = false
                onUserEarnedReward.invoke(isUserEarnedReward)
                loadRewardedInterstitialAd(this@isShowRewardedInterstitialAd)
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
     * activity.showRewardedInterstitialAd()
     */
    fun FragmentActivity.showRewardedInterstitialAd() {
        isUserEarnedReward = false

        if (isNeedToShowAds && isRewardedInterstitialAdLoaded) {
            if (!isAnyAdShowing) {
                isAnyAdShowing = true
                isAnyAdOpen = true
                mRewardedInterstitialAd?.show(this) {
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
        mRewardedInterstitialAd = null
    }
}