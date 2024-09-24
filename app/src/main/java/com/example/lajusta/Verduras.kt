package com.example.lajusta


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.VerduraEnvio
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import com.example.lajusta.util.model.Visita_Quinta
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Verduras : AppCompatActivity() {
    private var pagina = 0
    private var paginasTotales = 1
    private val cantidadPorPagina = 8.0
    private var verdurasObtenidas: ArrayList<VerduraEnvio>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verduras)
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
            verdurasObtenidas = laJustaService.getVerdurasEnvio().body() as ArrayList<VerduraEnvio>
            //Para el paginado
            setPaginasTotales(verdurasObtenidas!!.size)
            calcularElementosAPaginar()
            checkNavigationButtons()
        }
    }

    private fun setTabla(arreglo: ArrayList<VerduraEnvio>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout

        for(data in arreglo!!){
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_verdura_main, null, false)
            val name = tableRow.findViewById<View>(R.id.name) as TextView
            val siembra = tableRow.findViewById<View>(R.id.fechaI) as TextView
            val cosecha = tableRow.findViewById<View>(R.id.fechaF) as TextView
            name.text = data.nombre
            siembra.text = data.mes_siembra.toString()
            cosecha.text = data.tiempo_cosecha.toString()
            tableLayout.addView(tableRow)
            val id = data.id_verdura
            tableRow.setOnClickListener {
                goToVerVerdura(id.toString())
            }
        }
    }

    fun goToVerVerdura(id: String?) {
        val intent = Intent(this, VerVerdura::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }


    fun goToCrearVerdura(view: View?) {
        val intent = Intent(this, CrearVerdura::class.java)
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
        if(fin > verdurasObtenidas!!.size){
            fin = verdurasObtenidas!!.size
        }
        val subList = verdurasObtenidas!!.subList(inicio,fin)
        val newList: ArrayList<VerduraEnvio> = ArrayList(subList)
        setTabla(newList)
    }

    private fun setPaginasTotales(cantidad: Int) {
        paginasTotales = (Math.ceil(cantidad.toDouble() / cantidadPorPagina) - 1).toInt()
    }

    private fun actulizarNumeroDePagina() {
        val texNumeroPagina = findViewById<View>(R.id.text_numero_pagina) as TextView
        texNumeroPagina.text = (""+(pagina+1)+"/"+(paginasTotales+1)) as String
    }
}