@file:Suppress("unused")

package com.example.ads.helper.new_.demo.base.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import kotlin.math.roundToInt

//<editor-fold desc="For Get All Type of Resources">
/**
 * Extension method to Get String resource for Context.
 */
fun Context.getStringRes(@StringRes id: Int) = resources.getString(id)

fun Context.getStringRes(@StringRes id: Int, vararg formatArgs: String) = resources.getString(id, *formatArgs)

fun <T> Context.getStringRes(@StringRes id: Int, vararg formatArgs: T) = resources.getString(id, *formatArgs)

/**
 * Extension method to Get Color resource for Context.
 */
fun Context.getColorRes(@ColorRes id: Int) = ContextCompat.getColor(this, id)

fun Context.getColorStateRes(@ColorRes id: Int) = ContextCompat.getColorStateList(this, id)

/**
 * Extension method to Get Drawable for resource for Context.
 */
fun Context.getDrawableRes(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

fun Context.sdpToPx(@DimenRes id: Int) = resources.getDimensionPixelSize(id)

fun Context.dpToPx(dp: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp.toFloat(),
    resources.displayMetrics
).roundToInt()
//</editor-fold>

//<editor-fold desc="For StatusBar Entity">
fun Activity.hideStatusBar() {
    if (isRPlus()) {
        window.insetsController?.let {
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsets.Type.statusBars())
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }
}

fun Activity.showStatusBar() {
    if (isRPlus()) {
        window.setDecorFitsSystemWindows(false)
        window.insetsController?.show(WindowInsets.Type.statusBars())
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }
}
//</editor-fold>

//<editor-fold desc="Pass Or Get Enum Through Intent">
inline fun <reified T : Enum<T>> Intent.putEnumExtra(victim: T): Intent =
    putExtra(T::class.java.name, victim.ordinal)

inline fun <reified T : Enum<T>> Bundle.putEnumExtra(victim: T) = this.putInt(T::class.java.name, victim.ordinal)

inline fun <reified T : Enum<T>> Intent.getEnumExtra(): T =
    (getIntExtra(T::class.java.name, -1).takeUnless { it == -1 }
        ?.let { T::class.java.enumConstants?.get(it) } ?: T::class.java.enumConstants?.get(0)) as T

inline fun <reified T : Enum<T>> Bundle.getEnumExtra(): T =
    (this.getInt(T::class.java.name, -1).takeUnless { it == -1 }
        ?.let { T::class.java.enumConstants?.get(it) } ?: T::class.java.enumConstants?.get(0)) as T

fun <T : Enum<T>> getEnumExtra(bundle: Bundle, enumType: Class<T>): T? {
    val ordinal = bundle.getInt(enumType.name, -1)
    if (ordinal != -1) {
        val enumConstants = enumType.enumConstants
        if (enumConstants != null) {
            if (ordinal >= 0 && ordinal < enumConstants.size) {
                return enumConstants[ordinal]
            }
        }
    }
    return null // Or throw an exception if needed
}
//</editor-fold>

fun ViewGroup.setSelection() {

    for (view in this.children) {
        if (view is ViewGroup) {
            view.setSelection()
        } else {
            when (view) {
                is CheckBox -> {
                    view.isSelected = true
                }
                is RadioButton -> {
                    view.isSelected = true
                }
                is Button -> {
                    view.isSelected = true
                }
                is TextView -> {
                    view.isSelected = true
                }
            }
        }
    }
}

fun Context.makeText(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R