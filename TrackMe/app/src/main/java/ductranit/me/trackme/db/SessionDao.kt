package ductranit.me.trackme.db

import ductranit.me.trackme.models.Session
import ductranit.me.trackme.models.Session_
import ductranit.me.trackme.utils.ObjectBoxSingLiveData
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.android.ObjectBoxLiveData
import javax.inject.Inject

class SessionDao @Inject constructor(boxStore: BoxStore) {
    private val box: Box<Session> = boxStore.boxFor(Session::class.java)

    fun add(session: Session): Long {
        return box.put(session)
    }

    fun add(sessions: List<Session>) {
        box.put(sessions)
    }

    fun contains(id: Long): Boolean{
        return box[id] != null
    }

    fun getSession(id: Long): Session {
        return box[id]
    }

    fun getSessionLiveData(id: Long): ObjectBoxSingLiveData<Session> {
        val builder = box.query()
        builder.equal(Session_.__ID_PROPERTY, id)
        return ObjectBoxSingLiveData(builder.build())
    }

    fun getAll(): ObjectBoxLiveData<Session> {
        return ObjectBoxLiveData(box.query().orderDesc(Session_.startTime).build())
    }

    fun getSessions(offset:Long, limit: Long): List<Session> {
        return box.query().build().find(offset, limit)
    }
}