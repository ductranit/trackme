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

package ductranit.me.trackme.db

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ductranit.me.trackme.models.HistoryLocation
import io.objectbox.converter.PropertyConverter

class LocationConverter : PropertyConverter<MutableList<HistoryLocation>, String> {
    private val gson: Gson = Gson()
    override fun convertToDatabaseValue(entityProperty: MutableList<HistoryLocation>?): String {
        if (entityProperty == null) {
            return ""
        }

        return gson.toJson(entityProperty)

    }

    override fun convertToEntityProperty(databaseValue: String?): MutableList<HistoryLocation> {
        if (databaseValue == null) {
            return mutableListOf()
        }

        try {
            val typeToken = object : TypeToken<MutableList<HistoryLocation>>() {}.type
            return gson.fromJson<MutableList<HistoryLocation>>(databaseValue, typeToken)
        } catch (throwable: Throwable) {
            return mutableListOf()
        }
    }

}