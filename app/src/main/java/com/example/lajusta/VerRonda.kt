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
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class VerRonda : AppCompatActivity() {
    private var fechaInicio: EditText? = null
    private var fechaFin: EditText? = null
    private var cal = Calendar.getInstance()
    private var button_date: Button? = null
    private var button_date2: Button? = null
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_ronda)

        val myIntent = intent
        id = myIntent.getStringExtra("id")

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            val ronda = laJustaService.getRonda(id!!.toInt()).body()

            fechaInicio!!.setText(ronda!!.fecha_inicio.toString())
            fechaFin!!.setText(ronda!!.fecha_fin.toString())
        }

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())


        fechaInicio = findViewById<View>(R.id.fechaInicio) as EditText
        button_date = findViewById<View>(R.id.button_date_1) as Button
        //fecha!!.setEnabled(false)
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInViewInicio()
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@VerRonda,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })

        fechaFin = findViewById<View>(R.id.fechaFin) as EditText
        button_date2 = findViewById<View>(R.id.button_date_2) as Button

        //fecha!!.setEnabled(false)
        val dateSetListener2 = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInViewFin()
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date2!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@VerRonda,
                    dateSetListener2,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })


    }

    fun guardar(view: View?) {
        registrarVerdura()
    }

    fun borrar(view: View?) {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch() {
            laJustaService.deleteRonda(id!!.toInt())
            finish()
        }
    }

    private fun updateDateInViewInicio() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fechaInicio!!.setText(sdf.format(cal.getTime()))
    }

    private fun updateDateInViewFin() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fechaFin!!.setText(sdf.format(cal.getTime()))
    }

    private fun registrarVerdura() {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val fechaAUsarInicio = fechaInicio!!.text.toString()
            .replace("[","")
            .replace("]","")
            .replace(",","-")
            .replace(" ","")
        val fechaAUsarFin = fechaFin!!.text.toString()
            .replace("[","")
            .replace("]","")
            .replace(",","-")
            .replace(" ","")
        val firstDate: Date = sdf.parse(fechaAUsarInicio)
        val secondDate: Date = sdf.parse(fechaAUsarFin)
        if(firstDate.after(secondDate)){
            Toast.makeText(applicationContext, "La fecha de fin es menor a la de inicio, ingrese una mayor", Toast.LENGTH_LONG).show()
        }else{
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                val iniStrings: List<String> = fechaAUsarInicio.split("-")
                val finStrings: List<String> = fechaAUsarFin.split("-")
                val ArrayFechaInicio = iniStrings.map { it.toInt() }
                val ArrayFechaFin = finStrings.map { it.toInt() }

                var editarRonda = Ronda(id_ronda = id!!.toInt(), fecha_inicio = ArrayFechaInicio, fecha_fin = ArrayFechaFin)
                println(editarRonda)
                laJustaService.editRonda(editarRonda)
                finish()
            }
        }





    }
}