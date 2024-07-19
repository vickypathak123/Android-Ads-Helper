package com.example.app.ads.helper

/**
 * @author Akshay Harsoda
 * @since 27 Jun 2024
 */
enum class PlaceHolderType(var id: Int) {
    NONE(0),
    SHIMMER(1),
    TEXT(2),
    CUSTOM(3);

    companion object {

        @JvmStatic
        fun fromId(value: Int): PlaceHolderType {
            return entries.firstOrNull { it.id == value } ?: SHIMMER
        }
    }
}