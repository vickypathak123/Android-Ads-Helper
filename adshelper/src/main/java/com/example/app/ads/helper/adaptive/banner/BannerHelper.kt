package com.example.app.ads.helper.adaptive.banner

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.example.app.ads.helper.R
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.admob_banner_ad_id
import com.example.app.ads.helper.admob_interstitial_ad_model_list
import com.example.app.ads.helper.displayDensity
import com.example.app.ads.helper.displayWidth
import com.example.app.ads.helper.gone
import com.example.app.ads.helper.inflater
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.onGlobalLayout
import com.example.app.ads.helper.visible
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class BannerHelper(private val mContext: Activity) {

    private val TAG: String = "Admob_${javaClass.simpleName}"
    var isAdLoaded = false
    private var mFLayout: FrameLayout? = null
    private var mShimmerLayout: View? = null
    private val FrameLayout.adSize: AdSize
        get() {
            val displayDensity = mContext.displayDensity
            val displayWidth = mContext.displayWidth
            var adWidthPixels = this.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = displayWidth.toFloat()
            }
            val adWidth = (adWidthPixels / displayDensity).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth)
        }

    private var mAdIdPosition: Int = -1
    private fun getBannerAdID(): String? {

        mAdIdPosition =
            if (mAdIdPosition < admob_banner_ad_id.size) {
                if (mAdIdPosition == -1) {
                    0
                } else {
                    (mAdIdPosition + 1)
                }
            } else {
                0
            }

        return if (mAdIdPosition >= 0 && mAdIdPosition < admob_banner_ad_id.size) {
            admob_banner_ad_id[mAdIdPosition]
        } else {
            mAdIdPosition =
                -1
            null
        }
    }


    internal fun loadAds(
        fContext: Context,
        fAdSize: AdSize,
        onAdLoaded: (adView: AdView) -> Unit,
        onAdClicked:()->Unit,
        onAdClosed: () -> Unit,
        bannerType: BannerType
    ) {

        mAdView?.let {
            logI(
                tag = TAG,
                message = "onAdLoaded: Old Loaded Ad"
            )
            onAdLoaded.invoke(
                it
            )
        }
            ?: getBannerAdID()?.let { adsID ->
                logI(
                    tag = TAG,
                    message = "loadBannerAds: Id Index::-> '$mAdIdPosition', AdsID::-> '$adsID'"
                )

                AdView(fContext).apply {
                    adUnitId = adsID
                    setAdSize(fAdSize)
                    this.adListener =
                        object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                isAdLoaded = true
                                mAdIdPosition = -1
                                logI(
                                    tag = TAG,
                                    message = "onAdShow:Id Index::-> '$mAdIdPosition', AdsID::-> '$adsID'"
                                )
                                onAdLoaded.invoke(this@apply)
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                super.onAdFailedToLoad(adError)
                                isAdLoaded = false
                                logE(
                                    tag = TAG,
                                    message = "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}"
                                )
                                mAdView?.destroy()
                                mAdView = null

                                if ((mAdIdPosition + 1) >= admob_banner_ad_id.size) {
                                    mAdIdPosition = -1
                                } else {
                                    loadAds(fContext, fAdSize, onAdLoaded, onAdClicked,onAdClosed, bannerType)
                                }
                            }

                            override fun onAdClicked() {
                                super.onAdClicked()
                                isAdLoaded = false
                                mAdView?.destroy()
                                mAdView = null
                                logI(tag = TAG, message = "onAdClicked: ")
                                onAdClicked.invoke()
                            }


                            override fun onAdClosed() {
                                super.onAdClosed()
                                logI(tag = TAG, message = "onAdClosed: ")
                                onAdClosed.invoke()
                            }

                        }
                    val extras = Bundle()
                    val adRequest = when (bannerType) {
                        BannerType.NORMAL -> {
                            AdRequest.Builder().build()
                        }

                        BannerType.SPLASH -> {
                            extras.putString("is_splash_banner", "true")
                            AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
                        }

                        BannerType.COLLAPSIBLE_BOTTOM -> {
                            extras.putString("collapsible", "bottom")
                            AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
                        }

                        BannerType.COLLAPSIBLE_TOP -> {
                            extras.putString("collapsible", "top")
                            AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
                        }
                    }
                    this.loadAd(adRequest)
                }.also { mAdView = it }
            }
    }

    var mAdView: AdView? = null


    fun loadBanner(
        fBannerAdSize: BannerAdSize,
        fLayout: FrameLayout,
        isNeedToShowShimmer: Boolean? = true,
        isNeedToShowAd: Boolean = true,
        bannerType: BannerType
    ) {
        if (isNeedToShowAd && VasuAdsConfig.with(fLayout.context).remoteConfigBannerAds && fLayout.context.isOnline) {
            mFLayout = fLayout
            fLayout.gone
            fLayout.removeAllViews()
            val lAdSize: AdSize = when (fBannerAdSize) {
                BannerAdSize.BANNER -> AdSize.BANNER
                BannerAdSize.LARGE_BANNER -> AdSize.LARGE_BANNER
                BannerAdSize.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
                BannerAdSize.FULL_BANNER -> AdSize.FULL_BANNER
                BannerAdSize.LEADERBOARD -> AdSize.LEADERBOARD
                BannerAdSize.ADAPTIVE_BANNER -> fLayout.adSize
                BannerAdSize.SMART_BANNER -> AdSize.SMART_BANNER
            }
            if (isNeedToShowShimmer!!) {
                val shimmerLayout = when (fBannerAdSize) {
                    BannerAdSize.BANNER -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_banner_ad,
                        fLayout,
                        false
                    )

                    BannerAdSize.LARGE_BANNER -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_large_banner_ad,
                        fLayout,
                        false
                    )

                    BannerAdSize.MEDIUM_RECTANGLE -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_medium_rectangle_banner_ad,
                        fLayout,
                        false
                    )

                    BannerAdSize.FULL_BANNER -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_full_banner_ad,
                        fLayout,
                        false
                    )

                    BannerAdSize.LEADERBOARD -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_leaderboard_and_smart_banner_ad,
                        fLayout,
                        false
                    )

                    BannerAdSize.ADAPTIVE_BANNER -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_adaptive_banner_ad,
                        fLayout,
                        false
                    )

                    else -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_leaderboard_and_smart_banner_ad,
                        fLayout,
                        false
                    )
                }
                if (isNeedToShowShimmer) {
                    Log.d(TAG, "loadBanner: add shimmer")
                    fLayout.addView(shimmerLayout)
                    fLayout.visible
                } else {
                    fLayout.gone
                }
                mShimmerLayout = shimmerLayout
            }
            fLayout.onGlobalLayout {
                loadAds(
                    fContext = mContext,
                    fAdSize = lAdSize,
                    onAdLoaded = { adView ->
                        logE(
                            tag = TAG,
                            message = "loadBanner: onAdLoaded: ${mContext.localClassName}"
                        )
                        fLayout.removeAllViews()
                        fLayout.addView(adView)
                        fLayout.visible
                    },
                    onAdClicked = {
                        logI(
                            tag = TAG,
                            message = "loadBanner: onAdClicked: ${mContext.localClassName}"
                        )
                        loadBanner(
                            fBannerAdSize = fBannerAdSize,
                            fLayout = fLayout,
                            bannerType = bannerType
                        )
                    },
                    onAdClosed = {
                        logI(
                            tag = TAG,
                            message = "loadBanner: onAdClosed: ${mContext.localClassName}"
                        )
                    },
                    bannerType
                )
            }
        }
    }

    fun manageShimmerLayoutVisibility(
        isNeedToShowAd: Boolean,
        fBannerAdSize: BannerAdSize,
        fLayout: FrameLayout,
    ) {

        val shimmerLayout = when (fBannerAdSize) {
            BannerAdSize.BANNER -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_banner_ad,
                fLayout,
                false
            )

            BannerAdSize.LARGE_BANNER -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_large_banner_ad,
                fLayout,
                false
            )

            BannerAdSize.MEDIUM_RECTANGLE -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_medium_rectangle_banner_ad,
                fLayout,
                false
            )

            BannerAdSize.FULL_BANNER -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_full_banner_ad,
                fLayout,
                false
            )

            BannerAdSize.LEADERBOARD -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_leaderboard_and_smart_banner_ad,
                fLayout,
                false
            )

            BannerAdSize.ADAPTIVE_BANNER -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_adaptive_banner_ad,
                fLayout,
                false
            )

            else -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_leaderboard_and_smart_banner_ad,
                fLayout,
                false
            )
        }
        mShimmerLayout = shimmerLayout
        if (isNeedToShowAd && VasuAdsConfig.with(fLayout.context).remoteConfigBannerAds) {
            if (!isAdLoaded) {
                fLayout.removeAllViews()
                fLayout.addView(mShimmerLayout)
                fLayout.visible
            } else {
                fLayout.visible
            }
        } else {
//            mFLayout?.removeAllViews()
            fLayout.gone
        }

    }


    fun isBannerAdAvailable(): Boolean {
        return admob_interstitial_ad_model_list.find { it.interstitialAd != null }?.interstitialAd != null
    }
}