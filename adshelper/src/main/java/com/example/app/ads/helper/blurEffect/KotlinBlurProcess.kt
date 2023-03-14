package com.example.app.ads.helper.blurEffect

import android.graphics.Bitmap
import android.graphics.Matrix
import com.example.app.ads.helper.blurEffect.Executor.EXECUTOR_THREADS

class KotlinBlurProcess : BlurProcess {
    private var preScale: Float = 0.2f

    override fun blur(original: Bitmap, radius: Float): Bitmap? {
        val compressedBitmap: Bitmap = prescaleBitmap(original)
        val w = compressedBitmap.width
        val h = compressedBitmap.height
        val currentPixels = IntArray(w * h)
        compressedBitmap.getPixels(currentPixels, 0, w, 0, 0, w, h)
        val cores = EXECUTOR_THREADS
        val horizontal = ArrayList<BlurTask>(cores)
        val vertical = ArrayList<BlurTask>(cores)
        for (i in 0 until cores) {
            horizontal.add(
                BlurTask(
                    currentPixels,
                    w,
                    h,
                    radius.toInt(),
                    cores,
                    i,
                    1
                )
            )
            vertical.add(
                BlurTask(
                    currentPixels,
                    w,
                    h,
                    radius.toInt(),
                    cores,
                    i,
                    2
                )
            )
            Executor.blurIteration(
                currentPixels,
                w,
                h,
                radius.toInt(),
                cores,
                i,
                1
            )
            Executor.blurIteration(
                currentPixels,
                w,
                h,
                radius.toInt(),
                cores,
                i,
                2
            )
        }
        try {
            Executor.executeAll(horizontal)
        } catch (e: InterruptedException) {
            return null
        }
        try {
            Executor.executeAll(vertical)
        } catch (e: InterruptedException) {
            return null
        }
        return Bitmap.createBitmap(currentPixels, w, h, Bitmap.Config.ARGB_8888)
    }

    /**
     * trick to improve performance
     * */
    private fun prescaleBitmap(original: Bitmap): Bitmap {
        val compressedBitmap: Bitmap
        val preScale = preScale
        compressedBitmap = try {
            val matrix = Matrix()
            matrix.setScale(preScale, preScale)
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            original
        }
        return compressedBitmap
    }
}