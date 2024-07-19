package com.example.ads.helper.new_.demo.widget

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.WindowManager
import com.example.ads.helper.new_.demo.R
import com.example.ads.helper.new_.demo.base.utils.gone
import com.example.ads.helper.new_.demo.base.utils.visible
import com.example.ads.helper.new_.demo.databinding.DialogBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetDialog(private val fActivity: Activity) :
    BottomSheetDialog(fActivity, R.style.CustomBottomSheetDialog) {
    @Suppress("PropertyName")
    private val TAG: String = javaClass.simpleName

    private val mBinding: DialogBottomSheetBinding =
        DialogBottomSheetBinding.inflate(fActivity.layoutInflater)

    private var isTestNeedToShowAds = false

    init {
        this.setContentView(mBinding.root)
        window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        this.setCancelable(false)
        this.setCanceledOnTouchOutside(false)

        mBinding.exitCardNo.setOnClickListener {
            isTestNeedToShowAds = !isTestNeedToShowAds

            if (isTestNeedToShowAds) {
                mBinding.flNativeAdPlaceHolderBig.visible
                mBinding.ivDelete.gone
            } else {
                mBinding.flNativeAdPlaceHolderBig.gone
                mBinding.ivDelete.visible
            }
        }

        mBinding.exitCardYes.setOnClickListener {
            this.dismiss()
        }
    }

    override fun show() {
        if (!fActivity.isFinishing && !isShowing) {
            isTestNeedToShowAds = !isTestNeedToShowAds
            Log.e(TAG, "show: isTestNeedToShowAds: $isTestNeedToShowAds")
            if (isTestNeedToShowAds) {
                mBinding.flNativeAdPlaceHolderBig.visible
                mBinding.ivDelete.gone
            } else {
                mBinding.flNativeAdPlaceHolderBig.gone
                mBinding.ivDelete.visible
            }

            super.show()
        }
    }
}