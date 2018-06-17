package ductranit.me.trackme.utils

import ductranit.me.trackme.BuildConfig

class Constants {
    companion object {
        const val SHARE_PREF_NAME = "TrackMe_prefs"
        const val SESSION_ID = "SESSION_ID"
        const val INVALID_ID = Long.MIN_VALUE
        const val MAP_ZOOM_LEVEL = 15f
        const val MIN_DISTANCE = 2 // 2m
        const val MARKER_CIRCLE_RADIUS = 60.0

        // for location service
        const val KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES"
        const val KEY_LOCATION_LATITUDE = "KEY_LOCATION_LATITUDE"
        const val KEY_LOCATION_LONGITUDE = "KEY_LOCATION_LONGITUDE"
        const val KEY_LOCATION_SPEED = "KEY_LOCATION_SPEED"

        const val ACTION_BROADCAST = BuildConfig.APPLICATION_ID + "broadcast"
        const val EXTRA_LOCATION = BuildConfig.APPLICATION_ID + ".location"
        const val EXTRA_STARTED_FROM_NOTIFICATION = BuildConfig.APPLICATION_ID + ".started_from_notification"
        /**
         * The name of the channel for notifications.
         */
        const val LOCATION_CHANNEL_ID = "channel_location"

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 30 * 1000

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value.
         */
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

        /**
         * The identifier for the notification displayed for the foreground service.
         */
        const val NOTIFICATION_ID = 12345678

        // intent
        const val PERMISSIONS_REQUEST = 1
    }
}