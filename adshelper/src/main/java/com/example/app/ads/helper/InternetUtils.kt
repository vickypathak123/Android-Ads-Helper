package com.example.app.ads.helper

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


/**
 * @author Akshay Harsoda
 * @since 27 Jun 2024
 */


private const val TAG: String = "Admob_InternetUtils"


class InternetLiveData<T> : MutableLiveData<T>() {
    private val observers = mutableMapOf<Observer<in T>, Observer<in T>>()

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        observers[observer] = observer
        super.observe(owner, observer)
    }

    override fun observeForever(observer: Observer<in T>) {
        observers[observer] = observer
        super.observeForever(observer)
    }

    override fun removeObserver(observer: Observer<in T>) {
        observers.remove(observer)
        super.removeObserver(observer)
    }

    fun hasObserver(observer: Observer<in T>): Boolean {
        return observers.containsKey(observer)
    }
}

/**
 * Return true if internet or wi-fi connection is working fine
 * <p>
 * Required permission
 * <uses-permission android:name="android.permission.INTERNET"/>
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 *
 * @return true if you have the internet connection, or false if not.
 */
inline val isOnline: Boolean
    get() {
        return isInternetAvailable.value == true
    }
val isInternetAvailable: InternetLiveData<Boolean> = InternetLiveData()

private fun getUIThread(runOnUIThread: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        runOnUIThread.invoke()
    }
}

//<editor-fold desc="Network Related">
private val networkRequest = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    .build()

private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    // network is available for use
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        getUIThread {
            val oldValue = isInternetAvailable.value
            logW(tag = TAG, message = "onAvailable: oldValue::-> $oldValue")
            if (oldValue == false) {
                isInternetAvailable.value = true
            }
        }
    }

    // lost network connection
    override fun onLost(network: Network) {
        super.onLost(network)
        getUIThread {
            val oldValue = isInternetAvailable.value
            logW(tag = TAG, message = "onLost: oldValue::-> $oldValue")
            if (oldValue == true) {
                isInternetAvailable.value = false
            }
        }
    }

    override fun onUnavailable() {
        super.onUnavailable()
        getUIThread {
            val oldValue = isInternetAvailable.value
            logW(tag = TAG, message = "onUnavailable: oldValue::-> $oldValue")
            if (oldValue == true) {
                isInternetAvailable.value = false
            }
        }
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val isValidated: Boolean = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isInternet: Boolean = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)


        logI(tag = TAG, message = "onCapabilitiesChanged: isValidated::-> $isValidated, isInternet::-> $isInternet")
        val isAvailable: Boolean = (isValidated && isInternet)

        getUIThread {
            val oldValue = isInternetAvailable.value
            if (oldValue != isAvailable) {
                logI(tag = TAG, message = "onCapabilitiesChanged: oldValue::-> $oldValue, newValue::-> $isAvailable")
                isInternetAvailable.value = isAvailable
            }
        }
    }
}

private val Context.isOnlineApp: Boolean
    get() {
        (getSystemService(ConnectivityManager::class.java)).let { connectivityManager ->
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.let {
                val isValidated: Boolean = it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val isInternet: Boolean = it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                return (isValidated && isInternet)
            }
        }
        return false
    }
//</editor-fold>

internal fun initNetwork(fContext: Application) {
    isInternetAvailable.value = fContext.isOnlineApp
    (fContext.getSystemService(ConnectivityManager::class.java)).requestNetwork(networkRequest, networkCallback)
}