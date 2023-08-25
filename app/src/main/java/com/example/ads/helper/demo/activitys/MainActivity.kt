package com.example.ads.helper.demo.activitys

import android.Manifest
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.akshay.harsoda.permission.helper.AksPermission
import com.akshay.harsoda.permission.helper.utiles.OnAlertButtonClickListener
import com.akshay.harsoda.permission.helper.utiles.getPermissionName
import com.akshay.harsoda.permission.helper.utiles.showAlert
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.example.ads.helper.demo.*
import com.example.ads.helper.demo.R
import com.example.ads.helper.demo.base.BaseActivity
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.base.utils.*
import com.example.ads.helper.demo.databinding.ActivityMainBinding
import com.example.app.ads.helper.*
import com.example.app.ads.helper.activity.FullScreenNativeAdDialogActivity
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper.showInterstitialAd
import com.example.app.ads.helper.nativead.NativeAdModelHelper
import com.example.app.ads.helper.purchase.ProductPurchaseHelper
import com.example.app.ads.helper.reward.RewardedInterstitialAdHelper
import com.example.app.ads.helper.reward.RewardedInterstitialAdHelper.showRewardedInterstitialAd
import com.example.app.ads.helper.reward.RewardedVideoAdHelper
import com.example.app.ads.helper.reward.RewardedVideoAdHelper.showRewardedVideoAd
import kotlin.system.exitProcess


class MainActivity : BaseBindingActivity<ActivityMainBinding>(), ProductPurchaseHelper.ProductPurchaseListener {

    private val mExitDialog: ExitDialog by lazy { ExitDialog(mActivity) }

    override fun getActivityContext(): BaseActivity {
        return this@MainActivity
    }

    override fun setBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()

        ProductPurchaseHelper.initBillingClient(mActivity, this)

