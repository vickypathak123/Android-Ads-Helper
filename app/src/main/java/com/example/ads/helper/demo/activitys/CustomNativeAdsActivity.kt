package com.example.ads.helper.demo.activitys

import android.view.LayoutInflater
import android.view.View
import com.example.ads.helper.demo.R
import com.example.ads.helper.demo.base.BaseActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.base.utils.getDrawableRes
import com.example.ads.helper.demo.base.utils.gone
import com.example.ads.helper.demo.databinding.ActivityCustomNativeAdsBinding
import com.example.app.ads.helper.NativeAdsSize
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper.showInterstitialAd
import com.example.app.ads.helper.nativead.NativeAdModelHelper
import com.example.app.ads.helper.purchase.AdsManager

class CustomNativeAdsActivity : BaseBindingActivity<ActivityCustomNativeAdsBinding>() {


    var nativeModeHelper: NativeAdModelHelper? = null
    override fun getActivityContext(): BaseActivity {
        return this@CustomNativeAdsActivity
    }

    override fun setBinding(): ActivityCustomNativeAdsBinding {
        return ActivityCustomNativeAdsBinding.inflate(layoutInflater)
    }

    override fun initAds() {
        super.initAds()

        InterstitialAdHelper.loadAd(
            fContext = mActivity,
            onAdLoaded = {
                // Call When Open Ad Loaded Successfully
                // Perform your Action
            }
        )
        nativeModeHelper = NativeAdModelHelper((mActivity))

        nativeModeHelper?.loadNativeAdvancedAd(
            fSize = NativeAdsSize.Custom,
            fLayout = mBinding.flNativeAdPlaceHolderCustom,
            fCustomAdView = LayoutInflater.from(this).inflate(
                com.example.app.ads.helper.R.layout.layout_google_native_ad_custom_sample,
                null
            ),
            fCustomShimmerView = LayoutInflater.from(this).inflate(
                com.example.app.ads.helper.R.layout.layout_shimmer_google_native_ad_custom_sample,
                null
            ),
            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
        )
    }

    override fun initView() {
        super.initView()
        mBinding.layoutHeader.txtHeaderTitle.text = "Custom Native Ads"
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
        mActivity.showInterstitialAd(fIsShowFullScreenNativeAd = true) { _, _ ->
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        nativeModeHelper?.manageShimmerLayoutVisibility(
            AdsManager(this).isNeedToShowAds(),
            NativeAdsSize.Custom,
            mBinding.flNativeAdPlaceHolderCustom,
            LayoutInflater.from(this).inflate(
                com.example.app.ads.helper.R.layout.layout_shimmer_google_native_ad_custom_sample,
                null
            ),
        )
    }
}