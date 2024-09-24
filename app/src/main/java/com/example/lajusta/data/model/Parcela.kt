package com.example.lajusta.data.model

data class Parcela(
    val id_parcela: Int?,
    val cantidad_surcos: Int?,
    val cubierta: Boolean?,
    val cosecha: Boolean?,
    val verdura: Verdura?
)
