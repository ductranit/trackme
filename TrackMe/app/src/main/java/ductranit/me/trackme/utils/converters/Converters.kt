package ductranit.me.trackme.utils.converters

import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.view.View
import android.widget.TextView
import ductranit.me.trackme.R
import timber.log.Timber
import java.text.SimpleDateFormat
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
        val simpleDateFormat = SimpleDateFormat("hh:mm:ss", Locale.US)
        text = simpleDateFormat.format(date)
    } catch (throwable: Throwable) {
        Timber.e(throwable)
    }
}

@BindingAdapter("textDistance")
fun TextView.setDistance(distance: Double?) {
    if(distance != null) {
        val km  = distance / 1000
        text = context.getString(R.string.distance_text).format(km)
    }
}

@BindingAdapter("textSpeed")
fun TextView.setSpeed(speed: Int?) {
    if(speed != null) {
        text = context.getString(R.string.speed_text).format(speed)
    }
}
