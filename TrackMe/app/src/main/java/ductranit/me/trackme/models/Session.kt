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
    var averageSpeed: Int = 0 // km/h
    var startTime: Date? = null
    var endTime: Date? = null

    @Convert(converter = LocationConverter::class, dbType = String::class)
    var locations: MutableList<HistoryLocation> = mutableListOf()
}