        with(mBinding) {
            root.setSelection()

            openAdsSwitch.isChecked = mActivity.getBoolean(IS_OPEN_ADS_ENABLE, true)
            adsSwitch.isChecked = true

            with(layoutHeader) {
                ivHeaderBack.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
                ivHeaderRightIcon.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_share_blue))
            }
        }

    }

    override fun initAds() {
        super.initAds()

        with(mBinding) {

            //<editor-fold desc="Interstitial Ad Work">
            showInterstitialAds.alpha = 0.5f
            showInterstitialAds.isEnabled = false

            InterstitialAdHelper.loadAd(
                fContext = mActivity,
                onAdLoaded = {
                    Log.e(TAG, "Admob_ initAds: AdLoaded")
                    showInterstitialAds.alpha = 1.0f
                    showInterstitialAds.isEnabled = true
                }
            )
            //</editor-fold>

            //<editor-fold desc="Reward Video Ad Work">
            showRewardVideoAds.alpha = 0.5f
            showRewardVideoAds.isEnabled = false

            RewardedVideoAdHelper.loadAd(
                fContext = mActivity,
                onStartToLoadAd = {
                    showRewardVideoAds.alpha = 0.5f
                    showRewardVideoAds.isEnabled = false
                },
                onAdLoaded = {
                    showRewardVideoAds.alpha = 1f
                    showRewardVideoAds.isEnabled = true
                },
            )
            //</editor-fold>

            //<editor-fold desc="Reward Interstitial Video Ad Work">
            showRewardInterstitialAds.alpha = 0.5f
            showRewardInterstitialAds.isEnabled = false

            RewardedInterstitialAdHelper.loadAd(
                fContext = mActivity,
                onStartToLoadAd = {
                    Log.e(TAG, "initAds: RewardedInterstitialAd: onStartToLoadAd")
                    showRewardInterstitialAds.alpha = 0.5f
                    showRewardInterstitialAds.isEnabled = false
                },
                onAdLoaded = {
                    Log.e(TAG, "initAds: RewardedInterstitialAd: onAdLoaded")
                    showRewardInterstitialAds.alpha = 1f
                    showRewardInterstitialAds.isEnabled = true
                },
            )
            //</editor-fold>

            //<editor-fold desc="Native Advanced Ad Work">
//            NativeAdModelHelper(mActivity).loadNativeAdvancedAd(
//                fSize = NativeAdsSize.Big,
//                fLayout = flNativeAdPlaceHolderMedium,
//                isAddVideoOptions = false,
//                onAdLoaded = {
//                    Log.e(TAG, "Akshay_ initAds: onAdLoaded: Load Native Ad")
//                },
//                onAdFailed = {
//                    Log.e(TAG, "Akshay_ initAds: onAdFailed: Load Native Ad")
//                }
//            )
            //</editor-fold>
        }
    }

    override fun initViewListener() {
        super.initViewListener()

        with(mBinding) {
            adsSwitch.setOnCheckedChangeListener { _, _ ->
                if (NativeAdvancedModelHelper.getNativeAd != null) {
                    NativeAdvancedModelHelper.destroy()
                }
            }

            openAdsSwitch.setOnCheckedChangeListener { _, isChecked ->
                mActivity.save(IS_OPEN_ADS_ENABLE, isChecked)
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.e(TAG, "initViewListener: IS_OPEN_ADS_ENABLE::${mActivity.getBoolean(IS_OPEN_ADS_ENABLE, true)}")
                    triggerRebirth(mActivity)
                }, 500)
            }

            setClickListener(
                layoutHeader.ivHeaderBack,
                layoutHeader.ivHeaderRightIcon,
                showInterstitialAds,
                showFullScreenNativeAd,
                showRewardVideoAds,
                showRewardInterstitialAds,
                showNativeAds,
                showCustomNativeAds,
                showRunTimePermission,
                showDialogs,
                showBannerAds,
            )
        }
    }

    override fun onClick(v: View) {
        when (v) {
            mBinding.layoutHeader.ivHeaderBack -> {
                onBackPressed()
            }
            mBinding.layoutHeader.ivHeaderRightIcon -> {
                mActivity.shareApp
            }
            mBinding.showInterstitialAds -> {
                mActivity.showInterstitialAd { isAdShowing, isShowFullScreenAd ->
                    Log.e(TAG, "onClick: isAdShowing::$isAdShowing, isShowFullScreenAd::$isShowFullScreenAd")
                }
            }
            mBinding.showFullScreenNativeAd -> {
                if (!mActivity.isOnline) {
                    Toast.makeText(mActivity, "check your internet connection", Toast.LENGTH_SHORT).show()
                } else if (NativeAdvancedModelHelper.getNativeAd == null) {
                    Toast.makeText(mActivity, "native ad not load", Toast.LENGTH_SHORT).show()
                } else {

                    FullScreenNativeAdDialogActivity.lunchFullScreenAd(mActivity)
                }
            }
            mBinding.showRewardVideoAds -> {
                mActivity.showRewardedVideoAd(
                    onUserEarnedReward = { isUserEarnedReward ->
                        Log.e(TAG, "onClick: RewardedVideoAd: isUserEarnedReward::$isUserEarnedReward")
                        mBinding.showRewardVideoAds.alpha = 0.5f
                        mBinding.showRewardVideoAds.isEnabled = false
                    }
                )
            }
            mBinding.showRewardInterstitialAds -> {
                mActivity.showRewardedInterstitialAd(
                    onUserEarnedReward = { isUserEarnedReward ->
                        Log.e(TAG, "onClick: RewardedInterstitialAd: isUserEarnedReward::$isUserEarnedReward")
                        mBinding.showRewardInterstitialAds.alpha = 0.5f
                        mBinding.showRewardInterstitialAds.isEnabled = false
                    }
                )
            }
            mBinding.showNativeAds -> {
                launchActivity(getActivityIntent<NativeAdsActivity> { putBoolean("is_add_video_options", mBinding.adsSwitch.isChecked) })
            }
            mBinding.showCustomNativeAds -> {
                mActivity.showInterstitialAd { _, _ ->
                    launchActivity(getActivityIntent<CustomNativeAdsActivity> { putBoolean("is_add_video_options", mBinding.adsSwitch.isChecked) })
                }
            }
            mBinding.showRunTimePermission -> {
                needToBlockOpenAdInternally = true
                AksPermission.with(mActivity)
                    .permissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                    )
                    .isShowDefaultSettingDialog(false)
                    .request(
                        onGrantedResult = {},
                        onDeniedResult = {},
                        onPermanentlyDeniedResult = {
                            doNotAskAgain(it.toMutableList().getPermissionName().toString())
                        }
                    )
            }
            mBinding.showDialogs -> {
                needToBlockOpenAdInternally = true
                AlertDialog.Builder(mActivity)
                    .setTitle("Hello")
                    .setMessage("Hello")
                    .create()
                    .show()
            }
            mBinding.showBannerAds -> {
                launchActivity(getActivityIntent<BannerPortraitActivity>())
            }
        }
    }

    private fun doNotAskAgain(fMessage: String) {
        isInterstitialAdShow = true
        mActivity.showAlert(
            fTitle = mActivity.getStringRes(R.string.dialog_title),
            fMessage = mActivity.getStringRes(R.string.dialog_messages, fMessage),
            fPositiveText = mActivity.getStringRes(android.R.string.ok),
            fNegativeText = mActivity.getStringRes(android.R.string.cancel),
            fTitleColor = Color.BLACK,
            fMessageColor = Color.BLACK,
            fPositiveColor = Color.BLACK,
            fNegativeColor = Color.BLACK,
            fButtonClickListener = object : OnAlertButtonClickListener {
                override fun onPositiveButtonClick() {
                    mActivity.packageName?.let {
                        mActivity.startActivity(AksPermission.appDetailSettingsIntent(it))
                    }
                    isInterstitialAdShow = false
                }

                override fun onNegativeButtonClick() {
                    super.onNegativeButtonClick()
                    isInterstitialAdShow = false
                }
            }
        )
    }

    override fun onPurchasedSuccess(purchase: Purchase) {

    }

    override fun onProductAlreadyOwn() {

    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        ProductPurchaseHelper.initProductsKeys(
            context = mActivity,
            onInitializationComplete = {
                Log.e(TAG, "onBillingSetupFinished:")
            }
        )
    }

    override fun onBillingKeyNotFound(productId: String) {

    }

    override fun onBackPressed() {
        mExitDialog.let {
            it.showExitDialog {
                finishAffinity()
                exitProcess(1)
            }
        }
    }
}