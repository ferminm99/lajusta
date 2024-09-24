package com.example.lajusta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.*
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class VerBolson : AppCompatActivity() {
    private var verduras_incluidas: HashSet<Verdura>? = null
    private var id: String? = null
    private var fpSeleccionado: Int? = null
    private var verduraSeleccionada: Int? = null
    private var idRonda: Int? = null
    private var cantidadBolsones: EditText? = null
    private var verduras: ArrayList<Verdura>? = null

    private var familiasVerduras:HashMap<Int,HashSet<Int>>? = null
    private var verduras_base: HashSet<Verdura> = HashSet<Verdura>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_bolson)
        val myIntent = intent
        id = myIntent.getStringExtra("id")

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            var familiasProductoras = laJustaService.getFamiliasProductoras().body()
            var quintas = laJustaService.getQuintas().body()
            var visitas = laJustaService.getVisitas().body()?.sortedBy { q -> q.id_quinta }
            var parcelas = laJustaService.getParcelas().body()
            var rondas = ArrayList(laJustaService.getRondas().body())

            // Cosas para comprobaciones de propiedad de Bolson
            verduras = ArrayList(laJustaService.getVerduras().body())
            familiasVerduras = HashMap<Int,HashSet<Int>>()
            for (familia:FamiliaProductora in familiasProductoras!!) {
                val setVerduras = HashSet<Int>()
                for (quinta in quintas!!)
                    if(familia.id_fp == quinta.fpId) {
                        var visitaMax:Visita? = null
                        var fechaMax: ArrayList<Int> = ArrayList<Int>()
                        fechaMax.add(-1)
                        fechaMax.add(-1)
                        fechaMax.add(-1)
                        for (visita:Visita in visitas!!)
                            if((quinta.id_quinta == visita.id_quinta)){
                                var fechaAct:ArrayList<Int> =
                                    (visita.fecha_visita?.map {it.toInt()} as ArrayList<Int>?)!!
                                if(fechaAct[0] > fechaMax[0]) {
                                    visitaMax = visita
                                    fechaMax = fechaAct
                                } else if (fechaAct[0] == fechaMax[0]
                                    && fechaAct[1] > fechaMax[1]) {
                                    visitaMax = visita
                                    fechaMax=fechaAct
                                } else if (fechaAct[0] == fechaMax[0]
                                    && fechaAct[1] == fechaMax[1]
                                    && fechaAct[2] > fechaMax[2]) {
                                    visitaMax = visita
                                    fechaMax = fechaAct
                                }
                            }
                        for (parcela:ParcelaNormal in parcelas!!)
                            if(visitaMax?.id_visita == parcela.id_visita)
                                setVerduras.add(parcela.id_verdura!!)
                    }
                familiasVerduras!![familia.id_fp!!] = setVerduras
            }
            println("_____________________________")
            println(familiasVerduras)
            println("_____________________________")

            val bolson = laJustaService.getBolson(id!!.toInt()).body()
            val fp = laJustaService.getFamiliaProductora(bolson?.idFp!!).body()
            val ronda = laJustaService.getRonda(bolson.idRonda!!).body()

            //para spinners
            verduras = ArrayList(laJustaService.getVerduras().body())

            // Setea en base al estado actual del bolson
            cantidadBolsones = findViewById<View>(R.id.cantidadBolsones_edit) as EditText
            cantidadBolsones!!.setText(bolson.cantidad.toString())

            // Ronda Select
            val rondaId = ArrayList<Int>(rondas.size)
            for (r in rondas) {
                rondaId.add(r.id_ronda!!)
            }
            val ronda_spinner = findViewById<View>(R.id.ronda) as Spinner
            val ronda_spinnerAdapter = ArrayAdapter(this@VerBolson, android.R.layout.simple_spinner_item,rondaId)
            ronda_spinner.adapter = ronda_spinnerAdapter
            ronda_spinner.setSelection(ronda_spinnerAdapter.getPosition(ronda!!.id_ronda))
            ronda_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    idRonda =  rondaId[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            // Verdura Select
            val verduraId = ArrayList<Int>(verduras!!.size)
            val verduraName = ArrayList<String>(verduras!!.size)
            for (verdura in verduras!!) {
                verduraId.add(verdura.id_verdura!!)
                verduraName.add(verdura.nombre!!)
            }
            val spinner_verdura = findViewById<View>(R.id.verdura_select) as Spinner
            spinner_verdura.adapter = ArrayAdapter(this@VerBolson,
                android.R.layout.simple_spinner_item, verduraName)
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
            verduras_incluidas = HashSet<Verdura>()
            for(v in bolson.verduras!!){
                verduras_incluidas!!.add(laJustaService.getVerdura(v.id_verdura!!).body()!!)
            }
            verduras_base = verduras_incluidas!!.clone() as HashSet<Verdura>
            setTabla(verduras_incluidas!!)

            // FamiliaProductora Select
            val fpNames = ArrayList<String>(familiasProductoras.size)
            val fpId = ArrayList<Int>(familiasProductoras.size)
            for (fp in familiasProductoras) {
                fpId.add(fp.id_fp!!)
                fpNames.add(fp.nombre!!)
            }
            var fp_spinner = findViewById<View>(R.id.familiaProductora) as Spinner
            val fp_spinnerAdapter = ArrayAdapter(this@VerBolson, android.R.layout.simple_spinner_item,fpNames)
            fp_spinner.adapter = fp_spinnerAdapter
            fp_spinner.setSelection(fp_spinnerAdapter.getPosition(fp!!.nombre))
            fp_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    //actualizarListasDeVerduras()
                    fpSeleccionado = fpId[position]
                    verduras_incluidas = verduras_base.clone() as HashSet<Verdura>
                    setTabla(verduras_base)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setTabla(arreglo: Set<Verdura>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tabla_verduras) as TableLayout

        for(data in arreglo){
            val tableRow = LayoutInflater.from(this).inflate(R.layout.table_item_verdura, null, false)
            val nombre = tableRow.findViewById<View>(R.id.name) as TextView
            val descripcion = tableRow.findViewById<View>(R.id.descripcion) as TextView
            nombre.text = data.nombre.toString()
            descripcion.text = data.descripcion.toString()
            tableLayout.addView(tableRow)
            tableRow.setOnClickListener {
                //goToVerBolson(id);
            }
        }
    }

    private fun cleanTable() {
        val table = findViewById<View>(R.id.tabla_verduras) as TableLayout
        val childCount = table.childCount
        if (childCount > 1) {
            table.removeViews(1, childCount - 1)
        }
    }

    fun addVerdura(view: View?) {

        // Comprueba que con la nueva inclusion no mas de 2 verduras no pertenezcan
        // al productor actual
        val setIdVerdurasFp = familiasVerduras?.get(fpSeleccionado)
        val idsVerduras = verduras_incluidas!!.map { it.id_verdura }
        val setNoPropias = HashSet<Int>()
        for (idV in idsVerduras)
            if(!(setIdVerdurasFp?.contains(idV)!!))
                setNoPropias.add(idV!!)
        if(!(setIdVerdurasFp?.contains(verduraSeleccionada)!!))
            setNoPropias.add(verduraSeleccionada!!)
        val okMax2NoPropias:Boolean = (setNoPropias.count()<=2)

        // Comprueba que las verduras a agregar que uno no tenga por lo menos la tenga algun
        // compañero productor
        val setCultivadasPorAlguien = HashSet<Int>()
        for (idFp in familiasVerduras!!.keys)
            setCultivadasPorAlguien.addAll(familiasVerduras!![idFp]!!)
        val okCultivadaPorAlguien = setCultivadasPorAlguien.contains(verduraSeleccionada!!)

        if (!(setIdVerdurasFp.contains(verduraSeleccionada))
            && okMax2NoPropias && okCultivadaPorAlguien)
            Toast.makeText( applicationContext,
                "Adv: La verdura seleccionada es de un productor externo",
                Toast.LENGTH_LONG).show()

        // A lo mucho 7 verduras
        if(verduras_incluidas!!.size >= 7)
            Toast.makeText(applicationContext,
                " Ya hay 7 verduras seleccionadas", Toast.LENGTH_LONG).show()
        else if(!okCultivadaPorAlguien)
            Toast.makeText(applicationContext,
                "La verdura no es de campo propio y nadie mas la cultiva", Toast.LENGTH_LONG).show()
        else if(!okMax2NoPropias)
            Toast.makeText(applicationContext,
                "A lo mucho 2 verduras pueden ser de productores externos...",
                Toast.LENGTH_LONG).show()
        else {
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                val verdura = laJustaService.getVerdura(verduraSeleccionada!!).body()
                verduras_incluidas?.add(verdura!!)
                setTabla(verduras_incluidas!!)
            }
        }
    }

    fun deleteVerdura(view: View?) {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        var verdura: Any? = null
        lifecycleScope.launch() {
            val verdura = laJustaService.getVerdura(verduraSeleccionada!!).body()
            verduras_incluidas?.remove(verdura!!)
            setTabla(verduras_incluidas!!)
        }
    }

    fun cargarVerdurasEnMap() {
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout
        for (v in verduras!!) {
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_verdura_bolson, null, false)
            val name = tableRow.findViewById<View>(R.id.name) as TextView
            name.text = v.nombre
            tableLayout.addView(tableRow)
            val inputCant = tableRow.findViewById<View>(R.id.inputCant) as EditText
            inputCant.tag = verduras!!.size
        }
    }

    fun eliminar(view: View?) {
        eliminarBolson()
    }

    private fun eliminarBolson() {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch() {
            laJustaService.deleteBolson(id!!.toInt())
            val intent = Intent(this@VerBolson, Bolsones::class.java)
            startActivity(intent)
            //hay que chequear si se crea con un response pero no nos andaban...
        }
    }

    fun guardar(view: View?) {
        actualizarBolson()
    }

    fun actualizarBolson() {
        if(cantidadBolsones?.text?.toString().equals("")!!) {
            Toast.makeText(applicationContext, "Falta especificar la cantidad de bolsones", Toast.LENGTH_LONG).show()
        } else if(verduras_incluidas?.size != 7) {
            Toast.makeText(applicationContext, "Necesita seleccionar exactamente 7 verduras", Toast.LENGTH_LONG).show()
        } else {
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                var verduras = ArrayList<VerduraEnvio>()
                var ve: VerduraEnvio;
                var tc: ArrayList<Int>
                var ms: ArrayList<Int>
                for (v in verduras_incluidas!!) {
                    tc = ArrayList()
                    tc.add(v.tiempo_cosecha?.get(0)!!)
                    tc.add(v.tiempo_cosecha?.get(1)!!)
                    tc.add(v.tiempo_cosecha?.get(2)!!)
                    ms = ArrayList()
                    ms.add(v.mes_siembra?.get(0)!!)
                    ms.add(v.mes_siembra?.get(1)!!)
                    ms.add(v.mes_siembra?.get(2)!!)
                    ve = VerduraEnvio(id_verdura = v.id_verdura, tiempo_cosecha = tc,
                        mes_siembra = ms, archImg = v.archImg, nombre = v.nombre,
                        descripcion = v.descripcion)
                    verduras.add(ve)
                }
                var bolson = BolsonEnvio(id_bolson = id!!.toInt(),
                    cantidad = cantidadBolsones!!.text.toString().toInt(),
                    idFp = fpSeleccionado, idRonda = idRonda, verduras = verduras)
                laJustaService.editBolson(bolson)
                finish()
            }
        }
    }




}
