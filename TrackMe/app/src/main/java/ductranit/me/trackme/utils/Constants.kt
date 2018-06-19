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

package ductranit.me.trackme.utils

import ductranit.me.trackme.BuildConfig

class Constants {
    companion object {
        const val SHARE_PREF_NAME = "TrackMe_prefs"
        const val INVALID_ID = Long.MIN_VALUE
        const val MAP_ZOOM_LEVEL = 15f
        const val MIN_DISTANCE = 3 // 3 meters
        const val MARKER_CIRCLE_RADIUS = 30.0
        const val TIMER_TICK = 1000L
        const val PATH_WIDTH = 5f

        // for session manager
        const val KEY_SESSION_ID = "session_id"
        const val KEY_SESSION_STATE = "session_state"
        const val KEY_SESSION_IS_ADD_NEW = "is_add_new"

        // for location service
        const val KEY_REQUESTING_LOCATION_UPDATES = "request_location_updates"
        const val KEY_LOCATION_LATITUDE = "location_latitude"
        const val KEY_LOCATION_LONGITUDE = "location_longitude"
        const val KEY_LOCATION_SPEED = "location_speed"

        const val ACTION_CLEAR_NOTIFICATION = BuildConfig.APPLICATION_ID + ".clear_notification"
        const val ACTION_BROADCAST = BuildConfig.APPLICATION_ID + ".broadcast"
        /**
         * The name of the channel for notifications.
         */
        const val LOCATION_CHANNEL_ID = "channel_location"

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10 * 1000

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value.
         */
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

        /**
         * The identifier for the notification displayed for the foreground service.
         */
        const val NOTIFICATION_ID = 12345678
    }
}