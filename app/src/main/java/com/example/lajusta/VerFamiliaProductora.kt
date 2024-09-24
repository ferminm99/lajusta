package com.example.lajusta

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.FamiliaProductora
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class VerFamiliaProductora : AppCompatActivity() {
    private var nombre: EditText? = null
    private var fecha: EditText? = null
    private var cal = Calendar.getInstance()
    private var button_date: Button? = null
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_familia_productora)
        nombre = findViewById<View>(R.id.nombre) as EditText

        val myIntent = intent
        id = myIntent.getStringExtra("id")

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())

        fecha = findViewById<View>(R.id.fecha) as EditText
        button_date = findViewById<View>(R.id.button_date_1) as Button
        //fecha!!.setEnabled(false)
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@VerFamiliaProductora,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })


        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            val familiaProductora = laJustaService.getFamiliaProductora(id!!.toInt()).body()

            nombre!!.setText(familiaProductora!!.nombre)
            fecha!!.setText(familiaProductora!!.fecha_afiliacion.toString())
            println(fecha!!.text.toString())
        }
    }

    fun guardar(view: View?) {
        registrarFamiliaProductora()
    }

    fun borrar(view: View?) {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch() {
            laJustaService.deleteFamiliaProductora(id!!.toInt())
            finish()
        }
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fecha!!.setText(sdf.format(cal.getTime()))
    }


    private fun registrarFamiliaProductora() {

        if(nombre!!.text.toString().isEmpty() || fecha!!.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Debe ingresar nombre y fecha", Toast.LENGTH_SHORT).show()
        }else{
            val sdf = SimpleDateFormat("yyyy-M-dd")
            val currentDate = sdf.format(Date())
            val firstDate: Date = sdf.parse(currentDate.toString())
            val fechaAUsar = fecha!!.text.toString()
                .replace("[","")
                .replace("]","")
                .replace(",","-")
                .replace(" ","")
            println(fechaAUsar)
            val secondDate: Date = sdf.parse(fechaAUsar)

            if(secondDate.after(firstDate)){
                Toast.makeText(applicationContext, "No puede ingresar fechas futuras", Toast.LENGTH_SHORT).show()
            }else{
                val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(LaJustaService::class.java)
                lifecycleScope.launch() {
                    val fechasStrings: List<String> = fechaAUsar.split("-")
                    val fechas = fechasStrings.map { it.toInt() }
                    var nuevaFamiliaProductora = FamiliaProductora(id_fp = id!!.toInt(), nombre = nombre!!.text.toString(), fecha_afiliacion = fechas)
                    laJustaService.editFamiliaProductora(nuevaFamiliaProductora)
                    //val idResultante = db.insert("verduras", null, values)
                    //Toast.makeText(applicationContext, "Id Registro: $idResultante", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }




    }
}