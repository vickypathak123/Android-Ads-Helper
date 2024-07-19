@file:Suppress("unused")

package com.example.ads.helper.new_.demo.base

import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.annotation.UiThread
import androidx.viewbinding.ViewBinding
import com.example.ads.helper.new_.demo.base.BaseActivity

/**
 * @author Akshay Harsoda
 * @since 18 Mar 2021
 * @updated 03 Jul 2024
 *
 * BaseBindingActivity.kt - A simple class contains some modifications to the native Activity.
 * This Class use with ViewBinding property.
 * also you have use this class in JAVA or KOTLIN both language.
 *
 * use of this class
 * you have to extend your Activity using this class like.
 * in Java :- {class MainActivity extends BaseBindingActivity<ActivityMainBinding>}
 * in Kotlin :- {class MainActivity : BaseBindingActivity<ActivityMainBinding>()}
 *
 * @property VB this is your layout file binding object
 *
 * for more details {@see BaseActivity.kt}.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseBindingActivity<VB : ViewBinding> : BaseActivity() {

    // your activity binding object
    lateinit var mBinding: VB

    private var heightMax = 0
    private var isKeyBordVisible: Boolean = false
    private val keyBordListener: ViewTreeObserver.OnGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect()
        mBinding.root.getWindowVisibleDisplayFrame(rect)
        if (rect.bottom > heightMax) {
            heightMax = rect.bottom
        }

        // The difference between the two is the height of the keyboard
        val keyboardHeight = heightMax - rect.bottom

        val lVisible: Boolean = (keyboardHeight > 0)

        if (isKeyBordVisible != lVisible) {
            isKeyBordVisible = lVisible
            if (keyboardHeight > 0) {
                onKeyBordVisibilityStateChange(isVisible = true, keyboardHeight = keyboardHeight)
            } else {
                onKeyBordVisibilityStateChange(isVisible = false, keyboardHeight = keyboardHeight)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setParamBeforeLayoutInit()
        super.onCreate(null)

        this.mBinding = setBinding()
        setContentView(this.mBinding.root)

        this.mBinding.root.viewTreeObserver.addOnGlobalLayoutListener(keyBordListener)
    }

    override fun getLayoutRes(): Int? {
        return null
    }

    /**
     * For init your binding object
     *
     * @return Binding property
     * in Java :- {return ActivityMainBinding.inflate(layoutInflater);}
     * in Kotlin :- {return ActivityMainBinding.inflate(layoutInflater)}
     */
    @UiThread
    abstract fun setBinding(): VB

    @UiThread
    open fun onKeyBordVisibilityStateChange(isVisible: Boolean, keyboardHeight: Int) {

    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.root.viewTreeObserver.removeOnGlobalLayoutListener(keyBordListener)
    }
}