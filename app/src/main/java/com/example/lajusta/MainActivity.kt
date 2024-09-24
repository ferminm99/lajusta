package com.example.lajusta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import util.session.StoreManager


class MainActivity : AppCompatActivity() {

    private var user_id: String? = null
    private lateinit var toolbar: Toolbar
    private var auxThis = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myIntent = intent
        val storeManager = StoreManager(applicationContext)
        user_id = storeManager.token

        val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("ok",false).apply()
        sharedPref.edit().remove("key").apply()

        toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        // Config toolbar title
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            val user = laJustaService.getUser(user_id!!.toInt()).body()
            toolbar.title = user?.nombre +" "+user?.apellido

            // Solo mostrar Boton "Usuarios" si el usuario logeado es "Administrador"
            if(user?.roles != 0){
                val button_usuarios: Button = findViewById(R.id.usuarios)
                button_usuarios.isEnabled = false
                button_usuarios.visibility = View.GONE
            }
        }

    }

    override fun onRestart() {
        super.onRestart()
        // Config toolbar title
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            val user = laJustaService.getUser(user_id!!.toInt()).body()
            toolbar.title = user?.nombre +" "+user?.apellido

            // Solo mostrar Boton "Usuarios" si el usuario logeado es "Administrador"
            if(user?.roles != 0){
                val button_usuarios: Button = findViewById(R.id.usuarios)
                button_usuarios.isEnabled = false
                button_usuarios.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_edit_user -> {
                val intent = Intent(auxThis, EditUser::class.java)
                intent.putExtra("id", user_id.toString())
                intent.putExtra("id_obj", user_id.toString())
                startActivity(intent)
                true
            } R.id.button_log_out -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun goToVisitas(view: View?) {
        val intent = Intent(this, Visitas::class.java)
        startActivity(intent)
    }

    fun goToBolsones(view: View?) {
        val intent = Intent(this, Bolsones::class.java)
        startActivity(intent)
    }

    fun goToQuintas(view: View?) {
        val intent = Intent(this, Quintas::class.java)
        startActivity(intent)
    }




    fun goToUsuarios(view: View?) {
        val intent = Intent(this, Usuarios::class.java)
        intent.putExtra("id", user_id.toString())
        startActivity(intent)
    }

    fun goToVerduras(view: View?) {
        val intent = Intent(this, Verduras::class.java)
        startActivity(intent)
    }

    fun goToFamiliaProductora(view: View?) {
        val intent = Intent(this, FamiliasProductoras::class.java)
        startActivity(intent)
    }

    fun goToRondas(view: View?) {
        val intent = Intent(this, Rondas::class.java)
        startActivity(intent)
    }

}