package com.example.ads.helper.demo.activitys

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import com.example.ads.helper.demo.IS_OPEN_ADS_ENABLE
import com.example.ads.helper.demo.base.BaseActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.databinding.ActivitySplashBinding
import com.example.ads.helper.demo.base.utils.isOnline
import com.example.ads.helper.demo.getBoolean
import com.example.app.ads.helper.NativeAdvancedModelHelper
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper.showInterstitialAd
import com.example.app.ads.helper.openad.AppOpenAdHelper
import com.example.app.ads.helper.openad.AppOpenAdHelper.showAppOpenAd
import com.example.app.ads.helper.reward.RewardedInterstitialAdHelper
import com.example.app.ads.helper.reward.RewardedVideoAdHelper

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseBindingActivity<ActivitySplashBinding>() {

    private var mTimer: AdsCountDownTimer? = null

    private var isActivityPause: Boolean = false

    private var isNextActivityCall: Boolean = false

    override fun getActivityContext(): BaseActivity {
        return this@SplashActivity
    }

    override fun setBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()

        if (isActivityPause) {
            isActivityPause = false
            mTimer?.start()
        }
    }

    override fun initView() {
        super.initView()

        //<editor-fold desc="Destroy All Ads in splash once">
        AppOpenAdHelper.destroy()
        InterstitialAdHelper.destroy()
        RewardedInterstitialAdHelper.destroy()
        RewardedVideoAdHelper.destroy()
        NativeAdvancedModelHelper.destroy()
        //</editor-fold>

        setAdDelay()
    }

    private fun setAdDelay() {
        if (isOnline) {

            startTimer(3000)

            if (this.getBoolean(IS_OPEN_ADS_ENABLE, true)) {
                AppOpenAdHelper.loadAd(mActivity, true,onAdLoaded = {
                    Log.e(TAG, "onOpenAdLoad: ")
                    mTimer?.cancel()
                    openActivityWithAd()
                })
            } else {
                InterstitialAdHelper.loadAd(
                    fContext = mActivity
                ) {
                    Log.e(TAG, "Admob_ onInterstitialAdLoad: ")
                    mTimer?.cancel()
                    openActivityWithAd()
                }
            }

        } else {
            startTimer(1000)
        }
    }

    private fun startTimer(fTime: Long) {
        mTimer?.cancel()
        mTimer = AdsCountDownTimer(millisInFuture = fTime, countDownInterval = 1000)
        mTimer?.start()
    }

    internal fun openActivityWithAd() {

        mTimer?.cancel()
        mTimer = null

        if (this.getBoolean(IS_OPEN_ADS_ENABLE, true)) {
            if (AppOpenAdHelper.isAppOpenAdAvailable()) {
                if (!isNextActivityCall) {
                    Log.e(TAG, "openActivityWithAd: Call With Open Ad")
                    mActivity.showAppOpenAd(true) {
                        startNextActivity()
                    }
                }
            } else {
                if (!isNextActivityCall) {
                    Log.e(TAG, "openActivityWithAd: Call With Out Open Ad")
                    startNextActivity()
                }
            }
        } else {
            if (!isNextActivityCall) {
                mActivity.showInterstitialAd { _, _ ->
                    Log.e(TAG, "openActivityWithAd: Call With or With-Out Interstitial Ad")
                    startNextActivity()
                }
            }
        }
    }

    private fun startNextActivity() {

        isNextActivityCall = true
        launchActivity(
            fIntent = getActivityIntent<MainActivity>(),
            isNeedToFinish = false
        )
    }

    override fun onBackPressed() {

    }

    override fun onStop() {
        super.onStop()
        isActivityPause = true
        mTimer?.cancel()
    }

    private inner class AdsCountDownTimer(private val millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            Log.e(TAG, "onTick: Time::${(((millisInFuture - millisUntilFinished) / 1000) + 1)}")
        }

        override fun onFinish() {
            Log.e(TAG, "countDownTimer: onFinish")
            openActivityWithAd()
        }
    }

}