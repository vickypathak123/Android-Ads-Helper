package com.example.ads.helper.demo

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.example.ads.helper.demo.activitys.SecondActivity
import com.example.ads.helper.demo.activitys.SplashActivity
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.openad.AppOpenApplication

class AppApplication : AppOpenApplication(), AppOpenApplication.AppLifecycleListener {

    private val TAG = javaClass.simpleName

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        setAppLifecycleListener(this)

        Log.e(TAG, "onCreate: IS_OPEN_ADS_ENABLE::${this.getBoolean(IS_OPEN_ADS_ENABLE, true)}")

        VasuAdsConfig.with(this)
            .isNeedToLoadMultipleNativeAdRequest(true)
            .isEnableToRemoteConfigOpenAds(true)
            .isEnableToRemoteConfigBannerAds(true)
            .isEnableToRemoteConfigNativeAdvancedAds(true)
            .isEnableToRemoteConfigInterstitialAds(true)
            .isEnableToRemoteConfigInterstitialRewardAds(true)
            .isEnableToRemoteConfigRewardVideoAds(true)
            .isEnableOpenAd(this.getBoolean(IS_OPEN_ADS_ENABLE, true))
            .isDebugModeEnable(true)
            .needToTakeAllTestAdID(true)
            .needToBlockInterstitialAd(false)
            .setLifeTimeProductKey("android.test.purchased")
//            .setSubscriptionKey("com.screen.mirror.cast.share.tv.device.app.weekly","com.screen.mirror.cast.share.tv.device.app.monthly",
//            "com.screen.mirror.cast.share.tv.device.app.yearly")
            .needToGetProductListFromRevenueCat(false)
//            .setRevenueCatId("goog_DyhayqLIFIzWIoidnijVRHkLZXr")

//            .setAdmobBannerAdId()
//            .setAdmobNativeAdvancedAdId("ca-app-pub-3940256099942544/1044960115")
            .initialize()

        initMobileAds("7B65F08938431E179E187CA18BCE3422")

        destroyAllLoadedAd()
//        if (AdsManager(this).isNeedToShowAds() && isOnline)
//            AppOpenAdHelper.loadAd(this)
    }

    override fun onResumeApp(fCurrentActivity: Activity): Boolean {
        Log.e(TAG, "onResumeApp: fCurrentActivity::${fCurrentActivity.localClassName}")
        val isNeedToShowAd: Boolean = when {
            fCurrentActivity is SplashActivity -> {
                Log.d(TAG, "onResumeApp: fCurrentActivity is SplashActivity")
                false
            }

            fCurrentActivity is SecondActivity -> {
                Log.d(TAG, "onResumeApp: fCurrentActivity is ExitActivity")
                false
            }

            this@AppApplication.isNeedToLoadAd -> {
                true
            }

            else -> {
                false
            }
        }

        return isNeedToShowAd
    }
}