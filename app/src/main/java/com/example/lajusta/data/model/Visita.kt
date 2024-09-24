package com.example.lajusta.data.model

data class Visita(
    val id_visita: Int?,
    val fecha_visita: List<String>?,
    val descripcion: String?,
    val id_tecnico: Int?,
    val id_quinta: Int?,
    val parcelas: List<Parcela>
)
