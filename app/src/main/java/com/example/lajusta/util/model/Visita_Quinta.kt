package com.example.lajusta.util.model

import com.example.lajusta.data.model.Parcela
import com.example.lajusta.data.model.Quinta
import com.example.lajusta.data.model.Visita

data class Visita_Quinta(
    val visita: Visita,
    val quinta: Quinta
)