package com.example.lajusta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.FamiliaProductora
import com.example.lajusta.data.model.Quinta
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CrearQuinta : AppCompatActivity() {
    private var CampoNombre: EditText? = null
    private var CampoDireccion: EditText? = null
    private var CampoSuperficie: EditText? = null
    private var CampoLatitud: EditText? = null
    private var CampoLongitud: EditText? = null
    private var CampoFP: String? = null
    private var fpSeleccionada: Int? = null
    private val mMapView: MapView? = null
    private val mMapController: MapController? = null
    private val thisAux = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_quinta)

        // Habilitar navegacion UP
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        CampoNombre = findViewById<View>(R.id.editTextTextPersonName) as EditText
        CampoDireccion = findViewById<View>(R.id.editTextTextPersonName2) as EditText
        CampoSuperficie = findViewById<View>(R.id.editTextNumber) as EditText
        CampoLatitud = findViewById<View>(R.id.editTextNumber2) as EditText
        CampoLongitud = findViewById<View>(R.id.editTextNumber6) as EditText
        CampoLongitud!!.isEnabled = false
        CampoLatitud!!.isEnabled = false

        val activity = intent.getStringExtra("ViMapa")
        println(activity)
        if (activity.equals("si")) {
            val bundle = intent.extras
            val nombre = bundle?.getString("nombre")
            val fp = bundle?.getString("fp")
            val direccion = bundle?.getString("direccion")
            val latitud = bundle?.getDouble("latitud")!!
            val longitud = bundle?.getDouble("longitud")!!
            val superficie = bundle?.getInt("superficie")
            CampoNombre!!.setText(nombre)
            CampoDireccion!!.setText(direccion)
            CampoSuperficie!!.setText(superficie.toString())
            CampoLongitud!!.setText(longitud.toString())
            CampoLatitud!!.setText(latitud.toString())
            CampoFP = fp.toString()
            println(latitud)
        }

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            //val quintas = laJustaService.getQuintas().body() as ArrayList<Quinta>
            val fps = laJustaService.getFamiliasProductoras().body() as ArrayList<FamiliaProductora>

            // Familia Productora Select
            val spinner_quinta = findViewById<View>(R.id.fpName) as Spinner
            val fpId = ArrayList<Int>(fps!!.size)
            val fpName = ArrayList<String>(fps!!.size)
            for (fp in fps!!) {
                fpId.add(fp.id_fp!!)
                fpName.add(fp.nombre!!)
            }

            val fp_spinnerAdapter = ArrayAdapter(this@CrearQuinta, android.R.layout.simple_spinner_item,fpName)
            spinner_quinta.adapter = fp_spinnerAdapter
            if(activity.equals("si")){
                var fpActual = ""
                for (fp in fps!!){
                    if(fp.id_fp!!.equals(CampoFP!!.toInt())){
                        fpActual = fp.nombre.toString()
                        println(fpActual)
                    }
                }
                spinner_quinta.setSelection(fp_spinnerAdapter.getPosition(fpActual))
            }

            spinner_quinta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    //aca usar el quintaID para mandar cosas
                    fpSeleccionada = fpId[position]
                    //actualizarTabla(position);
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        }
    }

    fun onClick(view: View?) {
        registrarQuinta()
    }

    private fun registrarQuinta() {
        if(CampoNombre!!.text.toString().equals("") || CampoDireccion!!.text.toString().equals("") || CampoSuperficie!!.text.toString().equals("")
            || CampoLongitud!!.text.toString().equals("") || CampoLatitud!!.text.toString().equals("")){
            Toast.makeText(applicationContext, "Complete todos los campos", Toast.LENGTH_SHORT).show()
        }else{
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                //https://www.google.com/maps/place/LAS+ALICIAS/@-34.9754155,-58.0941763,12.33z/data=!4m5!3m4!1s0x0:0x49f8f926ca037272!8m2!3d-34.9607558!4d-58.131601?hl=es
                //hay que ponerlo con ese nombre?
                val sb = StringBuilder()
                    sb.append(CampoLatitud!!.text.toString()).append(",")
                    .append(CampoLongitud!!.text.toString()).append(",")
                    .append(CampoSuperficie!!.text.toString())
                val fpElegida = laJustaService.getFamiliaProductora(fpSeleccionada!!).body()
                var nuevaQuinta = Quinta(id_quinta = null, nombre = CampoNombre!!.text.toString(), geoImg = sb.toString(), fpId = fpElegida!!.id_fp, direccion = CampoDireccion!!.text.toString())
                println(nuevaQuinta)
                laJustaService.addQuinta(nuevaQuinta)
                val intent = Intent(thisAux, Quintas::class.java)
                navigateUpTo(intent)
            }
        }
    }


    fun verMapa(view: View?) {
        val intent = Intent(this, MapActivity::class.java)
        val myBundle = Bundle()
        myBundle.putString("nombre",  CampoNombre!!.text.toString())
        myBundle.putString("fp",  fpSeleccionada.toString())
        myBundle.putString("direccion",  CampoDireccion!!.text.toString())
        if(CampoLatitud!!.text.toString().equals("")){
            CampoLatitud!!.setText("-34.9214")
            CampoLongitud!!.setText("-57.9544")
        }
        if(CampoSuperficie!!.text.toString().equals("")){
            CampoSuperficie!!.setText("0")
        }
        myBundle.putDouble("latitud", CampoLatitud!!.text.toString().toDouble())
        myBundle.putDouble("longitud", CampoLongitud!!.text.toString().toDouble())
        myBundle.putInt("superficie", CampoSuperficie!!.text.toString().toInt())
        intent.putExtra("activity", "crear")
        intent.putExtras(myBundle)
        startActivity(intent)
    }
}

