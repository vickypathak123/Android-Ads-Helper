@file:Suppress("unused")

package com.example.ads.helper.new_.demo.base.utils

import android.Manifest
import android.app.Activity
import com.akshay.harsoda.permission.helper.AksPermission

private fun Activity.isPermissionGranted(fPermissions: Collection<String>): Boolean {
    return (AksPermission.isGranted(this, fPermissions))
}

private fun Activity.checkPermission(fPermissions: Collection<String>, onPermissionGranted: () -> Unit) {
    if (this.isPermissionGranted(fPermissions = fPermissions)) {
        onPermissionGranted.invoke()
    } else {
//        AdMobAppOpenAdHelper.stopShowingAppOpenAdInternally()
        AksPermission.with(this)
            .permissions(fPermissions)
            .request(
                onGrantedResult = {
//                    AdMobAppOpenAdHelper.stopShowingAppOpenAdInternally()
                    onPermissionGranted.invoke()
                },
                onDeniedResult = {
//                    AdMobAppOpenAdHelper.stopShowingAppOpenAdInternally()
                },
                onPermanentlyDeniedResult = {
//                    AdMobAppOpenAdHelper.stopShowingAppOpenAdInternally()
                },
            )
    }
}

fun Activity.getAllRequiredPermission(onPermissionGranted: () -> Unit) {
    val lPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        if (isTiramisuPlus()) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        },
    )
    this.checkPermission(fPermissions = lPermissions.asList(), onPermissionGranted = onPermissionGranted)
}