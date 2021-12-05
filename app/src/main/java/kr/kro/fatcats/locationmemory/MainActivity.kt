package kr.kro.fatcats.locationmemory

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.*
import kr.kro.fatcats.locationmemory.databinding.ActivityMainBinding
import kr.kro.fatcats.locationmemory.util.Preferences
import kotlin.coroutines.CoroutineContext

import android.os.Build
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kr.kro.fatcats.locationmemory.model.ClusterItems
import kr.kro.fatcats.locationmemory.util.MarkerRender
import com.gun0912.tedpermission.provider.TedPermissionProvider.context

class MainActivity : AppCompatActivity(), CoroutineScope, OnMapReadyCallback ,LocationListener{

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var binding : ActivityMainBinding
    private lateinit var map : GoogleMap
    private lateinit var locationManager: LocationManager
    private val mediaList = mutableListOf<ClusterItems>()
    private lateinit var clusterManager: ClusterManager<ClusterItems>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }


    private fun initView(){
        val permission =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        }else{
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        getPermission(permission)
        setUpGoogleMap()
        setClick()
    }
    //마지막으로 저장했던 위치로 이동
    private fun getSaveLocation(){
        if(Preferences.latitude != "0"){
            val saveLocation = LatLng(Preferences.latitude.toDouble(),Preferences.longitude.toDouble())
            currentLocationChange(saveLocation)
        }
    }

    private fun setClick() =with(binding){
        //현제 위치로 카메라 이동
        currentLocationButton.setOnClickListener {
            getLocation()
        }
        //파일 다운로드
        fileDownButton.setOnClickListener {
            launch(coroutineContext) {
                if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_MEDIA_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@MainActivity, "추가 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("durldi","durl")
                    Log.d("버전 체크","${Build.VERSION.SDK_INT} : ${Build.VERSION_CODES.Q}")
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
                            getPermission(arrayOf(
                                Manifest.permission.ACCESS_MEDIA_LOCATION
                            ))
                    }
                    getPhoto()
                    setMarker()
                }
            }
        }
    }



    private fun getLocation(){
        if(::locationManager.isInitialized.not()){
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        //gps가 있으면
       if(gpsEnabled){
            val permission: Array<String> = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            getPermission(permission)
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ){
                Toast.makeText(this, "권한을 허용해주세요", Toast.LENGTH_SHORT).show()
            }else{
                nowLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun nowLocation(){
        val locationProvider = LocationManager.GPS_PROVIDER
        //네트워크 위치 추후 필요하면 참고
//        val locationProvider2 = LocationManager.NETWORK_PROVIDER
//        locationManager.getLastKnownLocation(locationProvider2)?.let{
//            onLocationChanged(it)
//        }
        locationManager.getLastKnownLocation(locationProvider)?.let {
            onLocationChanged(it)
        }
    }

    private fun setUpGoogleMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    //현제 위치로 카메라 이동
    private fun currentLocationChange(locationLatLnd: LatLng){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLnd,CAMERA_ZOOM_LEVEL))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getSaveLocation()
    }


    companion object{
        const val CAMERA_ZOOM_LEVEL : Float = 17f
    }

    override fun onLocationChanged(location: Location) {
        val locationLatLnd = LatLng(
            location.latitude,
            location.longitude
        )
        Preferences.latitude = location.latitude.toString()
        Preferences.longitude = location.longitude.toString()
        Log.d("location","location :${location.latitude},${location.longitude}")
        currentLocationChange(locationLatLnd)
    }

    private suspend fun getPhoto()=withContext(Dispatchers.IO){
        binding.setVariable(BR.visibilityBool,true)
        binding.setVariable(BR.visibilityBool2,true)
        val listUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val proj = arrayOf<String>(MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE)
        val cursor = contentResolver.query(listUri,proj,null,null,null,null)
        var count = 0
        cursor?.let {
            while (cursor.moveToNext()){
                var index = cursor.getColumnIndex(proj[0])
                val id = cursor.getString(index)
                index = cursor.getColumnIndex(proj[1])
                val title = cursor.getString(index)
                val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                val position = getLnt(contentUri)
                var lat = 0.0
                position?.let {
                    lat = it.latitude
                }
                var lng = 0.0
                position?.let {
                    lng = it.longitude
                }
                val image = ClusterItems(id,lat,lng,title,contentUri)
                mediaList.add(image)
                count ++
            }
        }
    }
    private fun getLnt(uri : Uri) : LatLng? {
        var lat = 0.0
        var lng = 0.0
        var latLng: LatLng? = null
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let {
            var exif = ExifInterface(inputStream)
            val output: FloatArray = floatArrayOf(0f, 0f)
            if (exif.getLatLong(output)) {
                lat = output[0].toDouble()
                lng = output[1].toDouble()
                latLng = LatLng(lat, lng)
                Log.d("tts2","${lng} ")
                Log.d("tts1","${lat} ")
            }
            inputStream.close()
        }
        return latLng
    }

    private suspend fun setMarker()=withContext(Dispatchers.Main) {
        binding.setVariable(BR.visibilityBool,false)
        Log.d("진입확인","setMaker in")
        if(::clusterManager.isInitialized.not()){
            clusterManager = ClusterManager(this@MainActivity,map)
            map.setOnCameraIdleListener(clusterManager)
            Log.d("mediaList.size","size : ${mediaList.size}")
            if(mediaList.size != 0){
                mediaList.map{
                    val clusterRenderer = MarkerRender(this@MainActivity, map, clusterManager)
                    clusterManager.renderer = clusterRenderer
                    clusterManager.addItem(it)
                    Log.d("진입해서","${it.position.latitude}:${it.position.longitude}")
                }
            }
        }
        binding.setVariable(BR.visibilityBool2,false)
    }

    //권한
    private fun getPermission(permission: Array<String>){
        permission.map{
            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(it)
                .check()
        }
    }
    private val permissionListener: PermissionListener = object : PermissionListener {
        override fun onPermissionDenied(deniedPermissions: List<String?>) {
            Toast.makeText(
                this@MainActivity,
                "Permission Denied\n$deniedPermissions",
                Toast.LENGTH_SHORT
            ).show()
        }
        override fun onPermissionGranted() {
//            Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }



}