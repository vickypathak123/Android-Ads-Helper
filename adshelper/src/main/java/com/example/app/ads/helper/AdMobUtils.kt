@file:Suppress("unused")

package com.example.app.ads.helper

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author Akshay Harsoda
 * @since 16 Oct 2021
 * @updated 24 Jun 2024
 */

/**
 * Extension method to Get String resource for Context.
 */
internal fun Context.getStringRes(@StringRes id: Int) = ContextCompat.getString(this, id)

/**
 * Extension method to Get Color resource for Context.
 */
internal fun Context.getColorRes(@ColorRes id: Int) = ContextCompat.getColor(this, id)

internal fun Context.getColorStateRes(@ColorRes id: Int) = ContextCompat.getColorStateList(this, id)

//<editor-fold desc="For View">
/**
 * Extension method to get LayoutInflater
 */
internal inline val Context.inflater: LayoutInflater get() = LayoutInflater.from(this)

internal fun ViewGroup.inflateLayout(@LayoutRes resource: Int, attachToRoot: Boolean = false): View = this.context.inflater.inflate(resource, this, attachToRoot)

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

/**
 * Remove all margin of the view
 */
internal inline val View.removeMargin: View
    get() {
        val v: View = this
        v.updateLayoutParams<MarginLayoutParams> {
            this.leftMargin = 0
            this.topMargin = 0
            this.rightMargin = 0
            this.bottomMargin = 0
        }
        return v
    }

/**
 * add margin of the view
 *
 * @param fMargin set All side same margin of view
 * @param fLeftMargin set only Left side same margin of view
 * @param fTopMargin set only Top side same margin of view
 * @param fRightMargin set only Right side same margin of view
 * @param fBottomMargin set only Bottom side same margin of view
 */
internal fun View.addMargin(
    fMargin: Float? = null,
    fLeftMargin: Float? = null,
    fTopMargin: Float? = null,
    fRightMargin: Float? = null,
    fBottomMargin: Float? = null
): View {
    val v: View = this
    v.updateLayoutParams<MarginLayoutParams> {
        fMargin?.let {
            val lMargin: Int = v.context.dpToPx(dp = fMargin).roundToInt()
            this.leftMargin = lMargin
            this.topMargin = lMargin
            this.rightMargin = lMargin
            this.bottomMargin = lMargin
        } ?: fLeftMargin?.let {
            val lMargin: Int = v.context.dpToPx(dp = fLeftMargin).roundToInt()
            this.leftMargin = lMargin
        } ?: fTopMargin?.let {
            val lMargin: Int = v.context.dpToPx(dp = fTopMargin).roundToInt()
            this.topMargin = lMargin
        } ?: fRightMargin?.let {
            val lMargin: Int = v.context.dpToPx(dp = fRightMargin).roundToInt()
            this.rightMargin = lMargin
        } ?: fBottomMargin?.let {
            val lMargin: Int = v.context.dpToPx(dp = fBottomMargin).roundToInt()
            this.bottomMargin = lMargin
        }
    }

    return v
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

internal fun Context.dpToPx(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
//</editor-fold>

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

internal fun ArrayList<*>.clearAll() {
    this.clear()
    this.removeAll(this.toSet())
}


internal fun setColorAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float = 0.3f, @ColorInt baseColor: Int = 0x4cffffff): Int {
    val value: Float = alpha
    val clamp = min(1f.toDouble(), max(0f.toDouble(), value.toDouble())).toFloat()
    val intAlpha = (clamp * 255f).toInt()
    return intAlpha shl 24 or (baseColor and 0x00FFFFFF)
}

internal val String.toCamelCase: String
    get() {
        val words: Array<String> = this.split(" ").toTypedArray()

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

//internal var onDialogActivityDismiss: () -> Unit = {}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
internal inline val isPiePlus get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P






