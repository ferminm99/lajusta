package com.example.lajusta

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lajusta.data.model.Verdura
import com.example.lajusta.data.remote.ApiUtils
import com.example.lajusta.data.remote.LaJustaService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class CrearVerdura : AppCompatActivity() {
    private var CampoDescri: EditText? = null
    private var nombreVerdura: EditText? = null
    private var fechaCosecha: EditText? = null
    private var fechaSiembra: EditText? = null
    private var cal = Calendar.getInstance()
    private var imagen: Button? = null
    private var button_date: Button? = null
    private var button_date2: Button? = null
    private val GALLERY_REQUEST_CODE = 100
    private var imagenACargar: String? = null
    private var verImagen: ImageView? = null
    private val apiKey = "AIzaSyBVzAZzdbiIJpQndNLIhfC1krJfIsY7SbE"
    private val searchEngineId = "75f2e6f74f2b64e39"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_verdura)
        CampoDescri = findViewById<View>(R.id.descripcion) as EditText
        nombreVerdura = findViewById<View>(R.id.nombreVerdura) as EditText

        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())

        imagen = findViewById<View>(R.id.imagen) as Button
        verImagen = findViewById<View>(R.id.verImagen) as ImageView


        fechaCosecha = findViewById<View>(R.id.fechaInicio) as EditText
        button_date = findViewById<View>(R.id.button_date_1) as Button
        fechaCosecha!!.setText(currentDate)
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
                DatePickerDialog(this@CrearVerdura,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })

        fechaSiembra = findViewById<View>(R.id.fechaFin) as EditText
        button_date2 = findViewById<View>(R.id.button_date_2) as Button
        fechaSiembra!!.setText(currentDate)
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
                DatePickerDialog(this@CrearVerdura,
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

    //cosas para uplodear imagenadmi

    fun cargarImagen(view: View?){
        val options = arrayOf<CharSequence>("Buscar en google", "Elegir desde Galeria", "Cancelar")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Option")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Buscar en google" -> {

                    val query = "Zanahoria"

                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://www.googleapis.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val service = retrofit.create(GoogleCustomSearchService::class.java)

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED) {
                        Thread {
                            try {
                                service.search(apiKey, searchEngineId, query).execute()
                                val response = service.search(apiKey, searchEngineId, query).execute()

                                if (response.isSuccessful) {
                                    val items = response.body()?.items
                                    if (items != null && items.isNotEmpty()) {
                                        val imageUrl = items[0].link
                                        println(imageUrl)
                                        // get the URL of the first image result
                                        imagenACargar = imageUrl
                                        verImagen!!.setImageBitmap(getBitmapFromURL(imageUrl))

                                    } else {
                                        // no image results found
                                    }
                                } else {
                                    // error occurred
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }.start()

                    }





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

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            val myBitmap = BitmapFactory.decodeStream(input)
            myBitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
// Call the AsyncTask


    /*
    private fun searchImages(query: String) {
        val url = "https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$searchEngineId&q=$query&searchType=image&num=1"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                val items = response.getJSONArray("items")
                if (items.length() > 0) {
                    val item = items.getJSONObject(0)
                    val imageUrl = item.getString("link")
                    // save the imageUrl to your database
                } else {
                    // handle case where no images are returned
                }
            },
            Response.ErrorListener { error ->
                // handle error
            })
        requestQueue.add(request)
    }


     */

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

    private fun registrarVerdura() {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val firstDate: Date = sdf.parse(fechaSiembra!!.text.toString())
        val secondDate: Date = sdf.parse(fechaCosecha!!.text.toString())
        if (nombreVerdura!!.text.toString().equals("")) {
            Toast.makeText(
                applicationContext,
                "Nombre no puede ser vacio",
                Toast.LENGTH_SHORT
            ).show()
        } else if (firstDate.after(secondDate)) {
            Toast.makeText(
                applicationContext,
                "Cosecha debe ser posterior a siembra",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val laJustaService = Retrofit.Builder().baseUrl(ApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LaJustaService::class.java)
            lifecycleScope.launch() {
                val cosechasStrings: List<String> = fechaCosecha!!.text.toString().split("-")
                val siembrasStrings: List<String> = fechaSiembra!!.text.toString().split("-")
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

                var nuevaVerdura = Verdura(id_verdura = null, nombre = nombreVerdura!!.text.toString(),archImg=imagenACargar, tiempo_cosecha = cosechas, mes_siembra = siembras, descripcion = CampoDescri!!.text.toString())
                println(nuevaVerdura)
                laJustaService.addVerdura(nuevaVerdura)
                finish()
            }
        }






    }
}