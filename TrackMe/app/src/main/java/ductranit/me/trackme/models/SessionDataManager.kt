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

package ductranit.me.trackme.models

import android.content.SharedPreferences
import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.Constants.Companion.KEY_SESSION_ID
import ductranit.me.trackme.utils.Constants.Companion.KEY_SESSION_IS_ADD_NEW
import ductranit.me.trackme.utils.Constants.Companion.KEY_SESSION_STATE
import ductranit.me.trackme.utils.getEnum
import ductranit.me.trackme.utils.putEnum
import javax.inject.Inject
import javax.inject.Singleton

/**
 *  Singleton class contains current session information
 */
@Singleton
class SessionDataManager @Inject constructor(private val preference: SharedPreferences) {
    var sessionId: Long = INVALID_ID
        get() {
            return preference.getLong(KEY_SESSION_ID, INVALID_ID)
        }
        set(value) {
            preference.edit().putLong(KEY_SESSION_ID, value).apply()
            field = value
        }

    var state: State = State.DEFAULT
        get() {
            return preference.getEnum(KEY_SESSION_STATE, State.DEFAULT)
        }
        set(value) {
            preference.edit().putEnum(KEY_SESSION_STATE, value).apply()
            field = value
        }

    var isAddNew: Boolean = true
        get() {
            return preference.getBoolean(KEY_SESSION_IS_ADD_NEW, true)
        }
        set(value) {
            preference.edit().putBoolean(KEY_SESSION_IS_ADD_NEW, value).apply()
            field = value
        }

    fun clear() {
        preference.edit().remove(KEY_SESSION_ID).apply()
        preference.edit().remove(KEY_SESSION_STATE).apply()
        preference.edit().remove(KEY_SESSION_IS_ADD_NEW).apply()
        sessionId = INVALID_ID
        state = State.DEFAULT
        isAddNew = true
    }
}