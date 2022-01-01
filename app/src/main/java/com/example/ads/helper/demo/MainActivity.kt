package com.example.ads.helper.demo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.akshay.harsoda.permission.helper.AksPermission
import com.akshay.harsoda.permission.helper.utiles.OnAlertButtonClickListener
import com.akshay.harsoda.permission.helper.utiles.getPermissionName
import com.akshay.harsoda.permission.helper.utiles.showAlert
import com.example.ads.helper.demo.base.BaseBindingActivity
import com.example.ads.helper.demo.base.utils.*
import com.example.ads.helper.demo.databinding.ActivityMainBinding
import com.example.app.ads.helper.*
import com.example.app.ads.helper.InterstitialAdHelper.isShowInterstitialAd
import com.example.app.ads.helper.InterstitialRewardHelper.isShowRewardedInterstitialAd
import com.example.app.ads.helper.InterstitialRewardHelper.showRewardedInterstitialAd
import com.example.app.ads.helper.RewardVideoHelper.isShowRewardVideoAd
import com.example.app.ads.helper.RewardVideoHelper.showRewardVideoAd
import com.example.app.ads.helper.dialogs.FullScreenNativeAdDialog


class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    private var mExitDialog: ExitDialog? = null

    override fun getActivityContext(): AppCompatActivity {
        return this@MainActivity
    }

    override fun setBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initAds() {
        super.initAds()

        InterstitialAdHelper.loadInterstitialAd(fContext = mActivity)
        RewardVideoHelper.loadRewardVideoAd(fContext = mActivity)
        InterstitialRewardHelper.loadRewardedInterstitialAd(fContext = mActivity)
    }

    override fun initView() {
        super.initView()

        mBinding.layoutHeader.ivHeaderBack.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_new_header_back))
        mBinding.layoutHeader.ivHeaderRightIcon.setImageDrawable(mActivity.getDrawableRes(R.drawable.ic_share_blue))

        mExitDialog = ExitDialog(mActivity)
    }

    override fun initViewListener() {
        super.initViewListener()

        mBinding.openAdsSwitch.isChecked = mActivity.getBoolean(IS_OPEN_ADS_ENABLE, true)

        mBinding.adsSwitch.setOnCheckedChangeListener { _, _ ->
            if (NativeAdvancedModelHelper.getNativeAd != null) {
                NativeAdvancedModelHelper.destroy()
            }
        }

        mBinding.openAdsSwitch.setOnCheckedChangeListener { _, isChecked ->
            mActivity.save(IS_OPEN_ADS_ENABLE, isChecked)
            Handler(Looper.getMainLooper()).postDelayed({
                Log.e(TAG, "initViewListener: IS_OPEN_ADS_ENABLE::${mActivity.getBoolean(IS_OPEN_ADS_ENABLE, true)}")
                triggerRebirth(mActivity)
            }, 500)
        }

        mBinding.adsSwitch.isChecked = true

        setClickListener(
            mBinding.layoutHeader.ivHeaderBack,
            mBinding.layoutHeader.ivHeaderRightIcon,
            mBinding.showInterstitialAds,
            mBinding.showFullScreenNativeAd,
            mBinding.showRewardVideoAds,
            mBinding.showRewardInterstitialAds,
            mBinding.showNativeAds,
            mBinding.showRunTimePermission,
            mBinding.showDialogs,
        )

        //<editor-fold desc="Reward Video Ad Work">
        mBinding.showRewardVideoAds.alpha = 0.5f
        mBinding.showRewardVideoAds.isEnabled = false

        mActivity.isShowRewardVideoAd(
            onStartToLoadRewardVideoAd = {
                mBinding.showRewardVideoAds.alpha = 0.5f
                mBinding.showRewardVideoAds.isEnabled = false
            },
            onUserEarnedReward = { isUserEarnedReward ->
                Log.e(TAG, "initView: isUserEarnedReward::$isUserEarnedReward")
                mBinding.showRewardVideoAds.alpha = 0.5f
                mBinding.showRewardVideoAds.isEnabled = false
            },
            onAdLoaded = {
                mBinding.showRewardVideoAds.alpha = 1f
                mBinding.showRewardVideoAds.isEnabled = true
            }
        )
        //</editor-fold>

        //<editor-fold desc="Reward Interstitial Video Ad Work">
        mBinding.showRewardInterstitialAds.alpha = 0.5f
        mBinding.showRewardInterstitialAds.isEnabled = false

        mActivity.isShowRewardedInterstitialAd(
            onStartToLoadRewardedInterstitialAd = {
                mBinding.showRewardInterstitialAds.alpha = 0.5f
                mBinding.showRewardInterstitialAds.isEnabled = false
            },
            onUserEarnedReward = { isUserEarnedReward ->
                Log.e(TAG, "initView: isUserEarnedReward::$isUserEarnedReward")
                mBinding.showRewardInterstitialAds.alpha = 0.5f
                mBinding.showRewardInterstitialAds.isEnabled = false
            },
            onAdLoaded = {
                mBinding.showRewardInterstitialAds.alpha = 1f
                mBinding.showRewardInterstitialAds.isEnabled = true
            }
        )
        //</editor-fold>
    }


    override fun onClick(v: View) {
        when (v) {
            mBinding.layoutHeader.ivHeaderBack -> {
                onBackPressed()
            }
            mBinding.layoutHeader.ivHeaderRightIcon -> {
                shareApp("hello")
            }
            mBinding.showInterstitialAds -> {
                mActivity.isShowInterstitialAd { isShowFullScreenAd ->
                    Log.e(TAG, "onClick: isShowFullScreenAd::$isShowFullScreenAd")
                }
            }
            mBinding.showFullScreenNativeAd -> {
                FullScreenNativeAdDialog(activity = mActivity).showFullScreenNativeAdDialog(mBinding.adsSwitch.isChecked)
            }
            mBinding.showRewardVideoAds -> {
                mActivity.showRewardVideoAd()
            }
            mBinding.showRewardInterstitialAds -> {
                mActivity.showRewardedInterstitialAd()
            }

            mBinding.showNativeAds -> {
                mActivity.isShowInterstitialAd { _ ->
                    launchActivity(getActivityIntent<NativeAdsActivity> { putBoolean("is_add_video_options", mBinding.adsSwitch.isChecked) })
                }
            }
            mBinding.showRunTimePermission -> {
                isInterstitialAdShow = true
                AksPermission.with(mActivity)
                    .permissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                    )
                    .isShowDefaultSettingDialog(false)
                    .request(
                        onGrantedResult = {
                            isInterstitialAdShow = false
                        },
                        onDeniedResult = {
                            isInterstitialAdShow = false
                        },
                        onPermanentlyDeniedResult = {
                            isInterstitialAdShow = false
                            doNotAskAgain(it.toMutableList().getPermissionName().toString())
                        }
                    )
            }
            mBinding.showDialogs -> {
                AlertDialog.Builder(mActivity)
                    .setTitle("Hello")
                    .setMessage("Hello")
                    .setOnDismissListener {
                        isInterstitialAdShow = false
                    }
                    .create()
                    .show()
                isInterstitialAdShow = true
            }
        }
    }

    private fun Context.shareApp(msg: String?) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(intent, "Share Via"))
        } catch (e: java.lang.Exception) {
            Log.e("shareApp", "shareApp: $e")
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


    override fun onBackPressed() {
        if (mExitDialog != null) {
            mExitDialog?.let {
                it.showExitDialog(isAddVideoOptions = mBinding.adsSwitch.isChecked) {
                    mActivity.isShowInterstitialAd(isBackAds = true) { _ ->
                        launchActivity(getActivityIntent<SecondActivity>())
                    }
                }
            }
        }
    }
}