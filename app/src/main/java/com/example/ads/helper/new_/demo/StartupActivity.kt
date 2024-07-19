package com.example.ads.helper.new_.demo

import android.util.Log
import android.view.View
import com.example.ads.helper.new_.demo.activitys.MainActivity
import com.example.ads.helper.new_.demo.base.BaseActivity
import com.example.ads.helper.new_.demo.base.BaseBindingActivity
import com.example.ads.helper.new_.demo.base.shared_prefs.getBoolean
import com.example.ads.helper.new_.demo.databinding.ActivityStartupBinding
import com.example.ads.helper.new_.demo.utils.AppTimer
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper.showInterstitialAd
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.openad.AppOpenAdHelper
import com.example.app.ads.helper.openad.AppOpenAdHelper.showAppOpenAd


class StartupActivity : BaseBindingActivity<ActivityStartupBinding>() {

    private var isFirstTime: Boolean = true
    private var isAdLoaded: Boolean = false
    private var isLaunchScreenWithAd: Boolean = false
    private var isLaunchNextScreen: Boolean = false

    override fun getActivityContext(): BaseActivity {
        return this@StartupActivity
    }

    override fun setBinding(): ActivityStartupBinding {
        return ActivityStartupBinding.inflate(layoutInflater)
    }


    override fun onNoInternetDialogShow() {
        super.onNoInternetDialogShow()
        mTimer?.cancelTimer()
        mTimer = null
    }

    override fun onNoInternetDialogDismiss() {
        super.onNoInternetDialogDismiss()
        initView()
    }

    @Suppress("DEPRECATION")
    override fun initView() {
        super.initView()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        loadAdWithTimer()
    }

    private fun loadAdWithTimer() {
        if (isOnline) {
            startTimer(millisInFuture = 6000, countDownInterval = 1000)

            if (isFirstTime) {
                isFirstTime = false
                if (this.getBoolean(IS_OPEN_ADS_ENABLE, true)) {
                    /*AppOpenAdHelper.loadAd(mActivity, onAdLoaded = { isNewAdLoaded ->
                        if (isNewAdLoaded) {
                            Log.e(TAG, "Admob_ onOpenAdLoad: ")
                            isAdLoaded = true
                            checkAndLaunchScreenWithAd()
                        }
                    })*/

                    AppOpenAdHelper.setOnAppOpenAdLoadListener(fListener = object : AppOpenAdHelper.OnAppOpenAdLoadListener {
                        override fun onAdLoaded() {
                            Log.e(TAG, "Admob_ onOpenAdLoad: ")
                            isAdLoaded = true
                            checkAndLaunchScreenWithAd()
                        }
                    })
                } else {
                    /*InterstitialAdHelper.loadAd(fContext = mActivity) { isNewAdLoaded ->
                        if (isNewAdLoaded) {
                            Log.e(TAG, "Admob_ onInterstitialAdLoad: ")
                            isAdLoaded = true
                            checkAndLaunchScreenWithAd()
                        }
                    }*/

                    InterstitialAdHelper.setOnInterstitialAdLoadListener(fListener = object : InterstitialAdHelper.OnInterstitialAdLoadListener {
                        override fun onAdLoaded() {
                            Log.e(TAG, "Admob_Inte onInterstitialAdLoad: ")
                            isAdLoaded = true
                            checkAndLaunchScreenWithAd()
                        }
                    })
                }
            }
        } else {
            startTimer(millisInFuture = 1000, countDownInterval = 1000)
        }
    }


    private fun startTimer(millisInFuture: Long, countDownInterval: Long) {
        mTimer?.cancelTimer()
        mTimer = AppTimer(
            millisInFuture = millisInFuture,
            countDownInterval = countDownInterval,
            onTick = {
                Log.e(TAG, "Admob_Inte startTimer: onTick: $it")
            },
            onFinish = {
                Log.e(TAG, "Admob_Inte startTimer: onFinish: ")
//                checkAndLaunchScreenWithAd()
            }
        )

        mTimer?.start()
    }

    private fun checkAndLaunchScreenWithAd() {
        if (!isOnPause) {
            if (!isLaunchScreenWithAd) {
                launchScreenWithAd()
            }
        }
    }

    private fun launchScreenWithAd() {
        mTimer?.cancelTimer()
        mTimer = null
        isLaunchScreenWithAd = true

        if (this.getBoolean(IS_OPEN_ADS_ENABLE, true)) {
            if (!isLaunchNextScreen) {
                Log.e(TAG, "openActivityWithAd: Call With or With-Out Open Ad")
                mActivity.showAppOpenAd {
                    checkAndLaunchNextScreen()
                }
            }
        } else {
            if (!isLaunchNextScreen) {
                mActivity.showInterstitialAd { _, _ ->
                    Log.e(TAG, "openActivityWithAd: Call With or With-Out Interstitial Ad")
                    checkAndLaunchNextScreen()
                }
            }
        }
    }

    private fun startNextTimer() {
        startTimer(millisInFuture = 500, countDownInterval = 100)
    }

    private fun checkAndLaunchNextScreen() {
        if (!isLaunchNextScreen) {
            launchNextScreen()
        }
    }

    private fun launchNextScreen() {
        isLaunchNextScreen = true
        launchActivity(fIntent = getActivityIntent<MainActivity>(), isAdsShowing = false, isNeedToFinish = true)
    }
    //</editor-fold>

    override fun onResume() {
        if (isOnPause) {
            isOnPause = false

            mTimer?.cancelTimer()
            mTimer = null

            if (!isLaunchScreenWithAd && !isLaunchNextScreen) {
                if (isAdLoaded) {
                    startNextTimer()
                } else {
                    loadAdWithTimer()
                }
            } else if (isLaunchScreenWithAd) {
                checkAndLaunchNextScreen()
            } else {
                Log.e(TAG, "onResume: already going on next screen")
            }
        }
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        isOnPause = true
        mTimer?.cancelTimer()
    }

    override fun customOnBackPressed() {

    }
}