package com.example.lajusta

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Bolson
import com.example.lajusta.data.model.FamiliaProductora
import com.example.lajusta.data.model.Ronda
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import com.example.lajusta.util.model.Bolson_FP_Quinta
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.ceil

class Bolsones : AppCompatActivity() {
    private var pagina = 0
    private var paginasTotales = 1
    private val cantidadPorPagina = 8.0
    private var arreglo: ArrayList<Bolson_FP_Quinta>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bolsones)
        actualizarListado()
    }

    override fun onRestart() {
        super.onRestart()
        actualizarListado()
    }

    private fun actualizarListado(){
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            val familiasProductoras = laJustaService.getFamiliasProductoras().body()
            val quintas = laJustaService.getQuintas().body()
            val arregloDeBolsones: ArrayList<Bolson> = ArrayList<Bolson>();
            val rondas = laJustaService.getRondas().body()
            for(ronda in rondas!!){
                val bolson = laJustaService.getBolsones(ronda.id_ronda!!).body()
                if(bolson!!.isNotEmpty()){
                    for(b in bolson){
                        arregloDeBolsones.add(b)
                    }

                }
            }
            arreglo = ArrayList<Bolson_FP_Quinta>()
            var r: Ronda?;
            var fp: FamiliaProductora?;
            var bolsones = arregloDeBolsones
            println(bolsones)
            //no agarra los bolsonnes porque esto del GET no funciona
            println(quintas)
            println(familiasProductoras)

            for(bolson in bolsones){
                println(bolson)
                fp = familiasProductoras?.find { q -> bolson.idFp == q.id_fp }
                r = rondas.find { q -> bolson.idRonda == q.id_ronda }
                //quinta = quintas?.find { q -> bolson.idFp == q.fpId }
                if (fp != null)
                    arreglo!!.add(Bolson_FP_Quinta(fp, r!!,bolson))
            }
            //Para el paginado
            setPaginasTotales(arreglo!!.size)
            calcularElementosAPaginar()
            checkNavigationButtons()
        }
    }

    private fun setTabla(arreglo: ArrayList<Bolson_FP_Quinta>) {
        //SQLiteDatabase db = MainActivity.conn.getReadableDatabase();
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout

        for(data in arreglo){
            val tableRow = LayoutInflater.from(this)
                .inflate(R.layout.table_item_bolsones, null, false)
            val quintaName = tableRow.findViewById<View>(R.id.quintaName) as TextView
            val fpName = tableRow.findViewById<View>(R.id.fpName) as TextView
            val cb = tableRow.findViewById<View>(R.id.cantidadBolsones) as TextView
            //val title = tableRow.findViewById<View>(R.id.title) as TextView
            //val idQuinta = data.ronda.id_ronda
            //Cursor rowQuinta = db.rawQuery("SELECT nombre FROM quintas WHERE id='"+idQuinta+"'", null);
            //rowQuinta.moveToFirst();
            quintaName.text = data.ronda.id_ronda.toString()
            fpName.text = data.familiaProductora.nombre
            cb.text = data.bolson.cantidad.toString()
            tableLayout.addView(tableRow)
            val id = data.bolson.id_bolson
            tableRow.setOnClickListener {
                goToVerBolson(id);
            }
        }
    }

    private fun goToVerBolson(id: Int?) {
        val intent = Intent(this, VerBolson::class.java)
        intent.putExtra("id",id.toString())
        startActivity(intent)
    }


    /** Called when the user taps the Send button  */
    fun goToCrearBolson(view: View?) {
        val intent = Intent(this, CrearBolson::class.java)
        startActivity(intent)
    }

    private fun cleanTable() {
        val table = findViewById<View>(R.id.tableLayout) as TableLayout
        val childCount = table.childCount
        if (childCount > 1) {
            table.removeViews(1, childCount - 1)
        }
    }

    fun siguiente(view: View?) {
        if (pagina < paginasTotales) {
            pagina++
            cleanTable()
            calcularElementosAPaginar()
        }
        checkNavigationButtons()
    }

    fun anterior(view: View?) {
        if (pagina > 0) {
            pagina--
            cleanTable()
            calcularElementosAPaginar()
        }
        checkNavigationButtons()
    }

    private fun checkNavigationButtons(){
        val buttonBack = findViewById<View>(R.id.button_back) as Button
        buttonBack.isEnabled = (pagina != 0)
        val buttonNext = findViewById<View>(R.id.button_next) as Button
        buttonNext.isEnabled = (pagina != paginasTotales)
        actulizarNumeroDePagina()
    }

    private fun calcularElementosAPaginar(){
        val inicio = (pagina * cantidadPorPagina).toInt()
        var fin =((pagina * cantidadPorPagina)+cantidadPorPagina).toInt()
        if(fin > arreglo!!.size){
            fin = arreglo!!.size
        }
        val subList = arreglo!!.subList(inicio,fin)
        val newList: ArrayList<Bolson_FP_Quinta> = ArrayList(subList)
        setTabla(newList)
    }

    private fun setPaginasTotales(cantidad: Int) {
        paginasTotales = (ceil(cantidad.toDouble() / cantidadPorPagina) - 1).toInt()
    }

    private fun actulizarNumeroDePagina() {
        val texNumeroPagina = findViewById<View>(R.id.text_numero_pagina) as TextView
        texNumeroPagina.text = (""+(pagina+1)+"/"+(paginasTotales+1)) as String
    }
}