package com.example.ads.helper.demo

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.example.ads.helper.demo.base.utils.getStringRes
import com.example.app.ads.helper.VasuAdsConfig
import com.example.app.ads.helper.openad.AppOpenApplication
import com.example.app.ads.helper.openad.OpenAdHelper

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
            .isEnableOpenAd(this.getBoolean(IS_OPEN_ADS_ENABLE, true))
//            .isEnableOpenAd(false)
            .needToTakeAllTestAdID(true)
//            .needToBlockInterstitialAd(true)
            .setLifeTimeProductKey("android.test.purchased")
            .setAdmobNativeAdvancedAdId("ca-app-pub-3940256099942544/1044960115")
//            .setAdmobNativeAdvancedAdId(this.getStringRes(R.string.admob_native_advanced_ad_id))
            .setAdmobOpenAdId(this.getStringRes(R.string.admob_open_ad_id))
            .setAdmobInterstitialAdId(this.getStringRes(R.string.admob_interstitial_ad_id))
            .setAdmobInterstitialAdRewardId(this.getStringRes(R.string.admob_interstitial_ad_reward_id))
            .setAdmobRewardVideoAdId(this.getStringRes(R.string.admob_reward_video_ad_id))
            .isDebugModeEnable(true)
            .initialize()

        initMobileAds("FE593321B70E310B9A65C2FB1A8763E4")

        OpenAdHelper.destroy()
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

            this@AppApplication.isNeedToAdShow -> {
                true
            }
            else -> {
                false
            }
        }

        return isNeedToShowAd
    }
}