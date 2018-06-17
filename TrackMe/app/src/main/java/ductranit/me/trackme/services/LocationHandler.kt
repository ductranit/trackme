package ductranit.me.trackme.services

import android.content.Context
import android.location.Location
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.HistoryLocation
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import java.util.*
import javax.inject.Inject

class LocationHandler @Inject constructor(private val context: Context, private val sessionDao: SessionDao,
                                          private val sessionDataManager: SessionDataManager) {
    fun locationUpdating(location: Location) {
        if (sessionDataManager.sessionId == INVALID_ID) {
            return
        }

        val locationHistory = HistoryLocation()
        locationHistory.lng = location.longitude
        locationHistory.lat = location.latitude
        locationHistory.time = Date()
        val session = sessionDao.getSession(sessionDataManager.sessionId)
        session.locations.add(locationHistory)
        sessionDao.add(session)
    }
}