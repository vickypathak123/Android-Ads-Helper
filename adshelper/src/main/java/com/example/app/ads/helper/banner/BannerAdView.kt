package com.example.app.ads.helper.banner

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.content.res.use
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.app.ads.helper.AdMobAdsListener
import com.example.app.ads.helper.AdStatusModel
import com.example.app.ads.helper.PlaceHolderType
import com.example.app.ads.helper.R
import com.example.app.ads.helper.adRequestBuilder
import com.example.app.ads.helper.beVisibleIf
import com.example.app.ads.helper.clearAll
import com.example.app.ads.helper.databinding.PlaceholderShimmerBinding
import com.example.app.ads.helper.databinding.PlaceholderTextBinding
import com.example.app.ads.helper.displayDensity
import com.example.app.ads.helper.displayWidth
import com.example.app.ads.helper.getColorStateRes
import com.example.app.ads.helper.gone
import com.example.app.ads.helper.inflateLayout
import com.example.app.ads.helper.inflater
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAppNotPurchased
import com.example.app.ads.helper.isInternetAvailable
import com.example.app.ads.helper.isOnline
import com.example.app.ads.helper.is_enable_banner_ad_from_remote_config
import com.example.app.ads.helper.is_need_to_load_multiple_banner_ad_request
import com.example.app.ads.helper.list_of_admob_banner_ads
import com.example.app.ads.helper.list_of_admob_splash_banner_ads
import com.example.app.ads.helper.logE
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.setColorAlpha
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError


/**
 * @author Akshay Harsoda
 * @since 26 Dec 2022
 * @updated 27 Jun 2024
 *
 * BannerAdView.kt - Simple view which has load and handle your Banner AD data
 */
class BannerAdView : FrameLayout {

    @Suppress("PrivatePropertyName")
    private val TAG = "Admob_${javaClass.simpleName}"

    private val mCurrentView: BannerAdView get() = this@BannerAdView

    private val mInternetObserver = Observer<Boolean> {
        logE(tag = TAG, message = "onInternetChanged: $it")
        if (it) {
            if (!mCurrentView.isInEditMode) {
                if (!isAnyIndexLoaded) {
                    logE(tag = TAG, message = "onInternetChanged: isInternetAvailable -> Now Load Ad")
                    loadAd()
                }
            }
        } /*else {
            if (mCurrentView.tag is String) {
                when (mCurrentView.tag) {
                    "placeholder_loaded" -> showPlaceHolders()

                    "ad_loaded" -> {
                        showBannerAd(beVisible = true)
                    }

                    else -> showBannerAd(beVisible = false)
                }
            }
        }*/
    }

    private val shimmerBinding: PlaceholderShimmerBinding by lazy {
        PlaceholderShimmerBinding.inflate(context.inflater, mCurrentView, false).apply {
            this.root.layoutParams = actualLayoutParams
        }
    }

    private val placeHolderBinding: PlaceholderTextBinding by lazy {
        PlaceholderTextBinding.inflate(context.inflater, mCurrentView, false).apply {
            this.root.layoutParams = actualLayoutParams
            this.root.setBackgroundColor(setColorAlpha(baseColor = context.getColor(R.color.shimmer_placeholder)))
        }
    }

    private var mBannerAdType: BannerAdType = BannerAdType.NORMAL
    private var mBannerAdSize: BannerAdSize = BannerAdSize.BANNER
    private var mPlaceHolderType: PlaceHolderType = PlaceHolderType.SHIMMER
    private var mAutoLoad: Boolean = true

    @LayoutRes
    private var customPlaceHolderResourceId: Int? = null
    private var customPlaceholderView: View? = null


    //<editor-fold desc="Ad & View Size [Width & Height]">
    private val actualLayoutParams: LayoutParams
        get() {
            var lWidth: Int
            var lHeight: Int

            actualAdSize.let {
                val displayDensity = context.displayDensity
                lWidth = (it.width * displayDensity).toInt()
                lHeight = (it.height * displayDensity).toInt()
            }

            val layoutParams = LayoutParams(lWidth, lHeight)
            layoutParams.gravity = Gravity.CENTER

            return layoutParams
        }

