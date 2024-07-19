package com.example.app.ads.helper.banner

/**
 * @author Akshay Harsoda
 * @since 26 Dec 2022
 * @updated 26 Jun 2024
 */
enum class BannerAdSize(var id: Int) {
    BANNER(0),
    LARGE_BANNER(1),
    MEDIUM_RECTANGLE(2),
    FULL_BANNER(3),
    LEADERBOARD(4),
    ADAPTIVE_BANNER(5);
//    SMART_BANNER(6);

    companion object {

        @JvmStatic
        fun fromId(value: Int): BannerAdSize {
            return entries.firstOrNull { it.id == value } ?: BANNER
        }
    }
}