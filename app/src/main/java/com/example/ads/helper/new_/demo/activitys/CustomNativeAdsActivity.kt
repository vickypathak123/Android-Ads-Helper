package com.example.ads.helper.new_.demo.activitys

import android.util.Log
import android.view.View
import com.example.ads.helper.new_.demo.R
import com.example.ads.helper.new_.demo.base.BaseActivity
import com.example.ads.helper.new_.demo.base.BaseBindingActivity
import com.example.ads.helper.new_.demo.base.utils.getDrawableRes
import com.example.ads.helper.new_.demo.base.utils.gone
import com.example.ads.helper.new_.demo.databinding.ActivityCustomNativeAdsBinding
import com.example.app.ads.helper.nativead.NativeAdView

class CustomNativeAdsActivity : BaseBindingActivity<ActivityCustomNativeAdsBinding>() {

    override fun setBinding(): ActivityCustomNativeAdsBinding {
        return ActivityCustomNativeAdsBinding.inflate(layoutInflater)
    }

    override fun getActivityContext(): BaseActivity {
        return this@CustomNativeAdsActivity
    }

    override fun initView() {
        super.initView()
        mBinding.layoutHeader.txtHeaderTitle.text = "Custom Native Ads"
        mBinding.layoutHeader.ivHeaderBack.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
        mBinding.layoutHeader.ivHeaderRightIcon.gone

        mBinding.customNativeAdView.let { view ->
            view.setOnNativeAdViewListener(fListener = object : NativeAdView.OnNativeAdViewListener {
                override fun onAdCustomClosed() {
                    super.onAdCustomClosed()
                    Log.e(TAG, "onAdCustomClosed: ")
                    mActivity.runOnUiThread {
                        customOnBackPressed()
                    }
                }
            })
        }
    }

    override fun initViewListener() {
        super.initViewListener()
        setClickListener(
            mBinding.layoutHeader.ivHeaderBack,
        )
    }

    override fun onClick(v: View) {
        super.onClick(v)

        when (v) {
            mBinding.layoutHeader.ivHeaderBack -> customOnBackPressed()
        }
    }
}