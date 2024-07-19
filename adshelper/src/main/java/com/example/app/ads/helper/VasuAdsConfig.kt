@file:Suppress("unused")

package com.example.app.ads.helper

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.openad.AppOpenAdHelper
import com.example.app.ads.helper.purchase.ProductPurchaseHelper
import com.example.app.ads.helper.reward.RewardedInterstitialAdHelper
import com.example.app.ads.helper.reward.RewardedVideoAdHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.ref.WeakReference

/**
 * @author Akshay Harsoda
 * @since 16 Oct 2021
 * @updated 24 Jun 2024
 */

private const val TAG: String = "VasuAdsConfig"


object VasuAdsConfig {

//    private var setAdsID: SetAdsID? = null

    @JvmStatic
    fun with(fContext: Context): SetAdsID {
        return SetAdsID(fContext = fContext)
    }

    class SetAdsID(fContext: Context) : Serializable {
        private val mContextRef: WeakReference<Context> = WeakReference(fContext.applicationContext)
        private val mContext: Context get() = mContextRef.get()!!

        //<editor-fold desc="Product Key List">
        private val mLifeTimeProductKeyList: ArrayList<String> = ArrayList()
        private val mSubscriptionKeyList: ArrayList<String> = ArrayList()
        //</editor-fold>


        private var admobAppId: String = mContext.getStringRes(R.string.test_admob_app_id)

        //<editor-fold desc="Ads Id List">
        private var admobSplashBannerAdIds: ArrayList<String> = ArrayList()
        private var admobBannerAdIds: ArrayList<String> = ArrayList()
        private var admobInterstitialAdIds: ArrayList<String> = ArrayList()
        private var admobNativeAdvancedAdIds: ArrayList<String> = ArrayList()
        private var admobOpenAdIds: ArrayList<String> = ArrayList()
        private var admobRewardInterstitialAdIds: ArrayList<String> = ArrayList()
        private var admobRewardVideoAdIds: ArrayList<String> = ArrayList()
        //</editor-fold>

        //<editor-fold desc="Multiple Request Flag">
        /*private var isNeedToLoadMultipleBannerAdRequest: Boolean = false
        private var isNeedToLoadMultipleInterstitialAdRequest: Boolean = false
        private var isNeedToLoadMultipleNativeAdRequest: Boolean = false
        private var isNeedToLoadMultipleAppOpenAdRequest: Boolean = false
        private var isNeedToLoadMultipleRewardedInterstitialAdRequest: Boolean = false
        private var isNeedToLoadMultipleRewardedVideoAdRequest: Boolean = false*/
        //</editor-fold>

        //<editor-fold desc="Remote Config Flag">
        private var isEnableBannerAdFromRemoteConfig: Boolean = true
        private var isEnableInterstitialAdFromRemoteConfig: Boolean = true
        private var isEnableNativeAdFromRemoteConfig: Boolean = true
        private var isEnableAppOpenAdFromRemoteConfig: Boolean = true
        private var isEnableRewardedInterstitialAdFromRemoteConfig: Boolean = true
        private var isEnableRewardedVideoAdFromRemoteConfig: Boolean = true
        //</editor-fold>

        //<editor-fold desc="Developer Ads Flag">
        private var isBlockInterstitialAd: Boolean = false
        private var isEnableDebugMode: Boolean = false
        private var isEnableOpenAd: Boolean = false
        private var isTakeAllTestAdID: Boolean = false
        //</editor-fold>

        //<editor-fold desc="Set Ads ID">
        @JvmName("setAdmobAppId")
        fun setAdmobAppId(fAdmobAppId: String) = this@SetAdsID.apply {
            this.admobAppId = fAdmobAppId
        }

        @JvmName("setAdmobSplashBannerAdId")
        fun setAdmobSplashBannerAdId(vararg fAdmobSplashBannerAdIds: String) = this@SetAdsID.apply {
            this.admobSplashBannerAdIds.clearAll()
            this.admobSplashBannerAdIds.addAll(fAdmobSplashBannerAdIds)
        }

        @JvmName("setAdmobAdaptiveBannerAdId")
        fun setAdmobBannerAdId(vararg fAdmobBannerAdIds: String) = this@SetAdsID.apply {
            this.admobBannerAdIds.clearAll()
            this.admobBannerAdIds.addAll(fAdmobBannerAdIds)
        }

