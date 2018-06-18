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

package ductranit.me.trackme.utils

import android.arch.lifecycle.LiveData
import io.objectbox.query.Query
import io.objectbox.reactive.DataObserver
import io.objectbox.reactive.DataSubscription

class ObjectBoxSingLiveData<T>(private var query: Query<T>) : LiveData<T>() {
    private var subscription: DataSubscription? = null

    private val listener = DataObserver<List<T>> { data ->
        if (!data.isEmpty()) {
            postValue(data[0])
        }
    }

    override fun onActive() {
        // called when the LiveData object has an active observer
        if (subscription == null) {
            subscription = query.subscribe().observer(listener)
        }
    }

    override fun onInactive() {
        // called when the LiveData object doesn't have any active observers
        if (!hasObservers()) {
            subscription?.cancel()
            subscription = null
        }
    }
}