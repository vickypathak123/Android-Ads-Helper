package com.example.ads.helper.new_.demo.activitys

import android.view.View
import com.example.ads.helper.new_.demo.R
import com.example.ads.helper.new_.demo.base.BaseActivity
import com.example.ads.helper.new_.demo.base.BaseBindingActivity
import com.example.ads.helper.new_.demo.base.utils.getDrawableRes
import com.example.ads.helper.new_.demo.base.utils.getStringRes
import com.example.ads.helper.new_.demo.databinding.ActivityBannerTypeBinding
import com.example.app.ads.helper.VasuAdsConfig

class BannerPortraitActivity : BaseBindingActivity<ActivityBannerTypeBinding>() {

    override fun getActivityContext(): BaseActivity {
        return this@BannerPortraitActivity
    }

    override fun setBinding(): ActivityBannerTypeBinding {
        return ActivityBannerTypeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()

        with(mBinding) {
            layoutHeader.apply {
                txtHeaderTitle.text = mActivity.getStringRes(R.string.banner_ads)
                ivHeaderBack.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
                ivHeaderRightIcon.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_share_blue))
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
                layoutHeader.ivHeaderRightIcon -> launchActivity(fIntent = getActivityIntent<BannerUpdateActivity>())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        with(mBinding) {
            flCtBanner.pause()
            flBanner.pause()
            flLargeBanner.pause()
            flMediumBanner.pause()
            flFullBanner.pause()
            flLeaderBoard.pause()
            flAdaptiveBanner.pause()
            flCbBanner.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        with(mBinding) {
            flCtBanner.resume()
            flBanner.resume()
            flLargeBanner.resume()
            flMediumBanner.resume()
            flFullBanner.resume()
            flLeaderBoard.resume()
            flAdaptiveBanner.resume()
            flCbBanner.resume()
        }
    }

    override fun onDestroy() {
        with(mBinding) {
            flCtBanner.destroy()
            flBanner.destroy()
            flLargeBanner.destroy()
            flMediumBanner.destroy()
            flFullBanner.destroy()
            flLeaderBoard.destroy()
            flAdaptiveBanner.destroy()
            flCbBanner.destroy()
        }
        super.onDestroy()
    }
}