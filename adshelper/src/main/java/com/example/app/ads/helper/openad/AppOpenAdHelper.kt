package com.example.app.ads.helper.openad

import android.app.Activity
import android.content.Context
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.admob_app_open_ad_model_list
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAnyAdShowing
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.isOpenAdEnable
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

/**
 * @author Akshay Harsoda
 * @since 28 Nov 2022
 *
 * AppOpenAdHelper.kt - Simple object which has load and handle your AppOpen AD data
 */
object AppOpenAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    private var mListener: AdMobAdsListener? = null

    //<editor-fold desc="This Ads Related Flag">
    private var isThisAdShowing: Boolean = false
    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false
    //</editor-fold>

    internal var isNeedToLoadMultipleRequest: Boolean = false
    private var mAdIdPosition: Int = -1

    private var mOnAdLoaded: () -> Unit = {}

    private fun getAppOpenAdModel(
        onFindModel: (index: Int, openAdModel: OpenAdModel) -> Unit
    ) {
        mAdIdPosition = if (mAdIdPosition < admob_app_open_ad_model_list.size) {
            if (mAdIdPosition == -1) {
                0
            } else {
                (mAdIdPosition + 1)
            }
        } else {
            0
        }

        logE(TAG, "getAppOpenAdModel: AdIdPosition -> $mAdIdPosition")

        if (mAdIdPosition >= 0 && mAdIdPosition < admob_app_open_ad_model_list.size) {
            onFindModel.invoke(mAdIdPosition, admob_app_open_ad_model_list[mAdIdPosition])
        } else {
            mAdIdPosition = -1
        }
    }

    // TODO: Load Single Ad Using Model Class
    private fun loadNewAd(
        fContext: Context,
        fModel: OpenAdModel,
        fOrientation: Int,
        fIndex: Int
    ) {

        logI(tag = TAG, message = "loadNewAd: Index -> $fIndex\nAdsID -> ${fModel.adsID}")

        fModel.isAdLoadingRunning = true

        AppOpenAd.load(
            fContext,
            fModel.adsID,
            AdRequest.Builder().build(),
            fOrientation,
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
                                isAnyAdShowing = true
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
                                fModel.appOpenAd?.fullScreenContentCallback = null
                                fModel.appOpenAd = null

                                isAnyAdShowing = false
                                isAnyAdOpen = false
                                isThisAdShowing = false

                                fModel.listener?.onAdClosed()
                            }
                        }
                    }.also {
                        logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex")
                        fModel.appOpenAd = it
                        fModel.listener?.onAdLoaded()
                        fModel.listener?.onAppOpenAdLoaded(it)
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    logE(
                        tag = TAG,
                        message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}"
                    )
                    fModel.isAdLoadingRunning = false
                    fModel.appOpenAd = null
                    fModel.listener?.onAdFailed()
                }
            }
        )
    }

    private fun requestWithIndex(
        fContext: Context,
        openAdModel: OpenAdModel,
        index: Int,
        fOrientation: Int,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (fContext.isOnline
            && openAdModel.appOpenAd == null
            && !openAdModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fOrientation = fOrientation,
                fModel = openAdModel.apply {
                    this.listener = object : AdMobAdsListener {

                        override fun onAppOpenAdLoaded(appOpenAd: AppOpenAd) {
                            super.onAppOpenAdLoaded(appOpenAd)
                            mAdIdPosition = -1
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onAppOpenAdLoaded: Index -> $index"
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
            && openAdModel.appOpenAd != null
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
     * Call this method when you need to load your AppOpen AD
     * you need to call this method only once in your launcher activity or your application class
     *
     * @param fContext this is a reference to your activity context
     * @param onAdLoaded callback after ad successfully loaded
     */
    fun loadAd(
        fContext: Context,
        isNeedToShowAds: Boolean,
        fOrientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
        onAdLoaded: () -> Unit = {}
    ) {
        mOnAdLoaded = onAdLoaded
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false
        if (isNeedToShowAds && VasuAdsConfig.with(fContext).remoteConfigOpenAds && fContext.isOnline) {
            if (admob_app_open_ad_model_list.isNotEmpty()) {

                if (isNeedToLoadMultipleRequest) {
                    logI(tag = TAG, message = "loadAd: Request Ad From All ID at Same Time")
                    admob_app_open_ad_model_list.forEachIndexed { index, openAdModel ->
                        requestWithIndex(
                            fContext = fContext,
                            openAdModel = openAdModel,
                            index = index,
                            fOrientation = fOrientation,
                            onAdLoaded = onAdLoaded,
                            onAdFailed = {},
                        )
                    }
                } else {
                    logI(tag = TAG, message = "loadAd: Request Ad After Failed Previous Index Ad")
                    getAppOpenAdModel { index, openAdModel ->
                        logI(tag = TAG, message = "loadAd: getAppOpenAdModel: Index -> $index")
                        requestWithIndex(
                            fContext = fContext,
                            openAdModel = openAdModel,
                            index = index,
                            fOrientation = fOrientation,
                            onAdLoaded = onAdLoaded,
                            onAdFailed = {
                                if ((mAdIdPosition + 1) >= admob_app_open_ad_model_list.size) {
                                    mAdIdPosition = -1
                                } else {
                                    loadAd(
                                        fContext = fContext,
                                        isNeedToShowAds,
                                        onAdLoaded = mOnAdLoaded
                                    )
                                }
                            },
                        )
                    }
                }
            } else {
                throw RuntimeException("set AppOpen Ad Id First")
            }
        } else {
            onAdLoaded.invoke()
        }
    }

    /**
     * this method will check AppOpen Ad is Available or not
     */
    fun isAppOpenAdAvailable(): Boolean {
        return isOpenAdEnable && admob_app_open_ad_model_list.find { it.appOpenAd != null }?.appOpenAd != null
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
    fun Activity.showAppOpenAd(
        isNeedToShowAds: Boolean,
        onAdClosed: () -> Unit
    ) {
        if (!isThisAdShowing && isNeedToShowAds && VasuAdsConfig.with(this).remoteConfigOpenAds) {
            mListener = object : AdMobAdsListener {
                override fun onAdClosed(isShowFullScreenAd: Boolean) {
                    if (isAppForeground) {
                        onAdClosed.invoke()
                    }

                    logI(tag = TAG, message = "showAppOpenAd: onAdClosed: Load New Ad")
                    loadAd(fContext = this@showAppOpenAd, isNeedToShowAds)
                }
            }

            if (admob_app_open_ad_model_list.isNotEmpty()) {

                val loadedAdModel: OpenAdModel? =
                    admob_app_open_ad_model_list.find { it.appOpenAd != null }

                loadedAdModel?.let {
                    val lIndex: Int = admob_app_open_ad_model_list.indexOf(it)

                    if (isNeedToShowAds && !isThisAdShowing) {
                        if (isAppOpenAdAvailable() && it.appOpenAd != null && isOnline && !this.isFinishing) {
                            if (!isAnyAdShowing) {
                                isAnyAdShowing = true
                                isAnyAdOpen = true
                                isThisAdShowing = true

                                it.appOpenAd?.show(this)
                                logI(
                                    tag = TAG,
                                    message = "showAppOpenAd: Show AppOpen Ad Index -> $lIndex"
                                )
                            }
                        }
                    }
                }

                if (!isThisAdShowing) {
                    mListener?.onAdClosed(false)
                }
            } else {
                throw RuntimeException("set AppOpen Ad Id First")
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

        for (data in admob_app_open_ad_model_list) {
            data.appOpenAd?.fullScreenContentCallback = null
            data.appOpenAd = null
            data.listener = null
            data.isAdLoadingRunning = false
        }
    }
}