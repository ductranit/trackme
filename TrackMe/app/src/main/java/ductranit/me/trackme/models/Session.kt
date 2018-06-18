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

package ductranit.me.trackme.models

import ductranit.me.trackme.db.LocationConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

@Entity
class Session {
    @Id
    var id: Long = 0
    var distance: Double = 0.0 // in meter
    var averageSpeed: Float = 0.0f // m/s
    var startTime: Date? = null
    var endTime: Date? = null

    @Convert(converter = LocationConverter::class, dbType = String::class)
    var locations: MutableList<HistoryLocation> = mutableListOf()
}