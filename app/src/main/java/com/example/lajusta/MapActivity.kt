package com.example.lajusta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapActivity : AppCompatActivity() {
    var map: MapView? = null
    private var activity: String? = null
    private var id: String? = null
    private var latitud: Double? = null
    private var longitud: Double? = null
    private var superficie: Int? = null
    private var nombre: String? = null
    private var direccion: String? = null
    private var fp: String? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val bundle = intent.extras
        nombre = bundle?.getString("nombre")
        direccion = bundle?.getString("direccion")
        fp = bundle?.getString("fp")
        latitud = bundle?.getDouble("latitud")!!
        longitud = bundle?.getDouble("longitud")!!
        superficie = bundle?.getInt("superficie")

        activity = intent.getStringExtra("activity")
        if (activity == "ver") {
            id = intent.getStringExtra("id")
        }
        val ctx: Context = getApplicationContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setMultiTouchControls(true)
        val mapController: IMapController = map!!.getController()
        mapController.setZoom(15.5)
        var startPoint: GeoPoint? = null
        val puntos: ArrayList<OverlayItem> = ArrayList<OverlayItem>()
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                puntos.add(OverlayItem("", "", p))
                Toast.makeText(getBaseContext(), "Espere...", Toast.LENGTH_SHORT).show()
                setPuntos(puntos)
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        val OverlayEvents = MapEventsOverlay(mReceive)
        map!!.getOverlays().add(OverlayEvents)
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
        mapController.setCenter(startPoint)
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

        if (activity == "ver") {
            intent = Intent(this, VerQuinta::class.java)
            intent.putExtra("id", id)
        } else {
            intent = Intent(this, CrearQuinta::class.java)
        }

        val myBundle = Bundle()
        println(longitud)
        println(latitud)
        myBundle.putString("nombre", nombre!!)
        myBundle.putString("direccion",  direccion!!)
        myBundle.putString("fp",  fp!!)
        myBundle.putDouble("latitud", latitud!!)
        myBundle.putDouble("longitud", longitud!!)
        myBundle.putInt("superficie", superficie!!)
        intent.putExtra("activity", "crear")
        intent.putExtras(myBundle)

        intent.putExtra("ViMapa", "si")
        startActivity(intent)
    }
}