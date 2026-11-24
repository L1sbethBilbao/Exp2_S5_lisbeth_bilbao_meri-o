package com.example.pizzeriaapp.data

import com.example.pizzeriaapp.modelo.BebidaCola
import com.example.pizzeriaapp.modelo.BebidaJugo
import com.example.pizzeriaapp.modelo.BebidaSprite
import com.example.pizzeriaapp.modelo.CierreRecaudacion
import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.PizzaHawaiana
import com.example.pizzeriaapp.modelo.PizzaMargarita
import com.example.pizzeriaapp.modelo.PizzaPepperoni
import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.modelo.Reclamo
import com.example.pizzeriaapp.modelo.Repartidor
import com.example.pizzeriaapp.util.AppConstantes
import com.example.pizzeriaapp.util.Calculos
import com.example.pizzeriaapp.util.Inventario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Repositorio en memoria ( Semana 3 + Semana 5 ).
 * Clientes, pedidos, inventario, reclamos y recaudación.
 */
object RepositorioPizzeria {
    private val _clientes = mutableListOf<Cliente>()
    private val _pedidos = mutableListOf<Pedido>()
    private val _reclamos = mutableListOf<Reclamo>()
    private val _cierresRecaudacion = mutableListOf<CierreRecaudacion>()
    private var siguienteIdPedido = 3001
    private var siguienteNumeroAtencion = 1
    private var siguienteIdReclamo = 5001

    /** Stock vivo del día (baja con pedidos, sube con reposiciones). */
    private val inventario = mutableMapOf(
        "masa" to 40,
        "queso" to 30,
        "pepperoni" to 25,
        "tomate" to 25,
        "albahaca" to 20,
        "coca-cola" to 20,
        "sprite" to 15,
        "jugo" to 12
    )

    /**
     * Snapshot fijo al abrir el día.
     * Solo cambia al confirmar el cierre del día (pasa a ser el inicio del siguiente).
     */
    private val inventarioInicioDia = inventario.toMutableMap()

    private val repartidores = mutableListOf(
        Repartidor("Carlos Muñoz", "912111111", 3.2),
        Repartidor("María Soto", "912222222", 1.5),
        Repartidor("Pedro Rivas", "912333333", 4.8)
    )

    val catalogo: List<Producto> = listOf(
        PizzaPepperoni(),
        PizzaMargarita(),
        PizzaHawaiana(),
        BebidaCola(),
        BebidaSprite(),
        BebidaJugo()
    )

    val bebidas: List<Producto> = listOf(
        BebidaCola(),
        BebidaSprite(),
        BebidaJugo()
    )

    val clientes: List<Cliente>
        get() = _clientes.toList()

    val pedidos: List<Pedido>
        get() = _pedidos.toList()

    val reclamos: List<Reclamo>
        get() = _reclamos.toList()

    val cierresRecaudacion: List<CierreRecaudacion>
        get() = _cierresRecaudacion.toList()

    fun totalClientes(): Int = _clientes.size

    fun totalPedidos(): Int = _pedidos.size

    fun pedidosDelDia(fecha: LocalDate = LocalDate.now()): List<Pedido> =
        _pedidos.filter { it.fecha == fecha && !it.estado.equals("Cancelado", ignoreCase = true) }

    fun totalPedido(pedido: Pedido): Int =
        Calculos.calcularDesdeLineas(pedido.lineasAgrupadas(), pedido.fecha).total.toInt()

    /** Recaudación viva del día (pedidos no cancelados). */
    fun recaudacionDelDia(fecha: LocalDate = LocalDate.now()): Int =
        pedidosDelDia(fecha).sumOf { totalPedido(it) }

    fun diaRecaudacionCerrado(fecha: LocalDate = LocalDate.now()): Boolean =
        _cierresRecaudacion.any { it.fecha == fecha }

    fun cierreRecaudacion(fecha: LocalDate = LocalDate.now()): CierreRecaudacion? =
        _cierresRecaudacion.firstOrNull { it.fecha == fecha }

    /**
     * Confirma y cierra el monto del día con fecha.
     */
    fun confirmarCierreRecaudacionDia(fecha: LocalDate = LocalDate.now()): String {
        if (diaRecaudacionCerrado(fecha)) {
            val c = cierreRecaudacion(fecha)!!
            return "El día ${c.fecha} ya estaba cerrado con $${c.monto}."
        }
        val monto = recaudacionDelDia(fecha)
        _cierresRecaudacion.add(CierreRecaudacion(fecha = fecha, monto = monto))
        return "Recaudación del $fecha cerrada: $$monto. Queda registrada con fecha."
    }

