package com.example.app.ads.helper

import android.util.Log
import com.example.app.ads.helper.purchase.ProductPurchaseHelper

internal fun logD(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message)
    }
}

internal fun logI(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.i(tag, message)
    }
}

internal fun logE(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message)
    }
}
internal fun logW(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.w(tag, message)
    }
}