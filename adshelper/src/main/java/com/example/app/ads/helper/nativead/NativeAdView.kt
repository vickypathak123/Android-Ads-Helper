package com.example.app.ads.helper.nativead

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.app.ads.helper.PlaceHolderType
import com.example.app.ads.helper.R
import com.example.app.ads.helper.addMargin
import com.example.app.ads.helper.beVisibleIf
import com.example.app.ads.helper.blurEffect.BlurImage
import com.example.app.ads.helper.databinding.LayoutNativeAdMainBinding
import com.example.app.ads.helper.databinding.PlaceholderShimmerBinding
import com.example.app.ads.helper.databinding.PlaceholderTextBinding
import com.example.app.ads.helper.getColorRes
import com.example.app.ads.helper.getColorStateRes
import com.example.app.ads.helper.gone
import com.example.app.ads.helper.inflateLayout
import com.example.app.ads.helper.inflater
import com.example.app.ads.helper.invisible
import com.example.app.ads.helper.isInternetAvailable
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.removeMargin
import com.example.app.ads.helper.setColorAlpha
import com.example.app.ads.helper.toCamelCase
import com.example.app.ads.helper.visible
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * @author Akshay Harsoda
 * @since 20 Dec 2022
 * @updated 01 Jul 2024
 *
 * NativeAdView.kt - Simple view which has load and handle your Native AD data
 */
class NativeAdView : FrameLayout {
    @Suppress("PrivatePropertyName")
    private val TAG = "Admob_${javaClass.simpleName}"

    //<editor-fold desc="Current Ad View Callback">
    interface OnNativeAdViewListener {
        fun onAdLoaded() {}
        fun onAdClosed() {}
        fun onAdCustomClosed() {}
        fun onAdFailed() {}
    }

    private var mListener: OnNativeAdViewListener? = null
    //</editor-fold>

    private val mCurrentView: NativeAdView get() = this@NativeAdView
    private var mBinding: LayoutNativeAdMainBinding = LayoutNativeAdMainBinding.inflate(LayoutInflater.from(context), mCurrentView, true)

    private var isThisViewLoadNewAd: Boolean = true

    private val mInternetObserver = Observer<Boolean> {
        logE(tag = TAG, message = "onInternetChanged: $it")
        if (it) {
            if (!mCurrentView.isInEditMode) {
                if (NativeAdHelper.isNativeAdEnable()) {
                    logE(tag = TAG, message = "onInternetChanged: InternetAvailable -> Now Load Ad")
                    loadAd()
                }
            }
        }
    }

    private val Float.dpToPx get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

    //<editor-fold desc="FindViewById & inflate loading view">
    private val View.adInfoTextView: TextView? get() = this.findViewById(R.id.txt_ad)
    private val View.adMediaContainerFrameLayout: FrameLayout? get() = this.findViewById(R.id.native_ad_media_container)
    private val View.interstitialNativeProgressImageView: ImageView? get() = this.findViewById(R.id.iv_progress_interstitial_native)
    private val View.adAdvertiserTextView: TextView? get() = this.findViewById(R.id.ad_advertiser)
    private val View.adBodyTextView: TextView? get() = this.findViewById(R.id.ad_body)
    private val View.adHeadlineTextView: TextView? get() = this.findViewById(R.id.ad_headline)
    private val View.adPriceTextView: TextView? get() = this.findViewById(R.id.ad_price)
    private val View.adStoreTextView: TextView? get() = this.findViewById(R.id.ad_store)
    private val View.adStarsRatingBar: RatingBar? get() = this.findViewById(R.id.ad_stars)
    private val View.adAppIconImageView: ImageView? get() = this.findViewById(R.id.ad_app_icon)
    private val View.adBackgroundImageView: ImageView? get() = this.findViewById(R.id.ad_bg_main_image)
    private val View.adMediaView: com.google.android.gms.ads.nativead.MediaView? get() = this.findViewById(R.id.ad_media)
    private val View.adCallToActionTextView: TextView? get() = this.findViewById(R.id.ad_call_to_action)
    private val View.adStoreIconImageView: ImageView? get() = this.findViewById(R.id.iv_play_logo)
    private val View.adStarsRatingTextView: TextView? get() = this.findViewById(R.id.ad_txt_rating)
    private val View.adCallToCloseTextView: TextView? get() = this.findViewById(R.id.ad_call_to_close)

    private val shimmerBinding: PlaceholderShimmerBinding by lazy {
        PlaceholderShimmerBinding.inflate(context.inflater, mCurrentView, false).apply {
            this.root.layoutParams = actualLayoutParams
        }
    }

    private val placeHolderBinding: PlaceholderTextBinding by lazy {
        PlaceholderTextBinding.inflate(context.inflater, mCurrentView, false).apply {
            this.root.layoutParams = actualLayoutParams
        }
    }
    //</editor-fold>

