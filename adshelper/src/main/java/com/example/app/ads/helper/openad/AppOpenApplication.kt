package com.example.app.ads.helper.openad

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.webkit.WebView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.app.ads.helper.*
import com.example.app.ads.helper.activity.FullScreenNativeAdDialogActivity
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds

open class AppOpenApplication : MultiDexApplication(), DefaultLifecycleObserver {

    private val mTAG: String = "Admob_${javaClass.simpleName}"

    // variable to track pause event time
    private var isPause: Boolean = false
    private var mLastPauseTime: Long = 0
    private val mMinPauseDuration = 100

    private var mOpenAdManager: OpenAdManager? = null

    interface AppLifecycleListener {
        fun onResumeApp(fCurrentActivity: Activity): Boolean
    }

    private var mAppLifecycleListener: AppLifecycleListener? = null

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        mOpenAdManager = OpenAdManager(this@AppOpenApplication)

    }

    fun setAppLifecycleListener(fAppLifecycleListener: AppLifecycleListener) {
        this.mAppLifecycleListener = fAppLifecycleListener
    }

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

    //<editor-fold desc="For Application Lifecycle">
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        logI(tag = mTAG, message = "onPause: ")
        mLastPauseTime = SystemClock.elapsedRealtime()
        isPause = true
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isAppForeground = true
        logI(tag = mTAG, message = "onAppForegrounded: isAppForeground::$isAppForeground")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppForeground = false
        logI(tag = mTAG, message = "onAppBackgrounded: isAppForeground::$isAppForeground")

        NativeAdvancedHelper.startAdClickTimer()

        if (SystemClock.elapsedRealtime() - mLastPauseTime < mMinPauseDuration) {
            if (isPause) {
                isPause = false
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        logI(tag = mTAG, message = "onResume: ")
        if (isOpenAdEnable) {
            mAppLifecycleListener?.let { lListener ->
                mOpenAdManager?.let { lOpenAdManager ->
                    if (isAppForeground && !isPause) {
                        lOpenAdManager.mCurrentActivity?.let { fCurrentActivity ->
                            if (fCurrentActivity !is AdActivity) {
                                if (isAnyAdOpen) {
                                    isAnyAdOpen = false
                                } else {
                                    if (fCurrentActivity !is FullScreenNativeAdDialogActivity && !isInterstitialAdShow) {
                                        if (lListener.onResumeApp(fCurrentActivity)) {
                                            lOpenAdManager.showOpenAd()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isPause) {
                        isPause = false
                    }
                }
            }
        }
    }
    //</editor-fold>
}