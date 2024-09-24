package com.example.lajusta

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.FamiliaProductora
import com.example.lajusta.data.model.Quinta
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList

class VerQuinta : AppCompatActivity() {
    var id: String? = null
        private set
    private var CampoNombre: EditText? = null
    private var CampoDireccion: EditText? = null
    private var CampoSuperficie: EditText? = null
    private var CampoLatitud: EditText? = null
    private var CampoFP: String? = null
    private var CampoLongitud: EditText? = null
    private var fpSeleccionada: Int? = null
    private val thisAux = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_quinta)

        // Habilitar navegacion UP
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val myIntent = intent
        id = myIntent.getStringExtra("id")

        val spinner_quinta = findViewById<View>(R.id.fpName) as Spinner
        CampoNombre = findViewById<View>(R.id.editTextTextPersonName) as EditText
        CampoDireccion = findViewById<View>(R.id.editTextTextPersonName2) as EditText
        CampoSuperficie = findViewById<View>(R.id.editTextNumber) as EditText
        CampoLatitud = findViewById<View>(R.id.editTextNumber2) as EditText
        CampoLongitud = findViewById<View>(R.id.editTextNumber6) as EditText
        CampoLongitud!!.isEnabled = false
        CampoLatitud!!.isEnabled = false

        val activity = intent.getStringExtra("ViMapa")
        if (activity.equals("si")) {
            val bundle = intent.extras
            val nombre = bundle?.getString("nombre")
            val direccion = bundle?.getString("direccion")
            val latitud = bundle?.getDouble("latitud")!!
            val longitud = bundle?.getDouble("longitud")!!
            val superficie = bundle?.getInt("superficie")
            val fp = bundle?.getString("fp")
            println(fp.toString())
            CampoNombre!!.setText(nombre)
            CampoDireccion!!.setText(direccion)
            CampoSuperficie!!.setText(superficie.toString())
            CampoLongitud!!.setText(longitud.toString())
            CampoLatitud!!.setText(latitud.toString())
            CampoFP = fp.toString()
        }

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            val quinta = laJustaService.getQuinta(id!!.toInt()).body()
            var fpActual = ""
            val fps = laJustaService.getFamiliasProductoras().body() as ArrayList<FamiliaProductora>

            if (!activity.equals("si")) {
                CampoNombre!!.setText(quinta!!.nombre)
                CampoDireccion!!.setText(quinta!!.direccion)

                var list: ArrayList<String>? = null
                if("@" in quinta.geoImg!!) {
                    list = ArrayList<String>(
                        quinta.geoImg
                            .split("@")[1]
                            .split(",")
                            .subList(0,3))
                    list[2] = list[2].split("z")[0]
                } else
                    list = quinta.geoImg.split(",") as ArrayList<String>

                CampoLatitud!!.setText(list[0])
                CampoLongitud!!.setText(list[1])
                CampoSuperficie!!.setText(list[2])

                fpActual = laJustaService.getFamiliaProductora(quinta.fpId!!).body()!!.nombre.toString()

            }else{
                for (fp in fps){
                    if(fp.id_fp!!.equals(CampoFP!!.toInt())){
                        fpActual = fp.nombre.toString()
                    }
                }
            }

            // Familia Productora Select
            val fpId = ArrayList<Int>(fps.size)
            val fpName = ArrayList<String>(fps.size)
            for (fp in fps) {
                fpId.add(fp.id_fp!!)
                fpName.add(fp.nombre!!)
            }

            val fp_spinnerAdapter = ArrayAdapter(this@VerQuinta, android.R.layout.simple_spinner_item,fpName)
            spinner_quinta.adapter = fp_spinnerAdapter
            spinner_quinta.setSelection(fp_spinnerAdapter.getPosition(fpActual))

            spinner_quinta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    //aca usar el quintaID para mandar cosas
                    fpSeleccionada = fpId[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }


    fun verMapa(view: View?) {
        val intent = Intent(this, MapActivity::class.java)
        val myBundle = Bundle()
        myBundle.putString("nombre",  CampoNombre!!.text.toString())
        myBundle.putString("direccion",  CampoDireccion!!.text.toString())
        myBundle.putString("fp",  fpSeleccionada.toString())
        myBundle.putDouble("latitud", CampoLatitud!!.text.toString().toDouble())
        myBundle.putDouble("longitud", CampoLongitud!!.text.toString().toDouble())
        myBundle.putInt("superficie", CampoSuperficie!!.text.toString().toInt())
        intent.putExtra("activity", "ver")
        intent.putExtra("id", id)
        intent.putExtras(myBundle)
        startActivity(intent)
    }

    fun actualizarQuinta(view: View?) {
        if(CampoNombre!!.text.toString().equals("") || CampoDireccion!!.text.toString().equals("") || CampoSuperficie!!.text.toString().equals("")
            || CampoLongitud!!.text.toString().equals("") || CampoLatitud!!.text.toString().equals("")){
            Toast.makeText(applicationContext, "Complete todos los campos", Toast.LENGTH_SHORT).show()
        }else{
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                val sb = StringBuilder()
                sb.append(CampoLatitud!!.text.toString()).append(",")
                    .append(CampoLongitud!!.text.toString()).append(",")
                    .append(CampoSuperficie!!.text.toString())
                val fpElegida = laJustaService.getFamiliaProductora(fpSeleccionada!!).body()
                var nuevaQuinta = Quinta(id_quinta = id!!.toInt(), nombre = CampoNombre!!.text.toString(), geoImg = sb.toString(), fpId = fpElegida!!.id_fp, direccion = CampoDireccion!!.text.toString())
                println(nuevaQuinta)
                laJustaService.editQuinta(nuevaQuinta)
                val intent = Intent(thisAux, Quintas::class.java)
                navigateUpTo(intent)
            }
        }

    }

    fun deleteQuinta(view: View?) {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch() {
            laJustaService.deleteQuinta(id!!.toInt())
            finish()
        }
    }
}