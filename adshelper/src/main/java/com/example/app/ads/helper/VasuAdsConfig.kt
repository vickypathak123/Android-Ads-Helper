package com.example.app.ads.helper

import android.content.Context
import androidx.annotation.NonNull
import com.example.app.ads.helper.purchase.ProductPurchaseHelper
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

    private var admobInterstitialAdId: ArrayList<String> = ArrayList()

    private var admobNativeAdvancedAdId: ArrayList<String> = ArrayList()

    private var admobRewardVideoAdId: ArrayList<String> = ArrayList()

    private var admobInterstitialAdRewardId: ArrayList<String> = ArrayList()

    private var admobOpenAdId: ArrayList<String> = ArrayList()

    private var mIsEnable: Boolean = isOpenAdEnable

    private var isTakeAllTestAdID: Boolean = false
    private var mIsBlockInterstitialAd: Boolean = false

    private val mLifeTimeProductKeyList: ArrayList<String> = ArrayList()
    private val mSubscriptionKeyList: ArrayList<String> = ArrayList()

    //<editor-fold desc="Set Ads ID">
    @JvmName("setAdmobAppId")
    @NonNull
    fun setAdmobAppId(fAdmobAppId: String) = this@SetAdsID.apply {
        this.admobAppId = fAdmobAppId
    }

    @JvmName("setAdmobInterstitialAdId")
    @NonNull
    fun setAdmobInterstitialAdId(vararg fAdmobInterstitialAdIds: String) = this@SetAdsID.apply {
        this.admobInterstitialAdId.clearAll()
        this.admobInterstitialAdId.addAll(fAdmobInterstitialAdIds)
    }

    @JvmName("setAdmobNativeAdvancedAdId")
    @NonNull
    fun setAdmobNativeAdvancedAdId(vararg fAdmobNativeAdvancedAdIds: String) = this@SetAdsID.apply {
        this.admobNativeAdvancedAdId.clearAll()
        this.admobNativeAdvancedAdId.addAll(fAdmobNativeAdvancedAdIds)
    }

    @JvmName("setAdmobRewardVideoAdId")
    @NonNull
    fun setAdmobRewardVideoAdId(vararg fAdmobRewardVideoAdIds: String) = this@SetAdsID.apply {
        this.admobRewardVideoAdId.clearAll()
        this.admobRewardVideoAdId.addAll(fAdmobRewardVideoAdIds)
    }

    @JvmName("setAdmobInterstitialAdRewardId")
    @NonNull
    fun setAdmobInterstitialAdRewardId(vararg fAdmobInterstitialAdRewardIds: String) = this@SetAdsID.apply {
        this.admobInterstitialAdRewardId.clearAll()
        this.admobInterstitialAdRewardId.addAll(fAdmobInterstitialAdRewardIds)
    }

    @JvmName("setAdmobOpenAdId")
    @NonNull
    fun setAdmobOpenAdId(vararg fAdmobOpenAdIds: String) = this@SetAdsID.apply {
        this.admobOpenAdId.clearAll()
        this.admobOpenAdId.addAll(fAdmobOpenAdIds)
    }
    //</editor-fold>

    //<editor-fold desc="Manage Ads Flag">
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
    //</editor-fold>

    @JvmName("setLifeTimeProductKey")
    @NonNull
    fun setLifeTimeProductKey(vararg keys: String) = this@SetAdsID.apply  {
        mLifeTimeProductKeyList.clearAll()
        mLifeTimeProductKeyList.addAll(keys.filter { it.isNotEmpty() })
    }

    @JvmName("setSubscriptionKey")
    @NonNull
    fun setSubscriptionKey(vararg keys: String) = this@SetAdsID.apply  {
        mSubscriptionKeyList.clearAll()
        mSubscriptionKeyList.addAll(keys.filter { it.isNotEmpty() })
    }


    @JvmName("initialize")
    fun initialize() {
        admob_interstitial_ad_id.clearAll()
        admob_native_advanced_ad_id.clearAll()
        admob_reward_video_ad_id.clearAll()
        admob_interstitial_ad_reward_id.clearAll()
        admob_open_ad_id.clearAll()

        if (isTakeAllTestAdID) {
            admob_app_id = mContext.getStringRes(R.string.admob_app_id)

            admob_interstitial_ad_id.add(mContext.getStringRes(R.string.admob_interstitial_ad_id))
            admob_native_advanced_ad_id.add(mContext.getStringRes(R.string.admob_native_advanced_ad_id))
            admob_reward_video_ad_id.add(mContext.getStringRes(R.string.admob_reward_video_ad_id))
            admob_interstitial_ad_reward_id.add(mContext.getStringRes(R.string.admob_interstitial_ad_reward_id))
            admob_open_ad_id.add(mContext.getStringRes(R.string.admob_open_ad_id))
        } else {
            admob_app_id = this.admobAppId

            admob_interstitial_ad_id.addAll(this.admobInterstitialAdId)
            admob_native_advanced_ad_id.addAll(this.admobNativeAdvancedAdId)
            admob_reward_video_ad_id.addAll(this.admobRewardVideoAdId)
            admob_interstitial_ad_reward_id.addAll(this.admobInterstitialAdRewardId)
            admob_open_ad_id.addAll(this.admobOpenAdId)
        }

        ProductPurchaseHelper.setLifeTimeProductKey(*mLifeTimeProductKeyList.toTypedArray())
        ProductPurchaseHelper.setSubscriptionKey(*mSubscriptionKeyList.toTypedArray())

        isOpenAdEnable = this.mIsEnable
        isBlockInterstitialAd = this.mIsBlockInterstitialAd
    }
}

internal fun ArrayList<String>.clearAll() {
    this.clear()
    this.removeAll(this.toSet())
}