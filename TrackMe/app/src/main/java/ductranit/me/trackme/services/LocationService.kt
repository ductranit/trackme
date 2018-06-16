package ductranit.me.trackme.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.*
import android.support.annotation.NonNull
import android.support.constraint.Constraints.TAG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import ductranit.me.trackme.R
import android.support.v4.content.LocalBroadcastManager
import timber.log.Timber
import android.support.v4.app.NotificationCompat
import ductranit.me.trackme.ui.tracking.views.TrackingActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import dagger.android.AndroidInjection
import ductranit.me.trackme.utils.Constants.Companion.ACTION_BROADCAST
import ductranit.me.trackme.utils.Constants.Companion.LOCATION_CHANNEL_ID
import ductranit.me.trackme.utils.Constants.Companion.EXTRA_LOCATION
import ductranit.me.trackme.utils.Constants.Companion.EXTRA_STARTED_FROM_NOTIFICATION
import ductranit.me.trackme.utils.Constants.Companion.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
import ductranit.me.trackme.utils.Constants.Companion.KEY_REQUESTING_LOCATION_UPDATES
import ductranit.me.trackme.utils.Constants.Companion.NOTIFICATION_ID
import ductranit.me.trackme.utils.Constants.Companion.UPDATE_INTERVAL_IN_MILLISECONDS
import javax.inject.Inject
import android.os.Looper


class LocationService : Service() {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

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

    private var serviceHandler: Handler? = null

    /**
     * The current location.
     */
    private var location: Location? = null

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { onNewLocation(it) }
            }
        }

        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(LOCATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            notificationManager?.createNotificationChannel(mChannel)
        }

        requestLocationUpdates()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.i("Service started")
        val startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false)

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
        serviceHandler?.removeCallbacksAndMessages(null)
    }

    private fun requestLocationUpdates() {
        Timber.i("Requesting location updates")
        sharedPreferences.edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES, true).apply()
        startService(Intent(applicationContext, LocationService::class.java))
        try {
            fusedLocationClient?.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper())
        } catch (unlikely: SecurityException) {
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
            fusedLocationClient?.lastLocation?.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(@NonNull task: Task<Location>) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        location = task.getResult()
                    } else {
                        Timber.w("Failed to get location.")
                    }
                }
            })
        } catch (unlikely: SecurityException) {
            Timber.e("Lost location permission.$unlikely")
        }

    }

    private fun onNewLocation(location: Location) {
        Timber.i("New location: $location")

        this.location = location

        // Notify anyone listening for broadcasts about the new location.
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            notificationManager?.notify(NOTIFICATION_ID, getNotification())
        }
    }

    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    @Suppress("DEPRECATION")
    private fun getNotification(): Notification {
        val intent = Intent(this, LocationService::class.java)


        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, TrackingActivity::class.java), 0)

        val builder = NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.app_name),
                        activityPendingIntent)
                .addAction(R.drawable.ic_close, getString(R.string.app_name),
                        servicePendingIntent)
                .setContentText(getString(R.string.app_name))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(LOCATION_CHANNEL_ID) // Channel ID
        }

        return builder.build()
    }

    @Suppress("DEPRECATION")
    /**
     * Returns true if this is a foreground service.
     *
     * @param context The [Context].
     */
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
                Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }

        return false
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.stopService(intent)
        }
    }
}