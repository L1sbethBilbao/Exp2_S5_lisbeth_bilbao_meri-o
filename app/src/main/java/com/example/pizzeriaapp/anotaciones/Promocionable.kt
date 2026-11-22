package com.example.pizzeriaapp.anotaciones

/**
 * Anotación personalizada para marcar pizzas o combos con promoción especial.
 * Se lee en tiempo de ejecución con reflection (como en la clase Semana 3).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Promocionable(
    val descuento: Int = 10,
    val etiqueta: String = "Oferta"
)
