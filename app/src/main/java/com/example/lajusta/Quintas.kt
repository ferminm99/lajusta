package com.example.lajusta

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TableLayout
import android.view.LayoutInflater
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Quinta
import com.example.lajusta.data.model.VerduraEnvio
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Quintas : AppCompatActivity() {
    private var pagina = 0
    private var paginasTotales = 1
    private val cantidadPorPagina = 5.0
    private var quintas: ArrayList<Quinta>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quintas)
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
            quintas = laJustaService.getQuintas().body() as ArrayList<Quinta>
            //Para el paginado
            setPaginasTotales(quintas!!.size)
            calcularElementosAPaginar()
            checkNavigationButtons()
        }
    }

    private fun setTabla(arreglo: ArrayList<Quinta>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout
        for (data in arreglo) {
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_quinta, null, false)
            val name = tableRow.findViewById<View>(R.id.name) as TextView
            val direccion = tableRow.findViewById<View>(R.id.direccion) as TextView
            val verRuta = tableRow.findViewById<View>(R.id.button_make_path) as ImageButton
            name.text = data.nombre
            direccion.text = data.direccion
            tableLayout.addView(tableRow)
            val id = data.id_quinta
            verRuta.setOnClickListener {
                val intent = Intent(this, CrearRuta::class.java)
                intent.putExtra("id", id.toString())
                startActivity(intent)
            }
            tableRow.setOnClickListener {
                goToVerQuinta(id.toString())
            }
        }
    }

    fun goToVerQuinta(id: String?) {
        val intent = Intent(this, VerQuinta::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    /** Called when the user taps the Send button  */
    fun goToCrearQuinta(view: View?) {
        val intent = Intent(this, CrearQuinta::class.java)
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
        if(fin > quintas!!.size){
            fin = quintas!!.size
        }
        val subList = quintas!!.subList(inicio,fin)
        val newList: ArrayList<Quinta> = ArrayList(subList)
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