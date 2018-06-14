package ductranit.me.trackme.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.Date

@Entity
class HistoryLocation {
    @Id
    var id: Long = 0
    var lat: Double = 0.0
    var lng: Double = 0.0
    var time: Date? = null
    var session: ToOne<Session>? = null
}