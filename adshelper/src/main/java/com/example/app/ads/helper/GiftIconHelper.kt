@file:Suppress("unused")

package com.example.app.ads.helper

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.util.Log
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
            fivGiftIcon.gone
            fivBlastIcon.visible

            Log.i(TAG, "loadGiftAd: Gift Click For Ad Showing")

            fivBlastIcon.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!fContext.isNewInterstitialAdLoad) {
                        if (isInterstitialAdLoaded && interstitial != null) {
                            Log.i(TAG, "onAnimationEnd: Load New Ads")
                            isInterstitialAdLoaded = false
                            interstitial = null
                            isInterstitialAdShow = false
                            loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)
                        }
                    }
                    fivGiftIcon.gone
                    fivBlastIcon.gone
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }
            })

            fivBlastIcon.playAnimation()
        }
    }

    private fun loadNewInterstitialAd(@NonNull fContext: Context, @NonNull fivGiftIcon: ImageView, @NonNull fivBlastIcon: ImageView) {
        fivGiftIcon.gone
        fivBlastIcon.gone

        InterstitialAdHelper.loadAd(fContext, object : AdMobAdsListener {

            override fun onAdLoaded() {
                Log.e(TAG, "Gift Ad onAdLoaded: ")
                isInterstitialAdLoaded = true
                fivGiftIcon.visible
                fivBlastIcon.gone
            }

            override fun onInterstitialAdLoaded(interstitialAd: InterstitialAd) {
                super.onInterstitialAdLoaded(interstitialAd)
                Log.e(TAG, "Gift Ad onInterstitialAdLoaded: ")
                interstitial = interstitialAd
                isInterstitialAdLoaded = true
                fivGiftIcon.visible
                fivBlastIcon.gone
            }

            override fun onAdFailed() {
                Log.e(TAG, "Gift Ad onAdFailed: ")
                isInterstitialAdLoaded = false
                fivGiftIcon.gone
                fivBlastIcon.gone
                loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)
            }

            override fun onAdClosed(isShowFullScreenAd: Boolean) {
                Log.e(TAG, "Gift Ad onAdClosed: ")
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

    private val Activity.isNewInterstitialAdLoad: Boolean
        get() {
            return if (!isInterstitialAdShow && isInterstitialAdLoaded && interstitial != null) {
                if (!isAnyAdShowing) {
                    Log.i(TAG, "Gift Ad Show: ")
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