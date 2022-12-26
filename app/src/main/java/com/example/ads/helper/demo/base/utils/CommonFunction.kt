@file:Suppress("unused")

package com.example.ads.helper.demo.base.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
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

fun Context.getStringRes(@StringRes id: Int, vararg formatArgs: String) =
    resources.getString(id, *formatArgs)

fun <T> Context.getStringRes(@StringRes id: Int, vararg formatArgs: T) =
    resources.getString(id, *formatArgs)

/**
 * Extension method to Get Color resource for Context.
 */
fun Context.getColorRes(@ColorRes id: Int) = ContextCompat.getColor(this, id)

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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
    getIntExtra(T::class.java.name, -1).takeUnless { it == -1 }
        ?.let { T::class.java.enumConstants[it] } ?: T::class.java.enumConstants[0]

inline fun <reified T : Enum<T>> Bundle.getEnumExtra(): T =
    this.getInt(T::class.java.name, -1).takeUnless { it == -1 }
        ?.let { T::class.java.enumConstants[it] } ?: T::class.java.enumConstants[0]
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