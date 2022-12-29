package com.example.ads.helper.demo.activitys

import android.util.Log
import android.view.View
import com.example.ads.helper.demo.R
import com.example.ads.helper.demo.base.BaseActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.base.utils.getDrawableRes
import com.example.ads.helper.demo.base.utils.gone
import com.example.ads.helper.demo.databinding.ActivityNativeAdsBinding
import com.example.app.ads.helper.*
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper.showInterstitialAd
import com.example.app.ads.helper.nativead.NativeAdModelHelper

class NativeAdsActivity : BaseBindingActivity<ActivityNativeAdsBinding>() {

    override fun getActivityContext(): BaseActivity {
        return this@NativeAdsActivity
    }

    override fun setBinding(): ActivityNativeAdsBinding {
        return ActivityNativeAdsBinding.inflate(layoutInflater)
    }

    override fun initAds() {
        super.initAds()

        InterstitialAdHelper.loadAd(fContext = mActivity)

        NativeAdModelHelper(mActivity).loadNativeAdvancedAd(
            fSize = NativeAdsSize.Big,
            fLayout = mBinding.flNativeAdPlaceHolderBig,
            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
            onAdLoaded = {
                Log.e(TAG, "Akshay_ initAds: onAdLoaded: Load Native Ad -> Big")
            },
            onAdClosed = {
                Log.e(TAG, "Akshay_ initAds: onAdClosed: Load Native Ad -> Big")
            },
            onAdFailed = {
                Log.e(TAG, "Akshay_ initAds: onAdFailed: Load Native Ad -> Big")
            }
        )

        NativeAdModelHelper(mActivity).loadNativeAdvancedAd(
            fSize = NativeAdsSize.Medium,
            fLayout = mBinding.flNativeAdPlaceHolderMedium,
            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
            onAdLoaded = {
                Log.e(TAG, "Akshay_ initAds: onAdLoaded: Load Native Ad -> Medium")
            },
            onAdClosed = {
                Log.e(TAG, "Akshay_ initAds: onAdClosed: Load Native Ad -> Medium")
            },
            onAdFailed = {
                Log.e(TAG, "Akshay_ initAds: onAdFailed: Load Native Ad -> Medium")
            }
        )

//        NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
//            NativeAdsSize.Big,
//            mBinding.flNativeAdPlaceHolderBig,
//            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
//        )
//
//        NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
//            NativeAdsSize.Medium,
//            mBinding.flNativeAdPlaceHolderMedium,
//            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
//        )
    }

    override fun initView() {
        super.initView()
        mBinding.layoutHeader.txtHeaderTitle.text = "Native Ads"
        mBinding.layoutHeader.ivHeaderBack.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
        mBinding.layoutHeader.ivHeaderRightIcon.gone
    }

    override fun initViewListener() {
        super.initViewListener()
        setClickListener(
            mBinding.layoutHeader.ivHeaderBack
        )
    }

    override fun onClick(v: View) {
        super.onClick(v)

        when (v) {
            mBinding.layoutHeader.ivHeaderBack -> {
                onBackPressed()
            }
        }
    }


    override fun onBackPressed() {
        mActivity.showInterstitialAd { _, _ ->
            finish()
        }
    }
}