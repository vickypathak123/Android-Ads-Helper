package com.example.ads.helper.demo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.CountDownTimer
import android.util.Log
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.databinding.ActivitySplashBinding
import com.example.ads.helper.demo.base.utils.isOnline
import com.example.app.ads.helper.InterstitialAdHelper
import com.example.app.ads.helper.InterstitialAdHelper.isShowInterstitialAd
import com.example.app.ads.helper.InterstitialRewardHelper
import com.example.app.ads.helper.NativeAdvancedModelHelper
import com.example.app.ads.helper.RewardVideoHelper
import com.example.app.ads.helper.openad.OpenAdHelper
import com.example.app.ads.helper.openad.OpenAdHelper.isShowOpenAd

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseBindingActivity<ActivitySplashBinding>() {

    private var mTimer: AdsCountDownTimer? = null

    private var isActivityPause: Boolean = false

    override fun getActivityContext(): AppCompatActivity {
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
        OpenAdHelper.destroy()
        InterstitialAdHelper.destroy()
        NativeAdvancedModelHelper.destroy()
        RewardVideoHelper.destroy()
        InterstitialRewardHelper.destroy()
        //</editor-fold>

        setAdDelay()
    }

    private fun setAdDelay() {
        if (isOnline) {

            startTimer(3000)

            if (this.getBoolean(IS_OPEN_ADS_ENABLE, true)) {
                OpenAdHelper.loadOpenAd(mActivity, onAdLoad = {
                    Log.e(TAG, "onOpenAdLoad: ")
                    mTimer?.cancel()
                    openActivityWithAd()
                })
            } else {
                InterstitialAdHelper.loadInterstitialAd(
                    fContext = mActivity,
                    fIsShowFullScreenNativeAd = false
                ) {
                    Log.e(TAG, "onInterstitialAdLoad: ")
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
            if (OpenAdHelper.isAdAvailable()) {
                Log.e(TAG, "openActivityWithAd: Call With Open Ad")
                mActivity.isShowOpenAd {
                    startNextActivity()
                }
            } else {
                Log.e(TAG, "openActivityWithAd: Call With Out Open Ad")
                startNextActivity()
            }
        } else {
            mActivity.isShowInterstitialAd { _, _ ->
                Log.e(TAG, "openActivityWithAd: Call With or With-Out Interstitial Ad")
                startNextActivity()
            }
        }
    }

    private fun startNextActivity() {
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