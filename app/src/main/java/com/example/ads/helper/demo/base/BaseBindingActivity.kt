@file:Suppress("unused")

package com.example.ads.helper.demo.base

import android.os.Bundle
import androidx.annotation.UiThread
import androidx.viewbinding.ViewBinding

/**
 * @author Akshay Harsoda
 * @since 18 Mar 2021
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

    override fun onCreate(savedInstanceState: Bundle?) {
        setParamBeforeLayoutInit()
        super.onCreate(null)

        this.mBinding = setBinding()
        setContentView(this.mBinding.root)
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
}