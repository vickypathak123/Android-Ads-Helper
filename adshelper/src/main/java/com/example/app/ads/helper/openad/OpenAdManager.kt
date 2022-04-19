package com.example.app.ads.helper.openad

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.openad.OpenAdHelper.isShowOpenAd
import com.google.android.gms.ads.AdActivity

class OpenAdManager(
    private val myApplication: Application
) : Application.ActivityLifecycleCallbacks {

    private val TAG: String = "Admob_${javaClass.simpleName}"

    var mCurrentActivity: Activity? = null

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
    }

    //<editor-fold desc="ActivityLifecycleCallback methods">
    override fun onActivityResumed(activity: Activity) {
        mCurrentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        mCurrentActivity = null

//        if (!isAppForeground) {
//            if (FullScreenNativeAdDialog.isDialogShowing) {
//                FullScreenNativeAdDialog.dismissDialog()
//            }
//        }

    }

    override fun onActivityStopped(activity: Activity) {
        mCurrentActivity = null

//        if (FullScreenNativeAdDialog.isDialogShowing) {
//            FullScreenNativeAdDialog.dismissDialog()
//        }
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    //</editor-fold>

    fun showOpenAd() {
        mCurrentActivity?.let {
            it.isShowOpenAd {
                Log.i(TAG, "showOpenAd: Ads Close")
            }
        }
    }
}