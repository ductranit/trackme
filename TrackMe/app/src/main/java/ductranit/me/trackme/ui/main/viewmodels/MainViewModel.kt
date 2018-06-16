package ductranit.me.trackme.ui.main.viewmodels

import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.base.viewmodels.BaseViewModel
import io.objectbox.android.ObjectBoxLiveData
import javax.inject.Inject

class MainViewModel @Inject constructor(private val sessionDao: SessionDao) : BaseViewModel() {
    private lateinit var sessions: ObjectBoxLiveData<Session>
    private var isLoading: Boolean = false

    fun getSessions(): ObjectBoxLiveData<Session> {
        if (!isLoading) {
            sessions = sessionDao.getAll()
            isLoading = true
        }

        return sessions
    }
}