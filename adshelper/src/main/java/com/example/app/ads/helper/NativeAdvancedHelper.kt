@file:Suppress("unused")

package com.example.app.ads.helper

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_LEFT
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT

/**
 * @author Akshay Harsoda
 * @since 05 Aug 2021
 *
 * NativeAdvancedHelper.kt - Simple object which has load Native Advanced AD data
 */
internal object NativeAdvancedHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    internal var mNativeAd: NativeAd? = null

    internal val mListenerList: ArrayList<Triple<Activity, AdMobAdsListener, NativeAdsSize>> = ArrayList()

    private var mTime: Long? = null

    internal fun removeListener() {
        val lList = mListenerList.filter { it.third != NativeAdsSize.FullScreen }
        mListenerList.removeAll(mListenerList.toSet())
        mListenerList.addAll(lList)
    }

    private fun removeFinishedList() {
        val lList = mListenerList.filter {
            !it.first.isFinishing
        }
        mListenerList.removeAll(mListenerList.toSet())
        mListenerList.addAll(lList)
    }

    internal fun callOldAdView() {
        for (lListener in mListenerList) {
            Handler(Looper.getMainLooper()).postDelayed({
                lListener.second.onStartToLoadRewardVideoAd()
            }, 500)
        }
    }

    private var mAdIdPosition: Int = -1

    private fun getNativeAdvancedAdID(): String? {

        mAdIdPosition = if (mAdIdPosition < admob_native_advanced_ad_id.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        return if (mAdIdPosition >= 0 && mAdIdPosition < admob_native_advanced_ad_id.size) {
            admob_native_advanced_ad_id[mAdIdPosition]
        } else {
            mAdIdPosition = -1
            null
        }
    }

    /**
     * Call this method when you need to load your Native Advanced AD
     * you need to call this method only once in any activity or fragment
     *
     * @param fContext this is a reference to your activity or fragment context
     * @param isAddVideoOptions [by Default value = true] pass false if you don't need to add video option
     * @param fListener see once this [AdMobAdsListener] it's a callback of ads
     */
    internal fun loadNativeAdvancedAd(
        @NonNull fContext: Activity,
        isAddVideoOptions: Boolean = true,
        fSize: NativeAdsSize,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
        fListener: AdMobAdsListener
    ) {
        removeFinishedList()

        if (!mListenerList.contains(Triple(fContext, fListener, fSize))) {
            mListenerList.add(Triple(fContext, fListener, fSize))
        }

        if (mNativeAd == null) {

            if (mTime == null) {

                getNativeAdvancedAdID()?.let { adsID ->

                    mTime = SystemClock.uptimeMillis()

                    Log.e(TAG, "loadNativeAdvancedAd: New Ad Loading...")

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "loadNativeAdvancedAd: AdsID -> $adsID")
                    }

                    val builder = AdLoader.Builder(fContext, adsID)

                    builder.forNativeAd { unifiedNativeAd ->

                        mAdIdPosition = -1

                        for (lListener in mListenerList) {
                            if (mNativeAd == null) {
                                Log.i(TAG, "loadAd: new live Ad -> ${unifiedNativeAd.headline}")
                                mNativeAd = unifiedNativeAd
                                lListener.second.onNativeAdLoaded(unifiedNativeAd)
                            } else {
                                mNativeAd?.let {
                                    Log.i(TAG, "loadAd: new live Ad -> old stored Ad")
                                    lListener.second.onNativeAdLoaded(it)
                                }
                            }
                        }
                    }

                    val adOptionsBuilder: NativeAdOptions.Builder = NativeAdOptions.Builder()

                    adOptionsBuilder.setAdChoicesPlacement(adChoicesPlacement)


                    if (isAddVideoOptions) {
                        val videoOptions = VideoOptions.Builder()
                            .setStartMuted(false)
                            .build()

                        adOptionsBuilder.setVideoOptions(videoOptions)
                        adOptionsBuilder.setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE)
                    }

                    builder.withNativeAdOptions(adOptionsBuilder.build())

                    val adLoader = builder.withAdListener(object : AdListener() {

                        val fHandler: Handler = Handler(Looper.getMainLooper())

                        val fRunnable: Runnable = object : Runnable {
                            override fun run() {
                                if (isAppForeground) {
                                    onAdClosed()
                                } else {
                                    fHandler.postDelayed(this, 1000)
                                }
                            }
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e(TAG, "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}")

                            mTime = null
                            mNativeAd = null

                            if ((mAdIdPosition + 1) >= admob_native_advanced_ad_id.size) {
                                mAdIdPosition = -1
                                fListener.onAdFailed()
                            } else {
                                loadNativeAdvancedAd(fContext, isAddVideoOptions, fSize, adChoicesPlacement, fListener)
//                                loadNativeAdvancedAd(fContext, isAddVideoOptions, fSize, fListener)
                            }
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            isAnyAdOpen = true
                            isAnyAdShowing = true
                            fHandler.postDelayed(fRunnable, 1000)
                        }

                        override fun onAdClosed() {
                            super.onAdClosed()
                            Log.i(TAG, "onAdClosed: ")
                            isAnyAdShowing = false

                            fHandler.removeCallbacks(fRunnable)
                            if (fContext.isOnline) {
                                mTime = null
                                mNativeAd = null
                                for (lListener in mListenerList) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        lListener.second.onAdClosed()
                                    }, 500)
                                }
                            }
                        }
                    }).build()

                    adLoader.loadAd(AdRequest.Builder().build())
                }
            }
        } else {
            Log.i(TAG, "loadAd: old stored Ad")
            mNativeAd?.let {
                fListener.onNativeAdLoaded(it)
            }
        }
    }

    internal fun destroy() {
        mAdIdPosition = -1
        mListenerList.removeAll(mListenerList.toSet())
        mNativeAd?.destroy()
        mTime = null
        mNativeAd = null
    }
}