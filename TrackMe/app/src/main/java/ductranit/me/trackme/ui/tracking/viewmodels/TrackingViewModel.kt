package ductranit.me.trackme.ui.tracking.viewmodels

import android.arch.lifecycle.MutableLiveData
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.base.viewmodels.BaseViewModel
import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.utils.ObjectBoxSingLiveData
import java.util.*
import javax.inject.Inject

class TrackingViewModel @Inject constructor(private val sessionDao: SessionDao) : BaseViewModel() {
    private lateinit var session: ObjectBoxSingLiveData<Session>
    var state: MutableLiveData<State> = MutableLiveData()
    var sessionId: Long = -1

    fun getSession(): ObjectBoxSingLiveData<Session> {
        if (sessionId != -1L || !sessionDao.contains(sessionId)) {
            val session = Session()
            session.startTime = Date()
            session.endTime = Date()
            sessionId = sessionDao.add(session)
        }

        session = sessionDao.getSession(sessionId)
        return session
    }
}