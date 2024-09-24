package com.example.lajusta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.*
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.ceil
import kotlin.collections.HashSet

class CrearBolson : AppCompatActivity() {

    private var ronda: Spinner? = null
    private var fp: Spinner? = null
    private var cantidadBolsones: EditText? = null
    private var fpSeleccionado: Int? = null
    private var idRonda: Int? = null
    private var verduraSeleccionada: Int? = null

    // Variables para manejar la tabla verduras
    private var paginasTotales = 1.0
    private var pagina = 0
    private var verduras_incluidas = HashSet<Verdura>()

    // Arreglo para comprobaciones
    private var bolsones: ArrayList<Bolson>? = null

    private var verduras: ArrayList<Verdura>? = null

    private var familiasVerduras:HashMap<Int,HashSet<Int>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_bolson)

        ronda = findViewById<View>(R.id.ronda) as Spinner
        cantidadBolsones = findViewById<View>(R.id.editTextNumber8) as EditText
        fp = findViewById<View>(R.id.familiaProductora) as Spinner

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch() {

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

            // Verdura Select
            val spinner_verdura = findViewById<View>(R.id.verdura_select) as Spinner
            val verduraId = ArrayList<Int>(verduras!!.size)
            val verduraName = ArrayList<String>(verduras!!.size)
            for (verdura in verduras!!) {
                verduraId.add(verdura.id_verdura!!)
                verduraName.add(verdura.nombre!!)
            }

            spinner_verdura.adapter = ArrayAdapter(this@CrearBolson, android.R.layout.simple_spinner_item, verduraName)
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

            // Ronda select
            val rondaID = ArrayList<Int>(rondas.size)
            for (ronda in rondas) {
                //SpnName.get(i) = pojoArrayList.get(i).getName()
                rondaID.add(ronda.id_ronda!!)
            }

            val spinner2 = findViewById<View>(R.id.ronda) as Spinner
            val spinnerAdapter2 = ArrayAdapter(this@CrearBolson, android.R.layout.simple_spinner_item, rondaID)
            spinner2.adapter = spinnerAdapter2
            spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    idRonda =  rondaID[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            // FamiliaProductora Select
            val fpId = ArrayList<Int>(familiasProductoras!!.size)
            val fpName = ArrayList<String>(familiasProductoras!!.size)
            for (familiasProductora in familiasProductoras!!) {
                //SpnName.get(i) = pojoArrayList.get(i).getName()
                fpId.add(familiasProductora.id_fp!!)
                fpName.add(familiasProductora.nombre!!)
            }

            val spinner3 = findViewById<View>(R.id.familiaProductora) as Spinner
            val spinner3Adapter = ArrayAdapter(this@CrearBolson, android.R.layout.simple_spinner_item, fpName)
            spinner3.adapter = spinner3Adapter
            spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    //actualizarListasDeVerduras()
                    fpSeleccionado = fpId[position]
                    verduras_incluidas = HashSet<Verdura>()
                    setTabla(verduras_incluidas)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            // Recupera todos los bolsones existentes para comprobaciones posteriores
            bolsones = ArrayList<Bolson>();
            for(ronda in rondas!!){
                val bolson = laJustaService.getBolsones(ronda.id_ronda!!).body()
                if(bolson!!.isNotEmpty()){
                    println(ronda.id_ronda)
                    for(b in bolson!!){
                        bolsones!!.add(b)
                    }

                }
            }
        }
    }

    /*fun actualizarListasDeVerduras() {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)

        // Comprueba que no haya mas de 2 verduras externas
        var min_año = 999999
        var min_mes = 999999
        var min_dia = 999999
        var visita:Visita?
        for (fp in familiasProductoras!!)
            for (q in quintas!!){
                visita = null
                // Busca la ultima visita de la quinta actual
                if ((fp.id_fp != fpSeleccionado) and (fp.id_fp == q.fpId)){
                    for (v in visitas!!)
                        if((q.id_quinta == v.id_quinta) and
                            (parseInt(v.fecha_visita?.get(0)) <= min_año) and
                            (parseInt(v.fecha_visita?.get(1)) <= min_mes) and
                            (parseInt(v.fecha_visita?.get(2)) <= min_dia)){
                            min_año = parseInt(v.fecha_visita?.get(0))
                            min_mes = parseInt(v.fecha_visita?.get(1))
                            min_dia = parseInt(v.fecha_visita?.get(2))
                            visita = v
                        }
                }
                if(visita == null) {
                    // Aca iria una recopilacion de las verduras de las parcelas de la visita
                    // guardada en la variable "visita" para ser guardadas en el arreglo
                    // "verduras_de_otros". Lamentablemente el getParcelas esta roto, por lo
                    // que por ahora esto queda en pausa
                }

            }
    }*/

    fun completarTablaVerduras() {
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout
        for (key in cantVerduras.keys) {
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_verdura, null, false)
            val nombre = tableRow.findViewById<View>(R.id.name) as TextView
            nombre.text = key
            tableLayout.addView(tableRow)
            val descripcion = tableRow.findViewById<View>(R.id.descripcion) as TextView
            descripcion.tag = key
            descripcion.text = "0"
        }
    }

    companion object {
        var cantVerduras: Map<String, Int> = HashMap()
    }

    /*
    Comprueba que la verdura se pueda agregar al arreglo recibido.
    Los criterios de comprobacion son los siguientes:
    - Maximo de 7 verduras por bolson
    - Minimo 5 verduras tienen que pertenecer a la familia productora
        que desea registrar el bolson.
    - Las verduras que no pertenescan a la familia pueden ser a lo
        mucho 2 y deben ser cultivadas por alguna otra familia.
    */
    fun addVerdura(view: View?) {

        // Comprueba que con la nueva inclusion no mas de 2 verduras no pertenezcan
        // al productor actual
        val setIdVerdurasFp = familiasVerduras?.get(fpSeleccionado)
        val idsVerduras = verduras_incluidas.map { it.id_verdura }
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
        if(verduras_incluidas.size >= 7)
            Toast.makeText(applicationContext,
                "Ya hay 7 verduras seleccionadas", Toast.LENGTH_LONG).show()
        else if(!okCultivadaPorAlguien)
            Toast.makeText(applicationContext,
                "La verdura no es de campo propio y nadie mas la cultiva", Toast.LENGTH_LONG).show()
        else if(!okMax2NoPropias)
            Toast.makeText(applicationContext,
                "A lo mucho 2 verduras pueden ser de productores externos...",
                Toast.LENGTH_LONG).show()
        else{
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                val verdura = laJustaService.getVerdura(verduraSeleccionada!!).body()
                val fp = laJustaService.getFamiliaProductora(fpSeleccionado!!).body()
                verduras_incluidas.add(verdura!!)
                setTabla(verduras_incluidas)
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
            verduras_incluidas.remove(verdura!!)
            setTabla(verduras_incluidas)
        }
    }

    private fun setTabla(arreglo: Set<Verdura>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tabla_verduras) as TableLayout

        for(data in arreglo!!){
            val tableRow = LayoutInflater.from(this).inflate(R.layout.table_item_verdura, null, false)
            val nombre = tableRow.findViewById<View>(R.id.name) as TextView
            val descripcion = tableRow.findViewById<View>(R.id.descripcion) as TextView
            nombre.text = data.nombre.toString()
            descripcion.text = data.descripcion.toString()
            tableLayout.addView(tableRow)
            val id = data.id_verdura
            tableRow.setOnClickListener {
                //goToVerBolson(id);
            }
        }
    }

    fun siguiente(view: View?) {
        if (pagina < paginasTotales) {
            pagina++
            cleanTable()
            //consultarBolsones();
        } else {
            Toast.makeText(applicationContext, "No hay mas elementos", Toast.LENGTH_SHORT).show()
        }
    }

    fun anterior(view: View?) {
        if (pagina > 1) {
            pagina--
            cleanTable()
            //consultarBolsones();
        } else {
            Toast.makeText(applicationContext, "No hay mas elementos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cleanTable() {
        val table = findViewById<View>(R.id.tabla_verduras) as TableLayout
        val childCount = table.childCount
        if (childCount > 1) {
            table.removeViews(1, childCount - 1)
        }
    }

    private fun setPaginasTotales(cantidad: Int) {
        paginasTotales = ceil(cantidad.toDouble() / 12.0)
    }

    // Metodo que se ejecuta al clickear el boton Crear Bolson
    fun crearBolson(view: View?) {

        var ok_coincidencia_en_db:Boolean = true
        for (bolson:Bolson in bolsones!!){
            if ((bolson.idRonda == idRonda) && (bolson.idFp == fpSeleccionado)){
                ok_coincidencia_en_db =false
                break
            }
        }

        if(cantidadBolsones?.text?.toString().equals("")!!) {
            Toast.makeText(applicationContext, "Falta especificar la cantidad de bolsones", Toast.LENGTH_LONG).show()
        } else if(verduras_incluidas.size != 7) {
            Toast.makeText(applicationContext, "Necesita seleccionar exactamente 7 verduras para crear un bolson", Toast.LENGTH_LONG).show()
        } else if(!ok_coincidencia_en_db){
            Toast.makeText(applicationContext, "Ya existe un bolson de esta familia productora para la ronda dada", Toast.LENGTH_LONG).show()
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
                var bolson = BolsonEnvio(id_bolson = null, cantidad = cantidadBolsones!!.text.toString().toInt(),
                    idFp = fpSeleccionado, idRonda = idRonda, verduras = verduras)
                laJustaService.addBolson(bolson)
                finish()
            }
        }
    }
}