    private val adaptiveAdSize: AdSize
        get() {
            val displayDensity = context.displayDensity
            val displayWidth = context.displayWidth
            var adWidthPixels = mCurrentView.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = displayWidth.toFloat()
            }
            val adWidth = (adWidthPixels / displayDensity).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

    private val actualAdSize: AdSize
        get() {
            return when (mBannerAdSize) {
                BannerAdSize.BANNER -> AdSize.BANNER
                BannerAdSize.LARGE_BANNER -> AdSize.LARGE_BANNER
                BannerAdSize.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
                BannerAdSize.FULL_BANNER -> AdSize.FULL_BANNER
                BannerAdSize.LEADERBOARD -> AdSize.LEADERBOARD
                BannerAdSize.ADAPTIVE_BANNER -> adaptiveAdSize
            }
        }
    //</editor-fold>

    //<editor-fold desc="Data of Ads ID, Ad-Model & Loading Related">
    private var isThisAdClicked: Boolean = false
    private var isAnyIndexLoaded = false
    private var isAnyIndexAlreadyLoaded = false
    private var mAdIdPosition: Int = -1

    private val listOfBannerAdsModel: ArrayList<AdStatusModel<AdView>> = ArrayList()

    private val list_of_banner_ads: ArrayList<AdStatusModel<AdView>>
        get() {
            if (mBannerAdSize == BannerAdSize.ADAPTIVE_BANNER && mBannerAdType == BannerAdType.SPLASH) {
                logE(tag = TAG, message = "list_of_banner_ads: SPLASH BANNER")
                return list_of_admob_splash_banner_ads
            } else {
                logE(tag = TAG, message = "list_of_banner_ads: NON SPLASH BANNER")
                return list_of_admob_banner_ads
            }
        }

    private val isLastIndex: Boolean get() = (mAdIdPosition + 1) >= listOfBannerAdsModel.size

    private val isAdsIDSated: Boolean
        get() {
            if (listOfBannerAdsModel.isNotEmpty()) {
                return true
            } else {
                throw RuntimeException("set Banner Ad Id First")
            }
        }
    //</editor-fold>

    private fun getBannerAdModel(
        isNeedToLoadMultipleRequest: Boolean,
        onFindModel: (index: Int, fAdModel: AdStatusModel<AdView>) -> Unit
    ) {
        if (isAdsIDSated) {
            if (isNeedToLoadMultipleRequest) {
                logE(TAG, "getBannerAdModel: Load Multiple Request , $mBannerAdType, $mBannerAdSize")
                listOfBannerAdsModel.forEachIndexed { index, adStatusModel ->
                    onFindModel.invoke(index, adStatusModel)
                }
            } else {
                if (listOfBannerAdsModel.any { it.isAdLoadingRunning }) {
                    logE(tag = TAG, message = "getBannerAdModel: listOfBannerAdsModel.any { it.isAdLoadingRunning } == true, $mAdIdPosition")
                } else {
                    mAdIdPosition = if (mAdIdPosition < listOfBannerAdsModel.size) {
                        if (mAdIdPosition == -1) {
                            0
                        } else {
                            (mAdIdPosition + 1)
                        }
                    } else {
                        0
                    }

                    logE(TAG, "getBannerAdModel: AdIdPosition -> $mAdIdPosition, $mBannerAdType, $mBannerAdSize")

                    if (mAdIdPosition >= 0 && mAdIdPosition < listOfBannerAdsModel.size) {
                        onFindModel.invoke(mAdIdPosition, listOfBannerAdsModel[mAdIdPosition])
                    } else {
                        mAdIdPosition = -1
                    }
                }
            }
        }
    }

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
        attrs?.let { attr ->
            context.obtainStyledAttributes(attr, R.styleable.BannerAdView, 0, 0).use { osa ->
                (R.styleable.BannerAdView_banner_ad_size).let {
                    if (osa.hasValue(it)) {
                        mBannerAdSize = BannerAdSize.fromId(osa.getInt(it, 0))
                    }
                }
                (R.styleable.BannerAdView_banner_ad_type).let {
                    if (osa.hasValue(it)) {
                        mBannerAdType = BannerAdType.fromId(osa.getInt(it, 0))
                        if (mBannerAdSize != BannerAdSize.ADAPTIVE_BANNER) {
                            mBannerAdType = BannerAdType.NORMAL
                        }
                    }
                }
                (R.styleable.BannerAdView_banner_placeholder_type).let {
                    if (osa.hasValue(it)) {
                        mPlaceHolderType = PlaceHolderType.fromId(osa.getInt(it, 1))
                    }
                }
                (R.styleable.BannerAdView_banner_placeholder_text_color).let {
                    if (osa.hasValue(it)) {
                        placeHolderBinding.root.setTextColor(
                            osa.getColorStateList(it) ?: context.getColorStateRes(R.color.shimmer_placeholder)
                        )
                    }
                }
                (R.styleable.BannerAdView_banner_custom_placeholder).let {
                    if (osa.hasValue(it)) {
                        val defValue = 0
                        customPlaceHolderResourceId = osa.getResourceId(it, defValue).takeIf { id -> id != defValue }
                        customPlaceholderView = null
                    }
                }
                (R.styleable.BannerAdView_banner_auto_load).let {
                    if (osa.hasValue(it)) {
                        mAutoLoad = osa.getBoolean(it, true)
                    }
                }
            }
        }

