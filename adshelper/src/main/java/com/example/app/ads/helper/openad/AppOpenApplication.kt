package com.example.app.ads.helper.openad

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.app.ads.helper.dialogs.FullScreenNativeAdDialog
import com.example.app.ads.helper.isAppForeground
import com.example.app.ads.helper.isOpenAdEnable
import com.example.app.ads.helper.isAnyAdOpen
import com.example.app.ads.helper.isInterstitialAdShow
import com.example.app.ads.helper.setTestDeviceIds
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.collections.ArrayList

open class AppOpenApplication : MultiDexApplication(), DefaultLifecycleObserver {

    private val TAG: String = "AppOpenApplication"

    // variable to track pause event time
    private var isPause: Boolean = false
    private var mLastPauseTime: Long = 0
    private val mMinPauseDuration = 100

    private var mOpenAdManager: OpenAdManager? = null

    private val mTestDeviceIds: ArrayList<String> = ArrayList()

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
        Log.e(TAG, "onCreate: ")
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        mOpenAdManager = OpenAdManager(this@AppOpenApplication)

    }

    fun setAppLifecycleListener(fAppLifecycleListener: AppLifecycleListener) {
        this.mAppLifecycleListener = fAppLifecycleListener
    }

    fun setDeviceId(vararg fTestDeviceIds: String) {
        mTestDeviceIds.addAll(fTestDeviceIds)
    }

    fun initMobileAds(isAppInTesting: Boolean) {
        setMobileAds(isAppInTesting = isAppInTesting)
    }

    private fun setDeviceIds(isAppInTesting: Boolean) {
        if (isAppInTesting) {
            mTestDeviceIds.add(getAdsTestDeviceId())
            setTestDeviceIds(*mTestDeviceIds.toTypedArray())
        }
    }

    private fun setMobileAds(isAppInTesting: Boolean) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(applicationContext)
            if (processName != null && packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
                MobileAds.initialize(baseContext) {
                    Log.d(TAG, "onInitializationComplete.1")
                    setDeviceIds(isAppInTesting = isAppInTesting)
                }
            } else {
                MobileAds.initialize(baseContext) {
                    Log.d(TAG, "onInitializationComplete.2")
                    setDeviceIds(isAppInTesting = isAppInTesting)
                }
            }
        } else {
            MobileAds.initialize(baseContext) {
                Log.d(TAG, "onInitializationComplete.3")
                setDeviceIds(isAppInTesting = isAppInTesting)
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

    @SuppressLint("HardwareIds")
    private fun getAdsTestDeviceId(): String {
        return try {
            val androidId: String = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            val md5Data = customMD5(androidId)
            val deviceId = md5Data?.uppercase(java.util.Locale.ENGLISH) ?: "null"
            println("getDeviceId: $deviceId")
            deviceId
        } catch (e: Exception) {
            e.toString()
        }
    }

    private fun customMD5(md5: String): String? {
        try {
            val md = MessageDigest.getInstance("MD5")
            val array = md.digest(md5.toByteArray())
            val sb = StringBuffer()
            for (i in array.indices) {
                sb.append(Integer.toHexString(array[i].toInt() and 0xFF or 0x100).substring(1, 3))
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
        }
        return null
    }

    //<editor-fold desc="For Application Lifecycle">
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Log.e(TAG, "onPause: ")
        mLastPauseTime = SystemClock.elapsedRealtime()
        isPause = true
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.e(TAG, "onStart: onAppForegrounded: ")
        isAppForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.e(TAG, "onStop: onAppBackgrounded: ")
        isAppForeground = false

        if (SystemClock.elapsedRealtime() - mLastPauseTime < mMinPauseDuration) {
            Log.e(TAG, "onStop: Reset Pause Flag")
            if (isPause) {
                isPause = false
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.e(TAG, "onDestroy: ")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.e(TAG, "onResume")
        if (isOpenAdEnable) {
            mAppLifecycleListener?.let { lListener ->
                mOpenAdManager?.let { lOpenAdManager ->
                    if (isAppForeground && !isPause) {
                        lOpenAdManager.mCurrentActivity?.let { fCurrentActivity ->
                            if (fCurrentActivity !is AdActivity) {
                                if (isAnyAdOpen) {
                                    isAnyAdOpen = false
                                } else {
                                    if (!FullScreenNativeAdDialog.isDialogShowing && !isInterstitialAdShow) {
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