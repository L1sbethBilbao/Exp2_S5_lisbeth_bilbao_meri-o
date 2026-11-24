package com.example.pizzeriaapp.modelo

import java.time.LocalDate
import java.time.LocalDateTime

/** Monto del día confirmado y cerrado por el trabajador. */
data class CierreRecaudacion(
    val fecha: LocalDate,
    val monto: Int,
    val confirmadoEn: LocalDateTime = LocalDateTime.now()
)