        if (mCurrentView.isInEditMode) {
            refreshView()
            showPlaceHolders()
            refreshView()
        } else {
            if (isBannerAdEnable()) {
                refreshView()
                showPlaceHolders()
                refreshView()

                listOfBannerAdsModel.clearAll()

                list_of_banner_ads.forEach {
                    listOfBannerAdsModel.add(
                        AdStatusModel(
                            adID = it.adID
                        )
                    )
                }

                if (context is LifecycleOwner) {
                    (context as LifecycleOwner).lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            super.onResume(owner)
                            resume()
                        }

                        override fun onPause(owner: LifecycleOwner) {
                            super.onPause(owner)
                            pause()
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


//        showBannerAd(beVisible = false)
//        refreshView()
//        showPlaceHolders()
//        refreshView()
    }

    private fun loadAdWithInternetObserver() {
        isInternetAvailable.observeForever(mInternetObserver)

//        if (!isInternetAvailable.hasObserver(mInternetObserver)) {
//            logE(tag = TAG, message = "loadAdWithInternetObserver: START Observe, $mBannerAdSize $mBannerAdType")
//            isInternetAvailable.observeForever(mInternetObserver)
//        }
    }

    private fun showPlaceHolders() {
//        mCurrentView.apply {
//            this.removeAllViews()

        /*when (mPlaceHolderType) {
            PlaceHolderType.NONE -> {}
            PlaceHolderType.SHIMMER -> {
                shimmerBinding.root.apply {
                    this.layoutParams = actualLayoutParams
                }.also { view ->
                    mCurrentView.tag = "placeholder_loaded"
                    this.addView(view)
                }
            }

            PlaceHolderType.TEXT -> {
                placeHolderBinding.root.apply {
                    this.layoutParams = actualLayoutParams
                }.also { view ->
                    mCurrentView.tag = "placeholder_loaded"
                    this.addView(view)
                }
            }

            PlaceHolderType.CUSTOM -> {
                customPlaceHolderResourceId?.let { id ->
                    this.inflateLayout(resource = id).apply {
                        this.layoutParams = actualLayoutParams
                    }.also { view ->
                        mCurrentView.tag = "placeholder_loaded"
                        this.addView(view)
                    }
                } ?: customPlaceholderView?.let { placeHolderView ->
                    placeHolderView.apply {
                        this.layoutParams = actualLayoutParams
                    }.also { view ->
                        mCurrentView.tag = "placeholder_loaded"
                        this.addView(view)
                    }
                } ?: kotlin.run {
                    throw RuntimeException("custom placeholder NullPointerException")
                }
            }
        }*/

        val inflatedView = when (mPlaceHolderType) {
            PlaceHolderType.NONE -> null
            PlaceHolderType.SHIMMER -> shimmerBinding.root
            PlaceHolderType.TEXT -> placeHolderBinding.root

            PlaceHolderType.CUSTOM -> {
                customPlaceHolderResourceId?.let { id ->
                    this.inflateLayout(resource = id)
                } ?: customPlaceholderView ?: kotlin.run {
                    throw RuntimeException("custom placeholder NullPointerException")
                }
            }
        }

        mCurrentView.let { container ->
            inflatedView?.let {
                it.apply {
                    this.layoutParams = actualLayoutParams
                }.also { view ->
                    container.removeAllViews()
                    container.addView(view)
                }
            }
        }

//            showBannerAd(beVisible = mPlaceHolderType != PlaceHolderType.NONE)

        refreshView()
//        }
    }


    private fun loadNewAd(
        fContext: Context,
        fModel: AdStatusModel<AdView>,
        fAdSize: AdSize,
        fBannerAdType: BannerAdType,
        fIndex: Int
    ) {
        if (!(listOfBannerAdsModel.any { it.isAdLoadingRunning })) {
            logI(tag = TAG, message = "loadNewAd: Index -> $fIndex, $mBannerAdType, $mBannerAdSize\nAdID -> ${fModel.adID}")

            fModel.isAdLoadingRunning = true

            AdView(fContext).apply {
                this.adUnitId = fModel.adID
                this.setAdSize(fAdSize)
                this.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        logI(tag = TAG, message = "loadNewAd: onAdLoaded: Index -> $fIndex, $mBannerAdType, $mBannerAdSize")
                        fModel.isAdLoadingRunning = false
                        fModel.loadedAd = this@apply
                        fModel.listener?.onAdLoaded(this@apply)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        logE(
                            tag = TAG,
                            message = "loadNewAd: onAdFailedToLoad: Index -> $fIndex, $mBannerAdType, $mBannerAdSize\nAd failed to load -> \nresponseInfo::${adError.responseInfo}\nErrorCode::${adError.code}\nErrorMessage::${adError.message}"
                        )
                        if (this@apply.adSize == actualAdSize) {
                            this@apply.destroy()
                            fModel.apply {
                                this.isAdLoadingRunning = false
                                this.loadedAd?.destroy()
                                this.loadedAd = null
                                this.listener?.onAdFailed()
                            }
                        }
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        logI(
                            tag = TAG,
                            message = "loadNewAd: onAdClosed: Index -> $fIndex, $mBannerAdType, $mBannerAdSize"
                        )

                        if (isThisAdClicked) {
                            this@apply.destroy()
                            fModel.apply {
                                this.loadedAd?.destroy()
                                this.loadedAd = null
                                isAnyAdOpen = false
                                isThisAdClicked = false
                                this.listener?.onAdClosed()
                            }
                        }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        logI(
                            tag = TAG,
                            message = "loadNewAd: onAdClicked: Index -> $fIndex, $mBannerAdType, $mBannerAdSize"
                        )

                        isAnyAdOpen = true
                        isThisAdClicked = true
                    }
                }.also { fModel.defaultAdListener = it }

                val extras: Bundle? = when (fBannerAdType) {
                    BannerAdType.SPLASH -> Bundle().apply {
                        putString("is_splash_banner", "true")
                    }

                    BannerAdType.COLLAPSIBLE_BOTTOM -> Bundle().apply {
                        putString("collapsible", "bottom")
                    }

                    BannerAdType.COLLAPSIBLE_TOP -> Bundle().apply {
                        putString("collapsible", "top")
                    }

                    else -> null
                }

                val adRequest = extras?.let {
                    AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, it).build()
                } ?: run {
                    adRequestBuilder
                }

                this.loadAd(adRequest)
            }
        }
    }

    private fun requestWithIndex(
        fContext: Context,
        fModel: AdStatusModel<AdView>,
        fAdSize: AdSize,
        fBannerAdType: BannerAdType,
        fIndex: Int,
        onAdLoaded: () -> Unit,
        onAdClosed: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (isOnline
            && !isBannerAdAvailable()
            && fModel.loadedAd == null
            && !fModel.isAdLoadingRunning
        ) {
            loadNewAd(
                fContext = fContext,
                fModel = fModel.apply {
                    this.listener = object : AdMobAdsListener<AdView> {

                        override fun onAdLoaded(fLoadedAd: AdView) {
                            super.onAdLoaded(fLoadedAd)
                            mAdIdPosition = -1
                            logI(
                                tag = TAG,
                                message = "requestWithIndex: onAdLoaded: Index -> $fIndex, $mBannerAdType, $mBannerAdSize, ${fLoadedAd.adSize}"
                            )
                            if (!isAnyIndexLoaded) {
                                isAnyIndexLoaded = true
                                onAdLoaded.invoke()
                            }
                        }

                        override fun onAdClosed(isShowFullScreenAd: Boolean) {
                            super.onAdClosed(isShowFullScreenAd)
                            onAdClosed.invoke()
                        }

                        override fun onAdFailed() {
                            super.onAdFailed()
                            onAdFailed.invoke()
                        }
                    }
                },
                fAdSize = fAdSize,
                fBannerAdType = fBannerAdType,
                fIndex = fIndex
            )
        } else if (isOnline && fModel.loadedAd != null) {
            if (!isAnyIndexAlreadyLoaded) {
                logI(tag = TAG, message = "requestWithIndex: already loaded ad Index -> $fIndex, $mBannerAdType, $mBannerAdSize")
                isAnyIndexAlreadyLoaded = true
                onAdLoaded.invoke()
            }
        }
    }

    private fun loadAd() {
        if (!isBannerAdAvailable()) {
            isAnyIndexLoaded = false
            isAnyIndexAlreadyLoaded = false

            showPlaceHolders()

            if (isBannerAdEnable() && isOnline) {
                getBannerAdModel(isNeedToLoadMultipleRequest = is_need_to_load_multiple_banner_ad_request) { index, fAdModel ->
                    logI(tag = TAG, message = "loadAd: getBannerAdModel: Index -> $index, $mBannerAdType, $mBannerAdSize, ${fAdModel.isAdLoadingRunning}")
                    requestWithIndex(
                        fContext = context,
                        fModel = fAdModel,
                        fAdSize = actualAdSize,
                        fBannerAdType = mBannerAdType,
                        fIndex = index,
                        onAdLoaded = {
                            logI(tag = TAG, message = "loadAd: onAdLoaded: Index -> $index, ${fAdModel.loadedAd}, $mBannerAdType, $mBannerAdSize")
                            mCurrentView.apply {
                                this.removeAllViews()
                                fAdModel.loadedAd?.let { this.addView(it) }
                                refreshView()
//                                mCurrentView.tag = "ad_loaded"
//                                showBannerAd(beVisible = true)
                            }
                        },
                        onAdClosed = {
                            logI(tag = TAG, message = "loadAd: onAdClosed: Index -> $index, ${fAdModel.loadedAd}, $mBannerAdType, $mBannerAdSize")
                            mCurrentView.removeAllViews()
                            loadAd()
                        },
                        onAdFailed = {
                            if (!is_need_to_load_multiple_banner_ad_request) {
                                if (isLastIndex) {
                                    mAdIdPosition = -1
                                } else {
                                    loadAd()
                                }
                            }
                        },
                    )
                }
//            } else if (!isAppNotPurchased || !is_enable_banner_ad_from_remote_config) {
            } else if (!isBannerAdEnable()) {
                mCurrentView.gone
            }
        }
    }

    private fun isBannerAdEnable(): Boolean = isAppNotPurchased && is_enable_banner_ad_from_remote_config

    private fun isBannerAdAvailable(): Boolean = isBannerAdEnable() && listOfBannerAdsModel.any { it.loadedAd != null }

