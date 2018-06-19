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

import android.content.SharedPreferences

fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
        putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun SharedPreferences.getDouble(key: String, default: Double) =
        java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T) =
        this.getInt(key, -1).let { if (it >= 0) enumValues<T>()[it] else default }

fun <T : Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: T?): SharedPreferences.Editor =
        this.putInt(key, value?.ordinal ?: -1)