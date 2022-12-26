@file:Suppress("unused")

package com.example.app.ads.helper

import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import com.example.app.ads.helper.interstitialad.InterstitialAdModel
import com.example.app.ads.helper.nativead.NativeAdModel
import com.example.app.ads.helper.openad.OpenAdModel
import com.example.app.ads.helper.reward.RewardedInterstitialAdModel
import com.example.app.ads.helper.reward.RewardedVideoAdModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration


var isNeedToShowAds = true
var isAppForeground = false

var isInterstitialAdShow = false
var isAnyAdOpen = false

var needToBlockOpenAdInternally: Boolean = false

var isAnyAdShowing: Boolean = false

internal var admob_app_id: String? = null

internal var admob_interstitial_ad_model_list: ArrayList<InterstitialAdModel> = ArrayList()
internal var admob_app_open_ad_model_list: ArrayList<OpenAdModel> = ArrayList()
internal var admob_rewarded_interstitial_ad_model_list: ArrayList<RewardedInterstitialAdModel> = ArrayList()
internal var admob_rewarded_video_ad_model_list: ArrayList<RewardedVideoAdModel> = ArrayList()
internal var mList: ArrayList<NativeAdModel> = ArrayList()

internal var admob_native_advanced_ad_id: ArrayList<String> = ArrayList()
internal var admob_banner_ad_id: ArrayList<String> = ArrayList()

internal var isOpenAdEnable: Boolean = true
internal var isBlockInterstitialAd: Boolean = false

/**
 * Extension method to Get String resource for Context.
 */
internal fun Context.getStringRes(@StringRes id: Int) = resources.getString(id)

//<editor-fold desc="For View">
/**
 * Extension method to get LayoutInflater
 */
internal inline val Context.inflater: LayoutInflater get() = LayoutInflater.from(this)

/**
 * Show the view  (visibility = View.VISIBLE)
 */
internal inline val View.visible: View
    get() {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
        return this
    }

internal fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) visible else gone

/**
 * Hide the view. (visibility = View.INVISIBLE)
 */
internal inline val View.invisible: View
    get() {
        if (visibility != View.INVISIBLE) {
            visibility = View.INVISIBLE
        }
        return this
    }

/**
 * Remove the view (visibility = View.GONE)
 */
internal inline val View.gone: View
    get() {
        if (visibility != View.GONE) {
            visibility = View.GONE
        }
        return this
    }
//</editor-fold>

//<editor-fold desc="For get Display Data">
/**
 * Extension method to find a device DisplayMetrics
 */
internal inline val Context.displayMetrics: DisplayMetrics get() = resources.displayMetrics

/**
 * Extension method to find a device width in pixels
 */
internal inline val Context.displayWidth: Int get() = displayMetrics.widthPixels

/**
 * Extension method to find a device density
 */
internal inline val Context.displayDensity: Float get() = displayMetrics.density
//</editor-fold>

/**
 * ToDo.. Return true if internet or wi-fi connection is working fine
 * <p>
 * Required permission
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.INTERNET"/>
 *
 * @return true if you have the internet connection, or false if not.
 */
