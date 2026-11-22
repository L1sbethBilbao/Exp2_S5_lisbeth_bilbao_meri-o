package com.example.pizzeriaapp.util

import com.example.pizzeriaapp.anotaciones.Promocionable
import com.example.pizzeriaapp.modelo.Producto
import java.time.LocalDate

data class LineaDescuentoProducto(
    val nombre: String,
    val etiqueta: String,
    val porcentaje: Int,
    val baseLinea: Double,
    val montoDescuento: Double
)

data class ResultadoTotales(
    val subtotal: Double,
    val descuentoCantidad: Double,
    val descuentoPromoProducto: Double,
    val descuentoFecha: Double,
    val impuesto: Double,
    val total: Double,
    val aplicaDescuentoCantidad: Boolean,
    val aplicaPromoFecha: Boolean,
    val etiquetasPromo: List<String>,
    val detallePromoProductos: List<LineaDescuentoProducto> = emptyList(),
    val despuesPromoProductos: Double = 0.0,
    val despuesDescuentoCantidad: Double = 0.0,
    val despuesDescuentoFecha: Double = 0.0
)

object Calculos {
    const val IMPUESTO = 0.19
    const val DESCUENTO_CANTIDAD = 0.10
    const val DESCUENTO_FECHA_PROMO = 0.05

    /**
     * Cálculo claro por líneas de pedido (producto + cantidad).
     * Orden:
     * 1) Subtotal
     * 2) Descuento @Promocionable solo sobre productos con promo
     * 3) 10% si unidades totales > 3 (sobre lo que queda)
     * 4) 5% si la fecha está en periodo promo (sobre lo que queda)
     * 5) IVA 19% sobre la base final
     */
    fun calcularDesdeLineas(
        lineas: List<Pair<Producto, Int>>,
        fecha: LocalDate = LocalDate.now()
    ): ResultadoTotales {
        require(lineas.isNotEmpty()) { "El pedido no tiene productos" }
        val cantidadTotal = lineas.sumOf { it.second }
        require(cantidadTotal > 0) { "La cantidad debe ser mayor que 0" }

        var subtotal = 0.0
        var descuentoPromo = 0.0
        val detallePromo = mutableListOf<LineaDescuentoProducto>()
        val etiquetas = mutableListOf<String>()

        for ((producto, cant) in lineas) {
            val baseLinea = producto.precio * cant
            subtotal += baseLinea
            val promo = producto.javaClass.getAnnotation(Promocionable::class.java)
            if (promo != null) {
                val monto = baseLinea * (promo.descuento / 100.0)
                descuentoPromo += monto
                detallePromo.add(
                    LineaDescuentoProducto(
                        nombre = producto.nombre,
                        etiqueta = promo.etiqueta,
                        porcentaje = promo.descuento,
                        baseLinea = baseLinea,
                        montoDescuento = monto
                    )
                )
                etiquetas.add("${producto.nombre}: ${promo.etiqueta} -${promo.descuento}%")
            }
        }

        val despuesPromo = (subtotal - descuentoPromo).coerceAtLeast(0.0)

        val aplicaCantidad = cantidadTotal > 3
        val descuentoCantidad = if (aplicaCantidad) despuesPromo * DESCUENTO_CANTIDAD else 0.0
        val despuesCantidad = (despuesPromo - descuentoCantidad).coerceAtLeast(0.0)

        val aplicaFecha = Validaciones.estaEnPeriodoPromo(fecha)
        val descuentoFecha = if (aplicaFecha) despuesCantidad * DESCUENTO_FECHA_PROMO else 0.0
        if (aplicaFecha) {
            etiquetas.add(
                "Promo fechas ${Validaciones.formatearFecha(Validaciones.PROMO_INICIO)}" +
                    " a ${Validaciones.formatearFecha(Validaciones.PROMO_FIN)} (-${(DESCUENTO_FECHA_PROMO * 100).toInt()}%)"
            )
        }
        val baseFinal = (despuesCantidad - descuentoFecha).coerceAtLeast(0.0)
        val impuesto = baseFinal * IMPUESTO
        val total = baseFinal + impuesto

        return ResultadoTotales(
            subtotal = subtotal,
            descuentoCantidad = descuentoCantidad,
            descuentoPromoProducto = descuentoPromo,
            descuentoFecha = descuentoFecha,
            impuesto = impuesto,
            total = total,
            aplicaDescuentoCantidad = aplicaCantidad,
            aplicaPromoFecha = aplicaFecha,
            etiquetasPromo = etiquetas.distinct(),
            detallePromoProductos = detallePromo,
            despuesPromoProductos = despuesPromo,
            despuesDescuentoCantidad = despuesCantidad,
            despuesDescuentoFecha = baseFinal
        )
    }

    /**
     * Compatibilidad: lista expandida (1 ítem = 1 unidad) o un solo producto × cantidad.
     */
    fun calcularTotales(
        productos: List<Producto>,
        cantidad: Int,
        fecha: LocalDate = LocalDate.now()
    ): ResultadoTotales {
        if (productos.isEmpty()) {
            throw IllegalArgumentException("La cantidad debe ser mayor que 0")
        }
        val lineas = if (productos.size == 1) {
            listOf(productos.first() to cantidad)
        } else {
            productos
                .groupingBy { it.nombre to it.categoria }
                .eachCount()
                .map { (clave, cant) ->
                    val prod = productos.first { it.nombre == clave.first && it.categoria == clave.second }
                    prod to cant
                }
        }
        return calcularDesdeLineas(lineas, fecha)
    }

    /** Precio final unitario leyendo @Promocionable con reflection (demo de clase). */
    fun precioFinalConAnotacion(producto: Producto): Double {
        val promo = producto.javaClass.getAnnotation(Promocionable::class.java) ?: return producto.precio
        return producto.precio * (100 - promo.descuento) / 100.0
    }
}
