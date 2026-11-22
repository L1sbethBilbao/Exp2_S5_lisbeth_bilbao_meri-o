package com.example.pizzeriaapp.modelo

/**
 * Clase base con atributos comunes (nombre y contacto).
 * open permite herencia hacia Cliente y Repartidor.
 */
open class Usuario(
    open val nombre: String,
    open val contacto: String
) {
    open fun describir(): String = "Usuario: $nombre | Contacto: $contacto"
}

/**
 * Cliente de la pizzería.
 * Igualdad por nombre + correo. Desestructuración: nombre, correo, teléfono.
 */
class Cliente(
    override val nombre: String,
    override val contacto: String,
    val telefono: String,
    val correo: String,
    val direccion: String
) : Usuario(nombre, contacto) {

    override fun describir(): String =
        "Cliente: $nombre | Tel: $telefono | Correo: $correo | Dir: $direccion"

    operator fun component1(): String = nombre
    operator fun component2(): String = correo
    operator fun component3(): String = telefono

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cliente) return false
        return nombre.equals(other.nombre, ignoreCase = true) &&
            correo.equals(other.correo, ignoreCase = true)
    }

    override fun hashCode(): Int =
        31 * nombre.lowercase().hashCode() + correo.lowercase().hashCode()
}

/**
 * Repartidor con distancia (km) y disponibilidad (deuda técnica S2).
 */
class Repartidor(
    override val nombre: String,
    override val contacto: String,
    val distanciaKm: Double,
    var disponible: Boolean = true
) : Usuario(nombre, contacto) {

    fun entregarPedido(idPedido: Int): String =
        "Repartidor $nombre inicia entrega del pedido #$idPedido"

    fun liberar() {
        disponible = true
    }

    fun ocupar() {
        disponible = false
    }

    override fun describir(): String =
        "Repartidor: $nombre | Contacto: $contacto | Distancia: ${distanciaKm} km | Disponible: $disponible"
}
