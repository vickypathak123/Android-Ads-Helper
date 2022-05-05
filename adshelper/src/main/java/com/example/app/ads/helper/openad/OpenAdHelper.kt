package com.example.app.ads.helper.openad

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import com.example.app.ads.helper.*
import com.example.app.ads.helper.admob_open_ad_id
import com.example.app.ads.helper.getStringRes
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

    private fun loadOpenAd(
        @NonNull fContext: Context,
        @NonNull fListener: AdMobAdsListener
    ) {

        /*if (isAppInTesting) {
            val isTestDevice = AdRequest.Builder().build().isTestDevice(fContext)
            Log.e(TAG, "loadNativeAdvancedAd: isTestDevice::${isTestDevice}")
            if (!isTestDevice) {
                return
            }
        }*/


        var lAppOpenAd: AppOpenAd?

        AppOpenAd.load(
            fContext,
            admob_open_ad_id ?: fContext.getStringRes(R.string.admob_open_ad_id),
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    super.onAdLoaded(appOpenAd)
                    Log.i(TAG, "onAdLoaded: ")
                    lAppOpenAd = appOpenAd
                    adLoadTime = Date().time
                    fListener.onAppOpenAdLoaded(appOpenAd = appOpenAd)

                    lAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            Log.i(TAG, "onAdClosed: ")
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
                            Log.i(
                                TAG,
                                "onAdFailedToShowFullScreenContent: \nErrorMessage::${adError.message}\nErrorCode::${adError.code}"
                            )
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    Log.i(
                        TAG,
                        "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}"
                    )
                    lAppOpenAd = null
                    fListener.onAdFailed()
                }
            }
        )
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
            Log.i(TAG, "isShowOpenAd: isAdAvailable()::${isAdAvailable()}")
            if (isAdAvailable()) {
                if (!isAnyAdShowing) {
                    isAnyAdShowing = true
                    Log.i(TAG, "isShowOpenAd: Showing Open Ad")
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
        adLoadTime = 0
        isOpenAdShowing = false
        isAdLoading = false
        mListener = null
        mAppOpenAd = null
    }
}