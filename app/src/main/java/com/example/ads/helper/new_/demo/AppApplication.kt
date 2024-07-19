package com.example.ads.helper.new_.demo

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.example.ads.helper.new_.demo.base.shared_prefs.getBoolean
import com.example.ads.helper.new_.demo.base.shared_prefs.save
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.openad.AppOpenApplication

class AppApplication : AppOpenApplication() {

    private val TAG = javaClass.simpleName

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

//        this.save(IS_OPEN_ADS_ENABLE, true)

        Log.e(TAG, "onCreate: IS_OPEN_ADS_ENABLE::${this.getBoolean(IS_OPEN_ADS_ENABLE, true)}")

        VasuAdsConfig.with(this)
            .enableOpenAd(fIsEnable = true)
            .enableDebugMode(fIsEnable = true)
            .needToTakeAllTestAdID(fIsTakeAll = true)
            .needToBlockInterstitialAd(fIsBlock = false)
//            .enableAppOpenAdFromRemoteConfig(fIsEnable = false)
//            .enableBannerAdFromRemoteConfig(fIsEnable = false)
//            .enableInterstitialAdFromRemoteConfig(fIsEnable = false)
//            .enableNativeAdFromRemoteConfig(fIsEnable = false)
//            .enableRewardedInterstitialAdFromRemoteConfig(fIsEnable = false)
//            .enableRewardedVideoAdFromRemoteConfig(fIsEnable = false)
            .initialize()

        initMobileAds("747DD141C5DB53A9F7E3E452845C08FF")

        destroyAllLoadedAd()
    }

    override fun onResumeApp(fCurrentActivity: Activity): Boolean {
        return false
    }

//    override fun onResumeApp(fCurrentActivity: Activity): Boolean {
//        Log.e(TAG, "onResumeApp: fCurrentActivity::${fCurrentActivity.localClassName}")
////        val isNeedToShowAd: Boolean = when {
////            fCurrentActivity is SplashActivity -> {
////                Log.d(TAG, "onResumeApp: fCurrentActivity is SplashActivity")
////                false
////            }
////
////            fCurrentActivity is SecondActivity -> {
////                Log.d(TAG, "onResumeApp: fCurrentActivity is ExitActivity")
////                false
////            }
////
////            this@AppApplication.isNeedToLoadAd -> {
////                true
////            }
////
////            else -> {
////                false
////            }
////        }
////
////        return isNeedToShowAd
//
//        return false
//    }
}