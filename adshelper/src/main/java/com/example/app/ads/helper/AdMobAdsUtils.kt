@file:Suppress("unused")

package com.example.app.ads.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration


var isNeedToShowAds = true
var isAppForeground = false

var isInterstitialAdShow = false
internal var isAnyAdOpen = false

internal var isAnyAdShowing: Boolean = false

internal var admob_app_id: String? = null
internal var admob_banner_ad_id: String? = null
internal var admob_interstitial_ad_id: String? = null
internal var admob_native_advanced_ad_id: String? = null
internal var admob_reward_video_ad_id: String? = null
internal var admob_interstitial_ad_reward_id: String? = null
internal var admob_open_ad_id: String? = null
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
enum class NativeAdsSize {
    Big, Medium, FullScreen, Custom
}