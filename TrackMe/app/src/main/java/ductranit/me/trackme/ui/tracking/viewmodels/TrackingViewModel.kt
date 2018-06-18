package ductranit.me.trackme.ui.tracking.viewmodels

import android.arch.lifecycle.MutableLiveData
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.ui.base.viewmodels.BaseViewModel
import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.ObjectBoxSingLiveData
import java.util.*
import javax.inject.Inject

class TrackingViewModel @Inject constructor(private val sessionDataManager: SessionDataManager,
                                            private val sessionDao: SessionDao) : BaseViewModel() {
    private lateinit var session: ObjectBoxSingLiveData<Session>
    var state: MutableLiveData<State> = MutableLiveData()

    var sessionId: Long = INVALID_ID

    var isRecording: Boolean = true

    fun getSession(): ObjectBoxSingLiveData<Session> {
        if (sessionId == INVALID_ID || !sessionDao.contains(sessionId)) {
            val sessionValue = Session()
            sessionValue.startTime = Date()
            sessionValue.endTime = Date()
            sessionId = sessionDao.add(sessionValue)
            sessionDataManager.sessionId = sessionId
        }

        session = sessionDao.getSessionLiveData(sessionId)
        return session
    }

    fun stop() {
        val session = sessionDao.getSession(sessionId)
        session.endTime = Date()
        sessionDao.add(session)
    }
}