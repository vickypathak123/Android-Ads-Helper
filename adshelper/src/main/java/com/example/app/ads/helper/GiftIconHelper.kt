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

    private val TAG: String = javaClass.simpleName

    private var isInterstitialAdLoaded = false
    private var interstitial: InterstitialAd? = null

    /**
     * Call this method when you need to show your Gift AD
     *
     * @param fContext this is a reference to your activity context
     * @param fivGiftIcon this is your 1st visible main gift icon
     * @param fivBlastIcon this is your 2nd visible blast gift icon
     */
    fun loadGiftAd(@NonNull fContext: Activity, @NonNull fivGiftIcon: LottieAnimationView, @NonNull fivBlastIcon: LottieAnimationView) {

        fivGiftIcon.gone
        fivBlastIcon.gone


        loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)

        fivGiftIcon.setOnClickListener {
            fivGiftIcon.gone
            fivBlastIcon.visible

            isInterstitialAdShow = true

            fivBlastIcon.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    Log.e(TAG, "onAnimationEnd: isNewInterstitialAdLoad::${fContext.isNewInterstitialAdLoad}")
                    Log.e(TAG, "onAnimationEnd: isInterstitialAdLoaded::${isInterstitialAdLoaded}")
                    Log.e(TAG, "onAnimationEnd: interstitial != null::${interstitial != null}")
                    if (!fContext.isNewInterstitialAdLoad) {
                        if (isInterstitialAdLoaded && interstitial != null) {
                            Log.e(TAG, "onAnimationEnd: ")
                            isInterstitialAdLoaded = false
                            interstitial = null
                            loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)
//                            fivGiftIcon.visible
//                            fivBlastIcon.gone
                        }
                    }
                    fivGiftIcon.gone
                    fivBlastIcon.gone
                }

                override fun onAnimationCancel(animation: Animator?) {
                    Log.e(TAG, "onAnimationCancel: ")
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
                isInterstitialAdLoaded = true
                fivGiftIcon.visible
                fivBlastIcon.gone
            }

            override fun onInterstitialAdLoaded(interstitialAd: InterstitialAd) {
                super.onInterstitialAdLoaded(interstitialAd)
                interstitial = interstitialAd
                isInterstitialAdLoaded = true
                fivGiftIcon.visible
                fivBlastIcon.gone
            }

            override fun onAdFailed() {
                isInterstitialAdLoaded = false
                fivGiftIcon.gone
                fivBlastIcon.gone
                loadNewInterstitialAd(fContext, fivGiftIcon, fivBlastIcon)
            }

            override fun onAdClosed(isShowFullScreenAd: Boolean) {
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
                isInterstitialAdShow = true
                isAnyAdOpen = true
                interstitial?.show(this)
                true
            } else {
                false
            }
        }
}