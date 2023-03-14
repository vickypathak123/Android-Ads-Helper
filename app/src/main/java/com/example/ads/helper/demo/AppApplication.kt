package com.example.ads.helper.demo

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.example.ads.helper.demo.activitys.SecondActivity
import com.example.ads.helper.demo.activitys.SplashActivity
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.openad.AppOpenApplication
import com.example.app.ads.helper.revenuecat.getRevenueCatProductInfo
import com.revenuecat.purchases.PackageType

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
            .needToTakeAllTestAdID(true)
            .isDebugModeEnable(true)
            .needToBlockInterstitialAd(false)

            .isNeedToLoadMultipleNativeAdRequest(true)
            .isEnableOpenAd(this.getBoolean(IS_OPEN_ADS_ENABLE, true))
            .setLifeTimeProductKey("android.test.purchased")
            .needToGetProductListFromRevenueCat(true)
//            .setRevenueCatId("goog_DyhayqLIFIzWIoidnijVRHkLZXr")

//            .setAdmobBannerAdId()
//            .setAdmobNativeAdvancedAdId("ca-app-pub-3940256099942544/1044960115")
            .initialize()

        initMobileAds("7B65F08938431E179E187CA18BCE3422")

        destroyAllLoadedAd()
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