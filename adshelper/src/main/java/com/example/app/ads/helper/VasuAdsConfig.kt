package com.example.app.ads.helper

import android.content.Context

/**
 * @author Akshay Harsoda
 * @since 16 Oct 2021
 */
@Suppress("unused")
object VasuAdsConfig {

    @JvmStatic
    fun with(fContext: Context): SetAdsID {
        return SetAdsID(fContext)
    }
}