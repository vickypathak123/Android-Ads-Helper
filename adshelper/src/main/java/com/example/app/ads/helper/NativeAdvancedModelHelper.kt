package com.example.app.ads.helper

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.app.ads.helper.widgets.BlurDrawable
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * @author Akshay Harsoda
 * @since 24 Nov 2021
 *
 * NativeAdvancedModel.kt - Simple class which has load and handle your multiple size Native Advanced AD data
 * @param mContext this is a reference to your activity or fragment context
 */
class NativeAdvancedModelHelper(private val mContext: Context) : AdMobAdsListener {

    private val TAG = "Admob_" + javaClass.simpleName

    companion object {
        val getNativeAd: NativeAd?
            get() {
                return NativeAdvancedHelper.mNativeAd
            }

        fun destroy() {
            NativeAdvancedHelper.destroy()
        }
    }

    private var mCloseTimer: AdsCloseTimer? = null

    private var isAdClicked: Boolean = false
    private var isAdOwnerPause: Boolean = false


    private var mSize: NativeAdsSize = NativeAdsSize.Medium
    private var mLayout: FrameLayout = FrameLayout(mContext)
    private var mIsNeedLayoutShow: Boolean = true
    private var mIsAddVideoOptions: Boolean = false
    private var mIsAdLoaded: (isNeedToRemoveCloseButton: Boolean) -> Unit = {}
    private var mOnClickAdClose: () -> Unit = {}

    /**
     * Call this method when you need to load your Native Advanced AD
     * you need to call this method only once in any activity or fragment
     *
     * this method will load your Native Advanced AD with 4 different size like [NativeAdsSize.Medium], [NativeAdsSize.Big], [NativeAdsSize.FullScreen]
     * for Native Advanced AD Size @see [NativeAdsSize] once
     *
     * @param fSize it indicate your Ad Size
     * @param fLayout FrameLayout for add NativeAd View
     * @param isNeedLayoutShow [by Default value = true] pass false if you do not need to show AD at a time when it's loaded successfully
     * @param isAddVideoOptions [by Default value = false] pass true if you need to add video option
     * @param isAdLoaded lambda function call when ad isLoaded
     * @param onClickAdClose lambda function call when user click close button of ad
     */
    fun loadNativeAdvancedAd(
        @NonNull fSize: NativeAdsSize,
        @NonNull fLayout: FrameLayout,
        isNeedLayoutShow: Boolean = true,
        isAddVideoOptions: Boolean = false,
        isAdLoaded: (isNeedToRemoveCloseButton: Boolean) -> Unit = {},
        onClickAdClose: () -> Unit = {}
    ) {
        Log.i(TAG, "loadAd: ")
        mSize = fSize
        mLayout = fLayout
        mIsNeedLayoutShow = isNeedLayoutShow
        mIsAddVideoOptions = isAddVideoOptions
        mIsAdLoaded = isAdLoaded
        mOnClickAdClose = onClickAdClose

        mCloseTimer?.cancel()

        mCloseTimer = AdsCloseTimer(
            millisInFuture = 3000,
            countDownInterval = 1000,
            onFinish = {
                onClickAdClose.invoke()
            }
        )
        mCloseTimer?.start()

        NativeAdvancedHelper.loadNativeAdvancedAd(
            fContext = mContext,
            isAddVideoOptions = isAddVideoOptions,
            fListener = this,
        )
    }

