package com.example.lajusta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Parcela
import com.example.lajusta.data.model.ParcelaNormal
import com.example.lajusta.data.model.Verdura
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class VerParcela : AppCompatActivity() {
    //private var CampoDescri: EditText? = null
    private var CampoCubierto: CheckBox? = null
    private var CampoCosecha: CheckBox? = null
    private var CampoCantidadSurcos: EditText? = null
    private var verduraSeleccionada: Int? = null
    private var id: String? = null
    private var id_visita: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_parcela)
        //CampoDescri = findViewById<View>(R.id.descripcion) as EditText
        CampoCubierto = findViewById<View>(R.id.cubierto) as CheckBox
        CampoCosecha = findViewById<View>(R.id.cosecha) as CheckBox
        CampoCantidadSurcos = findViewById<View>(R.id.cantidadSurcos) as EditText

        val myIntent = intent
        id = myIntent.getStringExtra("id")
        id_visita = myIntent.getStringExtra("id_visita")

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            val visita = laJustaService.getVisita(id_visita!!.toInt()).body()
            val parcela = visita!!.parcelas.find { it.id_parcela == id!!.toInt() }

            CampoCubierto!!.isChecked= parcela!!.cubierta!!
            CampoCosecha!!.isChecked= parcela!!.cosecha!!
            CampoCantidadSurcos!!.setText(parcela!!.cantidad_surcos!!.toString())

            val verdurasObtenidas = laJustaService.getVerduras().body() as ArrayList<Verdura>

            // Verdura Select
            val spinner_verdura = findViewById<View>(R.id.verdura) as Spinner
            val verduraId = ArrayList<Int>(verdurasObtenidas!!.size)
            val verduraName = ArrayList<String>(verdurasObtenidas!!.size)
            for (verdura in verdurasObtenidas!!) {
                verduraId.add(verdura.id_verdura!!)
                verduraName.add(verdura.nombre!!)
            }

            var fp_spinnerAdapter = ArrayAdapter(this@VerParcela, android.R.layout.simple_spinner_item, verduraName)
            spinner_verdura.adapter = fp_spinnerAdapter
            spinner_verdura.setSelection(fp_spinnerAdapter.getPosition(parcela.verdura!!.nombre))

            spinner_verdura.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    //aca usar el quintaID para mandar cosas
                    verduraSeleccionada = verduraId[position]

                    //actualizarTabla(position);
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        }
    }

    fun onClick(view: View?) {

        if(CampoCantidadSurcos!!.text.toString().equals("")){
            Toast.makeText(applicationContext, "Complete la cantidad de surcos", Toast.LENGTH_LONG).show()
        }else{
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {

                val visita = laJustaService.getVisita(id_visita!!.toInt()).body()

                var cubierto = false
                var cosecha = false
                if(CampoCubierto!!.isChecked){
                    cubierto = true
                }
                if(CampoCosecha!!.isChecked){
                    cosecha = true
                }
                val verduraElegida = laJustaService.getVerdura(verduraSeleccionada!!).body()
                val parcelaAEditar = laJustaService.getParcela(id!!.toInt()).body()
                var parcelaACargar = ParcelaNormal(id_parcela = parcelaAEditar!!.id_parcela, cantidad_surcos = CampoCantidadSurcos!!.text.toString().toInt(), cubierta = cubierto, cosecha = cosecha, id_verdura = verduraElegida!!.id_verdura,id_visita=id_visita!!.toInt())
                laJustaService.editParcela(parcelaACargar)

                var nuevaParcela = Parcela(id_parcela = id!!.toInt(), cantidad_surcos = CampoCantidadSurcos!!.text.toString().toInt(), cubierta = cubierto, cosecha = cosecha, verdura = verduraElegida!!)
                println(nuevaParcela)

                //agarro de la cache si habia mas parcelas
                val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)

                var parcelasCargadas: Array<Parcela>? = null
                val ok = sharedPref.getBoolean("ok", false)
                val gson = Gson()

                parcelasCargadas = visita!!.parcelas.toTypedArray()

                parcelasCargadas = parcelasCargadas.filter { it.id_parcela != id!!.toInt() }.toTypedArray()

                parcelasCargadas = parcelasCargadas.plusElement(nuevaParcela)


                val jsonString = gson.toJson(parcelasCargadas)

                val editor = sharedPref.edit()
                editor.putString("key", jsonString)
                editor.putBoolean("ok", true)
                editor.apply()

                val sharedPref2 = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
                val jsonStringCargadas2 = sharedPref.getString("key", null)

                println("JSON CARGADO EN VER PARCELA ")
                println(jsonStringCargadas2)


                finish()

            }
            //val intent = Intent(this, CrearVisita::class.java)
            //startActivity(intent)
        }


    }

}