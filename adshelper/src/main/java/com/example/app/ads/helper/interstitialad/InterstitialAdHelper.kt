@file:Suppress("unused")

package com.example.app.ads.helper.interstitialad

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.AdStatusModel
import com.example.app.ads.helper.activity.InterstitialNativeAdActivity
import com.example.app.ads.helper.adRequestBuilder
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isAppNotPurchased
import com.example.app.ads.helper.isBlockInterstitialAd
import com.example.app.ads.helper.is_enable_interstitial_ad_from_remote_config
import com.example.app.ads.helper.is_need_to_load_multiple_interstitial_ad_request
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.list_of_admob_interstitial_ads
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.nativead.NativeAdHelper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 18 Nov 2022
 * @updated 25 Jun 2024
 *
 * InterstitialAdHelper.kt - Simple object which has load and handle your Interstitial AD data
 */
object InterstitialAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var isThisAdShowing: Boolean = false
    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false
    private var isAdsShowingFlagForDeveloper: Boolean = false
    private var mAdIdPosition: Int = -1

    private var mOnAdLoaded: (isNewAdLoaded: Boolean) -> Unit = {}
    private var mListener: AdMobAdsListener<InterstitialAd>? = null

    private val isLastIndex: Boolean get() = (mAdIdPosition + 1) >= list_of_admob_interstitial_ads.size

    private val isAdsIDSated: Boolean
        get() {
            if (list_of_admob_interstitial_ads.isNotEmpty()) {
                return true
            } else {
                throw RuntimeException("set Interstitial Ad Id First")
            }
        }

    private fun getInterstitialAdModel(
        isNeedToLoadMultipleRequest: Boolean,
        onFindModel: (index: Int, fAdModel: AdStatusModel<InterstitialAd>) -> Unit
    ) {
        if (isAdsIDSated) {
            if (isNeedToLoadMultipleRequest) {
                logE(TAG, "getInterstitialAdModel: Load Multiple Request")
                list_of_admob_interstitial_ads.forEachIndexed { index, adStatusModel ->
                    onFindModel.invoke(index, adStatusModel)
                }
            } else {
                if (list_of_admob_interstitial_ads.any { it.isAdLoadingRunning }) {
                    logE(tag = TAG, message = "getInterstitialAdModel: list_of_admob_interstitial_ads.any { it.isAdLoadingRunning } == true, $mAdIdPosition")
                } else {
                    logE(tag = TAG, message = "getInterstitialAdModel: list_of_admob_interstitial_ads.any { it.isAdLoadingRunning } == false, $mAdIdPosition")
                    mAdIdPosition = if (mAdIdPosition < list_of_admob_interstitial_ads.size) {
                        if (mAdIdPosition == -1) {
                            0
                        } else {
                            (mAdIdPosition + 1)
                        }
                    } else {
                        0
                    }

                    logE(TAG, "getInterstitialAdModel: AdIdPosition -> $mAdIdPosition")

                    if (mAdIdPosition >= 0 && mAdIdPosition < list_of_admob_interstitial_ads.size) {
                        onFindModel.invoke(mAdIdPosition, list_of_admob_interstitial_ads[mAdIdPosition])
                    } else {
                        mAdIdPosition = -1
                    }
                }
            }
        }
    }

    private fun loadNewAd(
        fContext: Context,
        fModel: AdStatusModel<InterstitialAd>,
        fIndex: Int
    ) {
        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdID -> ${fModel.adID}")
        fModel.isAdLoadingRunning = true

        InterstitialAd.load(
            fContext,
            fModel.adID,
            adRequestBuilder,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    fModel.isAdLoadingRunning = false

                    interstitialAd.apply {
                        fullScreenContentCallback = object : FullScreenContentCallback() {

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                logI(
                                    tag = TAG,
                                    message = "loadNewAd: onAdShowedFullScreenContent: Index -> $fIndex"
                                )
                                isAnyAdOpen = true
                                isThisAdShowing = true
                                isAdsShowingFlagForDeveloper = true
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                logE(
                                    tag = TAG,
                                    message = "loadNewAd: onAdFailedToShowFullScreenContent: Index -> $fIndex\nErrorMessage::${adError.message}\nErrorCode::${adError.code}"
                                )
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                logI(
                                    tag = TAG,
                                    message = "loadNewAd: onAdDismissedFullScreenContent: Index -> $fIndex"
                                )
                                fModel.apply {
                                    this.loadedAd?.fullScreenContentCallback = null
                                    this.loadedAd = null
                                    isAnyAdOpen = false
                                    isThisAdShowing = false
                                    this.listener?.onAdClosed()
                                }
                            }
                        }
                    }.also {
                        logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex")
                        fModel.apply {
                            this.loadedAd = it
                            this.listener?.onAdLoaded(it)
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    logE(
                        tag = TAG,
                        message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}"
                    )

                    fModel.apply {
                        this.isAdLoadingRunning = false
                        this.loadedAd = null
                        this.listener?.onAdFailed()
                    }
                }
            }
        )
    }

    private fun requestWithIndex(
        fContext: Context,
        fModel: AdStatusModel<InterstitialAd>,
        fIndex: Int,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (isOnline
            && fModel.loadedAd == null
            && !fModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = fModel.apply {
                    this.listener = object : AdMobAdsListener<InterstitialAd> {

                        override fun onAdLoaded(fLoadedAd: InterstitialAd) {
                            super.onAdLoaded(fLoadedAd)
                            mAdIdPosition = -1
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onAdLoaded: Index -> $fIndex"
                            )
                            if (!isAnyIndexLoaded) {
                                isAnyIndexLoaded = true
                                onAdLoaded.invoke()
                                if (onAdLoaded != mOnAdLoaded) {
                                    mOnAdLoaded.invoke(true)
                                }
                            }
                        }

                        override fun onAdClosed(isShowFullScreenAd: Boolean) {
                            super.onAdClosed(isShowFullScreenAd)
                            mListener?.onAdClosed(isShowFullScreenAd)
                        }

                        override fun onAdFailed() {
                            super.onAdFailed()
                            onAdFailed.invoke()
                        }
                    }
                },
                fIndex = fIndex
            )
        } else if (isOnline && fModel.loadedAd != null) {
            if (!isAnyIndexAlreadyLoaded) {
                logI(tag = TAG, message = "requestWithIndex: already loaded ad Index -> $fIndex")
                isAnyIndexAlreadyLoaded = true
                onAdLoaded.invoke()
                if (onAdLoaded != mOnAdLoaded) {
                    mOnAdLoaded.invoke(true)
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
    internal fun loadAd(
        fContext: Context,
        onAdLoaded: (isNewAdLoaded: Boolean) -> Unit = {}
    ) {
        mOnAdLoaded = onAdLoaded
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false

        if (isInterstitialAdEnable() && isOnline) {
            getInterstitialAdModel(isNeedToLoadMultipleRequest = is_need_to_load_multiple_interstitial_ad_request) { index, fAdModel ->
                logI(tag = TAG, message = "loadAd: getInterstitialAdModel: Index -> $index")
                requestWithIndex(
                    fContext = fContext,
                    fModel = fAdModel,
                    fIndex = index,
                    onAdLoaded = {
                        callAdLoaded()
                        onAdLoaded.invoke(true)
                    },
                    onAdFailed = {
                        if (!is_need_to_load_multiple_interstitial_ad_request) {
                            if (isLastIndex) {
                                mAdIdPosition = -1
                            } else {
                                loadAd(
                                    fContext = fContext,
                                    onAdLoaded = mOnAdLoaded
                                )
                            }
                        }
                    },
                )
            }
        } else if (isInterstitialAdEnable() && NativeAdHelper.isNativeAdEnable()) {
            onAdLoaded.invoke(NativeAdHelper.isNativeAdAvailable())
        }
    }

    //<editor-fold desc="Current Ad View Callback">
    interface OnInterstitialAdLoadListener {
        fun onAdLoaded()
    }

    private var mOnInterstitialAdLoadListener: OnInterstitialAdLoadListener? = null

    fun setOnInterstitialAdLoadListener(fListener: OnInterstitialAdLoadListener) {
        mOnInterstitialAdLoadListener = fListener
    }

    private fun callAdLoaded() {
        Handler(Looper.getMainLooper()).postDelayed({
            mOnInterstitialAdLoadListener?.let {
                it.onAdLoaded()
                mOnInterstitialAdLoadListener = null
            } ?: kotlin.run { callAdLoaded() }
        }, 100)
    }
    //</editor-fold>

    internal fun isInterstitialAdEnable(): Boolean = isAppNotPurchased && is_enable_interstitial_ad_from_remote_config

    /**
     * this method will check Interstitial Ad is Available or not
     */
    private fun isInterstitialAdAvailable(): Boolean {
        return isInterstitialAdEnable() && list_of_admob_interstitial_ads.any { it.loadedAd != null }
    }

    /**
     * Call this method when you need to show Interstitial AD
     * also this method call our offline native dialog AD [InterstitialNativeAdActivity] when Interstitial Ad fails and give call bake on same way
     *
     * Use of this Method
     * activity.showInterstitialAd {[your code which has run after AD show or if AD fails to show]}
     * call this method with [Activity] instance
     *
     * @param fIsShowFullScreenNativeAd pass false if you don't need native ad if interstitial ads not loaded
     * @param onAdClosed this is a call back of your ad close, it will call also if your ad was not showing to the user
     */
    fun Activity.showInterstitialAd(
        fIsShowFullScreenNativeAd: Boolean = true,
        onAdClosed: (isAdShowing: Boolean, isShowFullScreenAd: Boolean) -> Unit

    ) {
        mListener = object : AdMobAdsListener<InterstitialAd> {
            override fun onAdClosed(isShowFullScreenAd: Boolean) {
                if (isAppForeground) {
                    onAdClosed.invoke(isAdsShowingFlagForDeveloper, isShowFullScreenAd)
                    isAdsShowingFlagForDeveloper = false
                }

                logI(tag = TAG, message = "showInterstitialAd: onAdClosed: Load New Ad")
                loadAd(fContext = this@showInterstitialAd, onAdLoaded = mOnAdLoaded)
            }
        }

        if (!isThisAdShowing && isInterstitialAdAvailable() && isOnline) {
            if (isAdsIDSated) {
                list_of_admob_interstitial_ads.find { it.loadedAd != null }?.let { loadedAdModel ->
                    val lIndex: Int = list_of_admob_interstitial_ads.indexOf(loadedAdModel)

                    if (isInterstitialAdAvailable() && loadedAdModel.loadedAd != null && isOnline && !this.isFinishing && !isAnyAdOpen && !isThisAdShowing) {
                        isAdsShowingFlagForDeveloper = false
                        isAnyAdOpen = true
                        isThisAdShowing = true
                        loadedAdModel.loadedAd?.show(this)
                        logI(
                            tag = TAG,
                            message = "showInterstitialAd: Show Interstitial Ad Index -> $lIndex"
                        )
                    } else {
                        // show native ad
                        showFullScreenNativeAdDialog(fIsShowFullScreenNativeAd = fIsShowFullScreenNativeAd)
                    }

                    if (!isThisAdShowing) {
                        mListener?.onAdClosed(false)
                    }
                } ?: kotlin.run {
                    // show native ad
                    showFullScreenNativeAdDialog(fIsShowFullScreenNativeAd = fIsShowFullScreenNativeAd)
                }
            }
        } else if (!isThisAdShowing && NativeAdHelper.isNativeAdAvailable() && isOnline && isBlockInterstitialAd) {
            showFullScreenNativeAdDialog(fIsShowFullScreenNativeAd = fIsShowFullScreenNativeAd)
        } else {
            if (!isThisAdShowing) {
                onAdClosed.invoke(false, false)
            }
        }
    }

    /**
     * After Native Ad completed
     */
    private fun Activity.showFullScreenNativeAdDialog(fIsShowFullScreenNativeAd: Boolean = true) {
        if (fIsShowFullScreenNativeAd
            && NativeAdHelper.isNativeAdAvailable()
            && isOnline
            && !this.isFinishing
            && !isAnyAdOpen
            && !isThisAdShowing
        ) {
            isAdsShowingFlagForDeveloper = false

            logI(tag = TAG, message = "showFullScreenNativeAdDialog: Try To Open Dialog...")

            InterstitialNativeAdActivity.lunchFullScreenAd(
                fActivity = this,
                onInterstitialNativeAdClosed = {
                    isAnyAdOpen = false
                    isThisAdShowing = false
                    isAdsShowingFlagForDeveloper = true
                    mListener?.onAdClosed(true)
                }
            )

            isThisAdShowing = true
        } else if (isBlockInterstitialAd) {
            mListener?.onAdClosed(false)
        }
    }

    fun destroy() {
        mListener = null
        isThisAdShowing = false
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1

        for (data in list_of_admob_interstitial_ads) {
            data.loadedAd?.fullScreenContentCallback = null
            data.loadedAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}