package com.example.app.ads.helper.interstitialad

import android.app.Activity
import android.content.Context
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.NativeAdvancedModelHelper
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.activity.FullScreenNativeAdDialogActivity
import com.example.app.ads.helper.admob_interstitial_ad_model_list
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAnyAdShowing
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isInterstitialAdShow
import com.example.app.ads.helper.isNeedToShowAds
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.mList
import com.example.app.ads.helper.onDialogActivityDismiss
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * @author Akshay Harsoda
 * @since 18 Nov 2022
 *
 * InterstitialAdHelper.kt - Simple object which has load and handle your Interstitial AD data
 */
object InterstitialAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var mListener: AdMobAdsListener? = null

    //<editor-fold desc="This Ads Related Flag">
    private var isThisAdShowing: Boolean = false
    private var mIsShowFullScreenNativeAd: Boolean = true
    private var isAdsShowingFlagForDeveloper: Boolean = false
    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false
    //</editor-fold>

    internal var isNeedToLoadMultipleRequest: Boolean = false
    private var mAdIdPosition: Int = -1

    private var mOnAdLoaded: () -> Unit = {}

    private fun getInterstitialAdModel(
        onFindModel: (index: Int, interstitialAdModel: InterstitialAdModel) -> Unit,
    ) {
        mAdIdPosition =
            if (mList.size == 1) {
                0
            } else if (mAdIdPosition < admob_interstitial_ad_model_list.size) {
                if (mAdIdPosition == -1) {
                    0
                } else {
                    (mAdIdPosition + 1)
                }
            } else {
                0
            }

        logE(TAG, "getInterstitialAdModel: AdIdPosition -> $mAdIdPosition")

        if (mAdIdPosition >= 0 && mAdIdPosition < admob_interstitial_ad_model_list.size) {
            onFindModel.invoke(mAdIdPosition, admob_interstitial_ad_model_list[mAdIdPosition])
        } else {
            mAdIdPosition = -1
        }
    }

    // TODO: Load Single Ad Using Model Class
    private fun loadNewAd(
        fContext: Context,
        fModel: InterstitialAdModel,
        fIndex: Int,
    ) {

        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdsID -> ${fModel.adsID}")

        fModel.isAdLoadingRunning = true

        InterstitialAd.load(
            fContext,
            fModel.adsID,
            AdRequest.Builder().build(),
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
                                isAnyAdShowing = true
                                isInterstitialAdShow = true
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
                                fModel.interstitialAd?.fullScreenContentCallback = null
                                fModel.interstitialAd = null

                                isAnyAdShowing = false
                                isInterstitialAdShow = false
                                isAnyAdOpen = false
                                isThisAdShowing = false

                                fModel.listener?.onAdClosed()
                            }
                        }
                    }.also {
                        logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex")
                        fModel.interstitialAd = it
                        fModel.listener?.onAdLoaded()
                        fModel.listener?.onInterstitialAdLoaded(it)
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    logE(
                        tag = TAG,
                        message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}"
                    )
                    fModel.isAdLoadingRunning = false
                    fModel.interstitialAd = null
                    fModel.listener?.onAdFailed()
                }
            }
        )
    }

    private fun requestWithIndex(
        fContext: Context,
        interstitialAdModel: InterstitialAdModel,
        index: Int,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit,
    ) {
        if (fContext.isOnline
            && interstitialAdModel.interstitialAd == null
            && !interstitialAdModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = interstitialAdModel.apply {
                    this.listener = object : AdMobAdsListener {

                        override fun onInterstitialAdLoaded(interstitialAd: InterstitialAd) {
                            super.onInterstitialAdLoaded(interstitialAd)
                            mAdIdPosition = -1
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onInterstitialAdLoaded: Index -> $index"
                            )
                            if (!isAnyIndexLoaded) {
                                isAnyIndexLoaded = true
                                onAdLoaded.invoke()
                                if (onAdLoaded != mOnAdLoaded) {
                                    mOnAdLoaded.invoke()
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
                fIndex = index
            )
        } else if (fContext.isOnline
            && interstitialAdModel.interstitialAd != null
        ) {
            if (!isAnyIndexAlreadyLoaded) {
                logI(tag = TAG, message = "requestWithIndex: already loaded ad Index -> $index")
                isAnyIndexAlreadyLoaded = true
                onAdLoaded.invoke()
                if (onAdLoaded != mOnAdLoaded) {
                    mOnAdLoaded.invoke()
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
     * @param isNeedToShow check if Subscribe is done then ads will not show
     * @param remoteConfig check remote Config parameters is if true ads will show else false ads will not show
     */
    fun loadAd(
        fContext: Context,
        isNeedToShow: Boolean = true,
        onAdLoaded: () -> Unit = {},

        ) {
        if (isNeedToShow && VasuAdsConfig.with(fContext).remoteConfigInterstitialAds && fContext.isOnline) {
            mOnAdLoaded = onAdLoaded
            isAnyIndexLoaded = false
            isAnyIndexAlreadyLoaded = false
            if (admob_interstitial_ad_model_list.isNotEmpty()) {

                if (isNeedToLoadMultipleRequest) {
                    logI(tag = TAG, message = "loadAd: Request Ad From All ID at Same Time")
                    admob_interstitial_ad_model_list.forEachIndexed { index, interstitialAdModel ->
                        requestWithIndex(
                            fContext = fContext,
                            interstitialAdModel = interstitialAdModel,
                            index = index,
                            onAdLoaded = onAdLoaded,
                            onAdFailed = {},
                        )
                    }
                } else {
                    logI(tag = TAG, message = "loadAd: Request Ad After Failed Previous Index Ad")
                    getInterstitialAdModel { index, interstitialAdModel ->
                        logI(tag = TAG, message = "loadAd: getInterstitialAdModel: Index -> $index")
                        requestWithIndex(
                            fContext = fContext,
                            interstitialAdModel = interstitialAdModel,
                            index = index,
                            onAdLoaded = onAdLoaded,
                            onAdFailed = {
                                if ((mAdIdPosition + 1) >= admob_interstitial_ad_model_list.size) {
                                    mAdIdPosition = -1
                                } else {
                                    loadAd(fContext = fContext, onAdLoaded = mOnAdLoaded)
                                }
                            },
                        )
                    }
                }
            } else {
                throw RuntimeException("set Interstitial Ad Id First")
            }
        } else {
            onAdLoaded.invoke()
        }
    }

    /**
     * Call this method when you need to show Interstitial AD
     * also this method call our offline native dialog AD [FullScreenNativeAdDialogActivity] when Interstitial Ad fails and give call bake on same way
     *
     * Use of this Method
     * activity.showInterstitialAd {[your code which has run after AD show or if AD fails to show]}
     * call this method with [Activity] instance
     *
     * @param fIsShowFullScreenNativeAd pass false if you don't need native ad if interstitial ads not loaded
     * @param onAdClosed this is a call back of your ad close, it will call also if your ad was not showing to the user
     * @param isNeedToShow check if Subscribe is done then ads will not show
     * @param remoteConfig check remote Config parameters is if true ads will show else false ads will not show
     */
    fun Activity.showInterstitialAd(
        fIsShowFullScreenNativeAd: Boolean = true,
        isNeedToShow: Boolean = true,
        onAdClosed: (isAdShowing: Boolean, isShowFullScreenAd: Boolean) -> Unit

    ) {
        if (isNeedToShow && VasuAdsConfig.with(this).remoteConfigInterstitialAds) {


            this@InterstitialAdHelper.mIsShowFullScreenNativeAd = fIsShowFullScreenNativeAd

            mListener = object : AdMobAdsListener {
                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    if (isAppForeground) {
                        onAdClosed.invoke(isAdsShowingFlagForDeveloper, isShowFullScreenAd)
                        isAdsShowingFlagForDeveloper = false
                    }

                    logI(tag = TAG, message = "showInterstitialAd: onAdClosed: Load New Ad")
                    loadAd(fContext = this@showInterstitialAd, onAdLoaded = mOnAdLoaded)
                }
            }

            if (admob_interstitial_ad_model_list.isNotEmpty()) {

                val loadedAdModel: InterstitialAdModel? =
                    admob_interstitial_ad_model_list.find { it.interstitialAd != null }

                loadedAdModel?.let {
                    val lIndex: Int = admob_interstitial_ad_model_list.indexOf(it)

                    if (isNeedToShowAds && !isThisAdShowing && !isInterstitialAdShow) {
                        if (it.interstitialAd != null) {
                            if (!isAnyAdShowing) {
                                isAdsShowingFlagForDeveloper = false
                                isAnyAdShowing = true
                                isAnyAdOpen = true
                                isInterstitialAdShow = true
                                it.interstitialAd?.show(this)
                                logI(
                                    tag = TAG,
                                    message = "showInterstitialAd: Show Interstitial Ad Index -> $lIndex"
                                )
                                isThisAdShowing = true
                            }
                        } else {
                            // show native ad
                            showFullScreenNativeAdDialog()
                        }
                    }
                } ?: kotlin.run {
                    // show native ad
                    showFullScreenNativeAdDialog()
                }
                if (!isThisAdShowing && isOnline) {
                    mListener?.onAdClosed(false)
                }
            } else {
                throw RuntimeException("set Interstitial Ad Id First")
            }
        } else {
            if (!isThisAdShowing) {
                onAdClosed.invoke(true,false)
            }
        }

    }

    private fun Activity.showFullScreenNativeAdDialog() {
        if (mIsShowFullScreenNativeAd
            && NativeAdvancedModelHelper.getNativeAd != null
            && isOnline
            && !this.isFinishing
        ) {
            if (!isAnyAdShowing) {
                isAdsShowingFlagForDeveloper = false
                isAnyAdShowing = true
                logI(tag = TAG, message = "showFullScreenNativeAdDialog: Try To Open Dialog...")

                onDialogActivityDismiss = {
                    logE(
                        tag = TAG,
                        message = "showFullScreenNativeAdDialog: Dialog Activity Dismiss"
                    )
                    isThisAdShowing = false
                    isAdsShowingFlagForDeveloper = true
                    mListener?.onAdClosed(true)
                }

                FullScreenNativeAdDialogActivity.lunchFullScreenAd(this)

                isThisAdShowing = true
            }
        } else {
            mListener?.onAdClosed(true)
        }
    }

    fun destroy() {
        mListener = null
        isThisAdShowing = false
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false
        mIsShowFullScreenNativeAd = true
        mAdIdPosition = -1

        for (data in admob_interstitial_ad_model_list) {
            data.interstitialAd?.fullScreenContentCallback = null
            data.interstitialAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}