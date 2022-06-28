package com.example.app.ads.helper.demo

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur


fun blurBitmap(fContext: Context, bitmap: Bitmap): Bitmap? {

    val outBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

    val rs = RenderScript.create(fContext)

    val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

    val allIn = Allocation.createFromBitmap(rs, bitmap)
    val allOut = Allocation.createFromBitmap(rs, outBitmap)

    blurScript.setRadius(8f)

    blurScript.setInput(allIn)
    blurScript.forEach(allOut)

    allOut.copyTo(outBitmap)

    bitmap.recycle()

    rs.destroy()
    return outBitmap
}