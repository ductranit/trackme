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