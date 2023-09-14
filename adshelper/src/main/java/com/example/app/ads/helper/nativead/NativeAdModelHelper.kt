package com.example.app.ads.helper.nativead

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import com.example.app.ads.helper.*
import com.example.app.ads.helper.blurEffect.BlurImage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView


/**
 * @author Akshay Harsoda
 * @since 20 Dec 2022
 *
 * NativeAdModelHelper.kt - Simple class which has load and handle your multiple size Native Advanced AD data
 * @param mContext this is a reference to your activity or fragment context
 */
class NativeAdModelHelper(private val mContext: Activity) {

    private val TAG = "Akshay_Admob_${javaClass.simpleName}"
    private var mFLayout: FrameLayout? = null
    private var mShimmerLayout: View? = null

    var isAdLoaded = false

    companion object {
        fun destroy() {
            NativeAdHelper.destroy()
        }
    }

    /**
     * Call this method when you need to load your Native Advanced AD
     * you need to call this method only once in any activity or fragment
     *
     * this method will load your Native Advanced AD with 4 different size like [NativeAdsSize.Medium], [NativeAdsSize.Big], [NativeAdsSize.FullScreen], [NativeAdsSize.Custom]
     * for Native Advanced AD Size @see [NativeAdsSize] once
     *
     * @param fSize it indicate your Ad Size
     * @param fLayout FrameLayout for add NativeAd View
     * @param fCustomAdView your native ad custom layout
     * @param adChoicesPlacement Ads I icon place @see [NativeAdOptions.ADCHOICES_TOP_RIGHT], [NativeAdOptions.ADCHOICES_TOP_LEFT], [NativeAdOptions.ADCHOICES_BOTTOM_RIGHT], [NativeAdOptions.ADCHOICES_BOTTOM_LEFT]
     * @param isNeedLayoutShow [by Default value = true] pass false if you do not need to show AD at a time when it's loaded successfully
     * @param isAddVideoOptions [by Default value = true] pass false if you don't need to add video option
     * @param isSetDefaultButtonColor [by Default value = true] pass false if you don't need to change in ad action button
     * @param onAdLoaded lambda function call when ad isLoaded
     * @param onAdClosed lambda function call after ad closed
     * @param onAdFailed lambda function call after ad failed to load
     * @param onClickAdClose lambda function call when user click close button of ad
     * @param isNeedToShow check if Subscribe is done then ads will not show
     * @param remoteConfig check remote Config parameters is if true ads will show else false ads will not show
     */
    fun loadNativeAdvancedAd(
        fSize: NativeAdsSize,
        fLayout: FrameLayout,
        fCustomAdView: View? = null,
        fCustomShimmerView: View? = null,
        @NativeAdOptions.AdChoicesPlacement fAdChoicesPlacement: Int = NativeAdOptions.ADCHOICES_TOP_RIGHT,
        isNeedLayoutShow: Boolean = true,
        isAddVideoOptions: Boolean = true,
        isSetDefaultButtonColor: Boolean = true,
        isNeedToShowShimmerLayout: Boolean = true,
        isNeedToShowAd: Boolean = true,
        topMargin: Int = 0,
        startMargin: Int = 0,
        bottomMargin: Int = 0,
        endMargin: Int = 0,
        onAdLoaded: (isNeedToRemoveCloseButton: Boolean) -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdFailed: () -> Unit = {},
        onClickAdClose: () -> Unit = {},

        ) {
        if (isNeedToShowAd && VasuAdsConfig.with(fLayout.context).remoteConfigNativeAdvancedAds && fLayout.context.isOnline) {
            mFLayout = fLayout
            fLayout.tag = fSize.name

            logE(
                tag = TAG, message = "loadNativeAdvancedAd: New Request -> ${fSize.name}"
            )

            if (isNeedToShowShimmerLayout) {
                val shimmerLayout = when (fSize) {
                    NativeAdsSize.Big -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_native_ad_big, fLayout, false
                    )

                    NativeAdsSize.Medium -> mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_native_ad_medium, fLayout, false
                    )

                    NativeAdsSize.VOICE_GPS -> mContext.inflater.inflate(
                        R.layout.layout_google_native_ad_voice_gps_home_loading, fLayout, false
                    )

                    NativeAdsSize.Custom -> fCustomShimmerView ?: mContext.inflater.inflate(
                        R.layout.layout_shimmer_google_native_ad_big, fLayout, false
                    )

                    NativeAdsSize.FullScreen -> {
                        mContext.inflater.inflate(
                            R.layout.layout_shimmer_google_native_ad_exit_full_screen_app_store,
                            fLayout,
                            false
                        )
                    }
                }
                if (fSize == NativeAdsSize.VOICE_GPS) {
                    val clMain = shimmerLayout.findViewById<CardView>(R.id.clMain)
                    val param = clMain.layoutParams as ViewGroup.MarginLayoutParams
                    param.setMargins(startMargin, topMargin, endMargin, bottomMargin)
                    clMain.layoutParams =
                        param // Tested!! - You need this line for the params to be applied.
                }
                mShimmerLayout = shimmerLayout
                if (isNeedLayoutShow) {
                    Log.d(TAG, "loadNativeAdvancedAd: add shimmer")
                    fLayout.addView(shimmerLayout)
                    fLayout.visible
                } else {
//                fLayout.removeAllViews()
                    fLayout.gone
                }
            }
            NativeAdHelper.loadAd(fContext = mContext,
                fLayout = fLayout,
                isAddVideoOptions = isAddVideoOptions,
                adChoicesPlacement = fAdChoicesPlacement,
                onAdLoaded = { index, nativeAd ->
                    logE(tag = TAG, message = "loadNativeAdvancedAd: onAdLoaded: Index -> $index")
                    if (NativeAdvancedModelHelper.getNativeAd == null) NativeAdvancedHelper.mNativeAd =
                        nativeAd
                    isAdLoaded = true
                    loadAdWithPerfectLayout(
                        fSize = fSize,
                        fLayout = fLayout,
                        fNativeAd = nativeAd,
                        fCustomAdView = fCustomAdView,
                        isNeedLayoutShow = isNeedLayoutShow,
                        isSetDefaultButtonColor = isSetDefaultButtonColor,
                        topMargin = topMargin,
                        startMargin = startMargin,
                        endMargin = endMargin,
                        bottomMargin = bottomMargin,
                        onAdLoaded = onAdLoaded,
                        onClickAdClose = onClickAdClose
                    )
                },
                onAdClosed = { index ->
                    logE(tag = TAG, message = "loadNativeAdvancedAd: onAdClosed: Index -> $index")
                    fLayout.removeAllViews()
                    fLayout.gone
                    isAdLoaded = false
                    NativeAdvancedHelper.mNativeAd = null

                    loadNativeAdvancedAd(
                        fSize = fSize,
                        fLayout = fLayout,
                        fCustomAdView = fCustomAdView,
                        fCustomShimmerView = fCustomShimmerView,
                        fAdChoicesPlacement = fAdChoicesPlacement,
                        isNeedLayoutShow = isNeedLayoutShow,
                        isAddVideoOptions = isAddVideoOptions,
                        isSetDefaultButtonColor = isSetDefaultButtonColor,
                        isNeedToShowShimmerLayout = isNeedToShowShimmerLayout,
                        topMargin = topMargin,
                        startMargin = startMargin,
                        endMargin = endMargin,
                        bottomMargin = bottomMargin,
                        onAdLoaded = onAdLoaded,
                        onAdClosed = onAdClosed,
                        onAdFailed = onAdFailed,
                        onClickAdClose = onClickAdClose,
                        isNeedToShowAd = isNeedToShowAd,
                    )
                },
                onAdFailed = { index ->
                    logE(tag = TAG, message = "loadNativeAdvancedAd: onAdFailed: Index -> $index")
                    fLayout.removeAllViews()
                    fLayout.gone
                    isAdLoaded = false
                })
        } else {
            onAdLoaded.invoke(fSize == NativeAdsSize.FullScreen)
            onAdClosed.invoke()
            onClickAdClose.invoke()
        }
    }

    //old method don`t uncomment
