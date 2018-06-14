package ductranit.me.trackme.utils.converters

import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.view.View

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

