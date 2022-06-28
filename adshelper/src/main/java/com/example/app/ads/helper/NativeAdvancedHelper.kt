@file:Suppress("unused")

package com.example.app.ads.helper

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

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
        fListener: AdMobAdsListener
    ) {
        removeFinishedList()

        if (!mListenerList.contains(Triple(fContext, fListener, fSize))) {
            mListenerList.add(Triple(fContext, fListener, fSize))
        }

        if (mNativeAd == null) {

            if (mTime == null) {

                mTime = SystemClock.uptimeMillis()

                Log.e(TAG, "loadNativeAdvancedAd: New Ad Loading...")

                val builder =
                    AdLoader.Builder(
                        fContext,
                        admob_native_advanced_ad_id
                            ?: fContext.getStringRes(R.string.admob_native_advanced_ad_id)
                    )

                builder.forNativeAd { unifiedNativeAd ->

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

                if (isAddVideoOptions) {
                    val videoOptions = VideoOptions.Builder()
                        .setStartMuted(false)
                        .build()

                    val adOptions: NativeAdOptions = NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE)
                        .build()

                    builder.withNativeAdOptions(adOptions)
                }

                val adLoader = builder.withAdListener(object : AdListener() {

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.i(
                            TAG,
                            "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}"
                        )

                        mTime = null
                        mNativeAd = null

                        fListener.onAdFailed()
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        isAnyAdOpen = true
                        isAnyAdShowing = true
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        isAnyAdShowing = false
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
        } else {
            Log.i(TAG, "loadAd: old stored Ad")
            mNativeAd?.let {
                fListener.onNativeAdLoaded(it)
            }
        }
    }

    internal fun destroy() {
        mListenerList.removeAll(mListenerList.toSet())
        mNativeAd?.destroy()
        mTime = null
        mNativeAd = null
    }
}