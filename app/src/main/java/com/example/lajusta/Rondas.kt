package com.example.lajusta


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Ronda
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Rondas : AppCompatActivity() {
    private var pagina = 0
    private var paginasTotales = 1
    private val cantidadPorPagina = 10.0
    private var rondas: ArrayList<Ronda>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rondas)
        actualizarListado()
    }

    override fun onRestart() {
        super.onRestart()
        actualizarListado()
    }

    private fun actualizarListado() {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            rondas = laJustaService.getRondas().body() as ArrayList<Ronda>
            //Para el paginado
            setPaginasTotales(rondas!!.size)
            calcularElementosAPaginar()
            checkNavigationButtons()
        }
    }


    private fun setTabla(arreglo: ArrayList<Ronda>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout

        for(data in arreglo!!){
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_rondas, null, false)
            val fechaInicio = tableRow.findViewById<View>(R.id.fechaI) as TextView
            val fechaFin = tableRow.findViewById<View>(R.id.fechaF) as TextView
            //name.text = data.verdura!!.nombre.toString()
            fechaInicio.text = data.fecha_inicio.toString()
            fechaFin.text = data.fecha_fin.toString()
            tableLayout.addView(tableRow)
            val id = data.id_ronda
            tableRow.setOnClickListener {
                goToVerRonda(id.toString())
            }
        }
    }

    fun goToVerRonda(id: String?) {
        val intent = Intent(this, VerRonda::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    fun goToCrearRonda(view: View?) {
        val intent = Intent(this, CrearRonda::class.java)
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
        buttonNext.isEnabled = (pagina != paginasTotales.toInt())
        actulizarNumeroDePagina()
    }

    private fun calcularElementosAPaginar(){
        val inicio = (pagina * cantidadPorPagina).toInt()
        var fin =((pagina * cantidadPorPagina)+cantidadPorPagina).toInt()
        if(fin > rondas!!.size){
            fin = rondas!!.size
        }
        val subList = rondas!!.subList(inicio,fin)
        val newList: ArrayList<Ronda> = ArrayList(subList)
        setTabla(newList)
    }

    private fun setPaginasTotales(cantidad: Int) {
        paginasTotales = (Math.ceil(cantidad.toDouble() / cantidadPorPagina) - 1).toInt()
    }

    private fun actulizarNumeroDePagina() {
        val texNumeroPagina = findViewById<View>(R.id.text_numero_pagina) as TextView
        texNumeroPagina.text = ""+(pagina+1)+"/"+(paginasTotales+1)
    }
}