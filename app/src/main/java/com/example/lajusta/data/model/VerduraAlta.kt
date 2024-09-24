package com.example.lajusta.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import java.util.Date

data class VerduraAlta(
    val tiempo_cosecha: String?,
    val mes_siembra: String?,
    val archImg: String?,
    val nombre: String?,
    val descripcion: String?
)