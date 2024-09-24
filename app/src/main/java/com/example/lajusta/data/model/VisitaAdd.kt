package com.example.lajusta.data.model

data class VisitaAdd(
val fecha_visita: List<Int>?,
val descripcion: String?,
val id_tecnico: Int?,
val id_quinta: Int?,
val parcelas: List<Parcela>
)

