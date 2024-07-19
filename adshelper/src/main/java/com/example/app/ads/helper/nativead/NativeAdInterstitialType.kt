package com.example.app.ads.helper.nativead

/**
 * @author Akshay Harsoda
 * @since 04 Jul 2024
 *
 * different type of Native Interstitial Ad
 */
enum class NativeAdInterstitialType(var id: Int) {
    WEBSITE(0),
    APP_STORE(1);
//    FULL_SCREEN(2);

    companion object {

        @JvmStatic
        fun fromId(value: Int): NativeAdInterstitialType {
            return entries.firstOrNull { it.id == value } ?: WEBSITE
        }
    }
}