    //<editor-fold desc="ad view attributes reference">
    private var mNativeAdType: NativeAdType = NativeAdType.BIG
    private var mNativeAdInterstitialType: NativeAdInterstitialType = NativeAdInterstitialType.WEBSITE
    private var mPlaceHolderType: PlaceHolderType = PlaceHolderType.SHIMMER
    private var mAutoLoad: Boolean = true
    private var mIsMakeNewAdRequest = false
    private var mShowPlaceholder: Boolean = true
    private var mCardElevation: Float = (2.0f).dpToPx
    private var mCardContentPaddingLeft: Int = 0
    private var mCardContentPaddingRight: Int = 0
    private var mCardContentPaddingTop: Int = 0
    private var mCardContentPaddingBottom: Int = 0
    private var mCardRadius: Float = (10.0f).dpToPx
    private var mCardPreventCornerOverlap: Boolean = true
    private var mCardUseCompatPadding: Boolean = true

    private val defaultNativeAdsBackgroundColor: ColorStateList get() = ColorStateList.valueOf(mCurrentView.context.getColorRes(R.color.default_native_ads_background_color))
    private val defaultNativeAdsMainColor: ColorStateList get() = ColorStateList.valueOf(mCurrentView.context.getColorRes(R.color.default_native_ads_main_color))
    private val defaultNativeAdsLabelTextColor: ColorStateList get() = ColorStateList.valueOf(mCurrentView.context.getColorRes(R.color.default_native_ads_label_text_color))
    private val defaultNativeAdsBodyTextColor: ColorStateList get() = ColorStateList.valueOf(mCurrentView.context.getColorRes(R.color.default_native_ads_body_text_color))

    private var mNativeAdsBackgroundColor: ColorStateList = defaultNativeAdsBackgroundColor
    private var mNativeAdsMainColor: ColorStateList = defaultNativeAdsMainColor
    private var mNativeAdsLabelTextColor: ColorStateList = defaultNativeAdsLabelTextColor
    private var mNativeAdsBodyTextColor: ColorStateList = defaultNativeAdsBodyTextColor

    @LayoutRes
    private var customAdViewResourceId: Int? = null
    private var customAdView: View? = null

    @LayoutRes
    private var customPlaceHolderResourceId: Int? = null
    private var customPlaceholderView: View? = null
    //</editor-fold>

    //<editor-fold desc="inflate view according Ad Type & View Size [Width & Height]">
    private val inflateAdViewAccordingType: View
        get() {
            val inflatedView = when (mNativeAdType) {
                NativeAdType.BIG -> mCurrentView.inflateLayout(R.layout.layout_google_native_ad_big)
                NativeAdType.MEDIUM -> mCurrentView.inflateLayout(R.layout.layout_google_native_ad_medium)
                NativeAdType.INTERSTITIAL_NATIVE -> {
                    when (mNativeAdInterstitialType) {
                        NativeAdInterstitialType.WEBSITE -> mCurrentView.inflateLayout(R.layout.layout_google_native_ad_interstitial_website)
                        NativeAdInterstitialType.APP_STORE -> mCurrentView.inflateLayout(R.layout.layout_google_native_ad_interstitial_app_store)
//                        NativeAdInterstitialType.FULL_SCREEN -> mCurrentView.inflateLayout(R.layout.layout_google_full_screen_native_ad)
                    }
                }

                NativeAdType.CUSTOM -> {
                    customAdViewResourceId?.let { id ->
                        this.inflateLayout(resource = id)
                    } ?: customAdView ?: kotlin.run {
                        throw RuntimeException("custom adView NullPointerException")
                    }
                }
            }
            inflatedView.apply {
                this.layoutParams = actualLayoutParams
            }
            return inflatedView
        }

    private val inflateAdViewWithShimmer: View
        get() {
            val inflateAdView = inflateAdViewAccordingType

            if (inflateAdView is ViewGroup) {
                val backgroundColor = context.getColor(R.color.shimmer_placeholder)

                inflateAdView.adInfoTextView?.let { view ->
                    view.setBackgroundColor(backgroundColor)
                    view.setTextColor(Color.DKGRAY)
                }
                inflateAdView.adAdvertiserTextView?.setBackgroundColor(backgroundColor)
                inflateAdView.adBodyTextView?.setBackgroundColor(backgroundColor)
                inflateAdView.adHeadlineTextView?.setBackgroundColor(backgroundColor)
                inflateAdView.adPriceTextView?.setBackgroundColor(backgroundColor)
                inflateAdView.adStoreTextView?.setBackgroundColor(backgroundColor)
                inflateAdView.adStarsRatingBar?.let { view ->
                    view.progressTintList = ColorStateList.valueOf(backgroundColor)
                    view.rating = 5.0f
                }
                inflateAdView.adAppIconImageView?.setBackgroundColor(backgroundColor)
                inflateAdView.adMediaView?.setBackgroundColor(backgroundColor)
                inflateAdView.adCallToActionTextView?.setBackgroundColor(backgroundColor)
                inflateAdView.adMediaContainerFrameLayout?.setBackgroundColor(backgroundColor)
            }

            shimmerBinding.loadingAdContainer.let { container ->
                inflateAdView.also { view ->
                    container.removeAllViews()
                    container.addView(view)
                }
            }

            shimmerBinding.loadingAdContainer.background = null

            return shimmerBinding.root
        }

