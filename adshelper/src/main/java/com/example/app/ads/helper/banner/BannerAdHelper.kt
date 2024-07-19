@file:Suppress("unused")

package com.example.app.ads.helper.banner

import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import com.example.app.ads.helper.PlaceHolderType
import com.example.app.ads.helper.getColorRes
import com.example.app.ads.helper.getColorStateRes
import com.example.app.ads.helper.inflateLayout
import java.io.Serializable

/**
 * @author Akshay Harsoda
 * @since 28 Jun 2024
 *
 * BannerAdHelper.kt - Simple object which has handle your [BannerAdView] data
 */
object BannerAdHelper {

    private val TAG = "Admob_${javaClass.simpleName}"

    @JvmStatic
    fun with(fBannerAdView: BannerAdView): UpdateBannerAdView {
        return UpdateBannerAdView(fBannerAdView)
    }

    class UpdateBannerAdView(private val mBannerAdView: BannerAdView) : Serializable {

        //    xml attr : banner_placeholder_type
        private var placeHolderType: PlaceHolderType? = null

        //    xml attr : banner_ad_size
        private var adSize: BannerAdSize? = null

        //    xml attr : banner_ad_type
        private var adType: BannerAdType? = null

        //    xml attr : banner_placeholder_text_color
        private var placeholderTextColor: ColorStateList? = null

        //    xml attr : banner_custom_placeholder
        private var customPlaceholder: View? = null

        @JvmName("updateAdSize")
        fun updateAdSize(fBannerAdSize: BannerAdSize) = this@UpdateBannerAdView.apply {
            this.adSize = fBannerAdSize
        }

        @JvmName("updateAdType")
        fun updateAdType(fBannerAdType: BannerAdType) = this@UpdateBannerAdView.apply {
            this.adType = fBannerAdType
        }

        @JvmName("updatePlaceHolderType")
        fun updatePlaceHolderType(fPlaceHolderType: PlaceHolderType) = this@UpdateBannerAdView.apply {
            this.placeHolderType = fPlaceHolderType
        }

        //<editor-fold desc="Place Holder Text Color Using ColorStateList, ColorInt & ColorRes ID [ColorResources Or ColorStateListResources]">
        @JvmName("updatePlaceholderTextColor")
        fun updatePlaceholderTextColor(fColors: ColorStateList) = this@UpdateBannerAdView.apply {
            this.placeholderTextColor = fColors
        }

        @JvmName("updatePlaceholderTextColor")
        fun updatePlaceholderTextColor(@ColorInt fColorInt: Int) = this@UpdateBannerAdView.apply {
            updatePlaceholderTextColor(fColors = ColorStateList.valueOf(fColorInt))
        }

        @JvmName("updatePlaceholderTextColorResources")
        fun updatePlaceholderTextColorResources(@ColorRes id: Int) = this@UpdateBannerAdView.apply {
            updatePlaceholderTextColor(fColorInt = mBannerAdView.context.getColorRes(id))
        }

        @JvmName("updatePlaceholderTextColorStateListResources")
        fun updatePlaceholderTextColorStateListResources(@ColorRes id: Int) = this@UpdateBannerAdView.apply {
            mBannerAdView.context.getColorStateRes(id)?.let { updatePlaceholderTextColor(fColors = it) }
        }
        //</editor-fold>

        //<editor-fold desc="Custom Place Holder View Or LayoutRes ID">
        @JvmName("updateCustomPlaceholder")
        fun updateCustomPlaceholder(fView: View) = this@UpdateBannerAdView.apply {
            this.customPlaceholder = fView
        }

        @JvmName("updateCustomPlaceholder")
        fun updateCustomPlaceholder(@LayoutRes id: Int) = this@UpdateBannerAdView.apply {
            updateCustomPlaceholder(fView = mBannerAdView.inflateLayout(resource = id))
        }
        //</editor-fold>

//        fun loadAd() {
//            mBannerAdView.updateAdView(
//                fPlaceHolderType = this.placeHolderType,
//                fAdSize = this.adSize,
//                fAdType = this.adType,
//                fPlaceholderTextColor = this.placeholderTextColor,
//                fCustomPlaceholder = this.customPlaceholder,
//            )
//        }

        fun updateAdView() {
            mBannerAdView.updateAdView(
                fPlaceHolderType = this.placeHolderType,
                fAdSize = this.adSize,
                fAdType = this.adType,
                fPlaceholderTextColor = this.placeholderTextColor,
                fCustomPlaceholder = this.customPlaceholder,
            )
        }

        fun loadAd() {
            mBannerAdView.loadNonAutoAd()
        }
    }
}