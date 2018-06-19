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

package ductranit.me.trackme.utils.converters

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.load.engine.DiskCacheStrategy
import ductranit.me.trackme.utils.GlideApp

@BindingAdapter("backgroundRes")
fun setBackgroundDrawableRes(view: View?, resId: Int?) {
    if (view == null || resId == null) {
        return
    }

    view.setBackgroundResource(resId)
}

@BindingAdapter("imageRes")
fun setImageDrawableRes(view: ImageView?, resId: Int?) {
    if (view == null || resId == null) {
        return
    }

    view.setImageResource(resId)
}

@BindingAdapter(value = ["imageUrl", "placeholder"], requireAll = false)
fun setImageRemote(view: ImageView?, url: String?, placeholder: Drawable?) {
    if (view == null || url == null) {
        return
    }

    if (placeholder != null) {
        GlideApp.with(view)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(placeholder)
                .into(view)
    } else {
        GlideApp.with(view.context).load(url).into(view)
    }
}