    private val actualLayoutParams: LayoutParams
        get() {
            return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                this.gravity = Gravity.CENTER
            }
        }
    //</editor-fold>

    //<editor-fold desc="Constructor">
    constructor(context: Context) : super(context) {
        setUpLayout(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setUpLayout(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setUpLayout(context, attrs)
    }

    private fun refreshView() {
        invalidate()
    }
    //</editor-fold>

    private fun setUpLayout(context: Context, attrs: AttributeSet?) {


        val adsColorAttr = intArrayOf(
            R.attr.native_ads_main_color,
            R.attr.native_ads_label_text_color,
            R.attr.native_ads_body_text_color,
            R.attr.native_ads_background_color
        )
        context.obtainStyledAttributes(adsColorAttr).use { osa ->
            adsColorAttr.indexOf(R.attr.native_ads_main_color).let {
                if (osa.hasValue(it)) {
                    mNativeAdsMainColor = osa.getColorStateList(it) ?: defaultNativeAdsMainColor
                }
            }
            adsColorAttr.indexOf(R.attr.native_ads_label_text_color).let {
                if (osa.hasValue(it)) {
                    mNativeAdsLabelTextColor = osa.getColorStateList(it) ?: defaultNativeAdsLabelTextColor
                }
            }
            adsColorAttr.indexOf(R.attr.native_ads_body_text_color).let {
                if (osa.hasValue(it)) {
                    mNativeAdsBodyTextColor = osa.getColorStateList(it) ?: defaultNativeAdsBodyTextColor
                }
            }
            adsColorAttr.indexOf(R.attr.native_ads_background_color).let {
                if (osa.hasValue(it)) {
                    mNativeAdsBackgroundColor = osa.getColorStateList(it) ?: defaultNativeAdsBackgroundColor
                }
            }
        }

        attrs?.let { attr ->
            context.obtainStyledAttributes(attr, R.styleable.NativeAdView, 0, 0).use { osa ->
                (R.styleable.NativeAdView_native_ad_type).let {
                    if (osa.hasValue(it)) {
                        mNativeAdType = NativeAdType.fromId(osa.getInt(it, 0))
                    }
                }
                (R.styleable.NativeAdView_native_interstitial_type).let {
                    if (osa.hasValue(it)) {
                        mNativeAdInterstitialType = NativeAdInterstitialType.fromId(osa.getInt(it, 0))
                    }
                }

                (R.styleable.NativeAdView_native_placeholder_type).let {
                    if (osa.hasValue(it)) {
                        mPlaceHolderType = PlaceHolderType.fromId(osa.getInt(it, 1))
                    }
                }

                (R.styleable.NativeAdView_native_placeholder_text_color).let {
                    if (osa.hasValue(it)) {
                        placeHolderBinding.root.setTextColor(osa.getColorStateList(it) ?: context.getColorStateRes(R.color.shimmer_placeholder))
                    }
                }

                (R.styleable.NativeAdView_native_custom_placeholder).let {
                    if (osa.hasValue(it)) {
                        val defValue = 0
                        customPlaceHolderResourceId = osa.getResourceId(it, defValue).takeIf { id -> id != defValue }
                        customPlaceholderView = null
                    }
                }

                (R.styleable.NativeAdView_native_custom_ad_view).let {
                    if (osa.hasValue(it)) {
                        val defValue = 0
                        customAdViewResourceId = osa.getResourceId(it, defValue).takeIf { id -> id != defValue }
                        customAdView = null
                    }
                }

                (R.styleable.NativeAdView_native_auto_load).let {
                    if (osa.hasValue(it)) {
                        mAutoLoad = osa.getBoolean(it, true)
                    }
                }

                (R.styleable.NativeAdView_native_new_ad_request).let {
                    if (osa.hasValue(it)) {
                        mIsMakeNewAdRequest = osa.getBoolean(it, false)
                    }
                }

                (R.styleable.NativeAdView_native_show_placeholder).let {
                    if (osa.hasValue(it)) {
                        mShowPlaceholder = osa.getBoolean(it, true)
                    }
                }

                (R.styleable.NativeAdView_cardBackgroundColor).let {
                    if (osa.hasValue(it)) {
                        mNativeAdsBackgroundColor = osa.getColorStateList(it) ?: defaultNativeAdsBackgroundColor
                    }
                }

                (R.styleable.NativeAdView_cardCornerRadius).let {
                    if (osa.hasValue(it)) {
                        mCardRadius = osa.getDimensionPixelSize(it, 10).toFloat()
                    }
                }

                (R.styleable.NativeAdView_cardElevation).let {
                    if (osa.hasValue(it)) {
                        mCardElevation = osa.getDimensionPixelSize(it, 2).toFloat()
                    }
                }

                (R.styleable.NativeAdView_cardUseCompatPadding).let {
                    if (osa.hasValue(it)) {
                        mCardUseCompatPadding = osa.getBoolean(it, true)
                    }
                }

                (R.styleable.NativeAdView_cardPreventCornerOverlap).let {
                    if (osa.hasValue(it)) {
                        mCardPreventCornerOverlap = osa.getBoolean(it, true)
                    }
                }

                (R.styleable.NativeAdView_contentPadding).let { contentPadding ->
                    (R.styleable.NativeAdView_contentPaddingLeft).let { contentPaddingLeft ->
                        (R.styleable.NativeAdView_contentPaddingRight).let { contentPaddingRight ->
                            (R.styleable.NativeAdView_contentPaddingTop).let { contentPaddingTop ->
                                (R.styleable.NativeAdView_contentPaddingBottom).let { contentPaddingBottom ->
                                    if (osa.hasValue(contentPadding)
                                        || osa.hasValue(contentPaddingLeft)
                                        || osa.hasValue(contentPaddingRight)
                                        || osa.hasValue(contentPaddingTop)
                                        || osa.hasValue(contentPaddingBottom)
                                    ) {
                                        val lDefaultPadding = osa.getDimensionPixelSize(contentPadding, 0)
                                        mCardContentPaddingLeft = osa.getDimensionPixelSize(contentPaddingLeft, lDefaultPadding)
                                        mCardContentPaddingRight = osa.getDimensionPixelSize(contentPaddingRight, lDefaultPadding)
                                        mCardContentPaddingTop = osa.getDimensionPixelSize(contentPaddingTop, lDefaultPadding)
                                        mCardContentPaddingBottom = osa.getDimensionPixelSize(contentPaddingBottom, lDefaultPadding)
                                    }
                                }
                            }
                        }
                    }
                }

                (R.styleable.NativeAdView_strokeColor).let {
                    if (osa.hasValue(it)) {
                        val strokeColor = osa.getColorStateList(it) ?: defaultNativeAdsBackgroundColor
                        mBinding.cvAdContainer.setStrokeColor(strokeColor)
                    }
                }

                (R.styleable.NativeAdView_strokeWidth).let {
                    if (osa.hasValue(it)) {
                        val strokeWidth = osa.getDimensionPixelSize(it, 0)
                        mBinding.cvAdContainer.setStrokeWidth(strokeWidth)
                    }
                }
            }
        }

        if (mCurrentView.isInEditMode) {
            refreshView()
            if (mShowPlaceholder) {
                showPlaceHolders()
            } else {
                showMainView()
            }
            refreshView()
        } else {
            if (NativeAdHelper.isNativeAdEnable()) {
                refreshView()
                showPlaceHolders()
                refreshView()

                var isFromPauseState: Boolean = false

                if (context is LifecycleOwner) {
                    (context as LifecycleOwner).lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            super.onResume(owner)
                            if (isFromPauseState) {
                                isFromPauseState = false
                                updateViewStatus()
                            }
                        }

                        override fun onPause(owner: LifecycleOwner) {
                            super.onPause(owner)
                            isFromPauseState = true
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            super.onDestroy(owner)
                            logE(tag = TAG, message = "onDestroy: ")
                            destroy()
                        }
                    })
                }
            } else {
                mCurrentView.gone
            }
        }
    }

    private fun loadAdWithInternetObserver() {
        isInternetAvailable.observeForever(mInternetObserver)
    }

    private fun loadAd() {
        if (mCurrentView.isVisible) {
            logE(tag = TAG, message = "loadAd: isThisViewLoadNewAd::-> $isThisViewLoadNewAd, mIsMakeNewAdRequest::-> $mIsMakeNewAdRequest")
            if (mIsMakeNewAdRequest) {

                if (!NativeAdHelper.isAnyIndexAdLoadingRunning) {
                    NativeAdHelper.listOfNativeAdsModel.find { it.defaultAdListener != null }?.let { fModel ->

                        if (!isThisViewLoadNewAd) {
                            Log.e(TAG, "onAdClosed: Called")
                            isThisViewLoadNewAd = true
                            CoroutineScope(Dispatchers.Main).launch {
                                callAdClosed()
                                showPlaceHolders()
                            }
                        }

                        fModel.defaultAdListener?.onAdClosed()


                    }
                }
            }

            NativeAdHelper.loadAd(
                fContext = context,
                fNativeAdView = mCurrentView,
                isAddVideoOptions = true,
                adChoicesPlacement = NativeAdOptions.ADCHOICES_TOP_RIGHT,
//            mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_UNKNOWN, // test ad with video
//                mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY, // test ad with video
                mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE, // test ad with default like google & flood it
//            mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT, // test ad with default like google & flood it
//            mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE, // test ad with info icon
                onAdLoaded = { nativeAd ->
                    Log.e(TAG, "onAdLoaded: 1")
                    if (isThisViewLoadNewAd) {
                        isThisViewLoadNewAd = false
                        Log.e(TAG, "onAdLoaded: 2")
                        CoroutineScope(Dispatchers.Main).launch {
                            showMainView(fNativeAd = nativeAd)
                        }
                    }
                },
                onAdClosed = {
                    Log.e(TAG, "onAdClosed: 1")
                    if (!isThisViewLoadNewAd) {
                        Log.e(TAG, "onAdClosed: 2")
                        isThisViewLoadNewAd = true
                        CoroutineScope(Dispatchers.Main).launch {
                            callAdClosed()
                            showPlaceHolders()
                        }
                    }
                },
                onAdFailed = {
                    Log.e(TAG, "onAdFailed: 1")
                    if (!isThisViewLoadNewAd) {
                        Log.e(TAG, "onAdFailed: 2")
                        isThisViewLoadNewAd = true
                        CoroutineScope(Dispatchers.Main).launch {
                            callAdFailed()
                            showPlaceHolders()
                        }
                    }
                }
            )
        }
    }

    private fun updateParentViewHeight(afterUpdateHeight: () -> Unit) {
        if (mNativeAdType == NativeAdType.INTERSTITIAL_NATIVE) {
            mCurrentView.post {
                val params = mCurrentView.layoutParams
                params.height = when (mNativeAdInterstitialType) {
                    NativeAdInterstitialType.WEBSITE -> LayoutParams.MATCH_PARENT
                    NativeAdInterstitialType.APP_STORE -> LayoutParams.WRAP_CONTENT
//                    NativeAdInterstitialType.FULL_SCREEN -> LayoutParams.MATCH_PARENT
                }
                mCurrentView.layoutParams = params
                refreshView()
                afterUpdateHeight.invoke()
            }
        } else {
            afterUpdateHeight.invoke()
        }
    }

    private fun showMainView(fNativeAd: com.google.android.gms.ads.nativead.NativeAd? = null) {
        if (mNativeAdType == NativeAdType.INTERSTITIAL_NATIVE) {
            fNativeAd?.let {
                mNativeAdInterstitialType = if (it.starRating != null && it.price != null && it.store != null) {
                    NativeAdInterstitialType.APP_STORE
                } else {
                    NativeAdInterstitialType.WEBSITE
                }
            }
        }

        updateParentViewHeight {
            setCardParams(isLoading = false)

            if (mNativeAdType == NativeAdType.INTERSTITIAL_NATIVE) {
                mBinding.cvAdContainer.apply {
                    when (mNativeAdInterstitialType) {
//                        NativeAdInterstitialType.FULL_SCREEN,
                        NativeAdInterstitialType.WEBSITE -> {
                            this.setCardBackgroundColor(mNativeAdsBackgroundColor)
                            this.cardElevation = 0.0f
                            this.useCompatPadding = false
                            this.radius = 0.0f
                            this.setContentPadding(0, 0, 0, 0)
                            this.removeMargin
                        }

                        NativeAdInterstitialType.APP_STORE -> {
                            this.setCardBackgroundColor(Color.WHITE)
                            this.addMargin(fMargin = 8.0f)
                        }
                    }
                }
            }

            if (mCurrentView.isInEditMode && !mShowPlaceholder) {
                showMainViewInEditMode()
            } else {
                fNativeAd?.let {
                    showMainViewAfterLoading(fNativeAd = it)
                } ?: kotlin.run {
                    showPlaceHolders()
                }
            }

            refreshView()
        }
    }

    private fun showMainViewInEditMode() {
        mBinding.nativeAdContainer.let { container ->
            inflateAdViewAccordingType.also { view ->
                if (mNativeAdType != NativeAdType.CUSTOM) {
                    setThemeOnView(inflateAdView = view)
                }
                container.removeAllViews()
                container.addView(view)
                container.visible
            }
        }
    }

    private fun View.attachToAdMobNativeAdLayout(): com.google.android.gms.ads.nativead.NativeAdView {
        this.adMediaContainerFrameLayout?.let { ly ->
            com.google.android.gms.ads.nativead.MediaView(mCurrentView.context).apply {
                this.id = R.id.ad_media
            }.also {
                ly.addView(it, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            }
        }

        val lNativeAdLayout: com.google.android.gms.ads.nativead.NativeAdView = com.google.android.gms.ads.nativead.NativeAdView(mCurrentView.context)
        lNativeAdLayout.addView(this)

        return lNativeAdLayout
    }

    private fun showMainViewAfterLoading(fNativeAd: com.google.android.gms.ads.nativead.NativeAd) {
        mBinding.nativeAdContainer.let { container ->
            container.gone
            inflateAdViewAccordingType.attachToAdMobNativeAdLayout().also { view ->
                if (mNativeAdType != NativeAdType.CUSTOM) {
                    setThemeOnView(inflateAdView = view)
                }
                populateNativeAdView(
                    fNativeAd = fNativeAd,
                    fAdView = view,
                    onAdPopulated = {
                        logE(tag = TAG, message = "showMainViewAfterLoading: ")
                        container.removeAllViews()
                        container.addView(view)
                        callAdLoaded()
                        mBinding.heightInfoContainer.gone
                        container.visible
                    }
                )
            }
        }
    }

    private fun addHeightInfo() {
        updateParentViewHeight {
            addHeightView()
            refreshView()
        }
    }

    private fun addHeightView() {
        mBinding.heightInfoContainer.let { container ->
            inflateAdViewAccordingType.also { view ->
                container.removeAllViews()
                container.addView(view)
                container.invisible
            }
        }
    }

    private fun setCardParams(isLoading: Boolean) {
        mBinding.cvAdContainer.apply {
            this.setCardBackgroundColor(if (isLoading) ColorStateList.valueOf(Color.TRANSPARENT) else mNativeAdsBackgroundColor)
            this.cardElevation = if (isLoading) 0.0f else mCardElevation
            this.radius = mCardRadius
            this.useCompatPadding = mCardUseCompatPadding
            this.preventCornerOverlap = mCardPreventCornerOverlap
            this.setContentPadding(
                if (isLoading) 0 else mCardContentPaddingLeft,
                if (isLoading) 0 else mCardContentPaddingTop,
                if (isLoading) 0 else mCardContentPaddingRight,
                if (isLoading) 0 else mCardContentPaddingBottom
            )
        }
    }

    private fun setThemeOnView(inflateAdView: View) {
        inflateAdView.adInfoTextView?.let { view ->
            view.setBackgroundColor(mNativeAdsMainColor.defaultColor)
            if (mNativeAdType != NativeAdType.INTERSTITIAL_NATIVE || mNativeAdInterstitialType != NativeAdInterstitialType.APP_STORE) {
                view.setTextColor(mNativeAdsLabelTextColor)
            }
        }

        inflateAdView.adAdvertiserTextView?.setTextColor(mNativeAdsBodyTextColor)

        inflateAdView.adBodyTextView?.setTextColor(mNativeAdsBodyTextColor)

        inflateAdView.adHeadlineTextView?.let { view ->
            if (mNativeAdType == NativeAdType.INTERSTITIAL_NATIVE) {
                when (mNativeAdInterstitialType) {
                    NativeAdInterstitialType.WEBSITE -> view.setTextColor(mNativeAdsBodyTextColor)
                    NativeAdInterstitialType.APP_STORE -> {}
//                    NativeAdInterstitialType.FULL_SCREEN -> {}
//                    NativeAdInterstitialType.FULL_SCREEN -> view.setTextColor(mNativeAdsMainColor)
                }
            } else {
                view.setTextColor(mNativeAdsMainColor)
            }
        }

        inflateAdView.adPriceTextView?.let { view ->
            if (mNativeAdType == NativeAdType.INTERSTITIAL_NATIVE) {
                when (mNativeAdInterstitialType) {
                    NativeAdInterstitialType.WEBSITE -> view.setTextColor(mNativeAdsMainColor)
                    NativeAdInterstitialType.APP_STORE -> {}
//                    NativeAdInterstitialType.FULL_SCREEN -> {}
//                    NativeAdInterstitialType.FULL_SCREEN -> view.setTextColor(mNativeAdsBodyTextColor)
                }
            } else {
                view.setTextColor(mNativeAdsBodyTextColor)
            }
        }

        inflateAdView.adStoreTextView?.let { view ->
            if (mNativeAdType != NativeAdType.INTERSTITIAL_NATIVE || mNativeAdInterstitialType != NativeAdInterstitialType.APP_STORE) {
                view.setTextColor(mNativeAdsBodyTextColor)
            }
        }

        inflateAdView.adStarsRatingBar?.let { view ->
            if (mNativeAdType != NativeAdType.INTERSTITIAL_NATIVE) {
                view.progressTintList = mNativeAdsMainColor
            }
//                view.progressTintList = mNativeAdsBodyTextColor
        }

        inflateAdView.adStarsRatingTextView?.let { view ->
            if (mNativeAdType != NativeAdType.INTERSTITIAL_NATIVE) {
                view.setTextColor(mNativeAdsMainColor)
            }
//                view.setTextColor(mNativeAdsBodyTextColor)
        }

        inflateAdView.adAppIconImageView?.setBackgroundColor(Color.WHITE)

        inflateAdView.adMediaView?.setBackgroundColor(Color.TRANSPARENT)

        inflateAdView.adCallToActionTextView?.let { view ->
            view.setBackgroundColor(mNativeAdsMainColor.defaultColor)
            if (mNativeAdType != NativeAdType.INTERSTITIAL_NATIVE || mNativeAdInterstitialType != NativeAdInterstitialType.APP_STORE) {
                view.setTextColor(mNativeAdsLabelTextColor)
            }
        }


        if (!mCurrentView.isInEditMode) {
            if (mNativeAdType != NativeAdType.INTERSTITIAL_NATIVE) {
                inflateAdView.adMediaContainerFrameLayout?.setBackgroundColor(mNativeAdsBackgroundColor.defaultColor)
            } else {
                inflateAdView.adMediaContainerFrameLayout?.setBackgroundColor(Color.WHITE)
            }
        }
    }

    private fun startLoading(fView: View) {
        fView.interstitialNativeProgressImageView?.apply {
            RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                this.repeatCount = Animation.INFINITE
                this.duration = 1000
                this.interpolator = LinearInterpolator()
            }.also { rotate ->
                this.setColorFilter(mNativeAdsMainColor.defaultColor)
                this.startAnimation(rotate)
            }
        }
    }

    private fun showPlaceHolders() {
        addHeightInfo()

        setCardParams(isLoading = true)

        val inflatedView = when (mPlaceHolderType) {
            PlaceHolderType.NONE -> null
            PlaceHolderType.SHIMMER -> {
                mBinding.cvAdContainer.setCardBackgroundColor(setColorAlpha(baseColor = context.getColor(R.color.shimmer_placeholder)))
                inflateAdViewWithShimmer
            }

            PlaceHolderType.TEXT -> {
                mBinding.cvAdContainer.setCardBackgroundColor(setColorAlpha(baseColor = context.getColor(R.color.shimmer_placeholder)))
                placeHolderBinding.root
            }

            PlaceHolderType.CUSTOM -> {
                customPlaceHolderResourceId?.let { id ->
                    this.inflateLayout(resource = id)
                } ?: customPlaceholderView ?: kotlin.run {
                    throw RuntimeException("custom placeholder NullPointerException")
                }
            }
        }

        mBinding.nativeAdContainer.let { container ->
            inflatedView?.let {
                it.apply {
                    this.layoutParams = actualLayoutParams
                }.also { view ->
                    container.removeAllViews()
                    container.addView(view)
                    container.visible
                }

                startLoading(fView = it)
            }
        }

        refreshView()
    }

    private fun populateNativeAdView(
        fNativeAd: com.google.android.gms.ads.nativead.NativeAd,
        fAdView: com.google.android.gms.ads.nativead.NativeAdView,
        onAdPopulated: () -> Unit
    ) {
        var isPopulatedMediaView = false
        with(fAdView) {
            this.advertiserView = fAdView.adAdvertiserTextView
            this.bodyView = fAdView.adBodyTextView
            this.headlineView = fAdView.adHeadlineTextView
            this.priceView = fAdView.adPriceTextView
            this.storeView = fAdView.adStoreTextView
            this.starRatingView = fAdView.adStarsRatingBar
            this.iconView = fAdView.adAppIconImageView
            this.mediaView = fAdView.adMediaView
            this.imageView = fAdView.adBackgroundImageView
            this.callToActionView = fAdView.adCallToActionTextView

            this.mediaView?.let { fView ->
                fView.gone
                fNativeAd.mediaContent?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: Set Media View::-> $fData")
                    fView.mediaContent = fData
//                    fView.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
                    fView.visible
                    isPopulatedMediaView = true
                } ?: kotlin.run {
                    logI(tag = TAG, message = "populateNativeAdView: No Media View Found")
                    populateNativeAdView(fNativeAd = fNativeAd, fAdView = fAdView, onAdPopulated = onAdPopulated)
                }
            } ?: kotlin.run {
                logI(tag = TAG, message = "populateNativeAdView: Media View NULL")
                isPopulatedMediaView = true
            }

            if (isPopulatedMediaView) {

                this.advertiserView?.let { fView ->
                    fView.gone
                    fNativeAd.advertiser?.let { fData ->
                        (fView as TextView).text = fData
                        fView.isSelected = true
                        fView.visible
                    }
                }

                this.bodyView?.let { fView ->
                    fView.gone
                    fNativeAd.body?.let { fData ->
                        (fView as TextView).text = fData
                        fView.isSelected = true
                        fView.visible
                    }
                }

                this.headlineView?.let { fView ->
                    fView.gone
                    fNativeAd.headline?.let { fData ->
                        (fView as TextView).text = fData
                        fView.isSelected = true
                        fView.visible
                    }
                }

                this.priceView?.let { fView ->
                    fView.gone
                    fNativeAd.price?.let { fData ->
                        (fView as TextView).text = fData
                        fView.isSelected = true
                        fView.visible
                    }
                }

                this.storeView?.let { fView ->
                    fAdView.adStoreIconImageView?.gone
                    with(fView as TextView) {
                        this.gone
                        fNativeAd.store?.let { fData ->
                            this.text = fData
                            this.isSelected = true
                            this.visible
                            fAdView.adStoreIconImageView?.beVisibleIf(fData.equals("Google Play", false))
                        }
                    }
                }

                this.starRatingView?.let { fView ->
                    fAdView.adStarsRatingTextView?.gone
                    fView.gone
                    fNativeAd.starRating?.let { fData ->
                        (fView as RatingBar).rating = fData.toFloat()
                        fView.visible

                        fAdView.adStarsRatingTextView?.let { txtRating ->
                            txtRating.text = fData.toFloat().toString()
                            txtRating.visible
                        }
                    }
                }

                this.iconView?.let { fView ->
                    fView.gone
                    var lData: Drawable? = null
                    when {
                        fNativeAd.icon != null -> {
                            fNativeAd.icon?.drawable?.let { fData ->
                                lData = fData
                            }
                        }

                        fNativeAd.images.size > 0 -> {
                            fNativeAd.images[0]?.drawable?.let { fData ->
                                lData = fData
                            }
                        }

                        else -> lData = null
                    }

                    lData?.let {
                        if (fView is ImageView) {
                            fView.setImageDrawable(it)
                            fView.scaleType = ImageView.ScaleType.CENTER_CROP
                            fView.visible
                        }
                    }
                }

                this.imageView?.let { fView ->
                    fView.gone
                    if (fNativeAd.images.size > 0) {
                        fNativeAd.images[0]?.drawable?.let { fData ->
                            Bitmap.createBitmap(fData.intrinsicWidth, fData.intrinsicHeight, Bitmap.Config.ARGB_8888).apply {
                                Canvas(this).apply {
                                    fData.setBounds(0, 0, this.width, this.height)
                                    fData.draw(this)
                                }
                            }.also {
                                BlurImage().load(it)
                                    .radius(3f)
                                    .withCPU()
                                    .into((fView as ImageView))
                            }
                            fView.visible
                        }
                    }
                }

                this.callToActionView?.let { fView ->
                    fView.gone
                    fNativeAd.callToAction?.let { fData ->
                        when (fView) {
                            is Button -> fView.text = fData.toCamelCase
                            is androidx.appcompat.widget.AppCompatTextView -> fView.text = fData.toCamelCase
                            is TextView -> fView.text = fData.toCamelCase
                        }
                        fView.isSelected = true
                        fView.visible
                    }
                }

                this.adCallToCloseTextView?.let { fView ->
                    fView.setOnClickListener {
                        logE(tag = TAG, message = "populateNativeAdView: adCallToCloseTextView onClick")
                        callAdCustomClosed()
                    }
                }

                /*if (mNativeAdType == NativeAdType.INTERSTITIAL_NATIVE && mNativeAdInterstitialType == NativeAdInterstitialType.FULL_SCREEN) {
                    val isStoreVisible: Boolean = fNativeAd.store != null
                    val isRatingVisible: Boolean = fNativeAd.starRating != null
                    val isPriceVisible: Boolean = fNativeAd.price != null

                    if (isStoreVisible || isRatingVisible || isPriceVisible) {
                        fNativeAd.advertiser?.let {
                            this.advertiserView?.visible
                            this.bodyView?.gone
                        } ?: fNativeAd.body?.let {
                            this.advertiserView?.gone
                            this.bodyView?.visible
                        } ?: kotlin.run {
                            this.advertiserView?.gone
                            this.bodyView?.gone
                        }
                    } else {
                        this.findViewById<ConstraintLayout>(R.id.cl_store_rating_price)?.gone
                    }
                }*/

                /*fNativeAd.advertiser?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: advertiser::-> $fData")
                }

                fNativeAd.body?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: body::-> $fData")
                }

                fNativeAd.headline?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: headline::-> $fData")
                }

                fNativeAd.price?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: price::-> $fData")
                }

                fNativeAd.store?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: store::-> $fData")
                }

                fNativeAd.starRating?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: starRating::-> $fData")
                }

                fNativeAd.callToAction?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: callToAction::-> $fData")
                }

                fNativeAd.icon?.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: icon::-> $fData")
                }

                fNativeAd.images.let { fData ->
                    logI(tag = TAG, message = "populateNativeAdView: images::-> ${fData.size}")
                    if (fData.isNotEmpty()) {
                        fData.forEach {
                            logI(tag = TAG, message = "populateNativeAdView: images::-> $it")
                        }
                    }
                }*/


                this.setNativeAd(fNativeAd)
                onAdPopulated.invoke()
            }
        }
    }

    private fun callAdLoaded() {
        Handler(Looper.getMainLooper()).postDelayed({
            mListener?.onAdLoaded() ?: kotlin.run { callAdLoaded() }
        }, 100)
    }

    private fun callAdClosed() {
        Handler(Looper.getMainLooper()).postDelayed({
            mListener?.onAdClosed() ?: kotlin.run { callAdClosed() }
        }, 100)
    }

    private fun callAdCustomClosed() {
        Handler(Looper.getMainLooper()).postDelayed({
            mListener?.onAdCustomClosed() ?: kotlin.run { callAdCustomClosed() }
        }, 100)
    }

    private fun callAdFailed() {
        Handler(Looper.getMainLooper()).postDelayed({
            mListener?.onAdFailed() ?: kotlin.run { callAdFailed() }
        }, 100)
    }

    private fun destroy() {
//        mCurrentView.removeAllViews()
        isInternetAvailable.removeObserver(mInternetObserver)
        NativeAdHelper.removeView(fNativeAdView = mCurrentView)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logE(tag = TAG, message = "onAttachedToWindow: isThisViewLoadNewAd::-> $isThisViewLoadNewAd")
        if (mAutoLoad) {
            loadAdWithInternetObserver()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
        logE(tag = TAG, message = "onDetachedFromWindow: isThisViewLoadNewAd::-> $isThisViewLoadNewAd")
    }

    private fun updateViewStatus() {
        if (NativeAdHelper.isNativeAdEnable()) {
            if (!NativeAdHelper.isAnyIndexAdLoadingRunning) {
                NativeAdHelper.listOfNativeAdsModel.find { it.loadedAd != null }?.let { fModel ->
                    logE(tag = TAG, message = "onResume: ")
                    isThisViewLoadNewAd = false
                    showMainView(fNativeAd = fModel.loadedAd)
                } ?: kotlin.run {
                    showPlaceHolders()
                }
            } else {
                showPlaceHolders()
            }

        } else {
            mCurrentView.gone
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        logE(tag = TAG, message = "onVisibilityChanged: visibility::-> `${visibility}`")
        if (!mCurrentView.isInEditMode) {
            if (visibility == View.VISIBLE) {
                mCurrentView.beVisibleIf(NativeAdHelper.isNativeAdEnable())
            }
        }
    }

    /*fun updateInterstitialType() {
        refreshView()
        mNativeAdInterstitialType = if (mNativeAdInterstitialType == NativeAdInterstitialType.WEBSITE) {
            NativeAdInterstitialType.APP_STORE
        } else {
            NativeAdInterstitialType.WEBSITE
        }

        isThisViewLoadNewAd = true
        mInternetObserver.onChanged(isInternetAvailable.value ?: false)
        refreshView()
    }*/

    fun getNativeAdInterstitialType(): NativeAdInterstitialType = mNativeAdInterstitialType

    fun setOnNativeAdViewListener(fListener: OnNativeAdViewListener) {
        mListener = fListener
    }
}