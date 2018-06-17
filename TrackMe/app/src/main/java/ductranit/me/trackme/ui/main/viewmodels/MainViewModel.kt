package ductranit.me.trackme.ui.main.viewmodels

import android.arch.lifecycle.MutableLiveData
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.base.viewmodels.BaseViewModel
import ductranit.me.trackme.utils.TraceUtils
import io.objectbox.android.ObjectBoxLiveData
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainViewModel @Inject constructor(private val sessionDao: SessionDao) : BaseViewModel() {
    private lateinit var sessions: ObjectBoxLiveData<Session>
    private var loaded: Boolean = false
    var isLoaded: MutableLiveData<Boolean> = MutableLiveData()
        get() {
            if (!loaded) {
                loadSessions()
            }

            return field
        }

    fun getSessions(): ObjectBoxLiveData<Session> {
        return sessions
    }

    private fun loadSessions() {
        TraceUtils.begin("begin loadSessions")
        Flowable.fromCallable {
            sessions = sessionDao.getAll()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loaded = true
                    isLoaded.value = true
                    TraceUtils.end()
                }
    }
}