package com.example.lajusta

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Parcela
import com.example.lajusta.data.model.Ronda
import com.example.lajusta.data.model.VisitaAdd
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


//import org.osmdroid.views.MapController;
//import org.osmdroid.views.MapView;
class CrearVisita : AppCompatActivity() {
    /*
    private var CampoNombre: EditText? = null

    private var CampoDireccion: EditText? = null
    private var CampoSuperficie: EditText? = null
    private var CampoLatitud: EditText? = null
    private var CampoLongitud: EditText? = null
    private var datos: HashMap<*, *>? = null
    private var datosQuinta: HashMap<*, *>? = null
 */
    private var nombreQuinta: Spinner? = null
    private var fecha: EditText? = null
    private var cal = Calendar.getInstance()
    private var button_date: Button? = null
    private var descripcion: EditText? = null
    private var userSeleccionado: Spinner? = null
    private var quintaIdSeleccionado: Int? = null
    private var usuarioIdSeleccionado: Int? = null

    //private MapView mMapView;
    //private MapController mMapController;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_visita)

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())

        val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
        val jsonStringCargadas = sharedPref.getString("key", null)
        val ok = sharedPref.getBoolean("ok", false)
        val gson = Gson()

        println("Esta afuera")
        if(!ok){
            println("Esta en false???")
            sharedPref.edit().remove("key").apply()
        }else{
            println("Esta en OK")
            val parcelas: Array<Parcela> = gson.fromJson(jsonStringCargadas, Array<Parcela>::class.java)
            setTabla(parcelas.toCollection(ArrayList()))
        }

        fecha = findViewById<View>(R.id.fecha) as EditText
        button_date = findViewById<View>(R.id.button_date_1) as Button
        fecha!!.setText(currentDate)
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
                DatePickerDialog(this@CrearVisita,
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

            val visitas = laJustaService.getVisitas()
            nombreQuinta = findViewById<View>(R.id.nombreQuinta) as Spinner
            //direccion = findViewById<View>(R.id.direccion) as EditText
            descripcion = findViewById<View>(R.id.descripcion) as EditText
            userSeleccionado = findViewById<View>(R.id.tecnico) as Spinner


            if(visitas.isSuccessful){

                val quintas = laJustaService.getQuintas().body()
                val usuarios = laJustaService.getUsers().body()


                val quintaName = ArrayList<String>(quintas!!.size)
                val quintaId = ArrayList<Int>(quintas!!.size)

                for (fp in quintas) {
                    quintaId.add(fp.id_quinta!!)
                    quintaName.add(fp.nombre!!)
                }

                val usuarioName = ArrayList<String>(quintas!!.size)
                val usuarioId = ArrayList<Int>(quintas!!.size)

                for (u in usuarios!!) {
                    usuarioId.add(u.id_user!!)
                    usuarioName.add(u.nombre!!)
                }

                nombreQuinta = findViewById<View>(R.id.nombreQuinta) as Spinner
                val spinnerAdapter = ArrayAdapter(this@CrearVisita, android.R.layout.simple_spinner_item,quintaName)
                nombreQuinta!!.adapter = spinnerAdapter
                nombreQuinta!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        //actualizarTabla(position);
                        quintaIdSeleccionado = quintaId[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                userSeleccionado = findViewById<View>(R.id.tecnico) as Spinner
                val spinnerAdapter2 = ArrayAdapter(this@CrearVisita, android.R.layout.simple_spinner_item,usuarioName)
                userSeleccionado!!.adapter = spinnerAdapter2
                userSeleccionado!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        //actualizarTabla(position);
                        usuarioIdSeleccionado = usuarioId[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }



            }
        }
    }

    override fun onRestart() {
        val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
        val jsonStringCargadas = sharedPref.getString("key", null)
        val ok = sharedPref.getBoolean("ok", false)
        val gson = Gson()

        if(ok){
            val parcelas: Array<Parcela> = gson.fromJson(jsonStringCargadas, Array<Parcela>::class.java)
            setTabla(parcelas.toCollection(ArrayList()))
        }

        super.onRestart()
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fecha!!.setText(sdf.format(cal.getTime()))
    }

    fun registrarVisita(view: View) {

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())
        val firstDate: Date = sdf.parse(currentDate.toString())
        val secondDate: Date = sdf.parse(fecha!!.text.toString())

        val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
        val jsonStringCargadas = sharedPref.getString("key", null)
        val gson = Gson()

        if(firstDate.after(secondDate)){
            Toast.makeText(applicationContext, "La fecha que ingresaste es menor a la de hoy, ingrese una mayor", Toast.LENGTH_LONG).show()
        }else if(jsonStringCargadas!=null){
            println(jsonStringCargadas)
            //hay que agregar un ok al cache que compruebe si sali de esta pantalla posta o ya guarde sino no se borra
            val parcelasParaCargar: Array<Parcela> = gson.fromJson(jsonStringCargadas, Array<Parcela>::class.java)

            val iniStrings: List<String> = fecha!!.text.toString().split("-")
            val fechaFinal = iniStrings.map { it.toInt() }

            if(descripcion!!.text.toString().equals("")){
                descripcion!!.setText("")
            }
            var visita1 = VisitaAdd(fecha_visita = fechaFinal, descripcion = descripcion!!.text.toString(), id_quinta = quintaIdSeleccionado, id_tecnico = usuarioIdSeleccionado, parcelas = parcelasParaCargar.toList())
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                laJustaService.addVisita(visita1)
                sharedPref.edit().remove("key").apply()
                sharedPref.edit().putBoolean("ok",false).apply()
                finish()
            }
            //val intent = Intent(this, Visitas::class.java)
            //startActivity(intent)
        }else{
            Toast.makeText(applicationContext, "Agregue alguna parcela", Toast.LENGTH_LONG).show()
        }
    }
    fun agregarParcela(view: View?) {
        val intent = Intent(this, CrearParcela::class.java)
        startActivity(intent)
    }

    private fun goToVerParcela(p: Parcela?) {
        //hacer boton pa borrar
        val builder = AlertDialog.Builder(this@CrearVisita)
        builder.setMessage("Estas seguro queres borrar esta parcela?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->

                val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
                val gson = Gson()
                val jsonStringCargadas = sharedPref.getString("key", null)
                var parcelas: Array<Parcela> = gson.fromJson(jsonStringCargadas, Array<Parcela>::class.java)

                val editor = sharedPref.edit()

                parcelas = parcelas.filter { it != p }.toTypedArray()

                if(parcelas==null){
                    cleanTable()
                    editor.putBoolean("ok", false)
                    sharedPref.edit().remove("key").apply()
                }else{
                    val jsonString = gson.toJson(parcelas)
                    editor.putString("key", jsonString)
                    editor.apply()

                    setTabla(parcelas!!.toCollection(ArrayList()))
                }


            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }


    private fun setTabla(arreglo: ArrayList<Parcela>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout

        for(data in arreglo!!){
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_parcela, null, false)
            val surcos = tableRow.findViewById<View>(R.id.name) as TextView
            val cosecha = tableRow.findViewById<View>(R.id.title) as TextView
            val cubierta = tableRow.findViewById<View>(R.id.fechaF) as TextView
            surcos.text = data.cantidad_surcos.toString()
            cosecha.text = if (data.cosecha!!) "✔" else "X"
            cubierta.text = if (data.cubierta!!) "✔" else "X"
            tableLayout.addView(tableRow)
            val id = data.id_parcela
            tableRow.setOnClickListener {
                goToVerParcela(data)
            }
        }
    }

    private fun cleanTable() {
        val table = findViewById<View>(R.id.tableLayout) as TableLayout
        val childCount = table.childCount
        if (childCount > 1) {
            table.removeViews(1, childCount - 1)
        }
    }

}