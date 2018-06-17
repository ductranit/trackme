package ductranit.me.trackme.services

import android.location.Location
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.HistoryLocation
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.Constants.Companion.MIN_DISTANCE
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class LocationHandler @Inject constructor(private val sessionDao: SessionDao, private val sessionDataManager: SessionDataManager) {
    fun locationUpdating(location: Location) {
        if (sessionDataManager.sessionId == INVALID_ID) {
            return
        }

        Observable.fromCallable {
            val session = sessionDao.getSession(sessionDataManager.sessionId)
            if(!session.locations.isEmpty()) {
                val lastLocation = session.locations[session.locations.size - 1]
                if(distanceBetween(lastLocation.lat, lastLocation.lng, location.latitude, location.longitude) < MIN_DISTANCE) {
                    Timber.d("ignore location $location because it is near $MIN_DISTANCE m ")
                    return@fromCallable
                }
            }

            val locationHistory = HistoryLocation()
            locationHistory.lng = location.longitude
            locationHistory.lat = location.latitude
            locationHistory.time = Date()

            session.locations.add(locationHistory)
            session.distance = calculateDistance(session.locations)
            sessionDao.add(session)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("locationUpdating $location")
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
}