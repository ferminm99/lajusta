package com.example.lajusta

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.User
import com.example.lajusta.data.model.VerduraEnvio
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class Usuarios: AppCompatActivity() {
    private var pagina = 0
    private var paginasTotales = 1
    private val cantidadPorPagina = 10.0
    private var users: ArrayList<User>? = null
    private val rol_dict = java.util.HashMap<Int,String>()
    private var id_act: String? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        rol_dict[0] = "Administrador"
        rol_dict[1] = "Tecnico"

        val myIntent = intent
        id_act = myIntent.getStringExtra("id")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        actualizarListado()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRestart() {
        super.onRestart()
        actualizarListado()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun actualizarListado() {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            users = laJustaService.getUsers().body() as ArrayList<User>
            users!!.removeIf { user -> (user.id_user == id_act?.toInt())}
            //Para el paginado
            setPaginasTotales(users!!.size)
            calcularElementosAPaginar()
            checkNavigationButtons()
        }
    }

    private fun setTabla(arreglo: List<User>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout

        for(data in arreglo){
            val tableRow = LayoutInflater.from(this).inflate(R.layout.table_item_usuarios, null, false)
            val username = tableRow.findViewById<View>(R.id.username_column) as TextView
            val email = tableRow.findViewById<View>(R.id.email_column) as TextView
            val rol = tableRow.findViewById<View>(R.id.rol_column) as TextView
            username.text = data.nombre
            email.text = data.email
            rol.text = rol_dict.get(data.roles)
            tableLayout.addView(tableRow)
            val id_obj = data.id_user
            tableRow.setOnClickListener {
                goToEditUser(id_act?.toInt()!!, id_obj!!);
            }
        }
    }

    private fun goToEditUser(id_act: Int, id_obj: Int) {
        val intent = Intent(this, EditUser::class.java)
        intent.putExtra("id", id_act.toString())
        intent.putExtra("id_obj", id_obj.toString())
        startActivity(intent);
    }


    /** Called when the user taps the Send button  */
    fun goToCreateUser(view: View?) {
        val intent = Intent(this, CreateUser::class.java)
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
        if(fin > users!!.size){
            fin = users!!.size
        }
        val subList = users!!.subList(inicio,fin)
        val newList: ArrayList<User> = ArrayList(subList)
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