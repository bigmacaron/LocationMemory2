package kr.kro.fatcats.locationmemory.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterItem(lat: Double,
                  lng: Double,
                  title: String,
                  ) : ClusterItem {

    private val position: LatLng = LatLng(lat, lng)
    private val title: String = title

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return null
    }
}