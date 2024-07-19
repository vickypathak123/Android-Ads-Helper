@file:Suppress("unused")

package com.example.ads.helper.new_.demo.base.shared_prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.ads.helper.new_.demo.R
import com.example.ads.helper.new_.demo.base.utils.getStringRes

val Context.PrefsFileName get() = "${this.getStringRes(R.string.app_name)}_prefs"


/**
 * Extension method for get SharedPreferences
 */
inline val Context.getPrefs: SharedPreferences get() = this.getSharedPreferences(PrefsFileName, Context.MODE_PRIVATE)

/**
 * Extension method for check SharedPreferences contain passed key
 *
 * @param fKey SharedPreferences Key witch you want to check
 */
fun Context.contain(fKey: String): Boolean {
    return this.getPrefs.contains(fKey)
}

/**
 * Extension method for clear SharedPreferences object
 */
fun Context.clearPrefs() {
    this.getPrefs.edit().clear().apply()
}

/**
 * Extension method for remove SharedPreferences Key
 *
 * @param fKey SharedPreferences Key witch you want to remove
 */
fun Context.removeKey(fKey: String) {
    this.getPrefs.edit().remove(fKey).apply()
}

/**
 * Extension method for Save Boolean Value
 *
 * @param fKey SharedPreferences Key
 * @param fValue Boolean Value
 */
fun Context.save(fKey: String, fValue: Boolean) {
    this.getPrefs.edit().putBoolean(fKey, fValue).apply()
}

/**
 * Extension method for get Boolean Value
 * with your pre-defined default value
 *
 * @param fKey SharedPreferences Key
 * @param fDefaultValue your pre-defined default value
 */
fun Context.getBoolean(fKey: String, fDefaultValue: Boolean = false): Boolean {
    return this.getPrefs.getBoolean(fKey, fDefaultValue)
}

/**
 * Extension method for Save String Value
 *
 * @param fKey SharedPreferences Key
 * @param fValue String Value
 */
fun Context.save(fKey: String, fValue: String) {
    this.getPrefs.edit().putString(fKey, fValue).apply()
}

/**
 * Extension method for get String Value
 * with your pre-defined default value
 *
 * @param fKey SharedPreferences Key
 * @param fDefaultValue your pre-defined default value
 */
fun Context.getString(fKey: String, fDefaultValue: String = ""): String {
    return this.getPrefs.getString(fKey, fDefaultValue) ?: fDefaultValue
}

/**
 * Extension method for Save Integer Value
 *
 * @param fKey SharedPreferences Key
 * @param fValue Int Value
 */
fun Context.save(fKey: String, fValue: Int) {
    this.getPrefs.edit().putInt(fKey, fValue).apply()
}

/**
 * Extension method for get Integer Value
 * with your pre-defined default value
 *
 * @param fKey SharedPreferences Key
 * @param fDefaultValue your pre-defined default value
 */
fun Context.getInt(fKey: String, fDefaultValue: Int = 0): Int {
    return this.getPrefs.getInt(fKey, fDefaultValue)
}

/**
 * Extension method for Save Float Value
 *
 * @param fKey SharedPreferences Key
 * @param fValue Float Value
 */
fun Context.save(fKey: String, fValue: Float) {
    this.getPrefs.edit().putFloat(fKey, fValue).apply()
}

/**
 * Extension method for get Float Value
 * with your pre-defined default value
 *
 * @param fKey SharedPreferences Key
 * @param fDefaultValue your pre-defined default value
 */
fun Context.getFloat(fKey: String, fDefaultValue: Float = 0f): Float {
    return this.getPrefs.getFloat(fKey, fDefaultValue)
}

/**
 * Extension method for Save Long Value
 *
 * @param fKey SharedPreferences Key
 * @param fValue Long Value
 */
fun Context.save(fKey: String, fValue: Long) {
    this.getPrefs.edit().putLong(fKey, fValue).apply()
}

/**
 * Extension method for get Long Value
 * with your pre-defined default value
 *
 * @param fKey SharedPreferences Key
 * @param fDefaultValue your pre-defined default value
 */
fun Context.getLong(fKey: String, fDefaultValue: Long = 0): Long {
    return this.getPrefs.getLong(fKey, fDefaultValue)
}