//    private fun showBannerAd(beVisible: Boolean) {
//        if (!beVisible) {
//            if (!isForceViewVisibilityVisible) {
//                mCurrentView.gone
//            }
//        } else {
//            if (!isForceViewVisibilityGone) {
//                this.beVisibleIf(isBannerAdEnable())
//            }
//        }
//    }

    fun pause() {
        listOfBannerAdsModel.filter { it.loadedAd != null }.let { nonNullList ->
            nonNullList.forEach {
                it.loadedAd?.pause()
            }
        }
    }

    fun resume() {
        listOfBannerAdsModel.filter { it.loadedAd != null }.let { nonNullList ->
            nonNullList.forEach {
                it.loadedAd?.resume()
            }
        }
    }

    fun destroy() {
//        mCurrentView.tag = "placeholder_loaded"
//        mCurrentView.removeAllViews()
//        showBannerAd(beVisible = false)
        isThisAdClicked = false
        isAnyIndexLoaded = false
        isAnyIndexAlreadyLoaded = false
        mAdIdPosition = -1

        isInternetAvailable.removeObserver(mInternetObserver)

        listOfBannerAdsModel.filter { it.loadedAd != null }.let { nonNullList ->
            nonNullList.forEach {
                it.loadedAd?.destroy()
                it.loadedAd = null
                it.listener = null
                it.isAdLoadingRunning = false
            }
        }

        listOfBannerAdsModel.clearAll()

        list_of_banner_ads.forEach {
            listOfBannerAdsModel.add(
                AdStatusModel(
                    adID = it.adID
                )
            )
        }
    }

