package ductranit.me.trackme.utils

import android.os.Trace
import ductranit.me.trackme.BuildConfig
import timber.log.Timber

object TraceUtils {
    fun begin(tag: String) {
        if (BuildConfig.DEBUG) {
            Trace.beginSection(tag)
        }
    }

    fun end() {
        if (BuildConfig.DEBUG) {
            Trace.endSection()
        }
    }

    fun begin(tag: String, task: DoingTask) {
        val i = System.currentTimeMillis()
        try {
            begin(tag)
            task.doing()
        } finally {
            Timber.d(" in %ld", (System.currentTimeMillis() - i))
            end()
        }
    }

    interface DoingTask {
        fun doing()
    }

}