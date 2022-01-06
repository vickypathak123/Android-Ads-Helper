@file:Suppress("unused")

package com.example.app.ads.helper

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
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

    internal val mListenerList: ArrayList<Pair<Context, AdMobAdsListener>> = ArrayList()

    /**
     * Call this method when you need to load your Native Advanced AD
     * you need to call this method only once in any activity or fragment
     *
     * @param fContext this is a reference to your activity or fragment context
     * @param isAddVideoOptions [by Default value = true] pass false if you don't need to add video option
     * @param fListener see once this [AdMobAdsListener] it's a callback of ads
     */
    internal fun loadNativeAdvancedAd(
        @NonNull fContext: Context,
        isAddVideoOptions: Boolean = true,
        fListener: AdMobAdsListener
    ) {
        if (!mListenerList.contains(Pair(fContext, fListener))) {
            Log.e(TAG, "loadNativeAdvancedAd: New Listener::${fContext}")
            mListenerList.add(Pair(fContext, fListener))
        }

        if (mNativeAd == null) {

            val builder =
                AdLoader.Builder(
                    fContext,
                    admob_native_advanced_ad_id
                        ?: fContext.getStringRes(R.string.admob_native_advanced_ad_id)
                )

            builder.forNativeAd { unifiedNativeAd ->
                if (mNativeAd == null) {
                    Log.i(TAG, "loadAd: new live Ad -> ${unifiedNativeAd.headline}")
                    mNativeAd = unifiedNativeAd
                    fListener.onNativeAdLoaded(unifiedNativeAd)
                } else {
                    mNativeAd?.let {
                        Log.i(TAG, "loadAd: new live Ad -> old stored Ad")
                        fListener.onNativeAdLoaded(it)
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

                override fun onAdClicked() {
                    super.onAdClicked()
                    isAnyAdOpen = true
                    isAnyAdShowing = true
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    isAnyAdShowing = false
                    if (fContext.isOnline) {
                        mNativeAd = null
                        for (lListener in mListenerList) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                Log.i(TAG, "onAdClosed: ")
                                lListener.second.onAdClosed()
                            }, 500)
                        }
                    }
                }
            }).build()

            adLoader.loadAd(AdRequest.Builder().build())
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
        mNativeAd = null
    }
}