@Suppress("DEPRECATION")
internal inline val Context.isOnline: Boolean
    get() {
        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).let { connectivityManager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.let {
                    return it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }
            } else {
                try {
                    connectivityManager.activeNetworkInfo?.let {
                        if (it.isConnected && it.isAvailable) {
                            return true
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
        return false
    }

/**
 * Extension method for add AdMob Ads test devise id's
 *
 * Find This Log in your logcat for get your devise test id
 * I/Ads: Use RequestConfiguration.Builder.setTestDeviceIds("TEST_DEVICE_ID","TEST_DEVICE_ID")
 *
 * @param fDeviceId pass multiple your "TEST_DEVICE_ID"
 */
internal fun setTestDeviceIds(vararg fDeviceId: String) {

    val lTestDeviceIds: ArrayList<String> = ArrayList()
    lTestDeviceIds.add(AdRequest.DEVICE_ID_EMULATOR)
    lTestDeviceIds.addAll(fDeviceId)

    val lConfiguration = RequestConfiguration.Builder().setTestDeviceIds(lTestDeviceIds).build()

    MobileAds.setRequestConfiguration(lConfiguration)
}

/**
 * Extension method for add different size of Native Ad
 */
enum class NativeAdsSize { Big, Medium, FullScreen, Custom

}

internal var onDialogActivityDismiss: () -> Unit = {}

internal fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}

@RequiresApi(Build.VERSION_CODES.P)
fun setCloseIconPosition(fParentLayout: ConstraintLayout, fCloseIcon: ImageView, fIconPosition: IconPosition) {
    fParentLayout.setOnApplyWindowInsetsListener { _, insets ->
        insets.displayCutout?.let { cutout ->
            val cutOutRect: Rect = cutout.boundingRects[0]
            logE("setCloseIconPosition", "cutOutRect::->$cutOutRect")
            fCloseIcon.let { closeIcon ->
                closeIcon.onGlobalLayout {
                    val closeIconRect = Rect()
                    closeIcon.getGlobalVisibleRect(closeIconRect)
                    logE("setCloseIconPosition", "closeIconRect::->$closeIconRect")
                    logE("setCloseIconPosition", "----------------------------------------")
                    logE("setCloseIconPosition", "----------------------------------------")
                    logE("setCloseIconPosition", "cutOut contains close::->${cutOutRect.contains(closeIconRect)}")
                    logE("setCloseIconPosition", "cutOut contains close right::->${cutOutRect.contains(closeIconRect.right, closeIconRect.top)}")
                    logE("setCloseIconPosition", "cutOut contains close left::->${cutOutRect.contains(closeIconRect.left, closeIconRect.bottom)}")
                    logE("setCloseIconPosition", "cutOut contains close top::->${cutOutRect.contains(closeIconRect.left, closeIconRect.top)}")
                    logE("setCloseIconPosition", "cutOut contains close bottom::->${cutOutRect.contains(closeIconRect.right, closeIconRect.bottom)}")
                    logE("setCloseIconPosition", "----------------------------------------")
                    logE("setCloseIconPosition", "----------------------------------------")
                    logE("setCloseIconPosition", "close contains cutOut::->${closeIconRect.contains(cutOutRect)}")
                    logE("setCloseIconPosition", "close contains cutOut right::->${closeIconRect.contains(cutOutRect.right, cutOutRect.top)}")
                    logE("setCloseIconPosition", "close contains cutOut left::->${closeIconRect.contains(cutOutRect.left, cutOutRect.bottom)}")
                    logE("setCloseIconPosition", "close contains cutOut top::->${closeIconRect.contains(cutOutRect.left, cutOutRect.top)}")
                    logE("setCloseIconPosition", "close contains cutOut bottom::->${closeIconRect.contains(cutOutRect.right, cutOutRect.bottom)}")
                    if (closeIconRect.contains(cutOutRect)
                        || closeIconRect.contains(cutOutRect.right, cutOutRect.top)
                        || closeIconRect.contains(cutOutRect.left, cutOutRect.bottom)
                        || closeIconRect.contains(cutOutRect.left, cutOutRect.top)
                        || closeIconRect.contains(cutOutRect.right, cutOutRect.bottom)
                        || cutOutRect.contains(closeIconRect)
                        || cutOutRect.contains(closeIconRect.right, closeIconRect.top)
                        || cutOutRect.contains(closeIconRect.left, closeIconRect.bottom)
                        || cutOutRect.contains(closeIconRect.left, closeIconRect.top)
                        || cutOutRect.contains(closeIconRect.right, closeIconRect.bottom)
                    ) {
                        closeIcon.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            when (fIconPosition) {
                                IconPosition.RIGHT_TO_LEFT -> {
                                    startToStart = ConstraintSet.PARENT_ID
                                    endToEnd = ConstraintSet.UNSET
                                }
                                IconPosition.LEFT_TO_RIGHT -> {
                                    endToEnd = ConstraintSet.PARENT_ID
                                    startToStart = ConstraintSet.UNSET
                                }
                            }
                        }
                    }
                }
            }
        }
        return@setOnApplyWindowInsetsListener insets
    }
}

enum class IconPosition {
    RIGHT_TO_LEFT, LEFT_TO_RIGHT
}