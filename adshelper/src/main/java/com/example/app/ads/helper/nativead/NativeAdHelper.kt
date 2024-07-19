@file:Suppress("unused")

package com.example.app.ads.helper.nativead

import android.content.Context
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.AdStatusModel
import com.example.app.ads.helper.adRequestBuilder
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAppNotPurchased
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.is_enable_native_ad_from_remote_config
import com.example.app.ads.helper.is_need_to_load_multiple_native_ad_request
import com.example.app.ads.helper.list_of_admob_native_ads
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * @author Akshay Harsoda
 * @since 20 Dec 2022
 * @updated 05 Jul 2024
 *
 * NativeAdHelper.kt - Simple object which has load and handle your Interstitial AD data
 */
object NativeAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"


    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false


    private var mAdIdPosition: Int = -1
    private var showingAdIndex: Int = -1

    internal val listOfNativeAdsModel: ArrayList<AdStatusModel<NativeAd>> get() = list_of_admob_native_ads
    internal val isAnyIndexAdLoadingRunning: Boolean = (listOfNativeAdsModel.any { it.isAdLoadingRunning })

    private val isLastIndex: Boolean get() = (mAdIdPosition + 1) >= listOfNativeAdsModel.size

    private val isAdsIDSated: Boolean
        get() {
            if (listOfNativeAdsModel.isNotEmpty()) {
                return true
            } else {
                throw RuntimeException("set Native Ad Id First")
            }
        }

    private fun getNativeAdModel(
        isNeedToLoadMultipleRequest: Boolean,
        onFindModel: (index: Int, fAdModel: AdStatusModel<NativeAd>) -> Unit
    ) {
        if (isAdsIDSated) {
            if (isNeedToLoadMultipleRequest) {
                logE(TAG, "getNativeAdModel: Load Multiple Request")
                listOfNativeAdsModel.forEachIndexed { index, adStatusModel ->
                    onFindModel.invoke(index, adStatusModel)
                }
            } else {
                if (listOfNativeAdsModel.any { it.isAdLoadingRunning }) {
                    logE(tag = TAG, message = "getNativeAdModel: listOfNativeAdsModel.any { it.isAdLoadingRunning } == true, $mAdIdPosition")
                } else {
                    logE(tag = TAG, message = "getNativeAdModel: listOfNativeAdsModel.any { it.isAdLoadingRunning } == false, $mAdIdPosition")
                    mAdIdPosition = if (mAdIdPosition < listOfNativeAdsModel.size) {
                        if (mAdIdPosition == -1) {
                            0
                        } else {
                            (mAdIdPosition + 1)
                        }
                    } else {
                        0
                    }

                    logE(TAG, "getNativeAdModel: AdIdPosition -> $mAdIdPosition")

                    if (mAdIdPosition >= 0 && mAdIdPosition < listOfNativeAdsModel.size) {
                        onFindModel.invoke(mAdIdPosition, listOfNativeAdsModel[mAdIdPosition])
                    } else {
                        mAdIdPosition = -1
                    }
                }
            }
        }
    }

    private fun loadNewAd(
        fContext: Context,
        fModel: AdStatusModel<NativeAd>,
        fIndex: Int,
        isAddVideoOptions: Boolean,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
        @NativeAdOptions.NativeMediaAspectRatio mediaAspectRatio: Int,
    ) {
        if (!(listOfNativeAdsModel.any { it.isAdLoadingRunning })) {
            logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdID -> ${fModel.adID}")
            fModel.isAdLoadingRunning = true

            var adLoader: AdLoader? = null
            val adLoaderBuilder: AdLoader.Builder = AdLoader.Builder(fContext, fModel.adID)
                .forNativeAd { nativeAd ->
                    adLoader?.let { loader ->
                        if (!loader.isLoading) {
                            // The AdLoader has finished loading ads.
                            logI(tag = TAG, message = "loadNewAd: forNativeAd: Index -> $fIndex")
                            fModel.apply {
                                this.isAdLoadingRunning = false
                                this.loadedAd = nativeAd
                                this.listener?.onAdLoaded(fLoadedAd = nativeAd)
                            }
                        } else {
                            // The AdLoader is still loading ads.
                            // Expect more adLoaded or onAdFailedToLoad callbacks.
                            logE(tag = TAG, message = "loadNewAd: forNativeAd: AdLoader is still loading ads")
                        }
                    }
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        logE(tag = TAG, message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}")

                        val model: AdStatusModel<NativeAd> = listOfNativeAdsModel.find { it.loadedAd != null } ?: fModel
                        model.apply {
                            this.isAdLoadingRunning = false
                            this.loadedAd?.destroy()
                            this.loadedAd = null
                            this.listener?.onAdFailed()
                        }
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        logI(
                            tag = TAG,
                            message = "loadNewAd: onAdClosed: Index -> $fIndex"
                        )
                        val model: AdStatusModel<NativeAd> = listOfNativeAdsModel.find { it.loadedAd != null } ?: fModel
                        model.apply {
                            this.loadedAd?.destroy()
                            this.loadedAd = null
                            isAnyAdOpen = false
                            this.listener?.onAdClosed()
                        }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        logI(
                            tag = TAG,
                            message = "loadNewAd: onAdClicked: Index -> $fIndex"
                        )
                        isAnyAdOpen = true
                    }
                }.also { fModel.defaultAdListener = it }
                )
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(adChoicesPlacement)
                        .setMediaAspectRatio(mediaAspectRatio)
                        .setRequestMultipleImages(true)
                        .apply {
                            if (isAddVideoOptions) {
                                this.setVideoOptions(
                                    VideoOptions.Builder()
                                        .setStartMuted(true)
                                        .build()
                                )
                            }
                        }.build()
                )

            adLoader = adLoaderBuilder.build()
            adLoader.loadAd(adRequestBuilder)
        }
    }

    private fun requestWithIndex(
        fContext: Context,
        fModel: AdStatusModel<NativeAd>,
        isAddVideoOptions: Boolean,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
        @NativeAdOptions.NativeMediaAspectRatio mediaAspectRatio: Int,
        fIndex: Int,
        onAdLoaded: (nativeAd: NativeAd) -> Unit,
        onAdClosed: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (isOnline
            && !isNativeAdAvailable()
            && fModel.loadedAd == null
            && !fModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = fModel.apply {
                    this.listener = object : AdMobAdsListener<NativeAd> {

                        override fun onAdLoaded(fLoadedAd: NativeAd) {
                            super.onAdLoaded(fLoadedAd)
                            mAdIdPosition = -1
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onAdLoaded: Index -> $fIndex"
                            )
                            if (!isAnyIndexAlreadyLoaded) {
                                if (!isAnyIndexLoaded) {
                                    isAnyIndexLoaded = true
                                    showingAdIndex = fIndex
                                    onAdLoaded.invoke(fLoadedAd)
                                }
                            }
                        }

                        override fun onAdClosed(isShowFullScreenAd: Boolean) {
                            super.onAdClosed(isShowFullScreenAd)
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onAdClosed: Index -> $fIndex"
                            )
                            showingAdIndex = -1
                            onAdClosed.invoke()
                        }

                        override fun onAdFailed() {
                            super.onAdFailed()
                            if (!isAnyIndexLoaded && !isAnyIndexAlreadyLoaded) {
                                onAdFailed.invoke()
                            }
                        }
                    }
                },
                isAddVideoOptions = isAddVideoOptions,
                adChoicesPlacement = adChoicesPlacement,
                mediaAspectRatio = mediaAspectRatio,
                fIndex = fIndex
            )
        } else if (isOnline && fModel.loadedAd != null) {
            if (showingAdIndex != -1 && showingAdIndex == fIndex) {
                fModel.loadedAd?.let { nativeAd ->
                    if (!isAnyIndexAlreadyLoaded) {
                        logI(tag = TAG, message = "requestWithIndex: already loaded ad Index -> $fIndex")
                        isAnyIndexAlreadyLoaded = true
                        onAdLoaded.invoke(nativeAd)
                    }
                }
            }
        }
    }

    /**
     * Call this method when you need to load your Interstitial AD
     * you need to call this method only once in any activity or fragment
     *
     * @param fContext this is a reference to your activity context
     * @param onAdLoaded callback after ad successfully loaded
     */
    private fun loadAd(
        fContext: Context,
        isAddVideoOptions: Boolean,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
        @NativeAdOptions.NativeMediaAspectRatio mediaAspectRatio: Int,
        onAdLoaded: (nativeAd: NativeAd) -> Unit,
        onAdClosed: () -> Unit,
        onAdFailed: () -> Unit,
    ) {
        var isRequestNewAd = true
        if (isNativeAdAvailable()) {
            if (isNativeAdEnable()) {
                listOfNativeAdsModel.firstOrNull { it.loadedAd != null }?.let { item ->
                    item.loadedAd?.let {
                        isRequestNewAd = false
                        onAdLoaded.invoke(it)
                    }
                }
            }
        }

        if (isRequestNewAd) {
            isAnyIndexLoaded = false
            isAnyIndexAlreadyLoaded = false

            if (isNativeAdEnable() && isOnline) {
                if (!(listOfNativeAdsModel.any { it.isAdLoadingRunning })) {
                    getNativeAdModel(isNeedToLoadMultipleRequest = is_need_to_load_multiple_native_ad_request) { index, fAdModel ->
                        logI(tag = TAG, message = "loadAd: getNativeAdModel: Index -> $index")
                        requestWithIndex(
                            fContext = fContext,
                            fModel = fAdModel,
                            isAddVideoOptions = isAddVideoOptions,
                            adChoicesPlacement = adChoicesPlacement,
                            mediaAspectRatio = mediaAspectRatio,
                            fIndex = index,
                            onAdLoaded = { nativeAd ->
                                onAdLoaded.invoke(nativeAd)
                            },
                            onAdClosed = {
                                logI(tag = TAG, message = "loadAd: onAdClosed: Index -> $index")
                                onAdClosed.invoke()
                                loadAd(
                                    fContext = fContext,
                                    isAddVideoOptions = isAddVideoOptions,
                                    adChoicesPlacement = adChoicesPlacement,
                                    mediaAspectRatio = mediaAspectRatio,
                                    onAdLoaded = onAdLoaded,
                                    onAdClosed = onAdClosed,
                                    onAdFailed = onAdFailed
                                )
                            },
                            onAdFailed = {
                                if (!is_need_to_load_multiple_native_ad_request) {
                                    if (isLastIndex) {
                                        mAdIdPosition = -1
                                        onAdFailed.invoke()
                                    } else {
                                        loadAd(
                                            fContext = fContext,
                                            isAddVideoOptions = isAddVideoOptions,
                                            adChoicesPlacement = adChoicesPlacement,
                                            mediaAspectRatio = mediaAspectRatio,
                                            onAdLoaded = onAdLoaded,
                                            onAdClosed = onAdClosed,
                                            onAdFailed = onAdFailed
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
//            } else if (!isAppNotPurchased || !is_enable_native_ad_from_remote_config) {
            } else if (!isNativeAdEnable()) {
                onAdClosed.invoke()
            }
        }
    }

    fun loadAd(
        fContext: Context,
        fNativeAdView: NativeAdView,
        isAddVideoOptions: Boolean,
        @NativeAdOptions.AdChoicesPlacement adChoicesPlacement: Int,
        @NativeAdOptions.NativeMediaAspectRatio mediaAspectRatio: Int,
        onAdLoaded: (nativeAd: NativeAd) -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdFailed: () -> Unit = {},
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            listOfNativeAdView.filter { it.fNativeAdView == fNativeAdView }.let { listOfSavedNativeAdView ->
                if (listOfSavedNativeAdView.isEmpty()) {
                    logE(tag = TAG, message = "loadAd: View Added")
                    listOfNativeAdView.add(
                        NativeAdViewModel(
                            fNativeAdView = fNativeAdView,
                            onAdLoaded = onAdLoaded,
                            onAdClosed = onAdClosed,
                            onAdFailed = onAdFailed,
                        )
                    )
                }
            }

            if (!isAnyIndexAdLoadingRunning) {
                listOfNativeAdsModel.find { it.loadedAd != null }?.let { fModel ->
                    logE(tag = TAG, message = "onOldAdRequest: ")
                } ?: kotlin.run {
                    logE(tag = TAG, message = "onOldAdRequest Null: 1")
                }
            } else {
                logE(tag = TAG, message = "onOldAdRequest Null: 2")
            }

            loadAd(
                fContext = fContext,
                isAddVideoOptions = isAddVideoOptions,
                adChoicesPlacement = adChoicesPlacement,
                mediaAspectRatio = mediaAspectRatio,
                onAdLoaded = { nativeAd ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (listOfNativeAdView.isNotEmpty()) {
                            listOfNativeAdView.forEach {
                                if (it.fNativeAdView.isAttachedToWindow) {
                                    it.onAdLoaded.invoke(nativeAd)
                                }
                            }
                        }
                    }
                },
                onAdClosed = {
                    logI(tag = TAG, message = "loadAd: onAdClosed: Index -> ${listOfNativeAdView.size}")
                    CoroutineScope(Dispatchers.IO).launch {
                        if (listOfNativeAdView.isNotEmpty()) {
                            listOfNativeAdView.forEach {
                                if (it.fNativeAdView.isAttachedToWindow) {
                                    it.onAdClosed.invoke()
                                }
                            }
                        }
                    }
                },
                onAdFailed = {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (listOfNativeAdView.isNotEmpty()) {
                            listOfNativeAdView.forEach {
                                if (it.fNativeAdView.isAttachedToWindow) {
                                    it.onAdFailed.invoke()
                                }
                            }
                        }
                    }
                }
            )
        }

    }

    private data class NativeAdViewModel(
        var fNativeAdView: NativeAdView,
        var onAdLoaded: (nativeAd: NativeAd) -> Unit,
        var onAdClosed: () -> Unit,
        var onAdFailed: () -> Unit,
    )


    private val listOfNativeAdView: ArrayList<NativeAdViewModel> = ArrayList()

    /**
     * this method will check Interstitial Ad is Available or not
     */
    fun isNativeAdAvailable(): Boolean {
        return isNativeAdEnable() && listOfNativeAdsModel.any { it.loadedAd != null }
    }

    fun isNativeAdEnable(): Boolean {
        return isAppNotPurchased && is_enable_native_ad_from_remote_config
    }

//    fun addView(fNativeAdView: NativeAdView) {
//        CoroutineScope(Dispatchers.IO).launch {
//            logE(tag = TAG, message = "removeView: listOfNativeAdView Size Before Remove::${listOfNativeAdView.size}")
//            listOfNativeAdView.find { it.fNativeAdView == fNativeAdView }?.let {
//                listOfNativeAdView.remove(it)
//            }
//            logE(tag = TAG, message = "removeView: listOfNativeAdView Size After Remove::${listOfNativeAdView.size}")
//        }
//    }

    fun removeView(fNativeAdView: NativeAdView) {
        CoroutineScope(Dispatchers.IO).launch {
            logE(tag = TAG, message = "removeView: listOfNativeAdView Size Before Remove::${listOfNativeAdView.size}")
            listOfNativeAdView.find { it.fNativeAdView == fNativeAdView }?.let {
                listOfNativeAdView.remove(it)
            }
            logE(tag = TAG, message = "removeView: listOfNativeAdView Size After Remove::${listOfNativeAdView.size}")
        }
    }

    fun destroy() {
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1

        for (data in listOfNativeAdsModel) {
            data.loadedAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}