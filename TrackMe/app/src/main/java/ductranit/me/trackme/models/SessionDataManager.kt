package ductranit.me.trackme.models

import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataManager @Inject constructor(){
    var sessionId: Long = INVALID_ID
    var state: State = State.DEFAULT

    fun clear(){
        sessionId = INVALID_ID
        state = State.DEFAULT
    }
}