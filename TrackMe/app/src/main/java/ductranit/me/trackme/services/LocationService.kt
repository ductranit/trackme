/*
 * Copyright (C) 2018 ductranit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ductranit.me.trackme.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.support.annotation.Nullable
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.*
import dagger.android.AndroidInjection
import ductranit.me.trackme.GlobalApp
import ductranit.me.trackme.R
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.main.views.MainActivity
import ductranit.me.trackme.utils.Constants.Companion.ACTION_CLEAR_NOTIFICATION
import ductranit.me.trackme.utils.Constants.Companion.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_LATITUDE
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_LONGITUDE
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_SPEED
import ductranit.me.trackme.utils.Constants.Companion.KEY_REQUESTING_LOCATION_UPDATES
import ductranit.me.trackme.utils.Constants.Companion.LOCATION_CHANNEL_ID
import ductranit.me.trackme.utils.Constants.Companion.NOTIFICATION_ID
import ductranit.me.trackme.utils.Constants.Companion.UPDATE_INTERVAL_IN_MILLISECONDS
import ductranit.me.trackme.utils.putDouble
import timber.log.Timber
import javax.inject.Inject


class LocationService : Service() {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var app: GlobalApp

    @Inject
    lateinit var locationHandler: LocationHandler

    private var notificationManager: NotificationManager? = null

    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private var locationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var fusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Callback for changes in location.
     */
    private var locationCallback: LocationCallback? = null

    /**
     * The current location.
     */
    private var location: Location? = null

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        Timber.i("onCreate Service")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { onNewLocation(it) }
            }
        }

        createLocationRequest()
        getLastLocation()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        requestLocationUpdates()
        startForeground(NOTIFICATION_ID, getNotification(null))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.i("Service onStartCommand ${intent.action}")
        if (!app.wasInBackground && ACTION_CLEAR_NOTIFICATION == intent.action) {
            notificationManager?.cancel(NOTIFICATION_ID)
        }

        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        Timber.i("Service onDestroy")
        removeLocationUpdates()
        super.onDestroy()
    }


    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        Timber.i("Requesting location updates")
        sharedPreferences.edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES, true).apply()
        startService(Intent(applicationContext, LocationService::class.java))
        try {
            fusedLocationClient?.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper())
        } catch (unlikely: Throwable) {
            sharedPreferences.edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES, false).apply()
            Timber.e("Lost location permission. Could not request updates. $unlikely")
        }
    }

    private fun removeLocationUpdates() {
        Timber.i("Removing location updates")
        try {
            fusedLocationClient?.removeLocationUpdates(locationCallback)
            sharedPreferences.edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES, false).apply()
            stopSelf()
        } catch (unlikely: SecurityException) {
            sharedPreferences.edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES, true).apply()
            Timber.e("Lost location permission. Could not remove updates. $unlikely")
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest?.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    location = task.result
                    saveCurrentLocation()
                } else {
                    Timber.w("Failed to get location.")
                }
            }
        } catch (unlikely: SecurityException) {
            Timber.e("Lost location permission.$unlikely")
        }
    }

    private fun saveCurrentLocation() {
        if (location != null) {
            sharedPreferences.edit().putDouble(KEY_LOCATION_LATITUDE, location!!.latitude).apply()
            sharedPreferences.edit().putDouble(KEY_LOCATION_LONGITUDE, location!!.longitude).apply()
            sharedPreferences.edit().putFloat(KEY_LOCATION_SPEED, location!!.speed).apply()
        }
    }

    private fun onNewLocation(location: Location) {
        this.location = location
        saveCurrentLocation()
        locationHandler.locationUpdating(location, object : LocationUpdating {
            override fun onSessionReady(session: Session?) {
                if(session != null) {
                    getNotification(session)
                }
            }
        })
    }

    private fun getNotification(session: Session?): Notification {
        Timber.d("getNotification $session")
        var subText = ""
        if (session?.startTime != null) {
            val distance = getString(R.string.distance_text).format(session.distance / 1000)
            val speed = sharedPreferences.getFloat(KEY_LOCATION_SPEED, 0.0f)
            val speedText = getString(R.string.speed_text).format(speed * (3600 / 1000))
            subText = String.format("%s - %s", distance, speedText)
        }

        @Suppress("DEPRECATION")
        val builder = NotificationCompat.Builder(this, LOCATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setSubText(getString(R.string.location_running))
                .setContentText(subText)
                .setChannelId(LOCATION_CHANNEL_ID)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(LOCATION_CHANNEL_ID,
                    getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
            channel.description = getString(R.string.app_name)
            channel.setSound(null, null)
            notificationManager?.createNotificationChannel(channel)
            builder.setChannelId(LOCATION_CHANNEL_ID)
            builder.priority = NotificationManager.IMPORTANCE_LOW
        }

        val notification = builder.build()

        // The PendingIntent to launch activity.
        val activityIntent = Intent(this, MainActivity::class.java)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val activityPendingIntent = PendingIntent.getActivity(this, 0,
                activityIntent, 0)
        notification.contentIntent = activityPendingIntent
        notification.flags = notification.flags.or(Notification.FLAG_AUTO_CANCEL)
        notificationManager?.notify(NOTIFICATION_ID, notification)

        return notification
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.startService(intent)
        }

        fun startAndClearNotification(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            intent.action = ACTION_CLEAR_NOTIFICATION
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.stopService(intent)
        }
    }
}