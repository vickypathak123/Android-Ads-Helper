package com.example.app.ads.helper.banner

/**
 * @author Akshay Harsoda
 * @since 11 Apr 2024
 * @updated 26 Jun 2024
 */
enum class BannerAdType(var id: Int) {
    NORMAL(0),
    SPLASH(1),
    COLLAPSIBLE_BOTTOM(2),
    COLLAPSIBLE_TOP(3);

    companion object {

        @JvmStatic
        fun fromId(value: Int): BannerAdType {
            return entries.firstOrNull { it.id == value } ?: NORMAL
        }
    }
}