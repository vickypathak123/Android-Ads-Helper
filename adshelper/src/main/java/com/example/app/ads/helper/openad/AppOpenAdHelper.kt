@file:Suppress("unused")

package com.example.app.ads.helper.openad

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.AdStatusModel
import com.example.app.ads.helper.adRequestBuilder
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isAppNotPurchased
import com.example.app.ads.helper.is_enable_app_open_ad_from_remote_config
import com.example.app.ads.helper.isEnableOpenAd
import com.example.app.ads.helper.is_need_to_load_multiple_app_open_ad_request
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.list_of_admob_app_open_ads
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.nativead.NativeAdView.OnNativeAdViewListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

/**
 * @author Akshay Harsoda
 * @since 28 Nov 2022
 * @updated 25 Jun 2024
 *
 * AppOpenAdHelper.kt - Simple object which has load and handle your AppOpen AD data
 */
object AppOpenAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var isThisAdShowing: Boolean = false
    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false
    private var mAdIdPosition: Int = -1

    private var mOnAdLoaded: (isNewAdLoaded: Boolean) -> Unit = {}
    private var mListener: AdMobAdsListener<AppOpenAd>? = null

    private val isLastIndex: Boolean get() = (mAdIdPosition + 1) >= list_of_admob_app_open_ads.size

    private val isAdsIDSated: Boolean
        get() {
            if (list_of_admob_app_open_ads.isNotEmpty()) {
                return true
            } else {
                throw RuntimeException("set AppOpen Ad Id First")
            }
        }

    private fun getAppOpenAdModel(
        isNeedToLoadMultipleRequest: Boolean,
        onFindModel: (index: Int, fAdModel: AdStatusModel<AppOpenAd>) -> Unit
    ) {
        if (isAdsIDSated) {
            if (isNeedToLoadMultipleRequest) {
                logE(TAG, "getAppOpenAdModel: Load Multiple Request")
                list_of_admob_app_open_ads.forEachIndexed { index, adStatusModel ->
                    onFindModel.invoke(index, adStatusModel)
                }
            } else {
                if (list_of_admob_app_open_ads.any { it.isAdLoadingRunning }) {
                    logE(tag = TAG, message = "getAppOpenAdModel: list_of_admob_app_open_ads.any { it.isAdLoadingRunning } == true, $mAdIdPosition")
                } else {
                    logE(tag = TAG, message = "getAppOpenAdModel: list_of_admob_app_open_ads.any { it.isAdLoadingRunning } == false, $mAdIdPosition")
                    mAdIdPosition = if (mAdIdPosition < list_of_admob_app_open_ads.size) {
                        if (mAdIdPosition == -1) {
                            0
                        } else {
                            (mAdIdPosition + 1)
                        }
                    } else {
                        0
                    }

                    logE(TAG, "getAppOpenAdModel: AdIdPosition -> $mAdIdPosition")

                    if (mAdIdPosition >= 0 && mAdIdPosition < list_of_admob_app_open_ads.size) {
                        onFindModel.invoke(mAdIdPosition, list_of_admob_app_open_ads[mAdIdPosition])
                    } else {
                        mAdIdPosition = -1
                    }
                }
            }
        }
    }

    private fun loadNewAd(
        fContext: Context,
        fModel: AdStatusModel<AppOpenAd>,
        fIndex: Int
    ) {
        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdID -> ${fModel.adID}")
        fModel.isAdLoadingRunning = true

        AppOpenAd.load(
            fContext,
            fModel.adID,
            adRequestBuilder,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    super.onAdLoaded(appOpenAd)
                    fModel.isAdLoadingRunning = false

                    appOpenAd.apply {
                        fullScreenContentCallback = object : FullScreenContentCallback() {

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                logI(
                                    tag = TAG,
                                    message = "loadNewAd: onAdShowedFullScreenContent: Index -> $fIndex"
                                )
                                isAnyAdOpen = true
//                                isAnyAdShowing = true
                                isThisAdShowing = true
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

//                                isAnyAdShowing = false

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
        fModel: AdStatusModel<AppOpenAd>,
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
                    this.listener = object : AdMobAdsListener<AppOpenAd> {

                        override fun onAdLoaded(fLoadedAd: AppOpenAd) {
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
     * Call this method when you need to load your AppOpen AD
     * you need to call this method only once in your launcher activity or your application class
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

        if (isAppOpenAdEnable() && isOnline) {
            getAppOpenAdModel(isNeedToLoadMultipleRequest = is_need_to_load_multiple_app_open_ad_request) { index, fAdModel ->
                logI(tag = TAG, message = "loadAd: getAppOpenAdModel: Index -> $index")
                requestWithIndex(
                    fContext = fContext,
                    fModel = fAdModel,
                    fIndex = index,
                    onAdLoaded = {
                        callAdLoaded()
                        onAdLoaded.invoke(true)
                    },
                    onAdFailed = {
                        if (!is_need_to_load_multiple_app_open_ad_request) {
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
        } else {
            onAdLoaded.invoke(false)
        }
    }


    //<editor-fold desc="Current Ad View Callback">
    interface OnAppOpenAdLoadListener {
        fun onAdLoaded()
    }

    private var mOnAppOpenAdLoadListener: OnAppOpenAdLoadListener? = null

    fun setOnAppOpenAdLoadListener(fListener: OnAppOpenAdLoadListener) {
        mOnAppOpenAdLoadListener = fListener
    }

    private fun callAdLoaded() {
        Handler(Looper.getMainLooper()).postDelayed({
            mOnAppOpenAdLoadListener?.let {
                it.onAdLoaded()
                mOnAppOpenAdLoadListener = null
            } ?: kotlin.run { callAdLoaded() }
        }, 100)
    }
    //</editor-fold>

    internal fun isAppOpenAdEnable(): Boolean = isEnableOpenAd && isAppNotPurchased && is_enable_app_open_ad_from_remote_config

    /**
     * this method will check AppOpen Ad is Available or not
     */
    private fun isAppOpenAdAvailable(): Boolean {
        return isAppOpenAdEnable() && list_of_admob_app_open_ads.any { it.loadedAd != null }
    }

    /**
     * Call this method when you need to show AppOpen AD
     *
     * Use of this Method
     * activity.showAppOpenAd {[your code which has run after AD show or if AD fails to show]}
     * call this method with [Activity] instance
     *
     * @param onAdClosed this is a call back of your ad close, it will call also if your ad was not showing to the user
     */
    fun Activity.showAppOpenAd(onAdClosed: () -> Unit) {
        if (!isThisAdShowing && isAppOpenAdAvailable() && isOnline) {
            mListener = object : AdMobAdsListener<AppOpenAd> {
                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    if (isAppForeground) {
                        onAdClosed.invoke()
                    }

                    logI(tag = TAG, message = "showAppOpenAd: onAdClosed: Load New Ad")
                    loadAd(fContext = this@showAppOpenAd)
                }
            }
            if (isAdsIDSated) {
                list_of_admob_app_open_ads.find { it.loadedAd != null }?.let { loadedAdModel ->
                    val lIndex: Int = list_of_admob_app_open_ads.indexOf(loadedAdModel)

                    if (isAppOpenAdAvailable() && loadedAdModel.loadedAd != null && isOnline && !this.isFinishing && !isAnyAdOpen && !isThisAdShowing) {
                        isAnyAdOpen = true
                        isThisAdShowing = true
                        loadedAdModel.loadedAd?.show(this)
                        logI(tag = TAG, message = "showAppOpenAd: Show AppOpen Ad Index -> $lIndex")
                    }
                }
                if (!isThisAdShowing) {
                    mListener?.onAdClosed()
                }
            }
        } else {
            onAdClosed.invoke()
        }
    }

    fun destroy() {
        mListener = null
        isThisAdShowing = false
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1

        for (data in list_of_admob_app_open_ads) {
            data.loadedAd?.fullScreenContentCallback = null
            data.loadedAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}