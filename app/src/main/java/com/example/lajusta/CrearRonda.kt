package com.example.lajusta

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Ronda
import com.example.lajusta.data.model.Verdura
import com.example.lajusta.data.model.VerduraAlta
import com.example.lajusta.data.model.VerduraEnvio
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class CrearRonda : AppCompatActivity() {
    private var verdura: Spinner? = null
    private var fechaInicio: EditText? = null
    private var fechaFin: EditText? = null
    private var cal = Calendar.getInstance()
    private var button_date: Button? = null
    private var button_date2: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_ronda)

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())

        fechaInicio = findViewById<View>(R.id.fechaInicio) as EditText
        button_date = findViewById<View>(R.id.button_date_1) as Button
        fechaInicio!!.setText(currentDate)
        //fecha!!.setEnabled(false)
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInViewCosecha()
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@CrearRonda,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })

        fechaFin = findViewById<View>(R.id.fechaFin) as EditText
        button_date2 = findViewById<View>(R.id.button_date_2) as Button
        fechaFin!!.setText(currentDate)
        //fecha!!.setEnabled(false)
        val dateSetListener2 = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInViewSiembra()
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date2!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@CrearRonda,
                    dateSetListener2,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })
    }

    fun onClick(view: View?) {
        registrarRonda()
    }

    private fun updateDateInViewCosecha() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fechaInicio!!.setText(sdf.format(cal.getTime()))
    }

    private fun updateDateInViewSiembra() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fechaFin!!.setText(sdf.format(cal.getTime()))
    }

    private fun registrarRonda() {

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val firstDate: Date = sdf.parse(fechaInicio!!.text.toString())
        val secondDate: Date = sdf.parse(fechaFin!!.text.toString())
        if(firstDate.after(secondDate)){
            Toast.makeText(applicationContext, "La fecha de fin es menor a la de inicio, ingrese una mayor", Toast.LENGTH_LONG).show()
        }else{
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                val strs = fechaInicio!!.text.toString().split("-").toTypedArray()
                val iniStrings: List<String> = fechaInicio!!.text.toString().split("-")
                val finStrings: List<String> = fechaFin!!.text.toString().split("-")
                val ArrayFechaInicio = iniStrings.map { it.toInt() }
                val ArrayFechaFin = finStrings.map { it.toInt() }

                var nuevaRonda = Ronda(id_ronda = null, fecha_inicio = ArrayFechaInicio, fecha_fin = ArrayFechaFin)
                println(nuevaRonda)
                laJustaService.addRonda(nuevaRonda)
                finish()
            }

        }




    }
}