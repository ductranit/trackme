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

@file:Suppress("unused")

package ductranit.me.trackme.utils.converters

import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.view.View
import android.widget.TextView
import ductranit.me.trackme.R
import timber.log.Timber
import java.util.*

@BindingAdapter("visibleOrGone")
fun bindVisibleOrGone(view: View, b: Boolean) {
    view.visibility = if (b) View.VISIBLE else View.GONE
}

@BindingAdapter("visible")
fun bindVisible(view: View, b: Boolean) {
    view.visibility = if (b) View.VISIBLE else View.INVISIBLE
}

@BindingConversion
fun convertBooleanToVisibility(b: Boolean): Int {
    return if (b) View.VISIBLE else View.GONE
}

@BindingAdapter("textDate")
fun TextView.setDate(date: Date?) {
    if(date == null) {
        return
    }

    try {
        val now = Date()
        val diff = now.time - date.time
        val diffSeconds = diff / 1000 % 60
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 23
        text = String.format("%02d:%02d:%02d", diffHours, diffMinutes, diffSeconds)
    } catch (throwable: Throwable) {
        Timber.e(throwable)
    }
}

@BindingAdapter("textDateStart", "textDateEnd")
fun TextView.setDateRange(start: Date?, end: Date?) {
    if(start == null || end == null) {
        return
    }

    try {
        val diff = end.time - start.time
        val diffSeconds = diff / 1000 % 60
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 23
        text = String.format("%02d:%02d:%02d", diffHours, diffMinutes, diffSeconds)
    } catch (throwable: Throwable) {
        Timber.e(throwable)
    }
}

@BindingAdapter("textDistance")
fun TextView.setDistance(distance: Double?) {
    if(distance != null) {
        val km  = distance / 1000 // convert from m to km
        text = context.getString(R.string.distance_text).format(km)
    }
}

@BindingAdapter("textSpeed")
fun TextView.setSpeed(speed: Float?) {
    if(speed != null) {
        val kmh = speed * (3600/1000) // convert from m/s to km/h
        text = context.getString(R.string.speed_text).format(kmh)
    }
}
