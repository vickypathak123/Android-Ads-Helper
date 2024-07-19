package com.example.app.ads.helper.nativead

/**
 * @author Akshay Harsoda
 * @since 01 Jul 2024
 *
 * different type of Native Ad
 */
enum class NativeAdType(var id: Int) {
    BIG(0),
    MEDIUM(1),
    INTERSTITIAL_NATIVE(2),
    CUSTOM(3);

    //    FULL_SCREEN(2),
    companion object {

        @JvmStatic
        fun fromId(value: Int): NativeAdType {
            return entries.firstOrNull { it.id == value } ?: BIG
        }
    }
}