package kr.kro.fatcats.locationmemory

import android.app.Application
import kr.kro.fatcats.locationmemory.util.Preferences

class MyApplication: Application() {

    companion object {
        private val TAG = MyApplication::class.java.simpleName
        var instance: MyApplication? = null
    }
    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
    }
}