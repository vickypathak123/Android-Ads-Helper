@file:Suppress("unused")

package com.example.app.ads.helper.nativead

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.example.app.ads.helper.*
import com.example.app.ads.helper.reward.RewardedVideoAdHelper
import com.example.app.ads.helper.reward.RewardedVideoAdModel
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

    //<editor-fold desc="This Ads Related Flag">
    private var isThisAdShowing: Boolean = false
    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false
    //</editor-fold>

    internal var isNeedToLoadMultipleRequest: Boolean = false
    private var showingAdIndex: Int = -1
    private var mAdIdPosition: Int = -1

    private var mOnAdLoaded: (index: Int, nativeAd: NativeAd) -> Unit = { _, _ -> }

    private fun getNativeAdModel(
        onFindModel: (index: Int, nativeAdModel: NativeAdModel) -> Unit,
    ) {
        mAdIdPosition = if (mList.size == 1) {
            0
        } else if (mAdIdPosition < mList.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        logE(TAG, "getNativeAdModel: AdIdPosition -> $mAdIdPosition")

        if (mAdIdPosition >= 0 && mAdIdPosition < mList.size) {
            onFindModel.invoke(mAdIdPosition, mList[mAdIdPosition])
        } else {
            mAdIdPosition = -1
        }
    }

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
                    isInterstitialAdShow=false

                    fModel.listener?.onAdClosed()
                }
            })
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun requestWithIndex(
        fContext: Context,
        nativeAdModel: NativeAdModel,
        isAddVideoOptions: Boolean,
        adChoicesPlacement: Int,
        index: Int,
        onAdLoaded: (index: Int, nativeAd: NativeAd) -> Unit,
        onAdClosed: (index: Int) -> Unit,
        onAdFailed: (index: Int) -> Unit,
    ) {
        if (fContext.isOnline
            && !isNativeAdAvailable()
            && nativeAdModel.nativeAd == null
            && !nativeAdModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = nativeAdModel.apply {
                    this.listener = object : AdMobAdsListener {

                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            super.onNativeAdLoaded(nativeAd)
                            mAdIdPosition = -1
                            logI(tag = TAG, message = "requestWithIndex: onNativeAdLoaded: Index -> $index")
                            if (!isAnyIndexAlreadyLoaded) {
                                if (!isAnyIndexLoaded) {
                                    isAnyIndexLoaded = true
                                    showingAdIndex = index
                                    onAdLoaded.invoke(index, nativeAd)
//                                    if (onAdLoaded != mOnAdLoaded) {
//                                        mOnAdLoaded.invoke(index, nativeAd)
//                                    }
                                }
                            }
                        }

                        override fun onAdClosed(isShowFullScreenAd: Boolean) {
                            super.onAdClosed(isShowFullScreenAd)
                            showingAdIndex = -1
                            onAdClosed.invoke(index)
                        }

                        override fun onAdFailed() {
                            super.onAdFailed()
                            if (!isAnyIndexLoaded && !isAnyIndexAlreadyLoaded) {
                                onAdFailed.invoke(index)
                            }
                        }
                    }
                },
                fIndex = index,
                isAddVideoOptions = isAddVideoOptions,
                adChoicesPlacement = adChoicesPlacement
            )
        } else if (fContext.isOnline
            && nativeAdModel.nativeAd != null
        ) {
            if (showingAdIndex != -1
                && showingAdIndex == index
            ) {
                nativeAdModel.nativeAd?.let { nativeAd ->
                    if (!isAnyIndexAlreadyLoaded) {
                        logI(tag = TAG, message = "requestWithIndex: Index -> $index")
                        isAnyIndexAlreadyLoaded = true
                        onAdLoaded.invoke(index, nativeAd)
//                        if (onAdLoaded != mOnAdLoaded) {
//                            mOnAdLoaded.invoke(index, nativeAd)
//                        }
                    }
                }
            }
        }
    }

    data class TestModel(
        var fLayout: FrameLayout,
        var onAdLoaded: (index: Int, nativeAd: NativeAd) -> Unit = { _, _ -> },
        var onAdClosed: (index: Int) -> Unit = { },
        var onAdFailed: (index: Int) -> Unit = { },
    )

    internal val mViewList: ArrayList<TestModel> = ArrayList()

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
        fLayout: FrameLayout,
        isAddVideoOptions: Boolean = true,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
        onAdLoaded: (index: Int, nativeAd: NativeAd) -> Unit = { _, _ -> },
        onAdClosed: (index: Int) -> Unit = { },
        onAdFailed: (index: Int) -> Unit = { },
    ) {

        mOnAdLoaded = onAdLoaded
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false

        val viewListData = mViewList.filter { it.fLayout == fLayout }

        logE(tag = TAG, message = "loadAd: viewListData isEmpty::${viewListData.isEmpty()} -> ${fLayout.tag}")
        if (viewListData.isEmpty()) {
            mViewList.add(
                TestModel(
                    fLayout = fLayout,
                    onAdLoaded = onAdLoaded,
                    onAdClosed = onAdClosed,
                    onAdFailed = onAdFailed,
                )
            )
        }


        logE(tag = TAG, message = "loadAd: View List Size -> ${mViewList.size}  -> ${fLayout.tag}")


        if (mList.isNotEmpty()) {
            if (isNeedToLoadMultipleRequest) {
                mList.forEachIndexed { index, nativeAdModel ->
                    logI(tag = TAG, message = "loadAd: Request Ad From All ID at Same Time")
                    requestWithIndex(
                        fContext = fContext,
                        nativeAdModel = nativeAdModel,
                        isAddVideoOptions = isAddVideoOptions,
                        adChoicesPlacement = adChoicesPlacement,
                        index = index,
                        onAdLoaded = { indexLoaded, nativeAd ->
                            mViewList.forEach { data ->
                                data.onAdLoaded.invoke(indexLoaded, nativeAd)
                            }
                        },
                        onAdClosed = {
                            mViewList.forEach { data ->
                                data.onAdClosed.invoke(index)
                            }
                        },
                        onAdFailed = {
                            mViewList.forEach { data ->
                                data.onAdFailed.invoke(index)
                            }
                        },
                    )
                }
            } else {
                logI(tag = TAG, message = "loadAd: Request Ad After Failed Previous Index Ad")
                getNativeAdModel { index, nativeAdModel ->
                    logI(tag = TAG, message = "loadAd: getNativeAdModel: Index -> $index")
                    requestWithIndex(
                        fContext = fContext,
                        nativeAdModel = nativeAdModel,
                        isAddVideoOptions = isAddVideoOptions,
                        adChoicesPlacement = adChoicesPlacement,
                        index = index,
                        onAdLoaded = { indexLoaded, nativeAd ->
                            mViewList.forEach { data ->
                                data.onAdLoaded.invoke(indexLoaded, nativeAd)
                            }
                        },
                        onAdClosed = {
                            mViewList.forEach { data ->
                                data.onAdClosed.invoke(index)
                            }
                        },
                        onAdFailed = {
                            if ((mAdIdPosition + 1) >= mList.size) {
                                mAdIdPosition = -1
                                mViewList.forEach { data ->
                                    data.onAdFailed.invoke(index)
                                }
                            } else {
                                loadAd(
                                    fContext = fContext,
                                    fLayout = fLayout,
                                    isAddVideoOptions = isAddVideoOptions,
                                    adChoicesPlacement = adChoicesPlacement,
                                    onAdLoaded = onAdLoaded,
                                    onAdClosed = onAdClosed,
                                    onAdFailed = onAdFailed
                                )
                            }
                        },
                    )
                }
            }
        } else {
            throw RuntimeException("set Interstitial Ad Id First")
        }
    }

    private fun isNativeAdAvailable(): Boolean {
        return mList.find { it.nativeAd != null }?.nativeAd != null
    }

    fun destroy() {
        isThisAdShowing = false
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1
        mViewList.clear()
        for (data in mList) {
            data.nativeAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }

}