//    fun manageShimmerLayoutVisibility(isNeedToShowAd: Boolean) {
//        if (isNeedToShowAd) {
//            if (!isAdLoaded) {
//                mFLayout?.removeAllViews()
//                mFLayout?.addView(mShimmerLayout)
//                mFLayout?.visible
//            } else {
//                mFLayout?.visible
//            }
//        } else {
////            mFLayout?.removeAllViews()
//            mFLayout?.gone
//        }
//
//    }

    fun manageShimmerLayoutVisibility(
        isNeedToShowAd: Boolean,
        fSize: NativeAdsSize,
        fLayout: FrameLayout,
        fCustomShimmerView: View? = null,
    ) {
        val shimmerLayout = when (fSize) {
            NativeAdsSize.Big -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_native_ad_big, fLayout, false
            )

            NativeAdsSize.Medium -> mContext.inflater.inflate(
                R.layout.layout_shimmer_google_native_ad_medium, fLayout, false
            )

            NativeAdsSize.VOICE_GPS -> mContext.inflater.inflate(
                R.layout.layout_google_native_ad_voice_gps_home_loading, fLayout, false
            )

            NativeAdsSize.Custom -> fCustomShimmerView ?: mContext.inflater.inflate(
                R.layout.layout_shimmer_google_native_ad_big, fLayout, false
            )

            NativeAdsSize.FullScreen -> {
                mContext.inflater.inflate(
                    R.layout.layout_shimmer_google_native_ad_exit_full_screen_app_store,
                    fLayout,
                    false
                )
            }

        }

        mShimmerLayout = shimmerLayout
        if (isNeedToShowAd && VasuAdsConfig.with(fLayout.context).remoteConfigNativeAdvancedAds) {
            if (!isAdLoaded) {
                fLayout.removeAllViews()
                fLayout.addView(mShimmerLayout)
                fLayout.visible
            } else {
                fLayout.visible
            }
        } else {
//            mFLayout?.removeAllViews()
            fLayout.gone
        }

    }


    private fun loadAdWithPerfectLayout(
        fSize: NativeAdsSize,
        fLayout: FrameLayout,
        fNativeAd: NativeAd,
        fCustomAdView: View? = null,
        isNeedLayoutShow: Boolean,
        isSetDefaultButtonColor: Boolean = true,
        topMargin: Int = 0,
        startMargin: Int = 0,
        bottomMargin: Int = 0,
        endMargin: Int = 0,
        onAdLoaded: (isNeedToRemoveCloseButton: Boolean) -> Unit,
        onClickAdClose: () -> Unit,
    ) {
        fLayout.removeAllViews()
//        fLayout.visible
        Log.d(TAG, "loadAdWithPerfectLayout: ")
        val adView = when (fSize) {
            NativeAdsSize.Big -> mContext.inflater.inflate(
                R.layout.layout_google_native_ad_big, fLayout, false
            )

            NativeAdsSize.Medium -> mContext.inflater.inflate(
                R.layout.layout_google_native_ad_medium, fLayout, false
            )

            NativeAdsSize.VOICE_GPS -> mContext.inflater.inflate(
                R.layout.layout_google_native_ad_voice_gps_home, fLayout, false
            )

            NativeAdsSize.Custom -> fCustomAdView
                ?: mContext.inflater.inflate(R.layout.layout_google_native_ad_big, fLayout, false)

            NativeAdsSize.FullScreen -> {
                if (fNativeAd.starRating != null && fNativeAd.price != null && fNativeAd.store != null) {
                    mContext.inflater.inflate(
                        R.layout.layout_google_native_ad_exit_full_screen_app_store, fLayout, false
                    )
                } else {
                    mContext.inflater.inflate(
                        R.layout.layout_google_native_ad_exit_full_screen_website, fLayout, false
                    )
                }
            }
        }
        if (fSize == NativeAdsSize.VOICE_GPS) {
            val clMain = adView.findViewById<CardView>(R.id.clMain)
            val param = clMain.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(startMargin, topMargin, endMargin, bottomMargin)
            clMain.layoutParams =
                param // Tested!! - You need this line for the params to be applied.
        }

        if (isSetDefaultButtonColor) {
            val isChangeButtonDrawable: Boolean = if (fSize == NativeAdsSize.FullScreen) {
                !(fNativeAd.starRating != null && fNativeAd.price != null && fNativeAd.store != null)
            } else {
                true
            }

            if (isChangeButtonDrawable) {
                val value = TypedValue()
                mContext.theme.resolveAttribute(R.attr.native_ads_main_color, value, true)

                AppCompatResources.getDrawable(mContext, R.drawable.native_ad_button)
                    ?.let { unwrappedDrawable ->
                        DrawableCompat.wrap(unwrappedDrawable).let { wrappedDrawable ->
                            DrawableCompat.setTint(wrappedDrawable, value.data)
                            adView.findViewById<TextView>(R.id.ad_call_to_action).background =
                                wrappedDrawable
                        }
                    }
            }
        }

        adView.findViewById<NativeAdView>(R.id.native_ad_view)?.let { nativeAdView ->
            populateNativeAdView(
                fNativeAd = fNativeAd, nativeAdView = nativeAdView, onClickAdClose = onClickAdClose
            )
        }

        fLayout.apply {
            this.removeAllViews()
            this.addView(adView)
            this.beVisibleIf(isNeedLayoutShow)
        }

        onAdLoaded.invoke((fSize == NativeAdsSize.FullScreen && fNativeAd.starRating != null && fNativeAd.price != null && fNativeAd.store != null))
    }

    private fun populateNativeAdView(
        fNativeAd: NativeAd, nativeAdView: NativeAdView, onClickAdClose: () -> Unit
    ) {
        with(nativeAdView) {
            this.advertiserView = this.findViewById(R.id.ad_advertiser)
            this.bodyView = this.findViewById(R.id.ad_body)
            this.headlineView = this.findViewById(R.id.ad_headline)
            this.priceView = this.findViewById(R.id.ad_price)
            this.storeView = this.findViewById(R.id.ad_store)
            this.starRatingView = this.findViewById(R.id.ad_stars)
            this.iconView = this.findViewById(R.id.ad_app_icon)
            this.mediaView = this.findViewById(R.id.ad_media)
            this.callToActionView = this.findViewById(R.id.ad_call_to_action)
            this.imageView = this.findViewById(R.id.iv_bg_main_image)

            this.mediaView?.let { fView ->
                fView.gone
                if (fNativeAd.mediaContent != null) {
                    fNativeAd.mediaContent?.let { fData ->
                        logI(tag = TAG, message = "populateNativeAdView: Set Media View")
                        fView.setMediaContent(fData)
                        fView.setImageScaleType(ImageView.ScaleType.FIT_CENTER)
                        fView.visible
                    }
                } else {
                    populateNativeAdView(
                        fNativeAd = fNativeAd,
                        nativeAdView = nativeAdView,
                        onClickAdClose = onClickAdClose
                    )
                }
            }

            this.imageView?.let { fView ->
                if (fNativeAd.images.size > 0) {
                    fNativeAd.images[0]?.drawable?.let { fData ->
                        fView.visible

                        val bitmap: Bitmap = Bitmap.createBitmap(
                            fData.intrinsicWidth, fData.intrinsicHeight, Bitmap.Config.ARGB_8888
                        )

                        val canvas = Canvas(bitmap)
                        fData.setBounds(0, 0, canvas.width, canvas.height)
                        fData.draw(canvas)

                        BlurImage().load(bitmap).radius(3f).withCPU().into((fView as ImageView))
                    }
                }
            }

            this.advertiserView?.let { fView ->
                fView.gone
                fNativeAd.advertiser?.let { fData ->
                    (fView as TextView).text = fData
                    fView.visible
                }
            }

            this.bodyView?.let { fView ->
                fView.gone
                fNativeAd.body?.let { fData ->
                    (fView as TextView).text = fData
                    fView.visible
                }
            }

            this.headlineView?.let { fView ->
                fView.gone
                fNativeAd.headline?.let { fData ->
                    (fView as TextView).text = fData
                    fView.visible
                }
            }

            this.priceView?.let { fView ->
                fView.gone
                fNativeAd.price?.let { fData ->
                    (fView as TextView).text = fData
                    fView.visible
                }
            }

            this.storeView?.let { fView ->
                with(fView as TextView) {
                    this.gone
                    fNativeAd.store?.let { fData ->
                        this.text = fData
                        this.isSelected = true
                        this.visible

                        this.findViewById<View>(R.id.iv_play_logo)?.let { logoView ->
                            if (fData.equals("Google Play", false)) {
                                logoView.visible
                            } else {
                                logoView.gone
                            }
                        }
                    }
                }
            }

            this.starRatingView?.let { fView ->
                fView.gone
                this.findViewById<TextView>(R.id.txt_rating)?.gone

                fNativeAd.starRating?.let { fData ->
                    (fView as RatingBar).rating = fData.toFloat()
                    fView.visible

                    this.findViewById<TextView>(R.id.txt_rating)?.let { txtRating ->
                        txtRating.text = fData.toFloat().toString()
                        txtRating.visible
                    }
                }
            }

            this.iconView?.let { fView ->
                fView.gone

                when {
                    fNativeAd.icon != null -> {
                        fNativeAd.icon?.drawable?.let { fData ->
                            (fView as ImageView).setImageDrawable(fData)
                            fView.visible
                        }
                    }

                    fNativeAd.images.size > 0 -> {
                        fNativeAd.images[0]?.drawable?.let { fData ->
                            (fView as ImageView).setImageDrawable(fData)
                            fView.visible
                        }
                    }

                    else -> {
                        fView.gone
                    }
                }
            }

            this.callToActionView?.let { fView ->
                fView.gone
                fNativeAd.callToAction?.let { fData ->
                    when (fView) {
                        is Button -> {
                            fView.text = getCamelCaseString(fData)
                        }

                        is androidx.appcompat.widget.AppCompatTextView -> {
                            fView.text = getCamelCaseString(fData)
                        }

                        is TextView -> {
                            fView.text = getCamelCaseString(fData)
                        }
                    }

                    fView.isSelected = true
                    fView.visible
                }
            }

            if (this.storeView?.visibility == View.GONE && this.priceView?.visibility == View.GONE) {
                this.findViewById<View>(R.id.cl_ad_price_store)?.gone
            }

            this.findViewById<View>(R.id.ad_call_to_close)?.let { closeIcon ->
                closeIcon.setOnClickListener { onClickAdClose.invoke() }
            }

            this.setNativeAd(fNativeAd)
        }
    }

    private fun getCamelCaseString(text: String): String {

        val words: Array<String> = text.split(" ").toTypedArray()

        val builder = StringBuilder()
        for (i in words.indices) {
            var word: String = words[i]
            word = if (word.isEmpty()) word else Character.toUpperCase(word[0])
                .toString() + word.substring(1).lowercase()
            builder.append(word)
            if (i != (words.size - 1)) {
                builder.append(" ")
            }
        }
        return builder.toString()
    }
}