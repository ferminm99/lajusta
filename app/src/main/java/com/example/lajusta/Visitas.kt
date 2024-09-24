package com.example.lajusta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Quinta
import com.example.lajusta.data.model.VerduraEnvio
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import com.example.lajusta.util.model.Visita_Quinta
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Visitas : AppCompatActivity() {
    private var pagina = 0
    private var paginasTotales = 1
    private val cantidadPorPagina = 4.0
    private var arreglo: ArrayList<Visita_Quinta>? = null
    private var tecnicos: ArrayList<Pair<Int, String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitas)

        val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
        sharedPref.edit().remove("key").apply()
        sharedPref.edit().putBoolean("ok",false).apply()

        actualizarListado()
    }

    override fun onRestart() {
        super.onRestart()
        val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
        sharedPref.edit().remove("key").apply()
        sharedPref.edit().putBoolean("ok",false).apply()
        actualizarListado()
    }

    private fun actualizarListado() {
        print("__________________")
        print("ACTUALIZAR LISTADO")
        print("__________________")
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            arreglo = ArrayList<Visita_Quinta>()
            tecnicos = ArrayList<Pair<Int, String>>()
            val result = laJustaService.getVisitas()
            val result2 = laJustaService.getQuintas()
            val users = laJustaService.getUsers().body()
            for (tecnico in users!!){
                tecnicos!!.add(Pair(tecnico.id_user!!.toInt(),tecnico.nombre.toString()))
            }
            val visitas = result.body()?.sortedBy { q -> q.id_quinta };
            val quintas = result2.body();
            if(result.isSuccessful and result2.isSuccessful){
                var quinta: Quinta?;
                for(visita in visitas!!){
                    quinta = quintas?.find { q -> visita.id_quinta == q.id_quinta }
                    if (quinta != null)
                        arreglo!!.add(Visita_Quinta(visita, quinta));
                }
            }
            //Para el paginado
            setPaginasTotales(arreglo!!.size)
            calcularElementosAPaginar()
            checkNavigationButtons()
        }
    }

    private fun setTabla(arreglo: ArrayList<Visita_Quinta>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout
        for(data in arreglo!!){
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_visita, null, false)
            val name = tableRow.findViewById<View>(R.id.name) as TextView
            val direccion = tableRow.findViewById<View>(R.id.direccion) as TextView
            val fecha = tableRow.findViewById<View>(R.id.fecha) as TextView
            val tecnico = tableRow.findViewById<View>(R.id.tecnico) as TextView
            name.text = data.quinta.nombre
            direccion.text = data.quinta.direccion
            fecha.text = data.visita.fecha_visita.toString()

            val tecnicoActual = tecnicos!!.find { it.first == data.visita.id_tecnico }

            tecnico.text = tecnicoActual!!.second
            tableLayout.addView(tableRow)
            val id = data.visita.id_visita
            tableRow.setOnClickListener {
                goToVerVisita(id);
            }
        }
    }


    fun goToVerVisita(id: Int?) {
        val intent = Intent(this, VerVisita::class.java)
        intent.putExtra("id",id.toString());
        startActivity(intent)
    }

    fun goToCrearVisita(view: View?) {
        val intent = Intent(this, CrearVisita::class.java)
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
        if(fin > arreglo!!.size){
            fin = arreglo!!.size
        }
        val subList = arreglo!!.subList(inicio,fin)
        val newList: ArrayList<Visita_Quinta> = ArrayList(subList)
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