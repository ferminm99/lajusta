package com.example.lajusta

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Verdura
import com.example.lajusta.data.model.VerduraAlta
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class VerVerdura : AppCompatActivity() {
    private var CampoDescri: EditText? = null
    private var nombreVerdura: EditText? = null
    private var fechaCosecha: EditText? = null
    private var fechaSiembra: EditText? = null
    private var cal = Calendar.getInstance()
    private var button_date: Button? = null
    private var button_date2: Button? = null
    private var id: String? = null
    private var verdura: Verdura? = null
    private val GALLERY_REQUEST_CODE = 100
    private var imagenACargar: String? = null
    private var verImagen: ImageView? = null
    private var imagen: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_verdura)
        CampoDescri = findViewById<View>(R.id.descripcion) as EditText
        nombreVerdura = findViewById<View>(R.id.nombreVerdura) as EditText

        val myIntent = intent
        id = myIntent.getStringExtra("id")

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())

        imagen = findViewById<View>(R.id.imagen) as Button
        verImagen = findViewById<View>(R.id.verImagen) as ImageView



        fechaCosecha = findViewById<View>(R.id.fechaInicio) as EditText
        button_date = findViewById<View>(R.id.button_date_1) as Button
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
                DatePickerDialog(this@VerVerdura,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })

        fechaSiembra = findViewById<View>(R.id.fechaFin) as EditText
        button_date2 = findViewById<View>(R.id.button_date_2) as Button

        val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(LaJustaService::class.java)
        lifecycleScope.launch {

            verdura = laJustaService.getVerdura(id!!.toInt()).body()

            // Agarro la imagen y la muestro
            val imageBytes: ByteArray = Base64.decode((verdura!!.archImg), Base64.DEFAULT)

            if (imageBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                verImagen!!.setImageBitmap(bitmap)
                println("Entro a TRUE")
            } else {
                // Handle the case where imageBytes is null
                println("Entro a null")
            }

            fechaSiembra!!.setText(verdura!!.mes_siembra.toString())
            CampoDescri!!.setText(verdura!!.descripcion)
            nombreVerdura!!.setText(verdura!!.nombre)
            fechaCosecha!!.setText(verdura!!.tiempo_cosecha.toString())

        }

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
                DatePickerDialog(this@VerVerdura,
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
            laJustaService.deleteVerdura(id!!.toInt())
            finish()
        }
    }

    fun cargarImagen(view: View?){
        val options = arrayOf<CharSequence>("Tomar Foto", "Elegir desde Galeria", "Cancelar")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Option")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Tomar Foto" -> {
                    // TODO: launch camera intent to take a photo
                }
                options[item] == "Elegir desde Galeria" -> {
                    //val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    //startActivityForResult(intent, GALLERY_REQUEST_CODE)
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"

// Start the activity to select a file
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE)

                }
                options[item] == "Cancelar" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            //val inputStream: InputStream = contentResolver.openInputStream(imageUri!!)!!
            //val imageBytes: ByteArray = inputStream.readBytes()
            // encode the byte array using Base64
            //achico la imagen una banda probablemente se vea re mal porque le borro hasta un pedazo de string
            //imagenACargar = encodeImage(imageUri!!)!!.substring(0, min(encodeImage(imageUri!!)!!.length, 255))
            imagenACargar = encodeImage(imageUri!!)!!

            // Agarro la imagen y la muestro
            val imageBytes: ByteArray = Base64.decode(encodeImage(imageUri!!), Base64.DEFAULT)

            if (imageBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                verImagen!!.setImageBitmap(bitmap)
                println("Entro a TRUE")
            } else {
                // Handle the case where imageBytes is null
                println("Entro a null")
            }

        }
    }

    private fun encodeImage(imageUri: Uri): String? {
        var inputStream: InputStream? = null
        var encodedImage: String? = null
        try {
            inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream)
            val byteArray = outputStream.toByteArray()
            encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

            println("LA ENCODEO?")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return encodedImage
    }


    private fun updateDateInViewCosecha() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fechaCosecha!!.setText(sdf.format(cal.getTime()))
    }

    private fun updateDateInViewSiembra() {
        val myFormat = "yyyy-M-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        fechaSiembra!!.setText(sdf.format(cal.getTime()))
    }

    private fun registrarVerdura() {
        val fechaAUsarCosecha = fechaCosecha!!.text.toString()
            .replace("[","")
            .replace("]","")
            .replace(",","-")
            .replace(" ","")
        val fechaAUsarSiembra = fechaSiembra!!.text.toString()
            .replace("[","")
            .replace("]","")
            .replace(",","-")
            .replace(" ","")
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val firstDate: Date = sdf.parse(fechaAUsarSiembra)
        val secondDate: Date = sdf.parse(fechaAUsarCosecha)
        if(firstDate.after(secondDate)){
            Toast.makeText(applicationContext, "Cosecha debe ser posterior a siembra", Toast.LENGTH_SHORT).show()
        }else{
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                val cosechasStrings: List<String> = fechaAUsarCosecha.split("-")
                val siembrasStrings: List<String> = fechaAUsarSiembra.split("-")
                val cosechas = cosechasStrings.map { it.toInt() }
                val siembras = siembrasStrings.map { it.toInt() }
                if(imagenACargar!=null){
                    if(imagenACargar!!.length>255){
                        imagenACargar = "MuyLargoNoFunciono"
                        Toast.makeText(applicationContext, "La imagen no se pudo guardar correctamente", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    imagenACargar = "Ninguna"
                }

                var nuevaVerdura = Verdura(id_verdura = id!!.toInt(), nombre = nombreVerdura!!.text.toString(),archImg=null, tiempo_cosecha = cosechas, mes_siembra = siembras, descripcion = CampoDescri!!.text.toString())
                println(nuevaVerdura)
                laJustaService.editVerdura(nuevaVerdura)
                finish()
            }
        }



    }
}