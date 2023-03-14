@file:Suppress("unused")

package com.example.ads.helper.demo.base

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.ads.helper.demo.base.utils.isOnline
import com.example.ads.helper.demo.isNeedToLoadAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * @author Akshay Harsoda
 * @since 18 Mar 2021
 *
 * BaseActivity.kt - A simple class contains some modifications to the native Activity.
 * This Class use with or without ViewBinding property.
 * also you have use this class in JAVA or KOTLIN both language.
 *
 * use of this class
 * you have to extend your Activity using this class like.
 * in Java :- {class MainActivity extends BaseActivity}
 * in Kotlin :- {class MainActivity : BaseActivity()}
 *
 * NOTE :- if you use this class with ViewBinding then you must override onCreate() method of Activity
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseActivity : AppCompatActivity(), CoroutineScope, View.OnClickListener {

    /**
     * your log tag name
     */
    @Suppress("PropertyName")
    val TAG: String = javaClass.simpleName

    var mJob: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    /**
     * your activity context object
     */
    val mActivity: BaseActivity
        get() {
            return getActivityContext()
        }

    //<editor-fold desc="For Start Activity Result">
    private var mOnActivityResult: (resultCode: Int, data: Intent?) -> Unit = { _, _ -> }

    private val intentLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->

            val resultCode = result.resultCode
            val data = result.data

            mOnActivityResult.invoke(resultCode, data)
        }

    private val intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            mOnActivityResult.invoke(resultCode, data)
        }
    //</editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        setParamBeforeLayoutInit()
        super.onCreate(null)

        getLayoutRes()?.let {
            setContentView(it)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!mActivity.isNeedToLoadAd) {
            setDefaultAdUI()
        }
        initJob()
    }

    /**
     * If You Not Using viewBinding
     * @param layoutResID Pass Your Layout File Resource Id Like This 'R.layout.activity_main'
     */
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setContentView()
    }

    /**
     * If You Using viewBinding
     * @param view pass Your Layout File Lick 'ActivityMainBinding.inflate(getLayoutInflater()).getRoot()'
     */
    override fun setContentView(view: View) {
        super.setContentView(view)
        setContentView()
    }

    private fun setContentView() {
        initView()
        loadAds()
        initViewAction()
        initViewListener()
    }

    //<editor-fold desc="Default Function">
    /**
     * This Method for set you Activity Context
     *
     * @return
     * in Java :- {return MainActivity.this;}
     * in Kotlin :- {return this@MainActivity}
     */
    @UiThread
    abstract fun getActivityContext(): BaseActivity

    /**
     * This method for set your layout
     *
     * If you use this class without using ViewBinding,
     * then you don't need to override onCreate() method
     *
     * without using viewBinding @return Your Layout File Resource Id Lick This 'return R.layout.activity_main'
     *
     * or if you use viewBinding then @return 'return null' for base reference
     */
    @UiThread
    @LayoutRes
    abstract fun getLayoutRes(): Int?
    //</editor-fold>

    //<editor-fold desc="Open Function">
    /**
     * This method for set-up your data before call setContentView()
     */
    open fun setParamBeforeLayoutInit() {}

    /**
     * This method For Init All Ads.
     */
    @UiThread
    open fun setDefaultAdUI() {
    }

    @UiThread
    open fun initAds() {
    }

    /**
     * This method For Init Your Layout File's All View
     */
    @UiThread
    open fun initView() {
    }

    /**
     * This method For Init Your Default Action Performance On View
     */
    @UiThread
    open fun initViewAction() {
    }

    /**
     * This method For Set Your All Type Of Listeners
     */
    @UiThread
    open fun initViewListener() {
    }

    /**
     * This method For Set Your All View Click Listener,
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

    //<editor-fold desc="Call On Start Screen">
    /**
     * This method for initialized your Coroutine job
     */
    private fun initJob() {
        mJob = Job()
    }

    /**
     * This method for load your all type of ads
     */
    private fun loadAds() {
        setDefaultAdUI()
//        if (isOnline) {
            initAds()
//        }
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
        val lIntent = Intent(mActivity, T::class.java)

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
     * @param isCheckResolveActivity pass false if you don't check resolveActivity of this Intent
     * @param fEnterAnimId your activity Enter animation
     * @param fExitAnimId your activity Exit animation
     * @param onActivityResult in this method you get your Result
     */
    open fun launchActivityForResult(
        fIntent: Intent,
        isCheckResolveActivity: Boolean = true,
        @AnimatorRes @AnimRes fEnterAnimId: Int = android.R.anim.fade_in,
        @AnimatorRes @AnimRes fExitAnimId: Int = android.R.anim.fade_out,
        onActivityResult: (resultCode: Int, data: Intent?) -> Unit
    ) {
        mOnActivityResult = onActivityResult

        if (isCheckResolveActivity) {
            if (fIntent.resolveActivity(packageManager) != null) {
                intentLauncher.launch(
                    fIntent,
                    ActivityOptionsCompat.makeCustomAnimation(mActivity, fEnterAnimId, fExitAnimId)
                )
            }
        } else {
            intentLauncher.launch(
                fIntent,
                ActivityOptionsCompat.makeCustomAnimation(mActivity, fEnterAnimId, fExitAnimId)
            )
        }
    }

    /**
     * This Method will replace your default method of [startIntentSenderForResult]
     *
     * @param fIntentSender Your Launcher Screen IntentSender
     * @param fEnterAnimId your activity Enter animation
     * @param fExitAnimId your activity Exit animation
     * @param onActivityResult in this method you get your Result
     */
    open fun launchActivityForResult(
        fIntentSender: IntentSender,
        @AnimatorRes @AnimRes fEnterAnimId: Int = android.R.anim.fade_in,
        @AnimatorRes @AnimRes fExitAnimId: Int = android.R.anim.fade_out,
        onActivityResult: (resultCode: Int, data: Intent?) -> Unit
    ) {
        mOnActivityResult = onActivityResult

        val intentSenderRequest: IntentSenderRequest = IntentSenderRequest.Builder(fIntentSender).build()

        intentSenderLauncher.launch(
            intentSenderRequest,
            ActivityOptionsCompat.makeCustomAnimation(mActivity, fEnterAnimId, fExitAnimId)
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
        mActivity.startActivity(fIntent)
        mActivity.overridePendingTransition(fEnterAnimId, fExitAnimId)

        if (isNeedToFinish) {
            mActivity.finish()
        }
    }
    //</editor-fold>

    //<editor-fold desc="Update Fragment">
    /**
     * This method For Update Fragment & Attach In FrameLayout
     *
     * you have call this method using FrameLayout.
     * @param fFragment your fragment
     * @param enterAnim your fragment enter animation file
     * @param exitAnim your fragment exit animation file
     */
    @UiThread
    open fun FrameLayout.updateFragment(
        fFragment: Fragment,
        @AnimatorRes @AnimRes enterAnim: Int = android.R.anim.fade_in,
        @AnimatorRes @AnimRes exitAnim: Int = android.R.anim.fade_out,
    ) {

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(enterAnim, exitAnim)
            .replace(this.id, fFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
    //</editor-fold>

    override fun onClick(v: View) {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }
}