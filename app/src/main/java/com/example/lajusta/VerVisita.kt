package com.example.lajusta

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.*
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import java.text.SimpleDateFormat
import java.util.*

class VerVisita : AppCompatActivity() {
    var id: String? = null

    private var fecha: EditText? = null
    private var fechaActual: List<String>? = null
    private var cal = Calendar.getInstance()
    private var button_date: Button? = null
    private var descripcion: EditText? = null
    private var userSeleccionado: Spinner? = null
    private var quintaSeleccionada: Spinner? = null
    private var userIdSeleccionado: Int? = null
    private var quintaIdSeleccionado: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_visita)
        // SQLiteDatabase db = MainActivity.conn.getReadableDatabase();
        val myIntent = intent
        println("______________________________")
        println("ANTES ID="+id)
        println("_____________________________")
        if (id==null)
            id = myIntent.getStringExtra("id")
        println("______________________________")
        println("DESPUES ID="+id)
        println("_____________________________")

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())

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
                DatePickerDialog(this@VerVisita,
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

            val visita = laJustaService.getVisita(id!!.toInt())

            val parcelas: List<Parcela> = visita.body()!!.parcelas
            setTabla(parcelas.toCollection(ArrayList()))

            //Cargo parcelas en cache para editarlas
            val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
            val gson = Gson()
            val jsonString = gson.toJson(parcelas.toTypedArray())
            println(parcelas)
            val editor = sharedPref.edit()
            editor.putString("key", jsonString)
            editor.putBoolean("ok", true)
            editor.apply()



            fechaActual = visita.body()?.fecha_visita
            quintaIdSeleccionado = visita.body()?.id_quinta

            if(visita.isSuccessful){
                val quintaResult = laJustaService.getQuinta(visita.body()?.id_quinta!!)
                val tecnicoResult = laJustaService.getUser(visita.body()?.id_tecnico!!)
                val quinta = quintaResult.body()
                val tecnico = tecnicoResult.body()
                //para spinners
                val quintas = laJustaService.getQuintas().body()
                val usuarios = laJustaService.getUsers().body()

                if(quintaResult.isSuccessful and tecnicoResult.isSuccessful){

                    descripcion = findViewById<View>(R.id.descripcion) as EditText
                    descripcion!!.setText(visita.body()!!.descripcion.toString())

                    val quintaName = ArrayList<String>(quintas!!.size)
                    val quintaId = ArrayList<Int>(quintas.size)

                    for (q in quintas) {
                        quintaId.add(q.id_quinta!!)
                        quintaName.add(q.nombre!!)
                    }



                    val userName = ArrayList<String>(usuarios!!.size)
                    val userId = ArrayList<Int>(usuarios.size)

                    for (u in usuarios) {
                        userId.add(u.id_user!!)
                        userName.add(u.nombre!!)
                    }

                    userSeleccionado = findViewById<View>(R.id.tecnico) as Spinner
                    val spinnerAdapter = ArrayAdapter(this@VerVisita, android.R.layout.simple_spinner_item,userName)
                    userSeleccionado!!.adapter = spinnerAdapter
                    userSeleccionado!!.setSelection(spinnerAdapter.getPosition(tecnico!!.nombre))
                    userSeleccionado!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            //actualizarTabla(position);
                            userIdSeleccionado = userId[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    quintaSeleccionada = findViewById<View>(R.id.nombreQuinta) as Spinner
                    val spinnerAdapter2 = ArrayAdapter(this@VerVisita, android.R.layout.simple_spinner_item,quintaName)
                    quintaSeleccionada!!.adapter = spinnerAdapter2
                    quintaSeleccionada!!.setSelection(spinnerAdapter2.getPosition(quinta!!.nombre))

                    quintaSeleccionada!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

                    println("__________________________________")

                }
            }

            println(visita)


        }



    }

    private fun updateDateInView() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fecha!!.setText(sdf.format(cal.getTime()))
    }

    private fun setTabla(arreglo: ArrayList<Parcela>) {
        cleanTable()
        val tableLayout = findViewById<View>(R.id.tableLayout) as TableLayout

        for(data in arreglo!!){
            val tableRow =
                LayoutInflater.from(this).inflate(R.layout.table_item_parcela_edit, null, false)
            val surcos = tableRow.findViewById<View>(R.id.name) as TextView
            val cosecha = tableRow.findViewById<View>(R.id.title) as TextView
            val cubierta = tableRow.findViewById<View>(R.id.fechaF) as TextView
            val borrar = tableRow.findViewById<View>(R.id.eliminar) as ImageButton
            surcos.text = data.cantidad_surcos.toString()
            cosecha.text = if (data.cosecha!!) "✔" else "X"
            cubierta.text = if (data.cubierta!!) "✔" else "X"
            tableLayout.addView(tableRow)
            val id_parcela = data.id_parcela
            borrar.setOnClickListener {
                goToDeleteParcela(data)
            }
            tableRow.setOnClickListener {
                goToVerParcela(id_parcela.toString())
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

    private fun goToDeleteParcela(p: Parcela?) {
        //hacer boton pa borrar
        val builder = AlertDialog.Builder(this@VerVisita)
        builder.setMessage("Estas seguro queres borrar esta parcela?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->

                val sharedPref = getSharedPreferences("pref_name", Context.MODE_PRIVATE)
                val gson = Gson()
                val jsonStringCargadas = sharedPref.getString("key", null)
                var parcelas: Array<Parcela> = gson.fromJson(jsonStringCargadas, Array<Parcela>::class.java)
                //var parcelasCargadas: Array<Parcela>? = parcelas
                val editor = sharedPref.edit()

                println("PARCELAS CARGADAS EN CACHE = ")
                println(parcelas)

                parcelas = parcelas.filter { it != p }.toTypedArray()

                if(parcelas==null){
                    cleanTable()
                    editor.putBoolean("ok", false)
                    sharedPref.edit().remove("key").apply()
                }else{
                    val jsonString = gson.toJson(parcelas)
                    editor.putString("key", jsonString)
                    editor.apply()
                    println("entro")
                    println(parcelas!!.toCollection(ArrayList()))
                    for(data in parcelas!!.toCollection(ArrayList())!!){
                        println(data)
                    }
                    setTabla(parcelas!!.toCollection(ArrayList()))

                    val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build().create(LaJustaService::class.java)
                    lifecycleScope.launch() {
                        try {
                            laJustaService.deleteParcela(p!!.id_parcela!!)
                        } catch (_: KotlinNullPointerException) {
                        }
                    }
                }


            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }


    private fun goToVerParcela(id_parcela: String?) {
        val intent = Intent(this, VerParcela::class.java)
        intent.putExtra("id", id_parcela)
        intent.putExtra("id_visita", id.toString())
        startActivity(intent)
    }

    private fun cleanTable() {
        val table = findViewById<View>(R.id.tableLayout) as TableLayout
        val childCount = table.childCount
        if (childCount > 1) {
            table.removeViews(1, childCount - 1)
        }
    }

    fun agregarParcela(view: View?) {
        val intent = Intent(this, CrearParcela::class.java)
        startActivity(intent)
    }

    fun actualizarVisita(view: View?) {
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

            println("PARCELAS EDITADAS= ")
            println(jsonStringCargadas)
            //hay que agregar un ok al cache que compruebe si sali de esta pantalla posta o ya guarde sino no se borra
            val parcelasParaCargar: Array<Parcela> = gson.fromJson(jsonStringCargadas, Array<Parcela>::class.java)

            //var parcelasACargar: Array<Parcela>? = null
            var parcelasFinales: Array<Parcela>? = null
            parcelasFinales = parcelasFinales ?: arrayOf()
            val iniStrings: List<String> = fecha!!.text.toString().split("-")
            val fechaFinal = iniStrings.map { it.toInt() }

            if(descripcion!!.text.toString().equals("")){
                descripcion!!.setText("")
            }

            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {

                var parcela: ParcelaNormal? = null
                var parcelaParaID: ParcelaNormal? = null
                //val parcelasExistentes = laJustaService.getParcelas().body()

                if(parcelasParaCargar.isNotEmpty()){
                    for (p in parcelasParaCargar!!){
                        if(p.id_parcela==null){
                            println("ENTRO AL NULL")
                            parcela = ParcelaNormal(id_parcela = p.id_parcela,
                                cantidad_surcos = p.cantidad_surcos, cubierta = p.cubierta,
                                cosecha = p.cosecha, id_verdura = p.verdura!!.id_verdura,
                                id_visita=id!!.toInt())
                            parcelaParaID = laJustaService.addParcela(parcela).body()!!
                            //Edit post creacion de la parcela para que los atributos esten bien
                            laJustaService.editParcela(parcelaParaID)

                            println("ID DE PARCELA NUEVA= ")
                            println(parcelaParaID)
                            parcelasFinales = parcelasFinales!!
                                .plus(Parcela(id_parcela= parcelaParaID.id_parcela,
                                    cantidad_surcos = parcelaParaID.cantidad_surcos,
                                    cubierta = parcelaParaID.cubierta,
                                    cosecha = parcelaParaID.cosecha, verdura = p.verdura))
                        }else{
                            println("ENTRO AL NORMAL")
                            parcelasFinales = parcelasFinales!!.plus(Parcela(id_parcela= p.id_parcela,cantidad_surcos = p.cantidad_surcos, cubierta = p.cubierta, cosecha = p.cosecha,verdura = p.verdura))
                        }
                    }
                    val visita1 = VisitaEdit(id_visita = id!!.toInt(),fecha_visita = fechaFinal, descripcion = descripcion!!.text.toString(), id_quinta = quintaIdSeleccionado, id_tecnico = userIdSeleccionado, parcelas = parcelasFinales!!.toList())
                    laJustaService.editVisita(visita1)
                    println(visita1)
                }else{
                    val visita1 = VisitaEdit(id_visita = id!!.toInt(),fecha_visita = fechaFinal, descripcion = descripcion!!.text.toString(), id_quinta = quintaIdSeleccionado, id_tecnico = userIdSeleccionado, parcelas = emptyList())
                    laJustaService.editVisita(visita1)
                    println(visita1)
                }

                sharedPref.edit().remove("key").apply()
                sharedPref.edit().putBoolean("ok",false).apply()
                //val intent = Intent(this, Visitas::class.java)
                //startActivity(intent)
                finish()

            }

        }else{
            Toast.makeText(applicationContext, "Agregue alguna parcela", Toast.LENGTH_LONG).show()
        }

    }

    fun deleteVisita(view: View?) {
        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch() {
            laJustaService.deleteVisita(id!!.toInt())
            val intent = Intent(this@VerVisita, Visitas::class.java)
            startActivity(intent)
            //hay que chequear si se crea con un response pero no nos andaban...
            println("termino")
        }
    }
}