package kr.kro.fatcats.locationmemory.model

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

data class ImageInfo(
    val id: String,
    val title: String,
    val uri: Uri,
    var modelLatLng: LatLng?
)