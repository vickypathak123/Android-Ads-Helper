package com.example.ads.helper.new_.demo.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.AnimatorRes
import androidx.annotation.StyleRes
import androidx.annotation.UiThread
import androidx.viewbinding.ViewBinding
import com.example.ads.helper.new_.demo.R
import com.example.ads.helper.new_.demo.base.utils.displayWidth

abstract class BaseDialog<VB : ViewBinding>(
    val fActivity: BaseActivity,
    fWidth: Int = (fActivity.displayWidth * 0.95).toInt(),
    fHeight: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    @AnimatorRes @StyleRes fAnimation: Int = R.style.base_theme_dialog_animation
) : Dialog(fActivity), View.OnClickListener {

    @Suppress("PropertyName")
    val TAG: String = javaClass.simpleName

    val mBinding: VB = this.setBinding()

    @UiThread
    abstract fun setBinding(): VB

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        this.setContentView(mBinding.root)

        window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(fWidth, fHeight)

//            it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            it.statusBarColor = context.getColorRes(R.color.colorPrimary)

            ///////////   Animation  ////////
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(it.attributes)
//            lp.width = (activity.displayWidth * 0.85).toInt()
//            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.width = fWidth
            lp.height = fHeight
            lp.gravity = Gravity.CENTER
            lp.windowAnimations = fAnimation
            it.attributes = lp
        }

        this.setCancelable(false)
        this.setCanceledOnTouchOutside(false)

        this.initView()
        this.loadAds()
        this.initViewAction()
        this.initViewListener()
    }

    override fun onClick(v: View) {
    }

    @UiThread
    open fun initView() {

    }

    private fun loadAds() {
        setDefaultAdUI()
//        if (isOnline) {
//            initAds()
//        }
    }

    @UiThread
    open fun setDefaultAdUI() {
    }

    @UiThread
    open fun initAds() {
    }

    @UiThread
    open fun initViewAction() {
    }

    @UiThread
    open fun initViewListener() {
    }

    @UiThread
    open fun setClickListener(vararg fViews: View) {
        for (lView in fViews) {
            lView.setOnClickListener(this)
        }
    }

    override fun show() {
        if (!fActivity.isFinishing && !isShowing) {
            super.show()
        }
    }
}