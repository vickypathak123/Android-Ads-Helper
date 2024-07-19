package com.example.ads.helper.new_.demo.activitys

import android.view.View
import androidx.core.view.isVisible
import com.example.ads.helper.new_.demo.R
import com.example.ads.helper.new_.demo.base.BaseActivity
import com.example.ads.helper.new_.demo.base.BaseBindingActivity
import com.example.ads.helper.new_.demo.base.utils.getDrawableRes
import com.example.ads.helper.new_.demo.base.utils.visible
import com.example.ads.helper.new_.demo.databinding.ActivityNativeAdsBigBinding

class NativeAdsBigActivity : BaseBindingActivity<ActivityNativeAdsBigBinding>() {

    private val mCount: Int get() = intent?.extras?.getInt("screen_count", 0) ?: 0
    private val isAddVideoOptions: Boolean get() = intent?.extras?.getBoolean("is_add_video_options", false) ?: false

    override fun getActivityContext(): BaseActivity {
        return this@NativeAdsBigActivity
    }

    override fun setBinding(): ActivityNativeAdsBigBinding {
        return ActivityNativeAdsBigBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        mBinding.layoutHeader.txtHeaderTitle.text = "Native Ads - $mCount"
        mBinding.layoutHeader.ivHeaderBack.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
        mBinding.layoutHeader.ivHeaderRightIcon.visible
    }

    override fun initViewListener() {
        super.initViewListener()
        setClickListener(
            mBinding.layoutHeader.ivHeaderBack,
            mBinding.layoutHeader.ivHeaderRightIcon,
            mBinding.button
        )
    }

    override fun onClick(v: View) {
        super.onClick(v)

        when (v) {
            mBinding.layoutHeader.ivHeaderBack -> customOnBackPressed()
            mBinding.layoutHeader.ivHeaderRightIcon -> {
                launchActivity(
                    getActivityIntent<NativeAdsMediumActivity>(isAddFlag = false) {
                        putInt("screen_count", mCount + 1)
                        putBoolean("is_add_video_options", isAddVideoOptions)
                    }
                )
            }
            mBinding.button -> {
                mBinding.flNativeAdPlaceHolderBig.isVisible = !mBinding.flNativeAdPlaceHolderBig.isVisible
            }
        }
    }

    override fun needToShowBackAd(): Boolean {
        return false
    }
}