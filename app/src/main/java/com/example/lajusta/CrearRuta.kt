package com.example.lajusta

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.FamiliaProductora
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


class CrearRuta : AppCompatActivity() {
    var map: MapView? = null
    private var once: Boolean = false
    private var id: String? = null
    private var latitud: Double? = null
    private var longitud: Double? = null
    private var latitudOriginal: Double? = 0.0
    private var longitudOriginal: Double? = 0.0
    private var latitudOriginalAnterior: Double? = -34.9214
    private var longitudOriginalAnterior: Double? = -57.9544

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        id = intent.getStringExtra("id")

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            val myButton = findViewById<Button>(R.id.button27)
            myButton.visibility = View.INVISIBLE
            val quinta = laJustaService.getQuinta(id!!.toInt()).body()

            var list: ArrayList<String>? = null
            if("@" in quinta!!.geoImg!!) {
                list = ArrayList<String>(
                    quinta.geoImg!!
                        .split("@")[1]
                        .split(",")
                        .subList(0,3))
                //list[2] = list[2].split("z")[0]
            } else
                list = quinta.geoImg!!.split(",") as ArrayList<String>


            latitud = list[0].toDouble()
            longitud = list[1].toDouble()

            val ctx: Context = getApplicationContext()
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
            map = findViewById<View>(R.id.map) as MapView
            map!!.setTileSource(TileSourceFactory.MAPNIK)
            map!!.setMultiTouchControls(true)
            val mapController: IMapController = map!!.getController()
            mapController.setZoom(15.5)
            //map!!.controller.setCenter(GeoPoint(-34.9214, -57.9544))

            var startPoint: GeoPoint? = null
            val puntos: ArrayList<OverlayItem> = ArrayList<OverlayItem>()
            val mReceive: MapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    puntos.add(OverlayItem("", "", p))
                    Toast.makeText(getBaseContext(), "Espere...", Toast.LENGTH_LONG).show()
                    //setPuntos(puntos)
                    return false
                }

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    return false
                }
            }

            val OverlayEvents = MapEventsOverlay(mReceive)
            map!!.getOverlays().add(OverlayEvents)
            //setea posicion actual, hay que agarrarla (ahora pruebo)
            if (latitud.toString() != "") {
                startPoint = GeoPoint(
                    (latitud!!),
                    (longitud!!)
                )
                puntos.add(OverlayItem("", "", startPoint))
                setPuntos(puntos)
            } else {
                startPoint = GeoPoint(-34.9214, -57.9544)
            }
            //mapController.setCenter(startPoint)
            map!!.controller.zoomTo(17.0)

            getPosicionActual()
            crearRutaBien()

        }



    }

    fun getPosicionActual(){
        val REQUEST_LOCATION_PERMISSION = 1001
        /*
        Here are the basic steps to get the actual position:

        Check if the device has the necessary permissions to access location services. If not, request the permissions from the user.
        Create a LocationManager object to access the location services.
        Create a LocationListener object to receive updates about the device's location.
        Use the requestLocationUpdates method of the LocationManager object to request location updates.
        In the onLocationChanged method of the LocationListener object, update the map with the device's current location.
        Check if the device has the necessary permissions to access location services

         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the permissions from the user
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            // Create a LocationManager object to access the location services
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Create a LocationListener object to receive updates about the device's location
            val locationListener = object : LocationListener {
                override fun onLocationChanged(p0: Location) {
                    // Update the map with the device's current location
                    //map!!.controller.setCenter(GeoPoint(p0?.latitude ?: 0.0, p0?.longitude ?: 0.0))
                    latitudOriginal = p0?.latitude
                    longitudOriginal = p0.longitude


                    //map!!.controller.setCenter(GeoPoint(latitudOriginal!!, longitudOriginal!!))
                    //

                    crearRutaBien()
                    println("ENTRO A AGARRAR LOCALIZACION")
                }



                // Implement other methods of the LocationListener interface as needed
                // ...
            }


            // Use the requestLocationUpdates method of the LocationManager object to request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        }
    }

    fun crearRutaBien(){
        GlobalScope.launch(Dispatchers.Main) {
            //getPosicionActual()
            val endPoint = GeoPoint(latitud!!, longitud!!)
            if((latitudOriginalAnterior != latitudOriginal) && (longitudOriginalAnterior != longitudOriginal)){
                map!!.controller.setCenter(GeoPoint(latitudOriginal!!,longitudOriginal!!))
                latitudOriginalAnterior = latitudOriginal
                longitudOriginalAnterior = longitudOriginal
            }

             //GeoPoint(-34.9214, -57.9544)
            //deberia ser el get posicion actual
            if(latitudOriginal!=null){
                val startPoint = GeoPoint(latitudOriginal!!, longitudOriginal!!)
                val roadManager = OSRMRoadManager(this@CrearRuta,
                    "5b3ce3597851110001cf624832f098b81ac74ac1b4e9a8fc1ed61642")
                val road = withContext(Dispatchers.IO) {
                    roadManager.getRoad(arrayListOf(startPoint, endPoint))
                }
                val roadOverlay = RoadManager.buildRoadOverlay(road)
                roadOverlay.color = Color.RED
                roadOverlay.width = 10f

                map!!.overlays.add(roadOverlay)
            }
            //val startPoint = GeoPoint(latitudOriginal!!, longitudOriginal!!)









        }
    }

    fun setPuntos(puntos: ArrayList<OverlayItem>) {
        if (puntos.size == 2) {
            puntos.removeAt(0)
            map!!.getOverlays().removeAt(1)
        }
        latitud = puntos[0].point.latitude
        longitud = puntos[0].point.longitude

        val tap: OnItemGestureListener<OverlayItem?> = object : OnItemGestureListener<OverlayItem?> {
            override fun onItemLongPress(arg0: Int, arg1: OverlayItem?): Boolean {
                return false
            }

            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                return false
            }
        }
        val capa = ItemizedOverlayWithFocus(this, puntos, tap)
        capa.setFocusItemsOnTap(true)
        map!!.overlays.add(capa)
    }

    fun backToCrearQuinta(view: View?) {

        //a ver si uso esto pa algo
    }
}