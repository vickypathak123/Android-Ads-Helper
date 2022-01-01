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

    private val TAG: String = javaClass.simpleName

    private var mAppOpenAd: AppOpenAd? = null

    private var adLoadTime: Long = 0
    private var isOpenAdShowing: Boolean = false

    private var isAdLoading: Boolean = false

    private var mListener: AdMobAdsListener? = null

    private fun loadOpenAd(
        @NonNull fContext: Context,
        @NonNull fListener: AdMobAdsListener
    ) {
        var lAppOpenAd: AppOpenAd?

        AppOpenAd.load(
            fContext,
            admob_open_ad_id ?: fContext.getStringRes(R.string.admob_open_ad_id),
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    super.onAdLoaded(appOpenAd)
                    lAppOpenAd = appOpenAd
                    adLoadTime = Date().time
                    fListener.onAppOpenAdLoaded(appOpenAd = appOpenAd)

                    lAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
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
                                "onAdFailedToShowFullScreenContent: adError::${adError.code}"
                            )
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    Log.i(
                        TAG,
                        "onAdFailedToLoad: AppOpen, Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}"
                    )
                    lAppOpenAd = null
                    fListener.onAdFailed()
                }
            }
        )
    }

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
                    mAppOpenAd?.fullScreenContentCallback = null
                    mAppOpenAd = null
                    mListener?.onAdClosed()

                    Log.e(TAG, "onAdClosed: ${fContext is Activity}")

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

    fun isAdAvailable(): Boolean {
        return isOpenAdEnable && mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()
    }

    fun Activity.isShowOpenAd(@NonNull onAdClosed: () -> Unit) {
        if (!isOpenAdShowing) {
            mListener = object : AdMobAdsListener {
                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    Log.i(TAG, "onAdClosed: ")
                    onAdClosed.invoke()
                }
            }

            if (isAdAvailable()) {
                mAppOpenAd?.show(this)
            } else {
                onAdClosed.invoke()
            }
        }
    }
}