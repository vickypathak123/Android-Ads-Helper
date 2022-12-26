package com.example.ads.helper.demo.activitys

import android.view.View
import com.example.ads.helper.demo.R
import com.example.ads.helper.demo.base.BaseActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.base.utils.getDrawableRes
import com.example.ads.helper.demo.base.utils.getStringRes
import com.example.ads.helper.demo.databinding.ActivityBannerTypeBinding
import com.example.app.ads.helper.adaptive.banner.BannerHelper
import com.example.app.ads.helper.adaptive.banner.BannerAdSize

class BannerPortraitActivity : BaseBindingActivity<ActivityBannerTypeBinding>() {

    private val mBanner: BannerHelper by lazy { BannerHelper(mActivity) }
    private val mLargeBanner: BannerHelper by lazy { BannerHelper(mActivity) }
    private val mMediumRectangle: BannerHelper by lazy { BannerHelper(mActivity) }
    private val mFullBanner: BannerHelper by lazy { BannerHelper(mActivity) }
    private val mLeaderboard: BannerHelper by lazy { BannerHelper(mActivity) }
    private val mAdaptiveBanner: BannerHelper by lazy { BannerHelper(mActivity) }
    private val mSmartBanner: BannerHelper by lazy { BannerHelper(mActivity) }

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

    override fun initAds() {
        super.initAds()

        with(mBinding) {
            mBanner.loadBanner(fBannerAdSize = BannerAdSize.BANNER, fLayout = flBanner)
            mLargeBanner.loadBanner(fBannerAdSize = BannerAdSize.LARGE_BANNER, fLayout = flLargeBanner)
            mMediumRectangle.loadBanner(fBannerAdSize = BannerAdSize.MEDIUM_RECTANGLE, fLayout = flMediumBanner)
            mFullBanner.loadBanner(fBannerAdSize = BannerAdSize.FULL_BANNER, fLayout = flFullBanner)
            mLeaderboard.loadBanner(fBannerAdSize = BannerAdSize.LEADERBOARD, fLayout = flLeaderBoard)
            mAdaptiveBanner.loadBanner(fBannerAdSize = BannerAdSize.ADAPTIVE_BANNER, fLayout = flAdaptiveBanner)
            mSmartBanner.loadBanner(fBannerAdSize = BannerAdSize.SMART_BANNER, fLayout = flSmartBanner)
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
                layoutHeader.ivHeaderBack -> onBackPressed()
                layoutHeader.ivHeaderRightIcon -> launchActivity(getActivityIntent<BannerLandscapeActivity>())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mBanner.mAdView?.pause()
        mLargeBanner.mAdView?.pause()
        mMediumRectangle.mAdView?.pause()
        mFullBanner.mAdView?.pause()
        mLeaderboard.mAdView?.pause()
        mAdaptiveBanner.mAdView?.pause()
        mSmartBanner.mAdView?.pause()
    }

    override fun onResume() {
        super.onResume()
        mBanner.mAdView?.resume()
        mLargeBanner.mAdView?.resume()
        mMediumRectangle.mAdView?.resume()
        mFullBanner.mAdView?.resume()
        mLeaderboard.mAdView?.resume()
        mAdaptiveBanner.mAdView?.resume()
        mSmartBanner.mAdView?.resume()
    }

    override fun onDestroy() {
        mBanner.mAdView?.destroy()
        mLargeBanner.mAdView?.destroy()
        mMediumRectangle.mAdView?.destroy()
        mFullBanner.mAdView?.destroy()
        mLeaderboard.mAdView?.destroy()
        mAdaptiveBanner.mAdView?.destroy()
        mSmartBanner.mAdView?.destroy()
        super.onDestroy()
    }
}