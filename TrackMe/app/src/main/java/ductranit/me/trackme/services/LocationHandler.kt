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

import android.content.Context
import android.content.Intent
import android.location.Location
import android.support.v4.content.LocalBroadcastManager
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.HistoryLocation
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.utils.Constants
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.Constants.Companion.MIN_DISTANCE
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

interface LocationUpdating {
    fun onSessionReady(session: Session?)
}

class LocationHandler @Inject constructor(private val context: Context,
                                          private val sessionDao: SessionDao,
                                          private val sessionDataManager: SessionDataManager) {
    fun locationUpdating(location: Location, listener: LocationUpdating) {
        if (sessionDataManager.sessionId == INVALID_ID) {
            return
        }

        Single.create(SingleOnSubscribe<Session?> { emitter ->
            val session = sessionDao.getSession(sessionDataManager.sessionId)
            var isIgnore = false
            if (!session.locations.isEmpty()) {
                val lastLocation = session.locations[session.locations.size - 1]
                if (distanceBetween(lastLocation.lat, lastLocation.lng, location.latitude, location.longitude) < MIN_DISTANCE) {
                    Timber.d("ignore location $location because it is near $MIN_DISTANCE m ")
                    isIgnore = true
                    try {
                        throw RuntimeException("ignore location")
                    } catch (throwable: Throwable) {
                        emitter.onError(throwable)
                    }
                }
            }

            if (!isIgnore) {
                val locationHistory = HistoryLocation()
                locationHistory.lng = location.longitude
                locationHistory.lat = location.latitude
                locationHistory.time = Date()
                locationHistory.speed = location.speed

                session.locations.add(locationHistory)
                session.distance = calculateDistance(session.locations)
                session.averageSpeed = calculateAverageSpeed(session.locations)
                sessionDao.add(session)
            }

            emitter.onSuccess(session)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ session ->
                    Timber.d("locationUpdating $location")
                    val intent = Intent(Constants.ACTION_BROADCAST)
                    intent.putExtra(Constants.EXTRA_LOCATION, location)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    listener.onSessionReady(session)
                }) { error ->
                    run {
                        Timber.e(error.message)
                        listener.onSessionReady(null)
                    }
                }
    }

    private fun calculateDistance(locations: MutableList<HistoryLocation>): Double {
        var totalDistance = 0.0
        if (locations.isEmpty() || locations.size == 1) {
            return totalDistance
        }

        for (i in 0 until locations.size - 1) {
            val start = locations[i]
            val end = locations[i + 1]
            val distance = distanceBetween(start.lat, start.lng, end.lat, end.lng)
            totalDistance += distance
        }

        return totalDistance
    }

    private fun distanceBetween(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Double {
        val res = FloatArray(2)
        Location.distanceBetween(startLat, startLng, endLat, endLng, res)
        return res[0].toDouble()
    }

    private fun calculateAverageSpeed(locations: MutableList<HistoryLocation>): Float {
        var totalSpeed = 0.0f
        if (locations.isEmpty()) {
            return totalSpeed
        }

        for (location in locations) {
            totalSpeed += location.speed
        }

        totalSpeed /= locations.size
        return totalSpeed
    }
}