//    private var isForceViewVisibilityGone: Boolean = false
//    private var isForceViewVisibilityVisible: Boolean = false
//
//    fun forceViewVisibility(beVisible: Boolean) {
//        if (beVisible) {
//            isForceViewVisibilityVisible = true
//            isForceViewVisibilityGone = false
//
//            this.beVisibleIf(isBannerAdEnable())
//        } else {
//            isForceViewVisibilityGone = true
//            isForceViewVisibilityVisible = false
//
//            mCurrentView.gone
//        }
//    }

    fun updateAdView(
        fAdSize: BannerAdSize? = null,
        fAdType: BannerAdType? = null,
        fPlaceHolderType: PlaceHolderType? = null,
        fPlaceholderTextColor: ColorStateList? = null,
        fCustomPlaceholder: View? = null,
    ) {
        fPlaceHolderType?.let {
            mPlaceHolderType = it
            customPlaceholderView = null
            customPlaceHolderResourceId = null
        }
        fPlaceholderTextColor?.let { placeHolderBinding.root.setTextColor(it) }
        fCustomPlaceholder?.let {
            customPlaceholderView = it
            customPlaceHolderResourceId = null
        }

        if (isBannerAdEnable()) {
            if (!(listOfBannerAdsModel.any { it.loadedAd != null })) {
                refreshView()
                showPlaceHolders()
                refreshView()
            }
        }

        if (!mAutoLoad) {
            fAdSize?.let { mBannerAdSize = it }
            fAdType?.let {
                mBannerAdType = it
                if (mBannerAdSize != BannerAdSize.ADAPTIVE_BANNER) {
                    mBannerAdType = BannerAdType.NORMAL
                }
            }

            refreshView()
            destroy()
            showPlaceHolders()
            refreshView()

            loadAdWithInternetObserver()
        }
    }

    internal fun loadNonAutoAd() {
        if (!mAutoLoad) {
            loadAdWithInternetObserver()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logE(tag = TAG, message = "onAttachedToWindow: $mBannerAdType, $mBannerAdSize")
        if (mAutoLoad) {
            loadAdWithInternetObserver()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
        logE(tag = TAG, message = "onDetachedFromWindow: $mBannerAdType, $mBannerAdSize")
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        logE(tag = TAG, message = "onVisibilityChanged: visibility::-> `${visibility}`")
        if (!mCurrentView.isInEditMode) {
            if (visibility == View.VISIBLE) {
                mCurrentView.beVisibleIf(isBannerAdEnable())
            }
        }
    }
}