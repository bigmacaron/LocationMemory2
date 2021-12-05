package kr.kro.fatcats.locationmemory.model

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterItems(id : String,
                    lat: Double,
                  lng: Double,
                  title: String,
                   uri: Uri
                  ) : ClusterItem {
    private val id: String = id
    private val position: LatLng = LatLng(lat, lng)
    private val title: String = title
    private val uri : Uri = uri

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String? {
        return null
    }
    fun getUri() : Uri {
        return uri
    }
    fun getId() : String {
        return id
    }


}