package com.example.app.ads.helper.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import android.widget.Toast
import com.example.app.ads.helper.R
import com.example.app.ads.helper.*
import com.example.app.ads.helper.databinding.DialogFullScreenNativeAdBinding
import com.example.app.ads.helper.visible

class FullScreenNativeAdDialog(
    private val activity: Activity,
    private val onDialogDismiss: () -> Unit = {}
) : Dialog(activity, R.style.full_screen_dialog) {

    private val TAG: String = "Admob_${javaClass.simpleName}"

    private var mBinding: DialogFullScreenNativeAdBinding

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var testDialog: FullScreenNativeAdDialog? = null

        val isDialogShowing: Boolean
            get() {
                return testDialog != null && testDialog?.isShowing ?: false
            }
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        mBinding = DialogFullScreenNativeAdBinding.inflate(LayoutInflater.from(context))
        setContentView(mBinding.root)

        window?.let {

            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            ///////////   Animation  ////////
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(it.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            lp.gravity = Gravity.CENTER
            lp.windowAnimations = R.style.dialog_animation
            it.attributes = lp

        }

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        setOnDismissListener {
            isAnyAdShowing = false
            mBinding.flNativeAdPlaceHolder.removeAllViews()
        }

        mBinding.ivCloseAd.setOnClickListener {
            if (this != null && this.isShowing) {
                this.dismiss()
            }
            NativeAdvancedModelHelper.removeListener()
            onDialogDismiss.invoke()
            Log.i(TAG, "Dismiss FullScreen NativeAd Dialog: ")
        }
    }

    fun showFullScreenNativeAdDialog(checked: Boolean) {
        if (NativeAdvancedModelHelper.getNativeAd != null && !activity.isFinishing && !isShowing && activity.isOnline) {

            mBinding.ivCloseAd.visible

            NativeAdvancedModelHelper(activity).loadNativeAdvancedAd(
                fSize = NativeAdsSize.FullScreen,
                fLayout = mBinding.flNativeAdPlaceHolder,
                isAddVideoOptions = checked,
                isAdLoaded = { isNeedToRemoveCloseButton ->
                    if (!isNeedToRemoveCloseButton) {
                        mBinding.ivCloseAd.visible
                    } else {
                        mBinding.ivCloseAd.gone
                    }
                    mBinding.flNativeAdPlaceHolder.visible
                },
                onClickAdClose = {
                    mBinding.ivCloseAd.performClick()
                }
            )
            testDialog = this
            isInterstitialAdShow = true
            Log.i(TAG, "Show FullScreen NativeAd Dialog: ")
            show()
        } else if (!activity.isOnline) {
            Toast.makeText(activity, "check your internet connection", Toast.LENGTH_SHORT).show()
        } else if (NativeAdvancedModelHelper.getNativeAd != null) {
            Toast.makeText(activity, "native ad not load", Toast.LENGTH_SHORT).show()
        }
    }
}