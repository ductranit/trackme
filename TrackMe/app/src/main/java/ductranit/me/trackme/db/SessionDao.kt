package ductranit.me.trackme.db

import ductranit.me.trackme.models.Session
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.android.ObjectBoxLiveData
import javax.inject.Inject

class SessionDao @Inject constructor(val boxStore: BoxStore) {
    private val box: Box<Session> = boxStore.boxFor(Session::class.java)

    fun add(session: Session) {
        box.put(session)
    }

    fun add(sessions: List<Session>) {
        box.put(sessions)
    }

    fun getAll(): ObjectBoxLiveData<Session> {
        return ObjectBoxLiveData(box.query().build())
    }

    fun getSessions(offset:Long, limit: Long): List<Session> {
        return box.query().build().find(offset, limit)
    }
}