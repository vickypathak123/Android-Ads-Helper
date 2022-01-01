package com.example.ads.helper.demo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.CountDownTimer
import android.util.Log
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.databinding.ActivitySplashBinding
import com.example.app.ads.helper.openad.OpenAdHelper
import com.example.app.ads.helper.openad.OpenAdHelper.isShowOpenAd
import com.example.ads.helper.demo.base.utils.isOnline

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

    private fun openActivityWithAd() {

        mTimer?.cancel()
        mTimer = null

        if (OpenAdHelper.isAdAvailable()) {
            Log.e(TAG, "openActivityWithAd: Call With Open Ad")
            mActivity.isShowOpenAd {
                startNextActivity()
            }
        } else {
            Log.e(TAG, "openActivityWithAd: Call With Out Open Ad")
            startNextActivity()
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