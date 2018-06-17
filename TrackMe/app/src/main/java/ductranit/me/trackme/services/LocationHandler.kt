package ductranit.me.trackme.services

import android.content.Context
import android.content.Intent
import android.location.Location
import android.support.v4.content.LocalBroadcastManager
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.HistoryLocation
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.utils.Constants
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.Constants.Companion.MIN_DISTANCE
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class LocationHandler @Inject constructor(private val context: Context,
                                          private val sessionDao: SessionDao,
                                          private val sessionDataManager: SessionDataManager) {
    fun locationUpdating(location: Location) {
        if (sessionDataManager.sessionId == INVALID_ID) {
            return
        }

        Observable.fromCallable {
            val session = sessionDao.getSession(sessionDataManager.sessionId)
            if (!session.locations.isEmpty()) {
                val lastLocation = session.locations[session.locations.size - 1]
                if (distanceBetween(lastLocation.lat, lastLocation.lng, location.latitude, location.longitude) < MIN_DISTANCE) {
                    Timber.d("ignore location $location because it is near $MIN_DISTANCE m ")
                    return@fromCallable
                }
            }

            val locationHistory = HistoryLocation()
            locationHistory.lng = location.longitude
            locationHistory.lat = location.latitude
            locationHistory.time = Date()
            locationHistory.speed = location.speed

            session.locations.add(locationHistory)
            session.distance = calculateDistance(session.locations)
            session.averageSpeed = calculateAverageSpeed(session.locations)
            sessionDao.add(session)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("locationUpdating $location")
                    val intent = Intent(Constants.ACTION_BROADCAST)
                    intent.putExtra(Constants.EXTRA_LOCATION, location)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
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