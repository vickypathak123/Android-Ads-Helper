package com.example.app.ads.helper.openad

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import com.example.app.ads.helper.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*

object OpenAdHelper {

    private val TAG: String = "Admob_${javaClass.simpleName}"

    private var mAppOpenAd: AppOpenAd? = null

    private var adLoadTime: Long = 0
    private var isOpenAdShowing: Boolean = false

    private var isAdLoading: Boolean = false

    private var mListener: AdMobAdsListener? = null

    private var mAdIdPosition: Int = -1

    private fun getOpenID(): String? {

        mAdIdPosition = if (mAdIdPosition < admob_open_ad_id.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        return if (mAdIdPosition >= 0 && mAdIdPosition < admob_open_ad_id.size) {
            admob_open_ad_id[mAdIdPosition]
        } else {
            mAdIdPosition = -1
            null
        }
    }

    internal fun loadOpenAd(
        @NonNull fContext: Context,
        @NonNull fListener: AdMobAdsListener
    ) {

        getOpenID()?.let { adsID ->

            var lAppOpenAd: AppOpenAd?

            logI(tag = TAG, message = "loadOpenAd: AdsID -> $adsID")

            AppOpenAd.load(
                fContext,
                adsID,
                AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(appOpenAd: AppOpenAd) {
                        super.onAdLoaded(appOpenAd)
                        logI(tag = TAG, message = "onAdLoaded: ")
                        mAdIdPosition = -1
                        lAppOpenAd = appOpenAd
                        adLoadTime = Date().time
                        fListener.onAppOpenAdLoaded(appOpenAd = appOpenAd)

                        lAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                logI(tag = TAG, message = "onAdClosed: ")
                                isOpenAdShowing = false
                                fListener.onAdClosed()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                isOpenAdShowing = true
                                lAppOpenAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                logE(tag = TAG, message = "onAdFailedToShowFullScreenContent: \nErrorMessage::${adError.message}\nErrorCode::${adError.code}")
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        logE(tag = TAG, message = "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}")

                        lAppOpenAd = null
                        if ((mAdIdPosition + 1) >= admob_open_ad_id.size) {
                            mAdIdPosition = -1
                            fListener.onAdFailed()
                        } else {
                            loadOpenAd(fContext, fListener)
                        }
                    }
                }
            )
        }
    }

    /**
     * Call this method when you need to load your Open AD
     * you need to call this method only once in your launcher activity or your application class
     *
     * @param fContext this is a reference to your activity context
     * @param onAdLoad callback after ad successfully loaded
     */
    fun loadOpenAd(@NonNull fContext: Context, @NonNull onAdLoad: () -> Unit = {}) {
        if (isAdAvailable()) {
            onAdLoad.invoke()
        } else {
            isAdLoading = true
            loadOpenAd(fContext, object : AdMobAdsListener {
                override fun onAppOpenAdLoaded(appOpenAd: AppOpenAd) {
                    super.onAppOpenAdLoaded(appOpenAd)
                    mAppOpenAd = appOpenAd
                    onAdLoad.invoke()
                    mListener?.onAdLoaded()
                }

                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    super.onAdClosed(isShowFullScreenAd)
                    isAnyAdShowing = false
                    mAppOpenAd?.fullScreenContentCallback = null
                    mAppOpenAd = null
                    mListener?.onAdClosed()

                    if (fContext is Activity) {
                        loadOpenAd(fContext = fContext, onAdLoad = { })
                    } else {
                        loadOpenAd(fContext = fContext, onAdLoad = onAdLoad)
                    }
                }
            })
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference: Long = Date().time - adLoadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour
    }

    /**
     * this method will check openAd is Available or not
     */
    fun isAdAvailable(): Boolean {
        return isOpenAdEnable && mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()
    }

    fun Activity.isShowOpenAd(@NonNull onAdClosed: () -> Unit) {
        if (!isOpenAdShowing) {
            mListener = object : AdMobAdsListener {
                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    onAdClosed.invoke()
                }
            }
            if (isAdAvailable()) {
                if (!isAnyAdShowing) {
                    isAnyAdShowing = true
                    logI(tag = TAG, message = "isShowOpenAd: Showing Open Ad")
                    mAppOpenAd?.show(this)
                } else {
                    onAdClosed.invoke()
                }
            } else {
                onAdClosed.invoke()
            }
        }
    }

    fun destroy() {
        mAdIdPosition = -1
        adLoadTime = 0
        isOpenAdShowing = false
        isAdLoading = false
        mListener = null
        mAppOpenAd = null
    }
}