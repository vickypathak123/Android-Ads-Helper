package com.example.app.ads.helper.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.app.ads.helper.*
import com.example.app.ads.helper.databinding.DialogFullScreenNativeAdBinding


class FullScreenNativeAdDialogActivity : AppCompatActivity() {

    private lateinit var mBinding: DialogFullScreenNativeAdBinding

    private val mActivity: FragmentActivity
        get() {
            return this@FullScreenNativeAdDialogActivity
        }

    companion object {
        fun lunchFullScreenAd(fContext: Activity) {
            val intent = Intent(fContext, FullScreenNativeAdDialogActivity::class.java)
            fContext.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        setTheme(R.style.theme_full_screen_native_ad_dialog_activity)

        this.mBinding = DialogFullScreenNativeAdBinding.inflate(layoutInflater)
        setContentView(this.mBinding.root)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        setContentView()
    }

    private fun setContentView() {
        initView()
        loadAds()
        initViewAction()
        initViewListener()
    }

    private fun initView() {
        val value = TypedValue()
        mActivity.theme.resolveAttribute(R.attr.native_ads_main_color, value, true)

        val rotate = RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.repeatCount = Animation.INFINITE
        rotate.duration = 1000
        rotate.interpolator = LinearInterpolator()

        mBinding.ivProgress.apply {
            setColorFilter(value.data)
            startAnimation(rotate)
        }
    }

    private fun loadAds() {
        if (NativeAdvancedModelHelper.getNativeAd != null && !mActivity.isFinishing && mActivity.isOnline) {
            NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
                fSize = NativeAdsSize.FullScreen,
                fLayout = mBinding.flNativeAdPlaceHolder,
                isAddVideoOptions = true,
                isAdLoaded = { isNeedToRemoveCloseButton ->
                    if (!isNeedToRemoveCloseButton) {
                        mBinding.ivCloseAd.visible
                    } else {
                        mBinding.ivCloseAd.gone
                    }
                    mBinding.flNativeAdPlaceHolder.visible
                },
                onClickAdClose = {
                    mBinding.ivCloseAd.performClick()
                }
            )
        } else {
            mBinding.ivCloseAd.performClick()
        }
    }

    private fun initViewAction() {

    }

    private fun initViewListener() {
        mBinding.ivCloseAd.setOnClickListener {
            finishTask()
        }
    }

    private fun finishTask() {
        NativeAdvancedModelHelper.removeListener()
        isAnyAdShowing = false
        onDialogActivityDismiss.invoke()
        finishAfterTransition()
    }

    override fun onBackPressed() {

    }

    override fun finish() {
        super.finish()
        mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}