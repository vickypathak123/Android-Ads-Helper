package com.example.ads.helper.demo

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.base.utils.getDrawableRes
import com.example.ads.helper.demo.base.utils.gone
import com.example.ads.helper.demo.databinding.ActivityCustomNativeAdsBinding
import com.example.app.ads.helper.GiftIconHelper
import com.example.app.ads.helper.InterstitialAdHelper
import com.example.app.ads.helper.InterstitialAdHelper.isShowInterstitialAd
import com.example.app.ads.helper.NativeAdsSize
import com.example.app.ads.helper.NativeAdvancedModelHelper

class CustomNativeAdsActivity : BaseBindingActivity<ActivityCustomNativeAdsBinding>() {

    override fun getActivityContext(): AppCompatActivity {
        return this@CustomNativeAdsActivity
    }

    override fun setBinding(): ActivityCustomNativeAdsBinding {
        return ActivityCustomNativeAdsBinding.inflate(layoutInflater)
    }

    override fun initAds() {
        super.initAds()

        InterstitialAdHelper.loadInterstitialAd(
            fContext = mActivity,
            fIsShowFullScreenNativeAd = true /* Default Value */,
            onAdLoaded = {
                // Call When Open Ad Loaded Successfully
                // Perform your Action
            }
        )

        NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
            fSize = NativeAdsSize.Custom,
            fLayout = mBinding.flNativeAdPlaceHolderCustom,
            fCustomAdView = LayoutInflater.from(this).inflate(com.example.app.ads.helper.R.layout.layout_google_native_ad_custom_sample, null),
            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
        )

        GiftIconHelper.loadGiftAd(
            fContext = mActivity,
            fivGiftIcon = mBinding.layoutHeader.layoutGiftAd.giftAdIcon,
            fivBlastIcon = mBinding.layoutHeader.layoutGiftAd.giftBlastAdIcon
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
        mActivity.isShowInterstitialAd(isBackAds = true) { _ ->
            finish()
        }
    }
}