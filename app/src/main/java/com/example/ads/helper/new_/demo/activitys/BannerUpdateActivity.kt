package com.example.ads.helper.new_.demo.activitys

import android.view.View
import com.example.ads.helper.new_.demo.R
import com.example.ads.helper.new_.demo.base.BaseActivity
import com.example.ads.helper.new_.demo.base.BaseBindingActivity
import com.example.ads.helper.new_.demo.base.utils.getDrawableRes
import com.example.ads.helper.new_.demo.base.utils.getStringRes
import com.example.ads.helper.new_.demo.databinding.ActivityBannerUpdateBinding
import com.example.ads.helper.new_.demo.widget.UpdateBannerDialog
import com.example.app.ads.helper.banner.BannerAdHelper

class BannerUpdateActivity : BaseBindingActivity<ActivityBannerUpdateBinding>() {

    private val mUpdateBannerDialog: UpdateBannerDialog by lazy {
        UpdateBannerDialog(mActivity)
    }

    override fun setBinding(): ActivityBannerUpdateBinding {
        return ActivityBannerUpdateBinding.inflate(layoutInflater)
    }

    override fun getActivityContext(): BaseActivity {
        return this@BannerUpdateActivity
    }

    override fun initView() {
        super.initView()
        with(mBinding) {
            layoutHeader.apply {
                txtHeaderTitle.text = mActivity.getStringRes(R.string.banner_ads)
                ivHeaderBack.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
                ivHeaderRightIcon.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
                ivHeaderRightIcon.rotation = 180.0f
            }
        }
    }

    override fun initViewListener() {
        super.initViewListener()
        with(mBinding) {
            setClickListener(
                layoutHeader.ivHeaderBack, layoutHeader.ivHeaderRightIcon,
            )
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        with(mBinding) {
            when (v) {
                layoutHeader.ivHeaderBack -> customOnBackPressed()
                layoutHeader.ivHeaderRightIcon -> {
                    mUpdateBannerDialog.show {fAdSize, fAdType, fPlaceHolderType, fPlaceholderTextColor, isCustomPlaceholder ->
                        mUpdateBannerDialog.dismiss()
                        val updateBuilder = BannerAdHelper.with(bannerAdView)

                        fAdSize?.let { updateBuilder.updateAdSize(it) }
                        fAdType?.let { updateBuilder.updateAdType(it) }
                        fPlaceHolderType?.let { updateBuilder.updatePlaceHolderType(it) }
                        fPlaceholderTextColor?.let { updateBuilder.updatePlaceholderTextColor(it) }

                        if (isCustomPlaceholder) {
                            updateBuilder.updateCustomPlaceholder(R.layout.all_screen_header)
                        }

                        updateBuilder.loadAd()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mUpdateBannerDialog.dismiss()
    }
}