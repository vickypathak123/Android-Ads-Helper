package com.example.app.ads.helper.adaptive.banner

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.example.app.ads.helper.*
import com.google.android.gms.ads.*

class BannerHelper(private val mContext: Activity) {

    private val TAG: String = "Admob_${javaClass.simpleName}"

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

        mAdIdPosition = if (mAdIdPosition < admob_banner_ad_id.size) {
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
            mAdIdPosition = -1
            null
        }
    }

    internal fun loadAds(fContext: Context, fAdSize: AdSize, onAdLoaded: (adView: AdView) -> Unit, onAdClosed: () -> Unit) {

        mAdView?.let {
            logI(tag = TAG, message = "onAdLoaded: Old Loaded Ad")
            onAdLoaded.invoke(it)
        } ?: getBannerAdID()?.let { adsID ->
            logI(tag = TAG, message = "loadBannerAds: Id Index::-> '$mAdIdPosition', AdsID::-> '$adsID'")

            AdView(fContext).apply {
                adUnitId = adsID
                setAdSize(fAdSize)

                this.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        mAdIdPosition = -1
                        logI(tag = TAG, message = "onAdLoaded: New Ad Loaded")
                        onAdLoaded.invoke(this@apply)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        logE(tag = TAG, message = "onAdFailedToLoad: Ad failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}")

                        mAdView?.destroy()
                        mAdView = null

                        if ((mAdIdPosition + 1) >= admob_banner_ad_id.size) {
                            mAdIdPosition = -1
                        } else {
                            loadAds(fContext, fAdSize, onAdLoaded, onAdClosed)
                        }
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        mAdView?.destroy()
                        mAdView = null
                        logI(tag = TAG, message = "onAdClosed: ")
                        onAdClosed.invoke()
                    }
                }

                val adRequest: AdRequest = AdRequest.Builder().build()
                this.loadAd(adRequest)
            }.also { mAdView = it }

        }
    }

    var mAdView: AdView? = null

    fun loadBanner(fBannerAdSize: BannerAdSize, fLayout: FrameLayout) {
        fLayout.gone
        fLayout.removeAllViews()

        val lAdSize: AdSize = when(fBannerAdSize) {
            BannerAdSize.BANNER -> AdSize.BANNER
            BannerAdSize.LARGE_BANNER -> AdSize.LARGE_BANNER
            BannerAdSize.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
            BannerAdSize.FULL_BANNER -> AdSize.FULL_BANNER
            BannerAdSize.LEADERBOARD -> AdSize.LEADERBOARD
            BannerAdSize.ADAPTIVE_BANNER -> fLayout.adSize
            BannerAdSize.SMART_BANNER -> AdSize.SMART_BANNER
        }

        fLayout.onGlobalLayout {
            loadAds(
                fContext = mContext,
                fAdSize = lAdSize,
                onAdLoaded = { adView ->
                    logE(tag = TAG, message = "loadBanner: onAdLoaded: ${mContext.localClassName}")
                    fLayout.removeAllViews()
                    fLayout.addView(adView)
                    fLayout.visible
                },
                onAdClosed = {
                    logI(tag = TAG, message = "loadBanner: onAdClosed: ${mContext.localClassName}")
                    loadBanner(fBannerAdSize = fBannerAdSize, fLayout = fLayout)
                }
            )
        }
    }
}