    @SuppressLint("InflateParams")
    private fun loadAdWithPerfectLayout(
        @NonNull fSize: NativeAdsSize,
        @NonNull fLayout: FrameLayout,
        @NonNull nativeAd: NativeAd,
        isNeedLayoutShow: Boolean = true,
        isAdLoaded: (isNeedToRemoveCloseButton: Boolean) -> Unit = {},
        onClickAdClose: () -> Unit
    ) {

        mCloseTimer?.cancel()

        val adView = when (fSize) {

            NativeAdsSize.Big -> {
                mContext.inflater.inflate(
                    R.layout.layout_google_native_ad_big,
                    null
                ) as NativeAdView
            }

            NativeAdsSize.Medium -> {
                mContext.inflater.inflate(
                    R.layout.layout_google_native_ad_medium,
                    null
                ) as NativeAdView
            }

            NativeAdsSize.FullScreen -> {
                if (nativeAd.starRating != null && nativeAd.price != null && nativeAd.store != null) {
                    mContext.inflater.inflate(
                        R.layout.layout_google_native_ad_exit_full_screen_app_store,
                        null
                    ) as ConstraintLayout
                } else {
                    mContext.inflater.inflate(
                        R.layout.layout_google_native_ad_exit_full_screen_website,
                        null
                    ) as NativeAdView
                }
            }
        }


        when (fSize) {
            NativeAdsSize.FullScreen -> {
                populateFullScreenNativeAdView(
                    nativeAd,
                    adView.findViewById(R.id.native_ad_view),
                    onClickAdClose
                )
            }

            else -> {
                populateNativeAdView(nativeAd, adView as NativeAdView)
            }
        }

        fLayout.removeAllViews()
        fLayout.addView(adView)
        if (isNeedLayoutShow) {
            fLayout.visibility = View.VISIBLE
            if (fSize == NativeAdsSize.FullScreen && nativeAd.starRating != null && nativeAd.price != null && nativeAd.store != null) {
                isAdLoaded.invoke(true)
            } else {
                isAdLoaded.invoke(false)
            }
        } else {
            fLayout.visibility = View.GONE
        }
    }

    private fun populateFullScreenNativeAdView(
        nativeAd: NativeAd,
        adView: NativeAdView,
        onClickAdClose: () -> Unit
    ) {

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.imageView = adView.findViewById(R.id.iv_bg_main_image)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

        (adView.headlineView as TextView).text = nativeAd.headline

        if (nativeAd.mediaContent != null && adView.mediaView != null) {
            nativeAd.mediaContent?.let { mediaContent ->
                adView.mediaView?.setMediaContent(mediaContent)
            }
        } else {
            populateFullScreenNativeAdView(getNativeAd!!, adView, onClickAdClose)
        }


        if (nativeAd.images.size > 0) {
            if (nativeAd.images[0] != null && adView.imageView != null) {
                (adView.imageView as ImageView).setImageDrawable(nativeAd.images[0].drawable!!)

                val blurView: View = adView.findViewById(R.id.blur_view)
                val blurDrawable = BlurDrawable(adView.imageView, 15)
                blurView.background = blurDrawable
            }
        }

        if (nativeAd.body == null && adView.bodyView != null) {
            adView.bodyView?.visibility = View.GONE
        } else if (adView.bodyView != null) {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.icon == null && adView.iconView != null) {
            adView.iconView?.visibility = View.GONE
        } else if (adView.iconView != null) {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.starRating == null && adView.starRatingView != null) {
            adView.starRatingView?.visibility = View.GONE
            (adView.findViewById(R.id.txt_rating) as TextView?)?.visibility = View.GONE
        } else if (adView.starRatingView != null) {
            (adView.findViewById(R.id.txt_rating) as TextView?)?.text =
                nativeAd.starRating!!.toFloat().toString()
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
            (adView.findViewById(R.id.txt_rating) as TextView?)?.visibility = View.VISIBLE
        }

        if (nativeAd.callToAction == null && adView.callToActionView != null) {
            adView.callToActionView?.visibility = View.GONE
        } else if (adView.callToActionView != null) {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).isSelected = true
            nativeAd.callToAction?.let {
                (adView.callToActionView as Button).text = getCamelCaseString(it)
            }
        }

        if (nativeAd.store == null && adView.storeView != null) {
            adView.storeView?.visibility = View.GONE
        } else if (adView.storeView != null) {
            (adView.storeView as TextView).isSelected = true
            adView.storeView?.visibility = View.VISIBLE
            nativeAd.store?.let {
                (adView.storeView as TextView).text = it
                if (it.equals("Google Play", false)) {
                    (adView.findViewById(R.id.iv_play_logo) as View?)?.visibility = View.VISIBLE
                } else {
                    (adView.findViewById(R.id.iv_play_logo) as View?)?.visibility = View.GONE
                }
            }
        }

        if (nativeAd.price == null && adView.priceView != null) {
            adView.priceView?.visibility = View.GONE
        } else if (adView.priceView != null) {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.advertiser == null && adView.advertiserView != null) {
            adView.advertiserView?.visibility = View.GONE
        } else if (adView.advertiserView != null) {
            adView.advertiserView?.visibility = View.VISIBLE
            (adView.advertiserView as TextView).text = nativeAd.advertiser
        }

