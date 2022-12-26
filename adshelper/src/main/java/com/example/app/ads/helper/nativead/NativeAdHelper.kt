@file:Suppress("unused")

package com.example.app.ads.helper.nativead

import android.content.Context
import com.example.app.ads.helper.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

/**
 * @author Akshay Harsoda
 * @since 20 Dec 2022
 *
 * NativeAdHelper.kt - Simple object which has load Native Advanced AD data
 */
internal object NativeAdHelper {

    private val TAG = "Akshay_Admob_${javaClass.simpleName}"

    private var mListener: AdMobAdsListener? = null

    private var isThisAdShowing: Boolean = false

    private var mOnAdLoaded: (index: Int, nativeAd: NativeAd) -> Unit = { _, _ -> }

    private var showingAdIndex: Int = -1

    // TODO: Load Single Ad Using Model Class
    private fun loadNewAd(
        fContext: Context,
        fModel: NativeAdModel,
        fIndex: Int,
        isAddVideoOptions: Boolean,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
    ) {

        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdsID -> ${fModel.adsID}")

        fModel.isAdLoadingRunning = true

        AdLoader.Builder(fContext, fModel.adsID)
            .withNativeAdOptions(
                NativeAdOptions.Builder().apply {
                    this.setAdChoicesPlacement(adChoicesPlacement)
                    this.setMediaAspectRatio(MediaAspectRatio.LANDSCAPE)
                    if (isAddVideoOptions) {
                        this.setVideoOptions(
                            VideoOptions.Builder()
                                .setStartMuted(true)
                                .build()
                        )
                    }
                }.build()
            )
            .forNativeAd { nativeAd ->
                fModel.isAdLoadingRunning = false
                logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex")
                fModel.nativeAd = nativeAd
                fModel.listener?.onNativeAdLoaded(nativeAd = nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    fModel.isAdLoadingRunning = false
                    logE(tag = TAG, message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}")
                    fModel.nativeAd = null
                    fModel.listener?.onAdFailed()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    logI(tag = TAG, message = "loadNewAd: onAdClicked: Index -> $fIndex")
                    isAnyAdShowing = true
                    isInterstitialAdShow = true
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    logI(tag = TAG, message = "loadNewAd: onAdOpened: Index -> $fIndex")
                    isAnyAdShowing = true
                    isInterstitialAdShow = true
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    logI(tag = TAG, message = "loadNewAd: onAdClosed: Index -> $fIndex")
                    fModel.nativeAd = null

                    isAnyAdShowing = false
                    isAnyAdOpen = false
                    isThisAdShowing = false

                    fModel.listener?.onAdClosed()
                }
            })
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    /**
     * Call this method when you need to load your Native AD
     * you need to call this method only once in any activity or fragment
     *
     * @param fContext this is a reference to your activity context
     * @param isAddVideoOptions [by Default value = true] pass false if you don't need to add video option
     * @param adChoicesPlacement Ads I icon place @see [NativeAdOptions.ADCHOICES_TOP_RIGHT], [NativeAdOptions.ADCHOICES_TOP_LEFT], [NativeAdOptions.ADCHOICES_BOTTOM_RIGHT], [NativeAdOptions.ADCHOICES_BOTTOM_LEFT]
     * @param onAdLoaded callback after ad successfully loaded
     */
    internal fun loadAd(
        fContext: Context,
        isAddVideoOptions: Boolean = true,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
        onAdLoaded: (index: Int, nativeAd: NativeAd) -> Unit = { _, _ -> },
    ) {

        mOnAdLoaded = onAdLoaded

        var isAnyIndexLoaded = false
        var isAnyIndexAlreadyLoaded = false

        if (mList.isNotEmpty()) {
            mList.forEachIndexed { index, nativeAdModel ->
                if (fContext.isOnline && nativeAdModel.nativeAd == null && !nativeAdModel.isAdLoadingRunning) {
                    loadNewAd(
                        fContext = fContext,
                        fModel = nativeAdModel.apply {
                            this.listener = object : AdMobAdsListener {
                                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                                    super.onNativeAdLoaded(nativeAd)
                                    logI(tag = TAG, message = "loadAd: onNativeAdLoaded: Index -> $index")
                                    if (!isAnyIndexAlreadyLoaded) {
                                        if (!isAnyIndexLoaded) {
                                            isAnyIndexLoaded = true
                                            showingAdIndex = index
                                            onAdLoaded.invoke(index, nativeAd)
                                            if (onAdLoaded != mOnAdLoaded) {
                                                mOnAdLoaded.invoke(index, nativeAd)
                                            }
                                        }
                                    }
                                }

                                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                                    super.onAdClosed(isShowFullScreenAd)
                                    showingAdIndex = -1
                                    mListener?.onAdClosed(isShowFullScreenAd)
                                }
                            }
                        },
                        fIndex = index,
                        isAddVideoOptions = isAddVideoOptions,
                        adChoicesPlacement = adChoicesPlacement
                    )
                } else if (fContext.isOnline && nativeAdModel.nativeAd != null) {
                    if (showingAdIndex != -1 && showingAdIndex == index) {
                        nativeAdModel.nativeAd?.let { nativeAd ->
                            if (!isAnyIndexAlreadyLoaded) {
                                logI(tag = TAG, message = "loadAd: Index -> $index")
                                isAnyIndexAlreadyLoaded = true
                                onAdLoaded.invoke(index, nativeAd)
                                if (onAdLoaded != mOnAdLoaded) {
                                    mOnAdLoaded.invoke(index, nativeAd)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw RuntimeException("set Interstitial Ad Id First")
        }
    }

}