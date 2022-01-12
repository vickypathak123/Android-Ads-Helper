package com.example.app.ads.helper

import android.content.Context
import androidx.annotation.NonNull
import java.io.Serializable

/**
 * @author Akshay Harsoda
 * @since 16 Oct 2021
 */
@Suppress("unused")
object VasuAdsConfig {

    @JvmStatic
    fun with(fContext: Context): SetAdsID {
        return SetAdsID(fContext)
    }
}

/**
 * @author Akshay Harsoda
 * @since 16 Oct 2021
 */
@Suppress("unused")
class SetAdsID(private val mContext: Context) : Serializable {

    private var admobAppId: String = mContext.getStringRes(R.string.admob_app_id)
    private var admobBannerAdId: String = mContext.getStringRes(R.string.admob_banner_ad_id)
    private var admobInterstitialAdId: String =
        mContext.getStringRes(R.string.admob_interstitial_ad_id)
    private var admobNativeAdvancedAdId: String =
        mContext.getStringRes(R.string.admob_native_advanced_ad_id)
    private var admobRewardVideoAdId: String =
        mContext.getStringRes(R.string.admob_reward_video_ad_id)
    private var admobInterstitialAdRewardId: String =
        mContext.getStringRes(R.string.admob_interstitial_ad_reward_id)
    private var admobOpenAdId: String =
        mContext.getStringRes(R.string.admob_open_ad_id)

    private var mIsEnable: Boolean = isOpenAdEnable

    private var isTakeAllTestAdID: Boolean = false
    private var mIsBlockInterstitialAd: Boolean = false

    @JvmName("setAdmobAppId")
    @NonNull
    fun setAdmobAppId(fAdmobAppId: String) = this@SetAdsID.apply {
        this.admobAppId = fAdmobAppId
    }

    @JvmName("setAdmobBannerAdId")
    @NonNull
    fun setAdmobBannerAdId(fAdmobBannerAdId: String) = this@SetAdsID.apply {
        this.admobBannerAdId = fAdmobBannerAdId
    }

    @JvmName("setAdmobInterstitialAdId")
    @NonNull
    fun setAdmobInterstitialAdId(fAdmobInterstitialAdId: String) = this@SetAdsID.apply {
        this.admobInterstitialAdId = fAdmobInterstitialAdId
    }

    @JvmName("setAdmobNativeAdvancedAdId")
    @NonNull
    fun setAdmobNativeAdvancedAdId(fAdmobNativeAdvancedAdId: String) = this@SetAdsID.apply {
        this.admobNativeAdvancedAdId = fAdmobNativeAdvancedAdId
    }

    @JvmName("setAdmobRewardVideoAdId")
    @NonNull
    fun setAdmobRewardVideoAdId(fAdmobRewardVideoAdId: String) = this@SetAdsID.apply {
        this.admobRewardVideoAdId = fAdmobRewardVideoAdId
    }

    @JvmName("setAdmobInterstitialAdRewardId")
    @NonNull
    fun setAdmobInterstitialAdRewardId(fAdmobInterstitialAdRewardId: String) = this@SetAdsID.apply {
        this.admobInterstitialAdRewardId = fAdmobInterstitialAdRewardId
    }

    @JvmName("setAdmobOpenAdId")
    @NonNull
    fun setAdmobOpenAdId(fAdmobOpenAdId: String) = this@SetAdsID.apply {
        this.admobOpenAdId = fAdmobOpenAdId
    }

    @JvmName("isEnableOpenAd")
    @NonNull
    fun isEnableOpenAd(fIsEnable: Boolean) = this@SetAdsID.apply {
        this.mIsEnable = fIsEnable
    }

    @JvmName("needToTakeAllTestAdID")
    @NonNull
    fun needToTakeAllTestAdID(fIsEnable: Boolean) = this@SetAdsID.apply {
        this.isTakeAllTestAdID = fIsEnable
    }

    @JvmName("needToBlockInterstitialAd")
    @NonNull
    fun needToBlockInterstitialAd(fIsEnable: Boolean) = this@SetAdsID.apply {
        this.mIsBlockInterstitialAd = fIsEnable
    }


    @JvmName("initialize")
    fun initialize() {
        if (isTakeAllTestAdID) {
            admob_app_id = mContext.getStringRes(R.string.admob_app_id)
            admob_banner_ad_id = mContext.getStringRes(R.string.admob_banner_ad_id)
            admob_interstitial_ad_id = mContext.getStringRes(R.string.admob_interstitial_ad_id)
            admob_native_advanced_ad_id = mContext.getStringRes(R.string.admob_native_advanced_ad_id)
            admob_reward_video_ad_id = mContext.getStringRes(R.string.admob_reward_video_ad_id)
            admob_interstitial_ad_reward_id = mContext.getStringRes(R.string.admob_interstitial_ad_reward_id)
            admob_open_ad_id = mContext.getStringRes(R.string.admob_open_ad_id)
        } else {
            admob_app_id = this.admobAppId
            admob_banner_ad_id = this.admobBannerAdId
            admob_interstitial_ad_id = this.admobInterstitialAdId
            admob_native_advanced_ad_id = this.admobNativeAdvancedAdId
            admob_reward_video_ad_id = this.admobRewardVideoAdId
            admob_interstitial_ad_reward_id = this.admobInterstitialAdRewardId
            admob_open_ad_id = this.admobOpenAdId
        }

        isOpenAdEnable = this.mIsEnable
        isBlockInterstitialAd = this.mIsBlockInterstitialAd
    }

}