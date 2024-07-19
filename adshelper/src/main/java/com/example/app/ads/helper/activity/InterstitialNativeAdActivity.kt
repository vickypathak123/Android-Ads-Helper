package com.example.app.ads.helper.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.example.app.ads.helper.*
import com.example.app.ads.helper.nativead.NativeAdInterstitialType
import com.example.app.ads.helper.nativead.NativeAdView


class InterstitialNativeAdActivity : AppCompatActivity() {

    @Suppress("PropertyName")
    val TAG: String = javaClass.simpleName

    private var mOnBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
//            findViewById<ImageView>(R.id.iv_close_ad)?.performClick()
        }
    }

    private val mActivity: FragmentActivity
        get() {
            return this@InterstitialNativeAdActivity
        }

    companion object {
        private var onThisAdClosed:() -> Unit = {}
        fun lunchFullScreenAd(fActivity: Activity, onInterstitialNativeAdClosed:() -> Unit) {
            onThisAdClosed = onInterstitialNativeAdClosed
            Intent(fActivity, InterstitialNativeAdActivity::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }.also { fActivity.startActivity(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.theme_interstitial_native_ad_activity)
        window?.let { lWindow ->
            lWindow.decorView.let { lDecorView ->
                WindowInsetsControllerCompat(lWindow, lDecorView).apply {
                    this.isAppearanceLightStatusBars = true // true or false as desired.
                    this.isAppearanceLightNavigationBars = true
                    this.hide(WindowInsetsCompat.Type.systemBars())
                }
            }
        }


        setContentView(R.layout.activity_interstitial_native_ad)
        setContentView()
    }

    private fun setContentView() {
        isAnyAdOpen = true
        loadAds()
        initViewAction()
        initViewListener()
        mActivity.onBackPressedDispatcher.addCallback(mActivity, mOnBackPressedCallback)
    }

    private fun loadAds() {
        findViewById<NativeAdView>(R.id.interstitial_native_ad_view)?.let { view ->
            view.setOnNativeAdViewListener(fListener = object : NativeAdView.OnNativeAdViewListener {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isAnyAdOpen = true
                    Log.e(TAG, "onAdLoaded: ")
                    mActivity.runOnUiThread {
                        findViewById<ImageView>(R.id.iv_close_ad)?.beVisibleIf(view.getNativeAdInterstitialType() == NativeAdInterstitialType.WEBSITE)
                    }
                }

                override fun onAdCustomClosed() {
                    super.onAdCustomClosed()
                    Log.e(TAG, "onAdCustomClosed: ")
                    isAnyAdOpen = true
                    mActivity.runOnUiThread {
                        findViewById<ImageView>(R.id.iv_close_ad)?.performClick()
                    }
                }

                override fun onAdFailed() {
                    super.onAdFailed()
                    Log.e(TAG, "onAdFailed: ")
                    isAnyAdOpen = true
                    mActivity.runOnUiThread {
                        findViewById<ImageView>(R.id.iv_close_ad)?.performClick()
                    }
                }
            })
        }
    }

    private fun initViewAction() {

    }

    private fun initViewListener() {
        findViewById<ImageView>(R.id.iv_close_ad)?.setOnClickListener {
//            findViewById<NativeAdView>(R.id.interstitial_native_ad_view)?.let { view ->
//                view.updateInterstitialType(fType = NativeAdInterstitialType.WEBSITE)
//            }
            finishTask()
        }
    }

    private fun finishTask() {
//        NativeAdvancedModelHelper.removeListener()
//        isAnyAdShowing = false
//        onDialogActivityDismiss.invoke()
        onThisAdClosed.invoke()
        isAnyAdOpen = false
        finishAfterTransition()
    }
}