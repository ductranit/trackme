package ductranit.me.trackme.utils.converters

import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.view.View
import android.widget.TextView
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
    try {
        val simpleDateFormat = SimpleDateFormat("hh:mm:ss", Locale.US)
        text = simpleDateFormat.format(date)
    } catch (throwable: Throwable) {
        Timber.e(throwable)
    }
}

