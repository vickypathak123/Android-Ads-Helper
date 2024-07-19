@file:Suppress("unused")

package com.example.app.ads.helper.openad

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.webkit.WebView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.app.ads.helper.activity.InterstitialNativeAdActivity
import com.example.app.ads.helper.initNetwork
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isEnableOpenAd
import com.example.app.ads.helper.isPiePlus
import com.example.app.ads.helper.logD
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.need_to_block_open_ad_internally
import com.example.app.ads.helper.reward.RewardedInterstitialAdHelper
import com.example.app.ads.helper.reward.RewardedVideoAdHelper
import com.example.app.ads.helper.setTestDeviceIds
import com.example.app.ads.helper.startShowingOpenAdInternally
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author Akshay Harsoda
 * @since 28 Nov 2022
 * @updated 25 Jun 2024
 */
abstract class AppOpenApplication : MultiDexApplication(), DefaultLifecycleObserver {

    @Suppress("PrivatePropertyName")
    private val TAG: String = "Admob_${javaClass.simpleName}"

    private val mActivityLifecycleManager: ActivityLifecycleManager by lazy { ActivityLifecycleManager(this@AppOpenApplication) }

    //<editor-fold desc="OnCreate Function">
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        initNetwork(fContext = this@AppOpenApplication)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    //</editor-fold>

    abstract fun onResumeApp(fCurrentActivity: Activity): Boolean

    open fun destroyAllLoadedAd() {
        InterstitialAdHelper.destroy()
        AppOpenAdHelper.destroy()
        RewardedInterstitialAdHelper.destroy()
        RewardedVideoAdHelper.destroy()
//        NativeAdvancedModelHelper.destroy()
    }

    //<editor-fold desc="Init Ads & Set Test Device Id">
    open fun initMobileAds(vararg fDeviceId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            setMobileAds(fDeviceId = fDeviceId)
        }
    }

    private fun setMobileAds(vararg fDeviceId: String) {
        if (isPiePlus) {
            val processName = getProcessName(applicationContext)
            if (processName != null && packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
                MobileAds.initialize(baseContext) {
                    setDeviceIds(fDeviceId = fDeviceId)
                }
            } else {
                MobileAds.initialize(baseContext) {
                    setDeviceIds(fDeviceId = fDeviceId)
                }
            }
        } else {
            MobileAds.initialize(baseContext) {
                setDeviceIds(fDeviceId = fDeviceId)
            }
        }
    }

    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }

    private fun setDeviceIds(vararg fDeviceId: String) {
        logD(tag = TAG, message = "setDeviceIds: MobileAds Initialization Complete")
        setTestDeviceIds(*fDeviceId)
    }
    //</editor-fold>

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logI(tag = TAG, message = "onStart: Before Change isAppForeground::$isAppForeground")
        isAppForeground = true
        logI(tag = TAG, message = "onStart: After Change isAppForeground::$isAppForeground")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        logI(tag = TAG, message = "onStop: Before Change isAppForeground::$isAppForeground")
        isAppForeground = false
        logI(tag = TAG, message = "onStop: After Change isAppForeground::$isAppForeground")

//        NativeAdvancedHelper.startAdClickTimer()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        logI(tag = TAG, message = "onResume: ")
        if (isEnableOpenAd) {
            if (isAppForeground) {
                mActivityLifecycleManager.mCurrentActivity?.let { fCurrentActivity ->
                    if (fCurrentActivity !is AdActivity) {
                        logI(tag = TAG, message = "onResume: Current Activity Is Not Ad Activity, isAnyAdOpen::$isAnyAdOpen")
                        if (!isAnyAdOpen) {
                            if (fCurrentActivity !is InterstitialNativeAdActivity) {
                            logI(tag = TAG, message = "onResume: Need To Show Open Ad needToBlockOpenAdInternally::$need_to_block_open_ad_internally")
                            if (!need_to_block_open_ad_internally) {
                                val lDeveloperResumeFlag: Boolean = onResumeApp(fCurrentActivity)
                                logI(tag = TAG, message = "onResume: Need To Show Open Ad yourResumeFlag::$lDeveloperResumeFlag")
                                if (lDeveloperResumeFlag) {
                                    mActivityLifecycleManager.showOpenAd()
                                }
                            }
                            }
                        }
                    }
                }
            }

            if (need_to_block_open_ad_internally) {
                startShowingOpenAdInternally()
            }
        }
    }
}