        if (adView.storeView?.visibility == View.GONE && adView.priceView?.visibility == View.GONE) {
            (adView.findViewById(R.id.cl_ad_price_store) as View?)?.visibility = View.GONE
        }

        (adView.findViewById(R.id.ad_call_to_close) as Button?)?.let {
            it.setOnClickListener {
                onClickAdClose.invoke()
            }
        }

        adView.setNativeAd(nativeAd)
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        Log.i(TAG, Throwable().stackTrace[0].methodName)

        adView.mediaView = adView.findViewById(R.id.ad_media)

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        (adView.headlineView as TextView).text = nativeAd.headline

        if (nativeAd.mediaContent != null) {
            if (adView.mediaView != null) {
                adView.mediaView!!.setMediaContent(nativeAd.mediaContent!!)
            }
        } else {
            populateNativeAdView(getNativeAd!!, adView)
        }

        if (nativeAd.body == null && adView.bodyView != null) {
            adView.bodyView!!.visibility = View.GONE
        } else if (adView.bodyView != null) {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null && adView.callToActionView != null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else if (adView.callToActionView != null) {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon != null && adView.iconView != null) {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon!!.drawable
            )
            adView.iconView!!.visibility = View.VISIBLE
        } else if (adView.iconView != null) {
            if (nativeAd.images.size > 0) {
                if (nativeAd.images[0] != null && nativeAd.images[0].drawable != null) {
                    (adView.iconView as ImageView).setImageDrawable(nativeAd.images[0].drawable!!)
                    adView.iconView!!.visibility = View.VISIBLE
                } else {
                    adView.iconView!!.visibility = View.GONE
                }
            } else {
                adView.iconView!!.visibility = View.GONE
            }
        } else {
            adView.iconView!!.visibility = View.GONE
        }

        if (nativeAd.price == null && adView.priceView != null) {
            adView.priceView!!.visibility = View.INVISIBLE
        } else if (adView.priceView != null) {
            adView.priceView!!.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null && adView.storeView != null) {
            adView.storeView!!.visibility = View.INVISIBLE
        } else if (adView.storeView != null) {
            adView.storeView!!.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (adView.priceView != null) {
            adView.priceView!!.visibility = View.GONE
        }
        if (adView.storeView != null) {
            adView.storeView!!.visibility = View.GONE
        }

        if (nativeAd.starRating == null && adView.starRatingView != null) {
            adView.starRatingView!!.visibility = View.GONE
        } else if (adView.starRatingView != null) {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView!!.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null && adView.advertiserView != null) {
            adView.advertiserView!!.visibility = View.GONE
        } else if (adView.advertiserView != null) {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView!!.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
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

    override fun onAdClosed(isShowFullScreenAd: Boolean) {
        super.onAdClosed(isShowFullScreenAd)
        Log.i(TAG, "onAdClosed: ")

        isAdClicked = true

        if (!isAdOwnerPause) {
            mLayout.removeAllViews()

            loadNativeAdvancedAd(
                fSize = mSize,
                fLayout = mLayout,
                isNeedLayoutShow = mIsNeedLayoutShow,
                isAddVideoOptions = mIsAddVideoOptions,
                isAdLoaded = mIsAdLoaded,
                onClickAdClose = mOnClickAdClose
            )
        }
    }

    override fun onNativeAdLoaded(nativeAd: NativeAd) {
        super.onNativeAdLoaded(nativeAd)
        isAdClicked = false

        loadAdWithPerfectLayout(
            fSize = mSize,
            fLayout = mLayout,
            nativeAd = nativeAd,
            isNeedLayoutShow = mIsNeedLayoutShow,
            isAdLoaded = mIsAdLoaded,
            onClickAdClose = mOnClickAdClose
        )
    }

    inner class AdsCloseTimer(
        private val millisInFuture: Long,
        countDownInterval: Long,
        private val onFinish: () -> Unit
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            Log.e(TAG, "onTick: Time::${(((millisInFuture - millisUntilFinished) / 1000) + 1)}")
        }

        override fun onFinish() {
            onFinish.invoke()
        }
    }
}