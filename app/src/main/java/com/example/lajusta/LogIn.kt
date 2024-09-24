package com.example.lajusta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.User
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import util.session.StoreManager

class LogIn: AppCompatActivity() {

    private var actUsername: EditText? = null
    private var actPassword: EditText? = null
    private var auxThis = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        actUsername = findViewById<View>(R.id.username_input) as EditText
        actPassword = findViewById<View>(R.id.password_input) as EditText
    }

    fun logIn(view: View?) {
        val username = actUsername?.text?.toString()
        val password = actPassword?.text?.toString()
        if(username.equals("")) {
            Toast.makeText(applicationContext, "Falta especificar el nombre de usuario", Toast.LENGTH_LONG).show()
        } else if (password.equals("")) {
            Toast.makeText(applicationContext, "Falta especificar la contraseña", Toast.LENGTH_LONG).show()
        } else {
            var logInOk = false
            var user: User? = null
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch {
                val users = laJustaService.getUsers().body()
                for (u: User in users!!){
                    if (u.username.equals(username)) {
                        if (u.password.equals(password)) {
                            logInOk = true
                            user = u
                        }
                        else
                            break
                    }
                }
                if(!logInOk) {
                    Toast.makeText(applicationContext, "Usuario y/o contraseña incorrecto", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(auxThis, MainActivity::class.java)
                    val storeManager = StoreManager(applicationContext)
                    storeManager.setToken( user?.id_user.toString())
                    //intent.putExtra("id", user?.id_user.toString());
                    startActivity(intent)
                }
            }
        }
    }

}