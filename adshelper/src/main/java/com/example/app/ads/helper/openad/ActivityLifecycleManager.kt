@file:Suppress("unused")

package com.example.app.ads.helper.openad

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.app.ads.helper.openad.AppOpenAdHelper.showAppOpenAd

/**
 * @author Akshay Harsoda
 * @since 28 Nov 2022
 * @updated 25 Jun 2024
 */
class ActivityLifecycleManager(
    myApplication: Application
) : Application.ActivityLifecycleCallbacks {

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
    }

    override fun onActivityStopped(activity: Activity) {
        mCurrentActivity = null
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
        mCurrentActivity?.let { it.showAppOpenAd {} }
    }
}