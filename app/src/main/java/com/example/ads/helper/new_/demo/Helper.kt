package com.example.ads.helper.new_.demo

import android.content.Context
import android.content.Intent

private const val TAG = "Helper"

const val IS_OPEN_ADS_ENABLE: String = "is_open_ads_enable"

/**
 * Extension method to check is need to ad show or not
 */
inline val Context.isNeedToLoadAd: Boolean
    get() {
        return true
//        isNeedToShowAds = AdsManager(this).isNeedToShowAds()
//        return isNeedToShowAds
    }

fun triggerRebirth(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}