package com.example.lajusta

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.User
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern


class CreateUser: AppCompatActivity() {

    private var nameSelect: EditText? = null
    private var surnameSelect: EditText? = null
    private var addressSelect: EditText? = null
    private var emailSelect: EditText? = null
    private var usernameSelect: EditText? = null
    private var passwordSelect: EditText? = null
    private var roleSelect: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        // No mostrar Boton "Eliminar" para crear
        val buttonDelete: Button = findViewById(R.id.button_delete)
        buttonDelete.isEnabled = false
        buttonDelete.visibility = View.GONE

        // set nombre EditText
        nameSelect = findViewById<View>(R.id.name_input) as EditText

        // set apellido EditText
        surnameSelect = findViewById<View>(R.id.surname_input) as EditText

        // set direccion EditText
        addressSelect = findViewById<View>(R.id.address_input) as EditText

        // set email EditText
        emailSelect = findViewById<View>(R.id.email_input) as EditText

        // set username EditText
        usernameSelect = findViewById<View>(R.id.username_input) as EditText

        // set password EditText
        passwordSelect = findViewById<View>(R.id.password_input) as EditText

        // set role spinner
        val roleNames = java.util.ArrayList<String>()
        roleNames.add("Administrador")
        roleNames.add("Tecnico")
        val roleIds = java.util.ArrayList<Int>()
        roleIds.add(0)
        roleIds.add(1)
        val roleSpinner = findViewById<View>(R.id.role_spinner) as Spinner
        roleSpinner.adapter =
            ArrayAdapter(this@CreateUser, android.R.layout.simple_spinner_item, roleNames)
        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                roleSelect = roleIds[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        roleSpinner.setSelection(1)
    }

    fun saveChanges(view: View){
        if(nameSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar nombre", Toast.LENGTH_LONG).show()
        } else if(surnameSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar apellido", Toast.LENGTH_LONG).show()
        } else if(addressSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar direccion", Toast.LENGTH_LONG).show()
        } else if(emailSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar email", Toast.LENGTH_LONG).show()
        } else if (!validarEmail(emailSelect?.text?.toString()!!)) {
            Toast.makeText(applicationContext, "Formato no valido en email", Toast.LENGTH_LONG).show()
        } else if(usernameSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar nombre de usuario", Toast.LENGTH_LONG).show()
        } else if(passwordSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar contraseña", Toast.LENGTH_LONG).show()
        } else {
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch {
                val users = laJustaService.getUsers().body() as ArrayList<User>
                var singleUsername = true
                for (user:User in users)
                    if (user.username?.equals(usernameSelect)!!)
                        singleUsername = false
                if (!singleUsername) {
                    Toast.makeText(applicationContext, "El nombre de usuario escogido ya existe", Toast.LENGTH_LONG).show()
                } else {
                    val user =  User(id_user = null,
                        nombre = nameSelect?.text.toString(),
                        apellido = surnameSelect?.text.toString(),
                        direccion = addressSelect?.text.toString(),
                        email = emailSelect?.text.toString(),
                        username = usernameSelect?.text.toString(),
                        password = passwordSelect?.text.toString(),
                        roles = roleSelect)
                    val res = laJustaService.addUser(user)
                    finish()
                }
            }
        }
    }

    private fun validarEmail(email: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}