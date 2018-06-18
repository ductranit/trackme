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

import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataManager @Inject constructor(){
    var sessionId: Long = INVALID_ID
    var state: State = State.DEFAULT
    var isAddNew: Boolean = true

    fun clear(){
        sessionId = INVALID_ID
        state = State.DEFAULT
    }
}