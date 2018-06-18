package ductranit.me.trackme.utils

import android.app.Activity
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity

class LocationUtils {
    companion object {
        fun isGPSEnable(activity: Activity): Boolean {
            val locationManager = activity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }
}