package com.example.lajusta.data.model

import java.util.HashSet

data class Bolson(
    val id_bolson: Int?,
    val cantidad: Int?,
    val idFp: Int?,
    val idRonda: Int?,
    val verduras: List<Verdura>?
)