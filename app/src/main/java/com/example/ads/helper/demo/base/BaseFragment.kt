@file:Suppress("unused")

package com.example.ads.helper.demo.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.ads.helper.demo.base.utils.isOnline
import com.example.ads.helper.demo.isNeedToLoadAd

/**
 * @author Akshay Harsoda
 * @since 18 Mar 2021
 *
 * BaseFragment.kt - A simple class contains some modifications to the native Fragment.
 * This Class use with or without ViewBinding property.
 * also you have use this class in JAVA or KOTLIN both language.
 *
 * use of this class
 * you have to extend your Activity using this class like.
 * in Java :- {class MainFragment extends BaseFragment}
 * in Kotlin :- {class MainFragment : BaseFragment()}
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseFragment : Fragment(), View.OnClickListener {

    /**
     * your log tag name
     */
    @Suppress("PropertyName")
    val TAG = javaClass.simpleName

    /**
     * your activity context object
     */
    val mContext: BaseActivity
        get() {
            return requireActivity() as BaseActivity
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        getLayoutRes()?.let {
            return inflater.inflate(it, container, false)
        }

        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)

        initView()
        loadAds()
        initViewAction()
        initViewListener()
    }

    override fun onResume() {
        super.onResume()
        if (!mContext.isNeedToLoadAd) {
            setDefaultAdUI()
        }
    }

    /**
     * This method for load your all type of ads
     */
    private fun loadAds() {
        setDefaultAdUI()
        if (mContext.isOnline) {
            initAds()
        }
    }

    /**
     * This method for set your layout
     *
     * without using viewBinding @return Your Layout File Resource Id Lick This 'return R.layout.fragment_main'
     *
     * or if you use viewBinding then @return 'return null' for base reference
     */
    @UiThread
    @LayoutRes
    abstract fun getLayoutRes(): Int?

    //<editor-fold desc="Open Function">
    /**
     * For Init All Ads.
     */
    @UiThread
    open fun setDefaultAdUI() {
    }

    @UiThread
    open fun initAds() {
    }

    /**
     * For Init Your Layout File's All View
     */
    @UiThread
    open fun initView() {
    }

    /**
     * For Init Your Default Action Performance On View
     */
    @UiThread
    open fun initViewAction() {
    }

    /**
     * For Set Your All Type Of Listeners
     */
    @UiThread
    open fun initViewListener() {
    }

    /**
     * For Set Your All View Click Listener,
     * now you no need to write multiple line code for 'View.setOnClickListener(this)'
     * just call this method and pass your all view like
     *
     * setClickListener(view1, view2)
     *
     * @param fViews list of your all passed view's.
     */
    @UiThread
    open fun setClickListener(vararg fViews: View) {
        for (lView in fViews) {
            lView.setOnClickListener(this)
        }
    }
    //</editor-fold>

    override fun onClick(v: View) {

    }
}