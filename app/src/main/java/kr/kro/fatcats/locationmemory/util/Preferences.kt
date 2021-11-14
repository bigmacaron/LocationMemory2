package kr.kro.fatcats.locationmemory.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Preferences {
    private lateinit var prefs: SharedPreferences
    private const val LATITUDE_KEY               = "latitude"
    private const val LONGITUDE_KEY              = "longitude"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    var latitude: String
        get() = prefs.getString(LATITUDE_KEY,"0")?:"0"
        set(value) = prefs.edit {
            putString(LATITUDE_KEY, value).apply()
        }
    var longitude: String
        get() = prefs.getString(LONGITUDE_KEY, "0")?:"0"
        set(value) = prefs.edit {
            putString(LONGITUDE_KEY, value).apply()
        }

}