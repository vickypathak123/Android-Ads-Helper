package com.example.app.ads.helper

import android.util.Log
import com.example.app.ads.helper.purchase.ProductPurchaseHelper

internal var isDebugMode: Boolean = false

internal fun logD(tag: String, message: String) {
    if (isDebugMode) {
        Log.d(tag, message)
    }
}

internal fun logI(tag: String, message: String) {
    if (isDebugMode) {
        Log.i(tag, message)
    }
}

internal fun logE(tag: String, message: String) {
    if (isDebugMode) {
        Log.e(tag, message)
    }
}
internal fun logW(tag: String, message: String) {
    if (isDebugMode) {
        Log.w(tag, message)
    }
}