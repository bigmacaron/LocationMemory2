package kr.kro.fatcats.locationmemory.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kr.kro.fatcats.locationmemory.model.ClusterItems
import com.google.maps.android.clustering.ClusterManager
import kr.kro.fatcats.locationmemory.R


class MarkerRender(private val context: Context?,
                   map: GoogleMap?,
                   clusterManager: ClusterManager<ClusterItems>
) :
    DefaultClusterRenderer<ClusterItems>(context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: ClusterItems, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        val bitmapOptions = BitmapFactory.Options()
        // 불러올때 1/32 의 크기로 읽음
        bitmapOptions.inSampleSize = 32
        val iconImage = BitmapFactory.decodeStream(context?.contentResolver?.openInputStream(item.getUri()),null,bitmapOptions)
        iconImage?.let { bitmap ->
            val width = 150
            val height = (width * 3 ) / 4
            val smallMarker = Bitmap.createScaledBitmap(bitmap, width, height, false)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
        }
    }





}



