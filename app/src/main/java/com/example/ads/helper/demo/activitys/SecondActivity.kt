package com.example.ads.helper.demo.activitys

import android.os.CountDownTimer
import android.util.Log
import com.example.ads.helper.demo.base.BaseActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.databinding.ActivitySecondBinding

class SecondActivity : BaseBindingActivity<ActivitySecondBinding>() {

    private var mTimer: AdsCountDownTimer? = null

    private var isActivityPause: Boolean = false

    override fun getActivityContext(): BaseActivity {
        return this@SecondActivity
    }

    override fun setBinding(): ActivitySecondBinding {
        return ActivitySecondBinding.inflate(layoutInflater)
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
        startTimer(5000)
    }

    private fun startTimer(fTime: Long) {
        mTimer?.cancel()
        mTimer = AdsCountDownTimer(millisInFuture = fTime, countDownInterval = 1000)
        mTimer?.start()
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

            mBinding.txtExit.text = "Exit\n\n${(((millisInFuture - millisUntilFinished) / 1000) + 1)}"
        }

        override fun onFinish() {
            finishAffinity()
        }
    }
}