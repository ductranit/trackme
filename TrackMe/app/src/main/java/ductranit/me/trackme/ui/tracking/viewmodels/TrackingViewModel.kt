/*
 * Copyright (C) 2018 ductranit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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