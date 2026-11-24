package com.example.pizzeriaapp.modelo

import com.example.pizzeriaapp.anotaciones.Promocionable

/**
 * Producto del catálogo (pizza, bebida o ingrediente).
 * Igualdad por nombre + categoría (requerimiento Semana 3).
 */
open class Producto(
    val nombre: String,
    val precio: Double,
    val categoria: String,
    val ingredientes: List<String> = emptyList()
) {
    fun detallarProducto(): String {
        val lista = if (ingredientes.isEmpty()) {
            "(sin ingredientes)"
        } else {
            ingredientes.joinToString(", ")
        }
        return "$nombre [$categoria]  —  $${precio.toInt()}\n     Ingredientes: $lista"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Producto) return false
        return nombre.equals(other.nombre, ignoreCase = true) &&
            categoria.equals(other.categoria, ignoreCase = true)
    }

    override fun hashCode(): Int =
        31 * nombre.lowercase().hashCode() + categoria.lowercase().hashCode()

    override fun toString(): String = "$nombre ($categoria) $$precio"
}

/** Pizza Pepperoni con promoción Cyber 20%. */
@Promocionable(descuento = 20, etiqueta = "Cyber")
class PizzaPepperoni : Producto(
    nombre = "Pepperoni",
    precio = 8990.0,
    categoria = "pizza",
    ingredientes = listOf("masa", "queso", "pepperoni")
)

/** Pizza Margarita con promoción Oferta 10% (valores por defecto de la anotación). */
@Promocionable
class PizzaMargarita : Producto(
    nombre = "Margarita",
    precio = 7990.0,
    categoria = "pizza",
    ingredientes = listOf("masa", "queso", "tomate", "albahaca")
)

/** Pizza Hawaiana sin anotación @Promocionable. */
class PizzaHawaiana : Producto(
    nombre = "Hawaiana",
    precio = 8490.0,
    categoria = "pizza",
    ingredientes = listOf("masa", "queso", "tomate")
)

/** Bebidas (stock por clave en inventario). */
class BebidaCola : Producto(
    nombre = "Coca-Cola",
    precio = 1500.0,
    categoria = "bebida",
    ingredientes = listOf("coca-cola")
)

class BebidaSprite : Producto(
    nombre = "Sprite",
    precio = 1400.0,
    categoria = "bebida",
    ingredientes = listOf("sprite")
)

class BebidaJugo : Producto(
    nombre = "Jugo natural",
    precio = 1800.0,
    categoria = "bebida",
    ingredientes = listOf("jugo")
)
