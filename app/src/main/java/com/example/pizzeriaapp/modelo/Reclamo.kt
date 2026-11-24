package com.example.pizzeriaapp.modelo

import java.time.LocalDateTime

/**
 * Reclamo de un pedido con problema — visible para el trabajador.
 */
data class Reclamo(
    val codigo: String,
    val pedidoId: Int,
    val numeroAtencion: Int,
    val clienteNombre: String,
    val clienteTelefono: String,
    val clienteCorreo: String,
    val comoRecibio: String,
    val estadoProducto: String,
    val fechaHora: LocalDateTime = LocalDateTime.now(),
    var solucionado: Boolean = false,
    var fechaHoraSolucion: LocalDateTime? = null
)
