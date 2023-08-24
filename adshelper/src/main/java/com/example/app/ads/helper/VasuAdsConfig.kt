package com.example.app.ads.helper

import android.content.Context
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.interstitialad.InterstitialAdModel
import com.example.app.ads.helper.nativead.NativeAdHelper
import com.example.app.ads.helper.nativead.NativeAdModel
import com.example.app.ads.helper.openad.AppOpenAdHelper
import com.example.app.ads.helper.openad.OpenAdModel
import com.example.app.ads.helper.purchase.ProductPurchaseHelper
import com.example.app.ads.helper.reward.RewardedInterstitialAdHelper
import com.example.app.ads.helper.reward.RewardedInterstitialAdModel
import com.example.app.ads.helper.reward.RewardedVideoAdHelper
import com.example.app.ads.helper.reward.RewardedVideoAdModel
import java.io.Serializable

/**
 * @author Akshay Harsoda
 * @since 16 Oct 2021
 */

var TAG = VasuAdsConfig.javaClass.simpleName

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

    private var admobAppId: String = mContext.getStringRes(R.string.test_admob_app_id)

    //<editor-fold desc="Ads Id List">
    private var admobInterstitialAdId: ArrayList<String> = ArrayList()
    private var admobOpenAdId: ArrayList<String> = ArrayList()
    private var admobInterstitialAdRewardId: ArrayList<String> = ArrayList()
    private var admobRewardVideoAdId: ArrayList<String> = ArrayList()
    private var admobBannerAdId: ArrayList<String> = ArrayList()
    private var admobNativeAdvancedAdId: ArrayList<String> = ArrayList()
    //</editor-fold>

    //<editor-fold desc="Product Key List">
    private val mLifeTimeProductKeyList: ArrayList<String> = ArrayList()
    private val mSubscriptionKeyList: ArrayList<String> = ArrayList()
    //</editor-fold>

    //<editor-fold desc="Load Multiple Request Flag">
    private var mIsNeedToLoadMultipleInterstitialAdRequest: Boolean = false
    private var mIsNeedToLoadMultipleAppOpenAdRequest: Boolean = false
    private var mIsNeedToLoadMultipleRewardedInterstitialAdRequest: Boolean = false
    private var mIsNeedToLoadMultipleRewardedVideoAdRequest: Boolean = false
    private var mIsNeedToLoadMultipleNativeAdRequest: Boolean = false
    //</editor-fold>

    private var isTakeAllTestAdID: Boolean = false
    private var isNeedToGetProductListFromRevenueCat: Boolean = false
    private var revenueCatId: String = ""

    private var mIsEnable: Boolean = isOpenAdEnable
    private var mIsBlockInterstitialAd: Boolean = false
    private var mIsDebugMode: Boolean = false

    var remoteConfigOpenAds: Boolean = false
    var remoteConfigBannerAds: Boolean = false
    var remoteConfigNativeAdvancedAds: Boolean = false
    var remoteConfigInterstitialAds: Boolean = false
    var remoteConfigInterstitialAdRewardAds: Boolean = false
    var remoteConfigRewardVideoAds: Boolean = false


    /**
     * RemoteConfig Value set for All Ads
     */
    @JvmName("needToRemoteConfigBannerAds")
    fun needToRemoteConfigBannerAds(fIsEnable: Boolean=true) = this@SetAdsID.apply {
        this.remoteConfigBannerAds = fIsEnable
    }

    @JvmName("needToRemoteConfigBannerAds")
    fun needToRemoteConfigOpenAds(fIsEnable: Boolean=true) = this@SetAdsID.apply {
        this.remoteConfigOpenAds = fIsEnable
    }

    @JvmName("needToRemoteConfigNativeAdvancedAds")
    fun needToRemoteConfigNativeAdvancedAds(fIsEnable: Boolean=true) = this@SetAdsID.apply {
        this.remoteConfigNativeAdvancedAds = fIsEnable
    }

    @JvmName("needToRemoteConfigInterstitialAds")
    fun needToRemoteConfigInterstitialAds(fIsEnable: Boolean=true) = this@SetAdsID.apply {
        this.remoteConfigInterstitialAds = fIsEnable
    }

    @JvmName("needToRemoteConfigInterstitialAdRewardAds")
    fun needToRemoteConfigInterstitialAdRewardAds(fIsEnable: Boolean=true) = this@SetAdsID.apply {
        this.remoteConfigInterstitialAdRewardAds = fIsEnable
    }

    @JvmName("needToRemoteConfigRewardVideoAds")
    fun needToRemoteConfigRewardVideoAds(fIsEnable: Boolean=true) = this@SetAdsID.apply {
        this.remoteConfigBannerAds = fIsEnable
    }

    //<editor-fold desc="Set Ads ID">
    @JvmName("setAdmobAppId")
    fun setAdmobAppId(fAdmobAppId: String) = this@SetAdsID.apply {
        this.admobAppId = fAdmobAppId
    }

    @JvmName("setAdmobInterstitialAdId")
    fun setAdmobInterstitialAdId(vararg fAdmobInterstitialAdIds: String) = this@SetAdsID.apply {
        this.admobInterstitialAdId.clearAll()
        this.admobInterstitialAdId.addAll(fAdmobInterstitialAdIds)
    }

    @JvmName("setAdmobNativeAdvancedAdId")
    fun setAdmobNativeAdvancedAdId(vararg fAdmobNativeAdvancedAdIds: String) = this@SetAdsID.apply {
        this.admobNativeAdvancedAdId.clearAll()
        this.admobNativeAdvancedAdId.addAll(fAdmobNativeAdvancedAdIds)
    }

    @JvmName("setAdmobRewardVideoAdId")
    fun setAdmobRewardVideoAdId(vararg fAdmobRewardVideoAdIds: String) = this@SetAdsID.apply {
        this.admobRewardVideoAdId.clearAll()
        this.admobRewardVideoAdId.addAll(fAdmobRewardVideoAdIds)
    }

    @JvmName("setAdmobInterstitialAdRewardId")
    fun setAdmobInterstitialAdRewardId(vararg fAdmobInterstitialAdRewardIds: String) =
        this@SetAdsID.apply {
            this.admobInterstitialAdRewardId.clearAll()
            this.admobInterstitialAdRewardId.addAll(fAdmobInterstitialAdRewardIds)
        }

    @JvmName("setAdmobOpenAdId")
    fun setAdmobOpenAdId(vararg fAdmobOpenAdIds: String) = this@SetAdsID.apply {
        this.admobOpenAdId.clearAll()
        this.admobOpenAdId.addAll(fAdmobOpenAdIds)
    }

    @JvmName("setAdmobAdaptiveBannerAdId")
    fun setAdmobBannerAdId(vararg fAdmobBannerAdIds: String) = this@SetAdsID.apply {
        this.admobBannerAdId.clearAll()
        this.admobBannerAdId.addAll(fAdmobBannerAdIds)
    }
    //</editor-fold>

    //<editor-fold desc="Manage Ads Flag">
    @JvmName("isEnableOpenAd")
    fun isEnableOpenAd(fIsEnable: Boolean) = this@SetAdsID.apply {
        this.mIsEnable = fIsEnable
    }

    @JvmName("needToTakeAllTestAdID")
    fun needToTakeAllTestAdID(fIsEnable: Boolean) = this@SetAdsID.apply {
        this.isTakeAllTestAdID = fIsEnable
    }

    @JvmName("needToBlockInterstitialAd")
    fun needToBlockInterstitialAd(fIsEnable: Boolean) = this@SetAdsID.apply {
        this.mIsBlockInterstitialAd = fIsEnable
    }

    @JvmName("isDebugModeEnable")
    fun isDebugModeEnable(fIsDebugMode: Boolean) = this@SetAdsID.apply {
        mIsDebugMode = fIsDebugMode
    }
    //</editor-fold>

    //<editor-fold desc="Set Purchase Key">
    @JvmName("setLifeTimeProductKey")
    fun setLifeTimeProductKey(vararg keys: String) = this@SetAdsID.apply {
        mLifeTimeProductKeyList.clearAll()
        mLifeTimeProductKeyList.addAll(keys.filter { it.isNotEmpty() })
    }

    @JvmName("setSubscriptionKey")
    fun setSubscriptionKey(vararg keys: String) = this@SetAdsID.apply {
        mSubscriptionKeyList.clearAll()
        mSubscriptionKeyList.addAll(keys.filter { it.isNotEmpty() })
    }


    @JvmName("needToGetProductListFromRevenueCat")
    fun needToGetProductListFromRevenueCat(fIsEnable: Boolean) = this@SetAdsID.apply {
        this.isNeedToGetProductListFromRevenueCat = fIsEnable
    }


    @JvmName("setRevenueCatId")
    fun setRevenueCatId(key: String) = this@SetAdsID.apply {
        this.revenueCatId = key
    }


    //</editor-fold>

    //<editor-fold desc="Set Purchase Key">
    @JvmName("isNeedToLoadMultipleInterstitialAdRequest")
    fun isNeedToLoadMultipleInterstitialAdRequest(fIsNeedToLoadMultipleRequest: Boolean) =
        this@SetAdsID.apply {
            mIsNeedToLoadMultipleInterstitialAdRequest = fIsNeedToLoadMultipleRequest
        }

    @JvmName("isNeedToLoadMultipleAppOpenAdRequest")
    fun isNeedToLoadMultipleAppOpenAdRequest(fIsNeedToLoadMultipleRequest: Boolean) =
        this@SetAdsID.apply {
            mIsNeedToLoadMultipleAppOpenAdRequest = fIsNeedToLoadMultipleRequest
        }

    @JvmName("isNeedToLoadMultipleRewardedInterstitialAdRequest")
    fun isNeedToLoadMultipleRewardedInterstitialAdRequest(fIsNeedToLoadMultipleRequest: Boolean) =
        this@SetAdsID.apply {
            mIsNeedToLoadMultipleRewardedInterstitialAdRequest = fIsNeedToLoadMultipleRequest
        }

    @JvmName("isNeedToLoadMultipleRewardedVideoAdRequest")
    fun isNeedToLoadMultipleRewardedVideoAdRequest(fIsNeedToLoadMultipleRequest: Boolean) =
        this@SetAdsID.apply {
            mIsNeedToLoadMultipleRewardedVideoAdRequest = fIsNeedToLoadMultipleRequest
        }

    @JvmName("isNeedToLoadMultipleNativeAdRequest")
    fun isNeedToLoadMultipleNativeAdRequest(fIsNeedToLoadMultipleRequest: Boolean) =
        this@SetAdsID.apply {
            mIsNeedToLoadMultipleNativeAdRequest = fIsNeedToLoadMultipleRequest
        }
    //</editor-fold>

    @JvmName("initialize")
    fun initialize() {
        //<editor-fold desc="Clear All Ads Id List">
        admob_interstitial_ad_model_list.clearAll()
        admob_app_open_ad_model_list.clearAll()
        admob_rewarded_interstitial_ad_model_list.clearAll()
        admob_rewarded_video_ad_model_list.clearAll()
        mList.clearAll()

        admob_native_advanced_ad_id.clearAll()
        admob_banner_ad_id.clearAll()
        //</editor-fold>

        //<editor-fold desc="Set Ads Id List">
        if (isTakeAllTestAdID) {
            admob_app_id = mContext.getStringRes(R.string.test_admob_app_id)

            admob_interstitial_ad_model_list.add(
                InterstitialAdModel(
                    adsID = mContext.getStringRes(R.string.test_admob_interstitial_ad_id)
                )
            )

            admob_app_open_ad_model_list.add(
                OpenAdModel(
                    adsID = mContext.getStringRes(R.string.test_admob_open_ad_id)
                )
            )

            admob_rewarded_interstitial_ad_model_list.add(
                RewardedInterstitialAdModel(
                    adsID = mContext.getStringRes(R.string.test_admob_interstitial_ad_reward_id)
                )
            )

            admob_rewarded_video_ad_model_list.add(
                RewardedVideoAdModel(
                    adsID = mContext.getStringRes(R.string.test_admob_reward_video_ad_id)
                )
            )

            admob_native_advanced_ad_id.add(mContext.getStringRes(R.string.test_admob_native_advanced_ad_id))
            mList.add(
                NativeAdModel(
                    adsID = mContext.getStringRes(R.string.test_admob_native_advanced_ad_id)// + "1"
                )
            )
//            mList.add(
//                NativeAdModel(
//                    adsID = mContext.getStringRes(R.string.test_admob_native_advanced_ad_id)
//                )
//            )

            admob_banner_ad_id.add(mContext.getStringRes(R.string.test_admob_banner_ad_id))
        } else {
            admob_app_id = this.admobAppId

            this.admobInterstitialAdId.forEach {
                admob_interstitial_ad_model_list.add(
                    InterstitialAdModel(
                        adsID = it
                    )
                )
            }

            this.admobOpenAdId.forEach {
                admob_app_open_ad_model_list.add(
                    OpenAdModel(
                        adsID = it
                    )
                )
            }

            this.admobInterstitialAdRewardId.forEach {
                admob_rewarded_interstitial_ad_model_list.add(
                    RewardedInterstitialAdModel(
                        adsID = it
                    )
                )
            }

            this.admobRewardVideoAdId.forEach {
                admob_rewarded_video_ad_model_list.add(
                    RewardedVideoAdModel(
                        adsID = it
                    )
                )
            }

            admob_native_advanced_ad_id.addAll(this.admobNativeAdvancedAdId)
            this.admobNativeAdvancedAdId.forEach {
                mList.add(
                    NativeAdModel(
                        adsID = it
                    )
                )
            }

            admob_banner_ad_id.addAll(this.admobBannerAdId)
        }
        //</editor-fold>

        //<editor-fold desc="Set Product Key List">
        ProductPurchaseHelper.setLifeTimeProductKey(*mLifeTimeProductKeyList.toTypedArray())
        ProductPurchaseHelper.setSubscriptionKey(*mSubscriptionKeyList.toTypedArray())
        //</editor-fold>


        //<editor-fold desc="Set Revenue Cat ">
//        if (isNeedToGetProductListFromRevenueCat) {
//            if (revenueCatId.isNotEmpty()) {
//                Purchases.debugLogsEnabled = true
//                Purchases.configure(
//                    PurchasesConfiguration.Builder(mContext, revenueCatId).observerMode(false)
//                        .appUserID(null).build()
//                )
//                Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInformation ->
//                    // - Update our user's customerInfo object
//                    logD(TAG, "onCreate:  user info -->$customerInformation")
//                    customerInfo = customerInformation
//
//                }
//                initRevenueCatProductList{
//
//                }
//            }
//        }
        //</editor-fold>

        //<editor-fold desc="Set Load Multiple Ads Request Flag">
        InterstitialAdHelper.isNeedToLoadMultipleRequest =
            this.mIsNeedToLoadMultipleInterstitialAdRequest
        AppOpenAdHelper.isNeedToLoadMultipleRequest = this.mIsNeedToLoadMultipleAppOpenAdRequest
        RewardedInterstitialAdHelper.isNeedToLoadMultipleRequest =
            this.mIsNeedToLoadMultipleRewardedInterstitialAdRequest
        RewardedVideoAdHelper.isNeedToLoadMultipleRequest =
            this.mIsNeedToLoadMultipleRewardedVideoAdRequest
        NativeAdHelper.isNeedToLoadMultipleRequest = this.mIsNeedToLoadMultipleNativeAdRequest
        //</editor-fold>

        isOpenAdEnable = this.mIsEnable
        isBlockInterstitialAd = this.mIsBlockInterstitialAd
        isDebugMode = this.mIsDebugMode
    }
}

internal fun ArrayList<*>.clearAll() {
    this.clear()
    this.removeAll(this.toSet())
}