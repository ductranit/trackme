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