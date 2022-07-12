package com.example.app.ads.helper.blurEffect

import android.graphics.Bitmap
import android.widget.ImageView


class BlurImage {
    companion object {
        private const val MAX_RADIUS = 25F
        private const val MIN_RADIUS = 0F
    }

    private var blurProcess: BlurProcess = KotlinBlurProcess()

    private var radius = 10F // default
    private var source: Bitmap? = null

    /**
     * radius from 1 to 25
     * */
    fun radius(radius: Float): BlurImage {
        this.radius = if (radius <= MAX_RADIUS && radius > MIN_RADIUS) {
            radius
        } else {
            MAX_RADIUS
        }
        return this
    }

    fun withCPU(): BlurImage {
        blurProcess = KotlinBlurProcess()
        return this
    }

    fun load(bitmap: Bitmap): BlurImage {
        source = bitmap
        return this
    }

    private fun blur(): Bitmap? {
        return if (source == null) {
            null
        } else {
            blurProcess.blur(source!!, radius)
        }
    }

    fun into(imageView: ImageView) {
        Executor.io {

            source?.let {
                if (!it.isRecycled) {
                    val blurBitmap = blur()
                    if (blurBitmap != null) {
                        Executor.ui {
                            imageView.setImageBitmap(blurBitmap)
                        }
                    }
                    source?.recycle()
                    source = null
                }
            }

        }
    }
}