package com.example.lajusta.util.model

import com.example.lajusta.data.model.Bolson
import com.example.lajusta.data.model.FamiliaProductora
import com.example.lajusta.data.model.Quinta
import com.example.lajusta.data.model.Ronda

data class Bolson_FP_Quinta(
    val familiaProductora: FamiliaProductora,
    val ronda: Ronda,
    val bolson: Bolson,
)