    /** Suma de días cerrados del mes + (si hoy no cerrado) lo vivo de hoy. */
    fun recaudacionDelMes(mes: YearMonth = YearMonth.now()): Int {
        val cerrados = _cierresRecaudacion
            .filter { YearMonth.from(it.fecha) == mes }
            .sumOf { it.monto }
        val hoy = LocalDate.now()
        val incluyeHoyVivo = YearMonth.from(hoy) == mes && !diaRecaudacionCerrado(hoy)
        val vivoHoy = if (incluyeHoyVivo) recaudacionDelDia(hoy) else 0
        // Si hoy está cerrado, ya está en cerrados
        return cerrados + vivoHoy
    }

    fun registrarReclamo(
        pedidoId: Int,
        comoRecibio: String,
        estadoProducto: String
    ): Reclamo {
        val pedido = _pedidos.firstOrNull { it.id == pedidoId }
            ?: throw IllegalArgumentException("Pedido #$pedidoId no encontrado")
        val codigo = "REC-${siguienteIdReclamo++}"
        val reclamo = Reclamo(
            codigo = codigo,
            pedidoId = pedido.id,
            numeroAtencion = pedido.numeroAtencion,
            clienteNombre = pedido.cliente.nombre,
            clienteTelefono = pedido.cliente.telefono,
            clienteCorreo = pedido.cliente.correo,
            comoRecibio = comoRecibio,
            estadoProducto = estadoProducto,
            fechaHora = LocalDateTime.now()
        )
        _reclamos.add(reclamo)
        return reclamo
    }

    fun marcarReclamoSolucionado(codigo: String): String {
        val r = _reclamos.firstOrNull { it.codigo.equals(codigo, ignoreCase = true) }
            ?: return "Reclamo $codigo no encontrado"
        if (r.solucionado) {
            return "El reclamo ${r.codigo} ya estaba marcado como solucionado."
        }
        r.solucionado = true
        r.fechaHoraSolucion = LocalDateTime.now()
        return "Reclamo ${r.codigo} marcado como solucionado."
    }

    fun reclamoPorPedido(pedidoId: Int): Reclamo? =
        _reclamos.lastOrNull { it.pedidoId == pedidoId }

    fun ultimoCliente(): Cliente? = _clientes.lastOrNull()

    fun ultimoPedido(): Pedido? = _pedidos.lastOrNull()

    fun inventarioActual(): Map<String, Int> = inventario.toMap()

    fun inventarioInicioDia(): Map<String, Int> = inventarioInicioDia.toMap()

    /** Ingredientes en o bajo el umbral de stock bajo. */
    fun ingredientesStockBajo(
        umbral: Int = AppConstantes.UMBRAL_STOCK_BAJO
    ): Map<String, Int> =
        inventario.filter { it.value <= umbral }
            .toList()
            .sortedBy { it.second }
            .toMap()

    /**
     * Pedido de reposición: suma unidades al inventario actual del día.
     * @return mensaje de resultado
     */
    fun pedirIngrediente(nombre: String, cantidad: Int): String {
        val clave = nombre.trim().lowercase()
        if (clave.isBlank()) return "Indica el ingrediente"
        if (cantidad <= 0) return "La cantidad a pedir debe ser mayor a 0"
        if (!inventario.containsKey(clave)) {
            return "Ingrediente desconocido: $clave"
        }
        val antes = inventario[clave] ?: 0
        inventario[clave] = antes + cantidad
        return "Reposición: +$cantidad de $clave (ahora ${inventario[clave]})"
    }

    /**
     * Cierre de día: el stock actual queda fijo como inicio del día siguiente.
     */
    fun confirmarCierreInventarioDia(): String {
        inventarioInicioDia.clear()
        inventarioInicioDia.putAll(inventario)
        return "Cierre de día confirmado. El inventario actual queda fijo " +
            "como inicio del próximo día."
    }

    fun repartidoresActuales(): List<Repartidor> = repartidores.toList()

    fun buscarCliente(nombre: String, correo: String): Cliente? =
        _clientes.firstOrNull {
            it.nombre.equals(nombre, ignoreCase = true) &&
                it.correo.equals(correo, ignoreCase = true)
        }

    /** Pedidos previos de un cliente (mismo nombre + correo). */
    fun contarPedidosCliente(nombre: String, correo: String): Int =
        _pedidos.count {
            it.cliente.nombre.equals(nombre, ignoreCase = true) &&
                it.cliente.correo.equals(correo, ignoreCase = true)
        }

    fun agregarCliente(cliente: Cliente) {
        val existente = buscarCliente(cliente.nombre, cliente.correo)
        if (existente != null) {
            _clientes.remove(existente)
        }
        _clientes.add(cliente)
    }

    fun productoPorNombre(nombre: String): Producto = when (nombre.lowercase().trim()) {
        "margarita" -> PizzaMargarita()
        "hawaiana" -> PizzaHawaiana()
        "coca-cola", "cola" -> BebidaCola()
        "sprite" -> BebidaSprite()
        "jugo", "jugo natural" -> BebidaJugo()
        "pepperoni" -> PizzaPepperoni()
        else -> PizzaPepperoni()
    }

