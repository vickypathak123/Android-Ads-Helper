@file:Suppress("unused")

package com.example.app.ads.helper

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.widget.ImageView
import androidx.annotation.NonNull
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.interstitial.InterstitialAd

/**
 * @author Akshay Harsoda
 * @since 05 Aug 2021
 *
 * GiftIconHelper.kt - Simple object which has load and handle your gift AD data
 */
object GiftIconHelper {

    private val TAG: String = "Admob_${javaClass.simpleName}"

    private var isInterstitialAdLoaded = false
    private var interstitial: InterstitialAd? = null

    private const val mMinClickInterval: Long = 500
    private var mLastClickTime: Long = 0

    /**
     * Call this method when you need to show your Gift AD
     *
     * @param fContext this is a reference to your activity context
     * @param fivGiftIcon this is your 1st visible main gift icon
     * @param fivBlastIcon this is your 2nd visible blast gift icon
     */
    @JvmStatic
    fun loadGiftAd(@NonNull fContext: Activity, @NonNull fivGiftIcon: LottieAnimationView, @NonNull fivBlastIcon: LottieAnimationView) {

        fivGiftIcon.gone
        fivBlastIcon.gone

        loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)

        fivGiftIcon.setOnClickListener {

            val currentClickTime = SystemClock.uptimeMillis()
            val elapsedTime = currentClickTime - mLastClickTime
            mLastClickTime = currentClickTime
            if (elapsedTime <= mMinClickInterval) return@setOnClickListener


            fivGiftIcon.gone
            fivBlastIcon.visible

            fivBlastIcon.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    fivGiftIcon.gone
                    fivBlastIcon.gone
                    if (!fContext.isNewInterstitialAdLoad) {
                        isInterstitialAdLoaded = false
                        interstitial = null
                        isInterstitialAdShow = false
                        loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }
            })

            fivBlastIcon.playAnimation()
        }
    }

    internal fun loadNewInterstitialAd(@NonNull fContext: Context, @NonNull fivGiftIcon: ImageView, @NonNull fivBlastIcon: ImageView) {
        fivGiftIcon.gone
        fivBlastIcon.gone

        InterstitialAdHelper.loadAd(fContext, object : AdMobAdsListener {

            override fun onAdLoaded() {
                logI(tag = TAG, message = "onAdLoaded: Gift Icon Ad")
                isInterstitialAdLoaded = true
                fivGiftIcon.visible
                fivBlastIcon.gone
            }

            override fun onInterstitialAdLoaded(interstitialAd: InterstitialAd) {
                super.onInterstitialAdLoaded(interstitialAd)
                logI(tag = TAG, message = "onInterstitialAdLoaded: Gift Icon Ad")
                interstitial = interstitialAd
                isInterstitialAdLoaded = true
                fivGiftIcon.visible
                fivBlastIcon.gone
            }

            override fun onAdFailed() {
                logE(tag = TAG, message = "onAdFailed: Gift Icon Ad")
                isInterstitialAdLoaded = false
                fivGiftIcon.gone
                fivBlastIcon.gone
                loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)
            }

            override fun onAdClosed(isShowFullScreenAd: Boolean) {
                logI(tag = TAG, message = "onAdClosed: Gift Icon Ad")
                isAnyAdOpen = false
                isAnyAdShowing = false
                isInterstitialAdLoaded = false
                isInterstitialAdShow = false
                fivBlastIcon.gone
                fivGiftIcon.gone
                interstitial?.fullScreenContentCallback = null
                interstitial = null
                loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)
            }
        })
    }

    internal val Activity.isNewInterstitialAdLoad: Boolean
        get() {
            return if (!isInterstitialAdShow && isInterstitialAdLoaded && interstitial != null) {
                if (!isAnyAdShowing) {
                    logI(tag = TAG, message = "onAdShowed: Gift Icon Ad")
                    isAnyAdShowing = true
                    isInterstitialAdShow = true
                    isAnyAdOpen = true
                    interstitial?.show(this)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
}