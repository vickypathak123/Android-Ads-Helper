@file:Suppress("unused")

package com.example.app.base

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
import com.example.app.base.utils.isOnline

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
    val mContext: FragmentActivity
        get() {
            return requireActivity()
        }

    //<editor-fold desc="For Start Activity Result">
    private var mRequestCode: Int = 0

    private val launcher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            fromActivityResult(
                requestCode = mRequestCode,
                resultCode = result.resultCode,
                data = result.data
            )
        }
    //</editor-fold>

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
        initViewAction()
        initViewListener()
    }

    override fun onResume() {
        super.onResume()
        loadAds()
    }

    /**
     * This method for load your all type of ads
     */
    private fun loadAds() {
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

    //<editor-fold desc="New Activity Intent">
    /**
     * This Method for get your next activity intent
     *
     * @param isAddFlag [Default value:- true] for set up your activity flag in your intent
     * @param fBundle lambda fun for pass data throw intent
     */
    inline fun <reified T : Activity> getActivityIntent(
        isAddFlag: Boolean = true,
        fBundle: Bundle.() -> Unit = {},
    ): Intent {
        val lIntent = Intent(mContext, T::class.java)

        if (isAddFlag) {
            lIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            lIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        lIntent.putExtras(Bundle().apply(fBundle))

        return lIntent
    }

    /**
     * This Method will replace your default method of [startActivityForResult]
     *
     * @param fIntent Your Launcher Screen Intent
     * @param fRequestCode Your Request Code For Get Result Of Your Next Activity
     * @param fEnterAnimId your activity Enter animation
     * @param fExitAnimId your activity Exit animation
     */
    open fun launchActivityForResult(
        fIntent: Intent,
        fRequestCode: Int,
        @AnimatorRes @AnimRes fEnterAnimId: Int = android.R.anim.fade_in,
        @AnimatorRes @AnimRes fExitAnimId: Int = android.R.anim.fade_out
    ) {
        mRequestCode = fRequestCode
        launcher.launch(
            fIntent,
            ActivityOptionsCompat.makeCustomAnimation(mContext, fEnterAnimId, fExitAnimId)
        )
    }

    /**
     * This Method will replace your default method of [startActivity]
     *
     * @param fIntent Your Launcher Screen Intent
     * @param isNeedToFinish [Default value:- false] pass [isNeedToFinish = true] for finish your caller screen after call next screen
     * @param fEnterAnimId your activity Enter animation
     * @param fExitAnimId your activity Exit animation
     */
    open fun launchActivity(
        fIntent: Intent,
        isNeedToFinish: Boolean = false,
        @AnimatorRes @AnimRes fEnterAnimId: Int = android.R.anim.fade_in,
        @AnimatorRes @AnimRes fExitAnimId: Int = android.R.anim.fade_out
    ) {
        mContext.startActivity(fIntent)
        mContext.overridePendingTransition(fEnterAnimId, fExitAnimId)

        if (isNeedToFinish) {
            mContext.finish()
        }
    }

    /**
     * This Method will replace your default method of [onActivityResult]
     *
     * @param requestCode The integer request code originally supplied to launchActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @UiThread
    open fun fromActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
    //</editor-fold>

    override fun onClick(v: View) {

    }
}