    /**
     * Revisa si hay stock para al menos [cantidad] unidades del producto.
     * Usado al entrar a un producto y al agregar al carrito.
     */
    fun verificarStockProducto(producto: Producto, cantidad: Int = 1): Inventario.ResultadoStock {
        val necesario = Inventario.necesarioPorLineas(
            listOf(producto.ingredientes to cantidad)
        )
        return Inventario.verificarDisponibilidad(
            inventario = inventario,
            necesario = necesario,
            unidadesPedido = cantidad
        )
    }

    /** Unidades máximas aproximadas según el stock actual del producto. */
    fun unidadesPosibles(producto: Producto): Int {
        if (producto.ingredientes.isEmpty()) return 100
        var max = Int.MAX_VALUE
        for (ing in producto.ingredientes.distinct()) {
            val hay = inventario[ing] ?: 0
            max = minOf(max, hay)
        }
        return if (max == Int.MAX_VALUE) 0 else max.coerceAtLeast(0)
    }

    /**
     * Crea pedido con validación de inventario, cálculo, repartidor y estado inicial.
     * @return mensaje de resultado y pedido (nullable si se cancela por stock)
     */
    fun crearPedidoCompleto(
        cliente: Cliente,
        lineas: List<Pair<Producto, Int>>,
        notas: String?,
        fecha: LocalDate = LocalDate.now(),
        tipoEntrega: String = "retiro"
    ): ResultadoCrearPedido {
        require(lineas.isNotEmpty()) { "El pedido no tiene productos" }

        val productosExpandido = mutableListOf<Producto>()
        for ((prod, cant) in lineas) {
            repeat(cant) { productosExpandido.add(prod) }
        }
        val cantidadTotal = lineas.sumOf { it.second }.coerceAtLeast(1)
        val necesario = Inventario.necesarioPorLineas(
            lineas.map { (prod, cant) -> prod.ingredientes to cant }
        )
        val stock = Inventario.verificarDisponibilidad(
            inventario = inventario,
            necesario = necesario,
            unidadesPedido = cantidadTotal
        )

        val numAtencion = siguienteNumeroAtencion++
        val pedido = Pedido(
            id = siguienteIdPedido++,
            cliente = cliente,
            productos = productosExpandido,
            cantidad = cantidadTotal,
            notas = notas,
            fecha = fecha,
            tipoEntrega = tipoEntrega,
            numeroAtencion = numAtencion
        )

        if (!stock.disponible) {
            pedido.cambiarEstado("Cancelado")
            _pedidos.add(pedido)
            return ResultadoCrearPedido(
                pedido = pedido,
                stockOk = false,
                mensajeStock = stock.mensaje,
                repartidorNombre = null
            )
        }

        Inventario.actualizarInventario(inventario, necesario)
        // Solo domicilio lleva repartidor
        val repartidor = if (tipoEntrega == "domicilio") {
            asignarRepartidorDisponible()
        } else {
            null
        }
        pedido.repartidor = repartidor
        pedido.cambiarEstado("En preparación")
        _pedidos.add(pedido)

        return ResultadoCrearPedido(
            pedido = pedido,
            stockOk = true,
            mensajeStock = stock.mensaje,
            repartidorNombre = repartidor?.nombre
        )
    }

    fun asignarRepartidorDisponible(): Repartidor? =
        repartidores.filter { it.disponible }.minByOrNull { it.distanciaKm }

    fun cambiarEstadoPedido(idPedido: Int, nuevoEstado: String): String {
        val pedido = _pedidos.firstOrNull { it.id == idPedido }
            ?: return "Pedido no encontrado"
        val anterior = pedido.estado
        if (!Pedido.puedeTransicionar(anterior, nuevoEstado) && nuevoEstado != anterior) {
            return "No se puede cambiar de \"$anterior\" a \"$nuevoEstado\""
        }
        if (anterior == nuevoEstado) {
            return "El pedido #$idPedido ya está en $nuevoEstado"
        }
        pedido.cambiarEstado(nuevoEstado)
        if (nuevoEstado == "En camino") {
            pedido.repartidor?.ocupar()
            val r = pedido.repartidor
            return if (r != null) {
                "${r.entregarPedido(pedido.id)}. Estado: En camino."
            } else {
                "Pedido #$idPedido ahora está: En camino (sin repartidor)."
            }
        }
        return "${pedido.cliente.nombre}, su pedido #${pedido.id} ahora está: $nuevoEstado"
    }
}

data class ResultadoCrearPedido(
    val pedido: Pedido,
    val stockOk: Boolean,
    val mensajeStock: String,
    val repartidorNombre: String?
)
