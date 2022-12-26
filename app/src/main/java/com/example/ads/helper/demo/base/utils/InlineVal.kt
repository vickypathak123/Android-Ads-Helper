@file:Suppress("unused")

package com.example.ads.helper.demo.base.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.ads.helper.demo.R
import com.example.ads.helper.demo.base.BaseActivity
import kotlin.math.roundToInt

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
inline val Context.isOnline: Boolean
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

//<editor-fold desc="For View Data">
/**
 * Show the view  (visibility = View.VISIBLE)
 */
inline val View.visible: View
    get() {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
        return this
    }

/**
 * Hide the view. (visibility = View.INVISIBLE)
 */
inline val View.invisible: View
    get() {
        if (visibility != View.INVISIBLE) {
            visibility = View.INVISIBLE
        }
        return this
    }

/**
 * Remove the view (visibility = View.GONE)
 */
inline val View.gone: View
    get() {
        if (visibility != View.GONE) {
            visibility = View.GONE
        }
        return this
    }

/**
 * Remove the view (View.setEnable(true))
 */
inline val View.enable: View
    get() {
        isEnabled = true
        return this
    }

/**
 * Remove the view (View.setEnable(false))
 */
inline val View.disable: View
    get() {
        isEnabled = false
        return this
    }

/**
 * Extension method to get LayoutInflater
 */
inline val Context.inflater: LayoutInflater get() = LayoutInflater.from(this)
//</editor-fold>

//<editor-fold desc="For get Display Data">
/**
 * Extension method to get theme for Context.
 */
inline val Context.isDarkTheme: Boolean get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/**
 * Extension method to find a device width in pixels
 */
inline val Context.displayWidth: Int get() = resources.displayMetrics.widthPixels

/**
 * Extension method to find a device height in pixels
 */
inline val Context.displayHeight: Int get() = resources.displayMetrics.heightPixels

/**
 * Extension method to find a device density
 */
inline val Context.displayDensity: Float get() = resources.displayMetrics.density

/**
 * Extension method to find a device density in DPI
 */
inline val Context.displayDensityDpi: Int get() = resources.displayMetrics.densityDpi

/**
 * Extension method to find a device DisplayMetrics
 */
inline val Context.displayMetrics: DisplayMetrics get() = resources.displayMetrics
//</editor-fold>

//<editor-fold desc="For Text Entity">
inline val String.toEditable: Editable get() = Editable.Factory.getInstance().newEditable(this)

inline val String.removeMultipleSpace: String get() = this.trim().replace("\\s+".toRegex(), " ")

inline val String.getFromHtml: Spanned
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(this)
        }
    }
//</editor-fold>

//<editor-fold desc="For KeyBord">
/**
 * Extension method for Hide Key Bord And Clear Focus
 */
inline val View.hideKeyBord: Unit
    get() {
        this.clearFocus()
        (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(this.windowToken, 0)
    }

/**
 * Extension method for Hide Key Bord And Don't Clear Focus
 */
inline val EditText.hideKeyBordWithOutClearFocus: Unit
    get() {
        this.requestFocus()

        (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(this.windowToken, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.showSoftInputOnFocus = false
        } else {
            this.setTextIsSelectable(true)
        }
    }

/**
 * Extension method for Showing Key Bord
 */
inline val View.showKeyBord: Unit
    get() {
        this.requestFocus()
        (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, 0)
    }
//</editor-fold>

//<editor-fold desc="For Default Display Bar Height">
/**
 * Extension method for get StatusBar Height
 */
inline val Activity.statusBarHeight: Int
    get() {
        val resourceId =
            baseContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

/**
 * Extension method for get NavigationBar Height
 */
inline val Activity.navigationBarHeight: Int
    get() {
        val resourceId =
            baseContext.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }
//</editor-fold>

inline val isSDKBelow21: Boolean get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP

inline val Double.roundToHalf: Double get() = ((this * 2).roundToInt() / 2.0)

inline val Context.isValidContextForGlide: Boolean
    get() {
        if (this is Activity && this.isFinishing) {
            return false
        }
        return true
    }

inline val BaseActivity.shareApp: Unit
    get() {

        val lAppMsg = "Download this amazing " + getStringRes(R.string.app_name) + " app from play store, " +
                "Please search in play store or Click on the link given below to " +
                "download. "

        val lAppLink = "https://play.google.com/store/apps/details?id=" + this.packageName

        val lFinalMsg = String.format(lAppMsg, getStringRes(R.string.app_name), getStringRes(R.string.app_name))

        val lShareAppIntent = Intent(Intent.ACTION_SEND)
        lShareAppIntent.putExtra(Intent.EXTRA_TEXT, lFinalMsg + lAppLink)
        lShareAppIntent.type = "text/plain"

        launchActivityForResult(
            fIntent = Intent.createChooser(lShareAppIntent, "Share Via"),
            onActivityResult = { resultCode, data ->

            }
        )
    }

