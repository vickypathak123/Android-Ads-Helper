@file:Suppress("unused")

package com.example.ads.helper.new_.demo.utils

import android.os.CountDownTimer
import android.util.Log
import java.util.Locale

class AppTimer(
    millisInFuture: Long,
    countDownInterval: Long,
    private val onTick: (countDownTime: Long) -> Unit = {},
    private val onFinish: () -> Unit = {},
) : CountDownTimer(millisInFuture, countDownInterval) {

    var isRunning: Boolean = false

    override fun onTick(millisUntilFinished: Long) {
        val lCountDownTime = (millisUntilFinished / 1000)
        val formattedSeconds = String.format(Locale.getDefault(), "%02d", lCountDownTime)
        Log.e("AppTimer", "onTick: Formatted Time Number is $formattedSeconds")
        isRunning = true
        onTick.invoke(lCountDownTime)
    }

    override fun onFinish() {
        isRunning = false
        onFinish.invoke()
    }

    fun cancelTimer() {
        isRunning = false
        this.cancel()
    }
}