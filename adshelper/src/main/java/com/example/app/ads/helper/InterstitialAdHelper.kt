@file:Suppress("unused")

package com.example.app.ads.helper

import android.content.Context
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import com.example.app.ads.helper.activity.FullScreenNativeAdDialogActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 05 Aug 2021
 *
 * InterstitialAdHelper.kt - Simple object which has load and handle your Interstitial AD data
 */
object InterstitialAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var mInterstitialAdMob: InterstitialAd? = null

    private var mIsAdMobAdLoaded = false
    private var mIsAnyAdShow = false

    private var mListener: AdMobAdsListener? = null

    private var mIsShowFullScreenNativeAd: Boolean = true

    private var mAdIdPosition: Int = -1

    private fun getInterstitialAdID(): String? {

        mAdIdPosition = if (mAdIdPosition < admob_interstitial_ad_id.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        return if (mAdIdPosition >= 0 && mAdIdPosition < admob_interstitial_ad_id.size) {
            admob_interstitial_ad_id[mAdIdPosition]
        } else {
            mAdIdPosition = -1
            null
        }
    }

    internal fun loadAd(@NonNull fContext: Context, @NonNull fListener: AdMobAdsListener) {

        getInterstitialAdID()?.let { adsID ->

            var lInterstitialAd: InterstitialAd?

            logI(tag = TAG, message = "loadInterstitialAd: AdsID -> $adsID")

            InterstitialAd.load(
                fContext,
                adsID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        logI(tag = TAG, message = "onAdLoaded: ")
                        mAdIdPosition = -1
                        lInterstitialAd = interstitialAd
                        fListener.onInterstitialAdLoaded(interstitialAd = interstitialAd)

                        lInterstitialAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {

                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    logI(tag = TAG, message = "onAdDismissedFullScreenContent: ")
                                    lInterstitialAd = null
                                    isAnyAdShowing = false
                                    isInterstitialAdShow = false
                                    fListener.onAdClosed()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    logI(tag = TAG, message = "onAdShowedFullScreenContent: ")
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    super.onAdFailedToShowFullScreenContent(adError)
                                    logE(tag = TAG, message = "onAdFailedToShowFullScreenContent: \nErrorMessage::${adError.message}\nErrorCode::${adError.code}")
                                }

                            }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        logE(tag = TAG, message = "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}")

                        lInterstitialAd = null
                        if ((mAdIdPosition + 1) >= admob_interstitial_ad_id.size) {
                            mAdIdPosition = -1
                            fListener.onAdFailed()
                        } else {
                            loadAd(fContext, fListener)
                        }
                    }
                }
            )
        }
    }

    /**
     * Call this method when you need to load your Interstitial AD
     * you need to call this method only once in any activity or fragment
     *
     * @param fContext this is a reference to your activity context
     * @param fIsShowFullScreenNativeAd pass false if you don't need native ad if interstitial ads not loaded
     * @param onAdLoaded callback after ad successfully loaded
     */
    fun loadInterstitialAd(
        @NonNull fContext: Context,
        fIsShowFullScreenNativeAd: Boolean = true,
        onAdLoaded: () -> Unit = {}
    ) {

        this.mIsShowFullScreenNativeAd = fIsShowFullScreenNativeAd

        if (mInterstitialAdMob == null) {

            loadAd(fContext, object : AdMobAdsListener {
                override fun onAdLoaded() {
                    mIsAdMobAdLoaded = true
                }

                override fun onInterstitialAdLoaded(interstitialAd: InterstitialAd) {
                    super.onInterstitialAdLoaded(interstitialAd)
                    mIsAdMobAdLoaded = true
                    mInterstitialAdMob = interstitialAd
                    onAdLoaded.invoke()
                }

                override fun onAdFailed() {
                    mIsAdMobAdLoaded = false
                }

                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    mIsAdMobAdLoaded = false
                    mIsAnyAdShow = false
                    isAnyAdOpen = false
                    mInterstitialAdMob?.fullScreenContentCallback = null
                    mInterstitialAdMob = null
                    mListener?.onAdClosed()
                }
            })
        } else {
            onAdLoaded.invoke()
        }
    }

    /**
     * Call this method when you need to show Interstitial AD
     * also this method call our offline native dialog AD [FullScreenNativeAdDialogActivity] when Interstitial Ad fails and give call bake on same way
     *
     * Use of this Method
     * activity.isShowInterstitialAd {[your code which has run after AD show or if AD fails to show]}
     * call this method with [FragmentActivity] instance
     *
     * @param isBackAds pass true if you don't need to load ad after ad-close
     * @param onAdClosed this is a call back of your ad close, it will call also if your ad was not showing to the user
     */
    fun FragmentActivity.isShowInterstitialAd(isBackAds: Boolean = false, @NonNull onAdClosed: (isShowFullScreenAd: Boolean) -> Unit) {
        mListener = object : AdMobAdsListener {
            override fun onAdClosed(isShowFullScreenAd: Boolean) {
                isInterstitialAdShow = false
                mIsAnyAdShow = false
                if (isAppForeground) {
                    onAdClosed.invoke(isShowFullScreenAd)
                }
                if (!isBackAds) {
                    logI(tag = TAG, message = "onAdClosed: Load New Ad")
                    loadInterstitialAd(this@isShowInterstitialAd)
                }
            }
        }

        if (isBlockInterstitialAd) {
            mInterstitialAdMob = null
        }

        mIsAnyAdShow = if (!isInterstitialAdShow && isNeedToShowAds && !mIsAnyAdShow) {
            if (mIsAdMobAdLoaded && mInterstitialAdMob != null) {
                if (!isAnyAdShowing) {
                    isAnyAdShowing = true
                    isAnyAdOpen = true
                    isInterstitialAdShow = true
                    mInterstitialAdMob?.show(this)
                    logI(tag = TAG, message = "isShowInterstitialAd: Show Interstitial Ad")
                    true
                } else {
                    false
                }
            } else {
                if (mIsShowFullScreenNativeAd && NativeAdvancedModelHelper.getNativeAd != null && isOnline && !this.isFinishing) {
                    if (!isAnyAdShowing) {
                        isAnyAdShowing = true
                        logI(tag = TAG, message = "isShowInterstitialAd: Try To Open Dialog...")

                        onDialogActivityDismiss = {
                            logE(tag = TAG, message = "isShowInterstitialAd: Dialog Activity Dismiss")
                            mIsAnyAdShow = false
                            mListener?.onAdClosed(true)
                        }

                        FullScreenNativeAdDialogActivity.lunchFullScreenAd(this)

                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        } else {
            false
        }

        if (!mIsAnyAdShow) {
            mListener?.onAdClosed(false)
        }
    }

    fun destroy() {
        mAdIdPosition = -1
        mListener = null
        mIsAdMobAdLoaded = false
        mIsAnyAdShow = false
        mIsShowFullScreenNativeAd = true
        mInterstitialAdMob = null
    }
}