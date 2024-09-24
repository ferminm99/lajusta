package com.example.lajusta

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.User
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class EditUser: AppCompatActivity() {

    private var idAct: String? = null
    private var idObj: String? = null
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

        val myIntent = intent
        idAct = myIntent.getStringExtra("id")
        idObj = myIntent.getStringExtra("id_obj")

        // Solo mostrar Boton "Eliminar" para los demas usuarios
        if(idAct?.toInt() == idObj?.toInt()){
            val buttonDelete: Button = findViewById(R.id.button_delete)
            buttonDelete.isEnabled = false
            buttonDelete.visibility = View.GONE
        }

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            val user = laJustaService.getUser(idObj!!.toInt()).body()

            // set nombre
            nameSelect = findViewById<View>(R.id.name_input) as EditText
            nameSelect!!.setText(user?.nombre)

            // set apellido
            surnameSelect = findViewById<View>(R.id.surname_input) as EditText
            surnameSelect!!.setText(user?.apellido)

            // set direccion
            addressSelect = findViewById<View>(R.id.address_input) as EditText
            addressSelect!!.setText(user?.direccion)

            // set email
            emailSelect = findViewById<View>(R.id.email_input) as EditText
            emailSelect!!.setText(user?.email)

            // set username
            usernameSelect = findViewById<View>(R.id.username_input) as EditText
            usernameSelect!!.setText(user?.username)

            // set password
            passwordSelect = findViewById<View>(R.id.password_input) as EditText
            passwordSelect!!.setText(user?.password)

            // set role
            val roleNames = ArrayList<String>()
            roleNames.add("Administrador")
            roleNames.add("Tecnico")
            val roleIds = ArrayList<Int>()
            roleIds.add(0)
            roleIds.add(1)
            val roleSpinner = findViewById<View>(R.id.role_spinner) as Spinner
            roleSpinner.adapter =
                ArrayAdapter(this@EditUser, android.R.layout.simple_spinner_item, roleNames)
            roleSpinner.setSelection(user?.roles!!)
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
            roleSelect = user.roles
            if(laJustaService.getUser(idAct!!.toInt()).body()!!.roles != 0)
                roleSpinner.isEnabled = false
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveChanges(view: View){
        if(nameSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar nombre", Toast.LENGTH_LONG).show()
        } else if(surnameSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar apellido", Toast.LENGTH_LONG).show()
        } else if(addressSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar direccion", Toast.LENGTH_LONG).show()
        } else if(emailSelect?.text?.toString().equals("")) {
            Toast.makeText(applicationContext, "Falta especificar email", Toast.LENGTH_LONG).show()
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
                users.removeIf { user -> (user.id_user == idObj?.toInt())}
                for (user:User in users)
                    if (user.username?.equals(usernameSelect)!!)
                        singleUsername = false
                if (!singleUsername) {
                    Toast.makeText(applicationContext, "El nombre de usuario escogido ya existe", Toast.LENGTH_LONG).show()
                } else {
                    val user =
                        User(
                            id_user = idObj!!.toInt(),
                            nombre = nameSelect?.text.toString(),
                            apellido = surnameSelect?.text.toString(),
                            direccion = addressSelect?.text.toString(),
                            email = emailSelect?.text.toString(),
                            username = usernameSelect?.text.toString(),
                            password = passwordSelect?.text.toString(),
                            roles = roleSelect
                        )
                    laJustaService.editUser(user)
                    finish()
                }
            }
        }
    }

    fun deleteUser(view: View){
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {
            //Toast.makeText(applicationContext, "ID = "+idObj!!.toInt(), Toast.LENGTH_LONG).show()
            laJustaService.deleteUser(idObj!!.toInt())
            finish()
        }
    }
}