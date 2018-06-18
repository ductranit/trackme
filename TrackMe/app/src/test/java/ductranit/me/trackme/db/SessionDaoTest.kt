package ductranit.me.trackme.db

import ductranit.me.trackme.AbstractObjectBoxTest
import ductranit.me.trackme.models.Session
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

class SessionDaoTest : AbstractObjectBoxTest() {

    @Test
    fun insertAndRead() {
        val session = Session()
        session.startTime = Date()
        session.endTime = Date()
        session.averageSpeed = 10.0f

        val sessionDao = SessionDao(store)
        val id = sessionDao.add(session)
        assert(id != 0L)

        val loadedSession = sessionDao.getSession(id)
        assert(loadedSession.averageSpeed == 10.0f)
    }

    @Test
    fun stressTest(){
        val list = ArrayList<Session>()
        for (i in 0.. 1000) {
            val session = Session()
            session.startTime = Date()
            session.averageSpeed = 10.0f
            list.add(session)
        }

        val sessionDao = SessionDao(store)
        sessionDao.add(list)
        val loadedList = sessionDao.getSessions()
        assert(list.size == loadedList.size)
    }
}