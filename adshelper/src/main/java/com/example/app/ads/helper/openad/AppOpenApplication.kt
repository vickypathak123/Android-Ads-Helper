package com.example.app.ads.helper.openad

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.webkit.WebView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.app.ads.helper.NativeAdvancedHelper
import com.example.app.ads.helper.NativeAdvancedModelHelper
import com.example.app.ads.helper.activity.FullScreenNativeAdDialogActivity
import com.example.app.ads.helper.interstitialad.InterstitialAdHelper
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isInterstitialAdShow
import com.example.app.ads.helper.isOpenAdEnable
import com.example.app.ads.helper.logD
import com.example.app.ads.helper.logI
import com.example.app.ads.helper.needToBlockOpenAdInternally
import com.example.app.ads.helper.reward.RewardedInterstitialAdHelper
import com.example.app.ads.helper.reward.RewardedVideoAdHelper
import com.example.app.ads.helper.setTestDeviceIds
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds

open class AppOpenApplication : MultiDexApplication(), DefaultLifecycleObserver {

    interface AppLifecycleListener {
        fun onResumeApp(fCurrentActivity: Activity): Boolean
    }

    private val mTAG: String = "Admob_${javaClass.simpleName}"

    private var mActivityLifecycleManager: ActivityLifecycleManager? = null

    private var mAppLifecycleListener: AppLifecycleListener? = null
    var isNeedToShowAds = true;
    var remoteConfig = true;

    //<editor-fold desc="OnCreate Function">
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        mActivityLifecycleManager = ActivityLifecycleManager(this@AppOpenApplication)

    }
    //</editor-fold>

    fun setAppLifecycleListener(
        fAppLifecycleListener: AppLifecycleListener,
        isNeedToShowAds: Boolean = true,
    ) {
        this.mAppLifecycleListener = fAppLifecycleListener
        this.isNeedToShowAds = isNeedToShowAds
    }

    fun destroyAllLoadedAd() {
        InterstitialAdHelper.destroy()
        AppOpenAdHelper.destroy()
        RewardedInterstitialAdHelper.destroy()
        RewardedVideoAdHelper.destroy()
        NativeAdvancedModelHelper.destroy()
    }

    //<editor-fold desc="Init Ads & Set Test Device Id">
    fun initMobileAds(vararg fDeviceId: String) {
        setMobileAds(fDeviceId = fDeviceId)
    }

    private fun setDeviceIds(vararg fDeviceId: String) {
        logD(tag = mTAG, message = "setDeviceIds: MobileAds Initialization Complete")
        setTestDeviceIds(*fDeviceId)
    }

    private fun setMobileAds(vararg fDeviceId: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
    //</editor-fold>

    //<editor-fold desc="For Application Lifecycle">
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isAppForeground = true
        logI(tag = mTAG, message = "onStart: isAppForeground::$isAppForeground")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppForeground = false
        logI(tag = mTAG, message = "onStop: isAppForeground::$isAppForeground")

        NativeAdvancedHelper.startAdClickTimer()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        logI(tag = mTAG, message = "onResume: ")
        if (isOpenAdEnable) {
            mAppLifecycleListener?.let { lListener ->
                logI(tag = mTAG, message = "onResume: LifecycleListener Not Null")
                mActivityLifecycleManager?.let { lOpenAdManager ->
                    logI(
                        tag = mTAG,
                        message = "onResume: OpenAdManager Not Null isAppForeground::$isAppForeground"
                    )
                    if (isAppForeground) {
                        lOpenAdManager.mCurrentActivity?.let { fCurrentActivity ->
                            if (fCurrentActivity !is AdActivity) {
                                logI(
                                    tag = mTAG,
                                    message = "onResume: Current Activity Is Not Ad Activity, isAnyAdOpen::$isAnyAdOpen, isInterstitialAdShow::$isInterstitialAdShow"
                                )
                                if (isAnyAdOpen) {
                                    isAnyAdOpen = false
                                } else {
                                    if (fCurrentActivity !is FullScreenNativeAdDialogActivity && !isInterstitialAdShow) {
                                        logI(
                                            tag = mTAG,
                                            message = "onResume: Need To Show Open Ad needToBlockOpenAdInternally::$needToBlockOpenAdInternally"
                                        )
                                        if (!needToBlockOpenAdInternally) {
                                            val lDeveloperResumeFlag: Boolean =
                                                lListener.onResumeApp(fCurrentActivity)
                                            logI(
                                                tag = mTAG,
                                                message = "onResume: Need To Show Open Ad yourResumeFlag::$lDeveloperResumeFlag"
                                            )
                                            if (lDeveloperResumeFlag) {
                                                lOpenAdManager.showOpenAd(
                                                    isNeedToShowAds
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (needToBlockOpenAdInternally) {
                        needToBlockOpenAdInternally = false
                    }
                }
            }
        }
    }
    //</editor-fold>
}