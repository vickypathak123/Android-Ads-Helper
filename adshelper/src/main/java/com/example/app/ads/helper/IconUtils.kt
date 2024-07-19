@file:Suppress("unused")

package com.example.app.ads.helper

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams

/**
 * @author Akshay Harsoda
 * @since 24 Jun 2024
 */

enum class IconPosition {
    RIGHT_TO_LEFT, LEFT_TO_RIGHT
}

internal fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}

fun setCloseIconPosition(
    fParentLayout: ConstraintLayout,
    fCloseIcon: ImageView,
    fIconPosition: IconPosition
) {
    if (isPiePlus) {
        fParentLayout.setOnApplyWindowInsetsListener { _, insets ->
            insets.displayCutout?.let { cutout ->
                val cutOutRect: Rect = cutout.boundingRects[0]
                logE("setCloseIconPosition", "cutOutRect::->$cutOutRect")
                fCloseIcon.let { closeIcon ->
                    closeIcon.onGlobalLayout {
                        val closeIconRect = Rect()
                        closeIcon.getGlobalVisibleRect(closeIconRect)
                        logE("setCloseIconPosition", "closeIconRect::->$closeIconRect")
                        logE("setCloseIconPosition", "----------------------------------------")
                        logE("setCloseIconPosition", "----------------------------------------")
                        logE(
                            "setCloseIconPosition",
                            "cutOut contains close::->${cutOutRect.contains(closeIconRect)}"
                        )
                        logE(
                            "setCloseIconPosition",
                            "cutOut contains close right::->${
                                cutOutRect.contains(
                                    closeIconRect.right,
                                    closeIconRect.top
                                )
                            }"
                        )
                        logE(
                            "setCloseIconPosition",
                            "cutOut contains close left::->${
                                cutOutRect.contains(
                                    closeIconRect.left,
                                    closeIconRect.bottom
                                )
                            }"
                        )
                        logE(
                            "setCloseIconPosition",
                            "cutOut contains close top::->${
                                cutOutRect.contains(
                                    closeIconRect.left,
                                    closeIconRect.top
                                )
                            }"
                        )
                        logE(
                            "setCloseIconPosition",
                            "cutOut contains close bottom::->${
                                cutOutRect.contains(
                                    closeIconRect.right,
                                    closeIconRect.bottom
                                )
                            }"
                        )
                        logE("setCloseIconPosition", "----------------------------------------")
                        logE("setCloseIconPosition", "----------------------------------------")
                        logE(
                            "setCloseIconPosition",
                            "close contains cutOut::->${closeIconRect.contains(cutOutRect)}"
                        )
                        logE(
                            "setCloseIconPosition",
                            "close contains cutOut right::->${
                                closeIconRect.contains(
                                    cutOutRect.right,
                                    cutOutRect.top
                                )
                            }"
                        )
                        logE(
                            "setCloseIconPosition",
                            "close contains cutOut left::->${
                                closeIconRect.contains(
                                    cutOutRect.left,
                                    cutOutRect.bottom
                                )
                            }"
                        )
                        logE(
                            "setCloseIconPosition",
                            "close contains cutOut top::->${
                                closeIconRect.contains(
                                    cutOutRect.left,
                                    cutOutRect.top
                                )
                            }"
                        )
                        logE(
                            "setCloseIconPosition",
                            "close contains cutOut bottom::->${
                                closeIconRect.contains(
                                    cutOutRect.right,
                                    cutOutRect.bottom
                                )
                            }"
                        )
                        if (closeIconRect.contains(cutOutRect)
                            || closeIconRect.contains(cutOutRect.right, cutOutRect.top)
                            || closeIconRect.contains(cutOutRect.left, cutOutRect.bottom)
                            || closeIconRect.contains(cutOutRect.left, cutOutRect.top)
                            || closeIconRect.contains(cutOutRect.right, cutOutRect.bottom)
                            || cutOutRect.contains(closeIconRect)
                            || cutOutRect.contains(closeIconRect.right, closeIconRect.top)
                            || cutOutRect.contains(closeIconRect.left, closeIconRect.bottom)
                            || cutOutRect.contains(closeIconRect.left, closeIconRect.top)
                            || cutOutRect.contains(closeIconRect.right, closeIconRect.bottom)
                        ) {
                            closeIcon.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                when (fIconPosition) {
                                    IconPosition.RIGHT_TO_LEFT -> {
                                        startToStart = ConstraintSet.PARENT_ID
                                        endToEnd = ConstraintSet.UNSET
                                    }

                                    IconPosition.LEFT_TO_RIGHT -> {
                                        endToEnd = ConstraintSet.PARENT_ID
                                        startToStart = ConstraintSet.UNSET
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }
}