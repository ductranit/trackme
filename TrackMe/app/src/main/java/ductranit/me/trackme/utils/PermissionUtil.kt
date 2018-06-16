package ductranit.me.trackme.utils

import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import javax.inject.Inject

class PermissionUtil @Inject constructor(private val sharePref: SharedPreferences) {
    fun checkPermission(context: Activity, permission: String, listener: PermissionAskListener) {
        /*
        * If permission is not granted
        * */
        if (shouldAskPermission(context, permission)) {
            /*
            * If permission denied previously
            * */
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                listener.onPermissionPreviouslyDenied()
            } else {
                /*
                * Permission denied or first time requested
                * */
                if (sharePref.getBoolean(permission, true)) {
                    sharePref.edit().putBoolean(permission, false).apply()
                    listener.onNeedPermission()
                } else {
                    /*
                    * Handle the feature without permission or ask user to manually allow permission
                    * */
                    listener.onPermissionDisabled()
                }
            }
        } else {
            listener.onPermissionGranted()
        }
    }

    /*
    * Check if version is marshmallow and above.
    * Used in deciding to ask runtime permission
    * */
    private fun shouldAskPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun shouldAskPermission(context: Activity, permission: String): Boolean {
        if (shouldAskPermission()) {
            val permissionResult = ActivityCompat.checkSelfPermission(context, permission)
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }


    /*
    * Callback on various cases on checking permission
    *
    * 1.  Below M, runtime permission not needed. In that case onPermissionGranted() would be called.
    *     If permission is already granted, onPermissionGranted() would be called.
    *
    * 2.  Above M, if the permission is being asked first time onNeedPermission() would be called.
    *
    * 3.  Above M, if the permission is previously asked but not granted, onPermissionPreviouslyDenied()
    *     would be called.
    *
    * 4.  Above M, if the permission is disabled by device policy or the user checked "Never ask again"
    *     check box on previous request permission, onPermissionDisabled() would be called.
    * */
    interface PermissionAskListener {
        /*
        * Callback to ask permission
        * */
        fun onNeedPermission()

        /*
        * Callback on permission denied
        * */
        fun onPermissionPreviouslyDenied()

        /*
        * Callback on permission "Never show again" checked and denied
        * */
        fun onPermissionDisabled()

        /*
        * Callback on permission granted
        * */
        fun onPermissionGranted()
    }
}