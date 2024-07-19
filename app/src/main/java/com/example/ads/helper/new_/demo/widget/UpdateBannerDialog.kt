package com.example.ads.helper.new_.demo.widget

import android.content.res.ColorStateList
import android.view.View
import com.example.ads.helper.new_.demo.base.BaseActivity
import com.example.ads.helper.new_.demo.base.BaseDialog
import com.example.ads.helper.new_.demo.base.utils.inflater
import com.example.ads.helper.new_.demo.databinding.DialogUpdateBannerBinding
import com.example.app.ads.helper.banner.BannerAdSize
import com.example.app.ads.helper.banner.BannerAdType
import com.example.app.ads.helper.PlaceHolderType

class UpdateBannerDialog(fActivity: BaseActivity) : BaseDialog<DialogUpdateBannerBinding>(fActivity = fActivity) {
    override fun setBinding(): DialogUpdateBannerBinding = DialogUpdateBannerBinding.inflate(fActivity.inflater)

    private var mUpdateAction: (
        fAdSize: BannerAdSize?,
        fAdType: BannerAdType?,
        fPlaceHolderType: PlaceHolderType?,
        fPlaceholderTextColor: ColorStateList?,
        isCustomPlaceholder: Boolean
    ) -> Unit = { _, _, _, _, _ -> }

    override fun initViewListener() {
        super.initViewListener()
        with(mBinding) {
            setClickListener(btnCancel, btnUpdate)
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        with(mBinding) {
            when (v) {
                btnCancel -> dismiss()
                btnUpdate -> {
                    val adSizeItemID = spAdSize.selectedItemId
                    val adTypeItemID = spAdType.selectedItemId
                    val placeholderTypeItemID = spPlaceholderType.selectedItemId
                    val isCustomPlaceholder: Boolean = customPlaceholderSwitch.isChecked

                    val selectedColor = colorPiker.selectedColor

                    mUpdateAction(
                        BannerAdSize.fromId(adSizeItemID.toInt()),
                        BannerAdType.fromId(adTypeItemID.toInt()),
                        PlaceHolderType.fromId(placeholderTypeItemID.toInt()),
                        ColorStateList.valueOf(selectedColor),
                        isCustomPlaceholder
                    )
                }

                else -> {}
            }
        }
    }

    fun show(onUpdateAction: (fAdSize: BannerAdSize?, fAdType: BannerAdType?, fPlaceHolderType: PlaceHolderType?, fPlaceholderTextColor: ColorStateList?, isCustomPlaceholder: Boolean) -> Unit) {
        if (!fActivity.isFinishing && !isShowing) {
            this.mUpdateAction = onUpdateAction
            super.show()
        }
    }

}