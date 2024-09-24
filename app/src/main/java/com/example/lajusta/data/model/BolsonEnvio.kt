package com.example.lajusta.data.model

data class BolsonEnvio(
    val id_bolson: Int?,
    val cantidad: Int?,
    val idFp: Int?,
    val idRonda: Int?,
    val verduras: List<VerduraEnvio>?
)
