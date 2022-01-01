package com.example.app.ads.helper.openad

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.example.app.ads.helper.openad.OpenAdHelper.isShowOpenAd
import com.google.android.gms.ads.AdActivity

class OpenAdManager(
    private val myApplication: Application
) : Application.ActivityLifecycleCallbacks {

    private val TAG: String = javaClass.simpleName

    var mCurrentActivity: Activity? = null

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
    }

    //<editor-fold desc="ActivityLifecycleCallback methods">
    override fun onActivityResumed(activity: Activity) {
        mCurrentActivity = activity
        Log.e(TAG, "onActivityResumed: ${(activity is AdActivity)}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        mCurrentActivity = null
    }

    override fun onActivityStopped(activity: Activity) {
        mCurrentActivity = null
    }

    override fun onActivityStarted(activity: Activity) {
//        Log.e(TAG, "onActivityStarted: $activity")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityPaused(activity: Activity) {
        Log.e(TAG, "onActivityPaused: ")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    //</editor-fold>

    fun showOpenAd() {
        mCurrentActivity?.let {

            Log.e(TAG, "showOpenAd: packageName::${it.packageName}")

            it.isShowOpenAd {
                Log.e(TAG, "showOpenAd: Ads Close")
            }
        }
    }
}