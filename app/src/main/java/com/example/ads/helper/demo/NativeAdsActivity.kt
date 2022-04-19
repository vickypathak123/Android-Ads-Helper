package com.example.ads.helper.demo

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.base.utils.getDrawableRes
import com.example.ads.helper.demo.base.utils.gone
import com.example.ads.helper.demo.databinding.ActivityNativeAdsBinding
import com.example.app.ads.helper.*
import com.example.app.ads.helper.InterstitialAdHelper.isShowInterstitialAd

class NativeAdsActivity : BaseBindingActivity<ActivityNativeAdsBinding>() {

    private var isFirstTime: Boolean = true

    override fun getActivityContext(): AppCompatActivity {
        return this@NativeAdsActivity
    }

    override fun setBinding(): ActivityNativeAdsBinding {
        return ActivityNativeAdsBinding.inflate(layoutInflater)
    }

    override fun initAds() {
        super.initAds()

//        InterstitialAdHelper.loadInterstitialAd(fContext = mActivity)

//        NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
//            NativeAdsSize.Big,
//            mBinding.flNativeAdPlaceHolderBig,
//            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
//            isAdLoaded = {
//                /*if (isFirstTime) {
//                    isFirstTime = false
//                    NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
//                        NativeAdsSize.Medium,
//                        mBinding.flNativeAdPlaceHolderMedium,
//                        isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
//                    )
//                }*/
//            }
//        )

//        NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
//            NativeAdsSize.Medium,
//            mBinding.flNativeAdPlaceHolderMedium,
//            isAddVideoOptions = intent?.extras?.getBoolean("is_add_video_options") ?: false,
//        )

        GiftIconHelper.loadGiftAd(
            fContext = mActivity,
            fivGiftIcon = mBinding.layoutHeader.layoutGiftAd.giftAdIcon,
            fivBlastIcon = mBinding.layoutHeader.layoutGiftAd.giftBlastAdIcon
        )
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
//        mActivity.isShowInterstitialAd(isBackAds = true) { _ ->
            finish()
//        }
    }
}