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