        @JvmName("setAdmobInterstitialAdId")
        fun setAdmobInterstitialAdId(vararg fAdmobInterstitialAdIds: String) = this@SetAdsID.apply {
            this.admobInterstitialAdIds.clearAll()
            this.admobInterstitialAdIds.addAll(fAdmobInterstitialAdIds)
        }

        @JvmName("setAdmobNativeAdvancedAdId")
        fun setAdmobNativeAdvancedAdId(vararg fAdmobNativeAdvancedAdIds: String) = this@SetAdsID.apply {
            this.admobNativeAdvancedAdIds.clearAll()
            this.admobNativeAdvancedAdIds.addAll(fAdmobNativeAdvancedAdIds)
        }

        @JvmName("setAdmobOpenAdId")
        fun setAdmobOpenAdId(vararg fAdmobOpenAdIds: String) = this@SetAdsID.apply {
            this.admobOpenAdIds.clearAll()
            this.admobOpenAdIds.addAll(fAdmobOpenAdIds)
        }

        @JvmName("setAdmobRewardInterstitialAdId")
        fun setAdmobRewardInterstitialAdId(vararg fAdmobRewardInterstitialAdId: String) = this@SetAdsID.apply {
            this.admobRewardInterstitialAdIds.clearAll()
            this.admobRewardInterstitialAdIds.addAll(fAdmobRewardInterstitialAdId)
        }

        @JvmName("setAdmobRewardVideoAdId")
        fun setAdmobRewardVideoAdId(vararg fAdmobRewardVideoAdIds: String) = this@SetAdsID.apply {
            this.admobRewardVideoAdIds.clearAll()
            this.admobRewardVideoAdIds.addAll(fAdmobRewardVideoAdIds)
        }
        //</editor-fold>

        //<editor-fold desc="Manage Multiple Request Flag">
        /*@JvmName("needToLoadMultipleBannerAdRequest")
        fun needToLoadMultipleBannerAdRequest(fIsLoadMultipleRequest: Boolean) = this@SetAdsID.apply {
            this.isNeedToLoadMultipleBannerAdRequest = fIsLoadMultipleRequest
        }

        @JvmName("needToLoadMultipleInterstitialAdRequest")
        fun needToLoadMultipleInterstitialAdRequest(fIsLoadMultipleRequest: Boolean) = this@SetAdsID.apply {
            this.isNeedToLoadMultipleInterstitialAdRequest = fIsLoadMultipleRequest
        }

        @JvmName("needToLoadMultipleNativeAdRequest")
        fun needToLoadMultipleNativeAdRequest(fIsLoadMultipleRequest: Boolean) = this@SetAdsID.apply {
            this.isNeedToLoadMultipleNativeAdRequest = fIsLoadMultipleRequest
        }

        @JvmName("NeedToLoadMultipleAppOpenAdRequest")
        fun needToLoadMultipleAppOpenAdRequest(fIsLoadMultipleRequest: Boolean) = this@SetAdsID.apply {
            this.isNeedToLoadMultipleAppOpenAdRequest = fIsLoadMultipleRequest
        }

        @JvmName("needToLoadMultipleRewardedInterstitialAdRequest")
        fun needToLoadMultipleRewardedInterstitialAdRequest(fIsLoadMultipleRequest: Boolean) = this@SetAdsID.apply {
            this.isNeedToLoadMultipleRewardedInterstitialAdRequest = fIsLoadMultipleRequest
        }

        @JvmName("needToLoadMultipleRewardedVideoAdRequest")
        fun needToLoadMultipleRewardedVideoAdRequest(fIsLoadMultipleRequest: Boolean) = this@SetAdsID.apply {
            this.isNeedToLoadMultipleRewardedVideoAdRequest = fIsLoadMultipleRequest
        }*/
        //</editor-fold>

        //<editor-fold desc="Manage Remote Config Flag">
        @JvmName("enableBannerAdFromRemoteConfig")
        fun enableBannerAdFromRemoteConfig(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableBannerAdFromRemoteConfig = fIsEnable
        }

        @JvmName("enableInterstitialAdFromRemoteConfig")
        fun enableInterstitialAdFromRemoteConfig(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableInterstitialAdFromRemoteConfig = fIsEnable
        }

        @JvmName("enableNativeAdFromRemoteConfig")
        fun enableNativeAdFromRemoteConfig(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableNativeAdFromRemoteConfig = fIsEnable
        }

        @JvmName("enableAppOpenAdFromRemoteConfig")
        fun enableAppOpenAdFromRemoteConfig(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableAppOpenAdFromRemoteConfig = fIsEnable
        }

