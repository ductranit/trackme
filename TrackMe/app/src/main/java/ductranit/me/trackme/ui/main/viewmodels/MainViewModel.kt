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

package ductranit.me.trackme.ui.main.viewmodels

import android.arch.lifecycle.MutableLiveData
import ductranit.me.trackme.db.SessionDao
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.base.viewmodels.BaseViewModel
import ductranit.me.trackme.utils.TraceUtils
import io.objectbox.android.ObjectBoxLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainViewModel @Inject constructor(private val sessionDao: SessionDao) : BaseViewModel() {
    private lateinit var sessions: ObjectBoxLiveData<Session>
    private var loaded: Boolean = false
    private var disposable: Disposable? = null

    /**
     * Use LiveData to lazy load session list
     */
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
        disposable = Observable.fromCallable {
            sessions = sessionDao.getAll()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loaded = true
                    isLoaded.value = true
                    TraceUtils.end()
                }
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }
}