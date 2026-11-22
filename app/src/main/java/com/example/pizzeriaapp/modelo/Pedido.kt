package com.example.pizzeriaapp.modelo

import com.example.pizzeriaapp.util.Calculos
import com.example.pizzeriaapp.util.ResultadoTotales
import java.time.LocalDate

/**
 * Pedido con cliente, productos, cantidad, estado, notas y repartidor opcional.
 * Soporta operador + (combinar) y desestructuración (cliente, productos, total).
 */
class Pedido(
    val id: Int,
    val cliente: Cliente,
    val productos: List<Producto>,
    var cantidad: Int,
    var estado: String = "En preparación",
    val notas: String? = null,
    var repartidor: Repartidor? = null,
    val fecha: LocalDate = LocalDate.now()
) {
    fun calcularTotales(): ResultadoTotales =
        Calculos.calcularTotales(productos, cantidad, fecha)

    fun cambiarEstado(nuevoEstado: String) {
        estado = nuevoEstado
    }

    fun etiquetaRepartidor(): String = repartidor?.nombre ?: "Sin asignar"

    fun notasMostrar(): String = notas?.takeIf { it.isNotBlank() } ?: "Sin notas"

    /** Desestructuración: cliente, productos, total. */
    operator fun component1(): Cliente = cliente
    operator fun component2(): List<Producto> = productos
    operator fun component3(): Double = calcularTotales().total

    /**
     * Combina dos pedidos: une productos, suma cantidades.
     * Los totales NO se suman a ciegas: se recalculan sobre el pedido resultante
     * (así el 10% por >3 unidades y demases aplican sobre la boleta unificada).
     */
    operator fun plus(otro: Pedido): Pedido {
        val productosCombinados = productos + otro.productos
        // Si las listas vienen expandidas (1 ítem = 1 unidad), size es la cantidad real
        val cantidadCombinada = if (productosCombinados.size >= cantidad + otro.cantidad) {
            productosCombinados.size
        } else {
            cantidad + otro.cantidad
        }
        // Fecha de la boleta: la más reciente (define si aplica promo de fechas)
        val fechaBoleta = if (otro.fecha.isAfter(fecha)) otro.fecha else fecha
        return Pedido(
            id = id,
            cliente = cliente,
            productos = productosCombinados,
            cantidad = cantidadCombinada,
            estado = "En preparación",
            notas = listOfNotNull(notas, otro.notas)
                .filter { it.isNotBlank() }
                .joinToString(" | ")
                .ifBlank { null },
            repartidor = repartidor ?: otro.repartidor,
            fecha = fechaBoleta
        )
    }

    /** Agrupa productos iguales para calcular la boleta con descuentos correctos. */
    fun lineasAgrupadas(): List<Pair<Producto, Int>> {
        if (productos.isEmpty()) return emptyList()
        return productos
            .groupingBy { it.nombre to it.categoria }
            .eachCount()
            .map { (clave, cant) ->
                val prod = productos.first {
                    it.nombre == clave.first && it.categoria == clave.second
                }
                prod to cant
            }
    }

    companion object {
        private val ORDEN_ESTADOS = listOf("En preparación", "Listo", "En camino", "Entregado")

        /**
         * Solo permite avanzar en la cadena (deuda técnica S2).
         * Cancelado se permite desde cualquier estado no final.
         */
        fun puedeTransicionar(desde: String, hacia: String): Boolean {
            if (hacia == "Cancelado") {
                return desde != "Entregado" && desde != "Cancelado"
            }
            val iDesde = ORDEN_ESTADOS.indexOf(desde)
            val iHacia = ORDEN_ESTADOS.indexOf(hacia)
            if (iDesde < 0 || iHacia < 0) return false
            return iHacia >= iDesde
        }
    }
}