        @JvmName("enableRewardedInterstitialAdFromRemoteConfig")
        fun enableRewardedInterstitialAdFromRemoteConfig(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableRewardedInterstitialAdFromRemoteConfig = fIsEnable
        }

        @JvmName("enableRewardedVideoAdFromRemoteConfig")
        fun enableRewardedVideoAdFromRemoteConfig(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableRewardedVideoAdFromRemoteConfig = fIsEnable
        }
        //</editor-fold>

        //<editor-fold desc="Manage Ads Flag">
        @JvmName("enableDebugMode")
        fun enableDebugMode(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableDebugMode = fIsEnable
        }

        @JvmName("enableOpenAd")
        fun enableOpenAd(fIsEnable: Boolean) = this@SetAdsID.apply {
            this.isEnableOpenAd = fIsEnable
        }

        @JvmName("needToBlockInterstitialAd")
        fun needToBlockInterstitialAd(fIsBlock: Boolean) = this@SetAdsID.apply {
            this.isBlockInterstitialAd = fIsBlock
        }

        @JvmName("needToTakeAllTestAdID")
        fun needToTakeAllTestAdID(fIsTakeAll: Boolean) = this@SetAdsID.apply {
            this.isTakeAllTestAdID = fIsTakeAll
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
        //</editor-fold>

        @JvmName("initialize")
        fun initialize() {
            //region Multiple Request Flag
//            is_need_to_load_multiple_banner_ad_request = this.isNeedToLoadMultipleBannerAdRequest
//            is_need_to_load_multiple_interstitial_ad_request = this.isNeedToLoadMultipleInterstitialAdRequest
//            is_need_to_load_multiple_native_ad_request = this.isNeedToLoadMultipleNativeAdRequest
//            is_need_to_load_multiple_app_open_ad_request = this.isNeedToLoadMultipleAppOpenAdRequest
//            is_need_to_load_multiple_rewarded_interstitial_ad_request = this.isNeedToLoadMultipleRewardedInterstitialAdRequest
//            is_need_to_load_multiple_rewarded_video_ad_request = this.isNeedToLoadMultipleRewardedVideoAdRequest
            //endregion

            //region Remote Config Flag
            is_enable_banner_ad_from_remote_config = this.isEnableBannerAdFromRemoteConfig
            is_enable_interstitial_ad_from_remote_config = this.isEnableInterstitialAdFromRemoteConfig
            is_enable_native_ad_from_remote_config = this.isEnableNativeAdFromRemoteConfig
            is_enable_app_open_ad_from_remote_config = this.isEnableAppOpenAdFromRemoteConfig
            is_enable_rewarded_interstitial_ad_from_remote_config = this.isEnableRewardedInterstitialAdFromRemoteConfig
            is_enable_rewarded_video_ad_from_remote_config = this.isEnableRewardedVideoAdFromRemoteConfig
            //endregion

            //region Developer Ads Flag
            com.example.app.ads.helper.isBlockInterstitialAd = (this.isBlockInterstitialAd && this.isTakeAllTestAdID)
            com.example.app.ads.helper.isEnableDebugMode = this.isEnableDebugMode
            com.example.app.ads.helper.isEnableOpenAd = this.isEnableOpenAd
            //endregion

            //region Clear & Set All Ads Id List
            setAdmobAppID(fAppID = if (isTakeAllTestAdID) mContext.getStringRes(R.string.test_admob_app_id) else this.admobAppId)
            setAdmobSplashBannerID(
                fIDs = if (isTakeAllTestAdID) {
                    arrayOf(
                        mContext.getStringRes(R.string.test_admob_adaptive_banner_ad_id),
                    )
                } else {
                    this.admobSplashBannerAdIds.toTypedArray()
                }
            )
            setAdmobBannerID(
                fIDs = if (isTakeAllTestAdID) {
                    arrayOf(
                        mContext.getStringRes(R.string.test_admob_banner_ad_id),
                        mContext.getStringRes(R.string.test_admob_adaptive_banner_ad_id),
                        mContext.getStringRes(R.string.test_admob_fixed_size_banner_ad_id)
                    )
                } else {
                    this.admobBannerAdIds.toTypedArray()
                }
            )
            setAdmobInterstitialID(
                fIDs = if (isTakeAllTestAdID) {
                    arrayOf(
                        mContext.getStringRes(R.string.test_admob_interstitial_ad_id),
                        mContext.getStringRes(R.string.test_admob_interstitial_video_ad_id),
                    )
                } else {
                    this.admobInterstitialAdIds.toTypedArray()
                }
            )
            setAdmobNativeID(
                fIDs = if (isTakeAllTestAdID) {
                    arrayOf(
//                        mContext.getStringRes(R.string.test_admob_native_advanced_ad_id) + "Failed",
                        mContext.getStringRes(R.string.test_admob_native_advanced_ad_id),
                        mContext.getStringRes(R.string.test_admob_native_advanced_video_ad_id),
                    )
                } else {
                    this.admobNativeAdvancedAdIds.toTypedArray()
                }
            )
            setAdmobAppOpenID(
                fIDs = if (isTakeAllTestAdID) {
                    arrayOf(mContext.getStringRes(R.string.test_admob_open_ad_id))
                } else {
                    this.admobOpenAdIds.toTypedArray()
                }
            )
            setAdmobRewardedInterstitialID(
                fIDs = if (isTakeAllTestAdID) {
                    arrayOf(mContext.getStringRes(R.string.test_admob_reward_interstitial_ad_id))
                } else {
                    this.admobRewardInterstitialAdIds.toTypedArray()
                }
            )
            setAdmobRewardedVideoID(
                fIDs = if (isTakeAllTestAdID) {
                    arrayOf(mContext.getStringRes(R.string.test_admob_reward_video_ad_id))
                } else {
                    this.admobRewardVideoAdIds.toTypedArray()
                }
            )
            //endregion

            //<editor-fold desc="Set Product Key List">
            ProductPurchaseHelper.setLifeTimeProductKey(*mLifeTimeProductKeyList.toTypedArray())
            ProductPurchaseHelper.setSubscriptionKey(*mSubscriptionKeyList.toTypedArray())
            //</editor-fold>

            loadAdRequest()
        }

        /*@JvmName("initializeMultipleRequest")
        fun initializeMultipleRequest() {
            //region Multiple Request Flag
            is_need_to_load_multiple_banner_ad_request = this.isNeedToLoadMultipleBannerAdRequest.takeIf { it != is_need_to_load_multiple_banner_ad_request } ?: is_need_to_load_multiple_banner_ad_request
            is_need_to_load_multiple_interstitial_ad_request = this.isNeedToLoadMultipleInterstitialAdRequest.takeIf { it != is_need_to_load_multiple_interstitial_ad_request } ?: is_need_to_load_multiple_interstitial_ad_request
            is_need_to_load_multiple_native_ad_request = this.isNeedToLoadMultipleNativeAdRequest.takeIf { it != is_need_to_load_multiple_native_ad_request } ?: is_need_to_load_multiple_native_ad_request
            is_need_to_load_multiple_app_open_ad_request = this.isNeedToLoadMultipleAppOpenAdRequest.takeIf { it != is_need_to_load_multiple_app_open_ad_request } ?: is_need_to_load_multiple_app_open_ad_request
            is_need_to_load_multiple_rewarded_interstitial_ad_request = this.isNeedToLoadMultipleRewardedInterstitialAdRequest.takeIf { it != is_need_to_load_multiple_rewarded_interstitial_ad_request } ?: is_need_to_load_multiple_rewarded_interstitial_ad_request
            is_need_to_load_multiple_rewarded_video_ad_request = this.isNeedToLoadMultipleRewardedVideoAdRequest.takeIf { it != is_need_to_load_multiple_rewarded_video_ad_request } ?: is_need_to_load_multiple_rewarded_video_ad_request
            //endregion
        }*/

        @JvmName("initializeRemoteConfig")
        fun initializeRemoteConfig() {
            //region Remote Config Flag
            is_enable_banner_ad_from_remote_config = this.isEnableBannerAdFromRemoteConfig.takeIf { it != is_enable_banner_ad_from_remote_config } ?: is_enable_banner_ad_from_remote_config
            is_enable_interstitial_ad_from_remote_config = this.isEnableInterstitialAdFromRemoteConfig.takeIf { it != is_enable_interstitial_ad_from_remote_config } ?: is_enable_interstitial_ad_from_remote_config
            is_enable_native_ad_from_remote_config = this.isEnableNativeAdFromRemoteConfig.takeIf { it != is_enable_native_ad_from_remote_config } ?: is_enable_native_ad_from_remote_config
            is_enable_app_open_ad_from_remote_config = this.isEnableAppOpenAdFromRemoteConfig.takeIf { it != is_enable_app_open_ad_from_remote_config } ?: is_enable_app_open_ad_from_remote_config
            is_enable_rewarded_interstitial_ad_from_remote_config = this.isEnableRewardedInterstitialAdFromRemoteConfig.takeIf { it != is_enable_rewarded_interstitial_ad_from_remote_config } ?: is_enable_rewarded_interstitial_ad_from_remote_config
            is_enable_rewarded_video_ad_from_remote_config = this.isEnableRewardedVideoAdFromRemoteConfig.takeIf { it != is_enable_rewarded_video_ad_from_remote_config } ?: is_enable_rewarded_video_ad_from_remote_config
            //endregion
        }

        @JvmName("initializeDeveloperAdsFlag")
        fun initializeDeveloperAdsFlag() {
            //region Developer Ads Flag
            com.example.app.ads.helper.isBlockInterstitialAd = (this.isBlockInterstitialAd && this.isTakeAllTestAdID).takeIf { it != com.example.app.ads.helper.isBlockInterstitialAd } ?: com.example.app.ads.helper.isBlockInterstitialAd
            com.example.app.ads.helper.isEnableDebugMode = this.isEnableDebugMode.takeIf { it != com.example.app.ads.helper.isEnableDebugMode } ?: com.example.app.ads.helper.isEnableDebugMode
            com.example.app.ads.helper.isEnableOpenAd = this.isEnableOpenAd.takeIf { it != com.example.app.ads.helper.isEnableOpenAd } ?: com.example.app.ads.helper.isEnableOpenAd
            //endregion
        }

        private fun setAdmobAppID(fAppID: String) {
            admob_app_id = fAppID
        }

        private fun setAdmobSplashBannerID(vararg fIDs: String) {
            list_of_admob_splash_banner_ads.clearAll()
            if (fIDs.isNotEmpty()) {
                fIDs.forEach {
                    list_of_admob_splash_banner_ads.add(
                        AdStatusModel(
                            adID = it
                        )
                    )
                }
            }
        }

        private fun setAdmobBannerID(vararg fIDs: String) {
            list_of_admob_banner_ads.clearAll()
            if (fIDs.isNotEmpty()) {
                fIDs.forEach {
                    list_of_admob_banner_ads.add(
                        AdStatusModel(
                            adID = it
                        )
                    )
                }
            }
        }

        private fun setAdmobInterstitialID(vararg fIDs: String) {
            list_of_admob_interstitial_ads.clearAll()
            if (fIDs.isNotEmpty()) {
                fIDs.forEach {
                    list_of_admob_interstitial_ads.add(
                        AdStatusModel(
                            adID = it
                        )
                    )
                }
            }
        }

        private fun setAdmobNativeID(vararg fIDs: String) {
            list_of_admob_native_ads.clearAll()
            if (fIDs.isNotEmpty()) {
                fIDs.forEach {
                    list_of_admob_native_ads.add(
                        AdStatusModel(
                            adID = it
                        )
                    )
                }
            }
        }

        private fun setAdmobAppOpenID(vararg fIDs: String) {
            list_of_admob_app_open_ads.clearAll()
            if (fIDs.isNotEmpty()) {
                fIDs.forEach {
                    list_of_admob_app_open_ads.add(
                        AdStatusModel(
                            adID = it
                        )
                    )
                }
            }
        }

        private fun setAdmobRewardedInterstitialID(vararg fIDs: String) {
            list_of_admob_rewarded_interstitial_ads.clearAll()
            if (fIDs.isNotEmpty()) {
                fIDs.forEach {
                    list_of_admob_rewarded_interstitial_ads.add(
                        AdStatusModel(
                            adID = it
                        )
                    )
                }
            }
        }

        private fun setAdmobRewardedVideoID(vararg fIDs: String) {
            list_of_admob_rewarded_video_ads.clearAll()
            if (fIDs.isNotEmpty()) {
                fIDs.forEach {
                    list_of_admob_rewarded_video_ads.add(
                        AdStatusModel(
                            adID = it
                        )
                    )
                }
            }
        }

        private fun loadAdRequest() {
            CoroutineScope(Dispatchers.Main).launch {
                isInternetAvailable.observeForever {
                    if (it) {
                        if (InterstitialAdHelper.isInterstitialAdEnable()) {
                            InterstitialAdHelper.loadAd(fContext = mContext)
                        }

                        if (AppOpenAdHelper.isAppOpenAdEnable()) {
                            AppOpenAdHelper.loadAd(fContext = mContext)
                        }
                    }
                }
            }
        }

    }
}