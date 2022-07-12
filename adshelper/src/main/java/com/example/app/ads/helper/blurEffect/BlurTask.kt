package com.example.app.ads.helper.blurEffect

import java.util.concurrent.Callable

class BlurTask(
    src: IntArray,
    w: Int,
    h: Int,
    radius: Int,
    totalCore: Int,
    coreIndex: Int,
    round: Int
) : Callable<Void> {
    private var mSrc: IntArray = src
    private var mWidth = w
    private var mHeight = h
    private var mRadius = radius
    private var mTotalCores = totalCore
    private var mCoreIndex = coreIndex
    private var mRound = round

    override fun call(): Void? {
        Executor.blurIteration(
            mSrc,
            mWidth,
            mHeight,
            mRadius,
            mTotalCores,
            mCoreIndex,
            mRound
        )
        return null
    }
}