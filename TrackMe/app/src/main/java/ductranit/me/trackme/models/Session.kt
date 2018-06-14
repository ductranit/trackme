package ductranit.me.trackme.models

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.util.Date

@Entity
class Session {
    @Id
    var id: Long = 0
    var distance: Int = 0
    var averageSpeed: Int = 0
    var startTime: Date? = null
    var endTime: Date? = null
    @Backlink
    var locations: ToMany<HistoryLocation>? = null
}