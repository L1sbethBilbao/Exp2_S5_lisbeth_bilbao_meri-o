package com.example.pizzeriaapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzeriaapp.anotaciones.Promocionable
import com.example.pizzeriaapp.data.RepositorioPizzeria
import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.util.AppConstantes
import com.example.pizzeriaapp.util.Calculos
import com.example.pizzeriaapp.util.Validaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ClienteItemUi(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val direccion: String
)

data class PedidoItemUi(
    val id: Int,
    val clienteNombre: String,
    val producto: String,
    val cantidad: Int,
    val estado: String,
    val repartidor: String,
    val total: Int,
    val boleta: String
)

data class IngredienteStockUi(
    val nombre: String,
    val cantidad: Int,
    val esBajo: Boolean
)

/** Ficha de venta del día (estilo boleta) para filtros del trabajador. */
data class VentaClienteUi(
    val pedidoId: Int,
    val numeroAtencion: Int,
    val clienteNombre: String,
    val telefono: String,
    val correo: String,
    val direccion: String,
    val productos: String,
    val tipoEntregaClave: String,
    val tipoEntregaTexto: String,
    val total: Int,
    val estadoPedido: String,
    val tieneProblema: Boolean,
    val reclamoCodigo: String?,
    val reclamoSolucionado: Boolean,
    val comoRecibio: String?,
    val estadoProducto: String?
)

data class ResumenUi(
    val totalClientes: Int = 0,
    val totalPedidos: Int = 0,
    val totalPedidosHoy: Int = 0,
    val ultimoClienteNombre: String = "Sin clientes registrados",
    val ultimoPedidoTotal: String = "Sin pedidos",
    val clientes: List<ClienteItemUi> = emptyList(),
    val pedidos: List<PedidoItemUi> = emptyList(),
    /** Ventas del día para filtros del trabajador. */
    val ventasHoy: List<VentaClienteUi> = emptyList(),
    val problemasPendientes: Int = 0,
    /** Snapshot fijo al abrir el día. */
    val inventarioInicio: List<IngredienteStockUi> = emptyList(),
    /** Stock vivo (fin del día en curso). */
    val inventarioActual: List<IngredienteStockUi> = emptyList(),
    val inventarioTexto: String = "",
    val ultimoPedidoId: Int? = null,
    val recaudacionHoy: Int = 0,
    val recaudacionHoyCerrada: Boolean = false,
    val recaudacionHoyFechaCierre: String? = null,
    val recaudacionMes: Int = 0,
    val mesEtiqueta: String = "",
    /** Fecha de hoy (día/mes/año); se actualiza al avanzar los días. */
    val fechaHoyEtiqueta: String = ""
)

/** Detalle de un producto para la UI de pedido. */
data class ProductoDetalleUi(
    val clave: String,
    val nombre: String,
    val precio: Int,
    val categoria: String,
    val contiene: String,
    val stockOk: Boolean,
    val mensajeStock: String,
    val unidadesDisponibles: Int,
    /** Ej: "Cyber -20%" o null si no tiene @Promocionable. */
    val promoProducto: String? = null,
    /** Avisos de descuentos S3 aplicables al comprar. */
    val avisosDescuento: List<String> = emptyList()
)

data class BoletaLineaUi(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Int,
    val subtotal: Int,
    val notas: String = ""
)

data class BoletaDescuentoUi(
    val etiqueta: String,
    val monto: Int
)

/** Boleta del último pedido confirmado (para mostrar colapsable). */
data class BoletaUi(
    val pedidoId: Int,
    val titulo: String,
    val estado: String,
    val repartidor: String,
    val tipoEntrega: String,
    /** retiro | domicilio | local */
    val claveEntrega: String,
    val numeroAtencion: Int,
    val fechaHora: String,
    val lineas: List<BoletaLineaUi>,
    val subtotal: Int,
    val descuentos: List<BoletaDescuentoUi>,
    val baseAntesIva: Int,
    val iva: Int,
    val total: Int,
    /** Texto corto para el diálogo de aviso. */
    val resumenAviso: String
)

/** Línea del carrito antes de confirmar el pedido. */
data class LineaCarritoUi(
    val id: Long,
    val claveProducto: String,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Int,
    val notas: String
) {
    val subtotal: Int get() = precioUnitario * cantidad
}

/**
 * ViewModel Semana 5 + flujo Semana 3 (validaciones, stock, boleta, repartidor, estados).
 */
class PizzeriaViewModel : ViewModel() {

    private val _resumen = MutableLiveData(ResumenUi())
    val resumen: LiveData<ResumenUi> = _resumen

    private val _mensaje = MutableLiveData("")
    val mensaje: LiveData<String> = _mensaje

    /** Contador para que el mismo texto de error vuelva a disparar el aviso en la UI. */
    private val _mensajeId = MutableLiveData(0)
    val mensajeId: LiveData<Int> = _mensajeId

    private val _carrito = MutableLiveData<List<LineaCarritoUi>>(emptyList())
    val carrito: LiveData<List<LineaCarritoUi>> = _carrito

    private val _ultimaBoleta = MutableLiveData<BoletaUi?>(null)
    val ultimaBoleta: LiveData<BoletaUi?> = _ultimaBoleta

    private var siguienteIdLinea = 1L

    init {
        refrescarResumen()
    }

    fun limpiarMensaje() {
        _mensaje.value = ""
    }

    private fun avisar(texto: String) {
        _mensaje.postValue(texto)
        _mensajeId.postValue((_mensajeId.value ?: 0) + 1)
    }

    fun detalleProducto(clave: String): ProductoDetalleUi {
        val producto = RepositorioPizzeria.productoPorNombre(clave)
        val stock = RepositorioPizzeria.verificarStockProducto(producto, 1)
        val unidades = RepositorioPizzeria.unidadesPosibles(producto)
        val contiene = if (producto.ingredientes.isEmpty()) {
            "Sin detalle de ingredientes"
        } else {
            producto.ingredientes.joinToString(", ")
        }
        val promo = producto.javaClass.getAnnotation(Promocionable::class.java)
        val promoTexto = promo?.let { "${it.etiqueta} -${it.descuento}%" }
        val avisos = mutableListOf<String>()
        if (promo != null) {
            avisos.add("${promo.etiqueta}: ${promo.descuento}% menos en este producto")
        } else {
            avisos.add("Sin descuento especial en este producto")
        }
        avisos.add("Más de 3 unidades en el pedido: 10% menos")
        avisos.add(
            "Entre ${Validaciones.formatearFecha(Validaciones.PROMO_INICIO)}" +
                " y ${Validaciones.formatearFecha(Validaciones.PROMO_FIN)}: 5% menos"
        )
        return ProductoDetalleUi(
            clave = clave,
            nombre = producto.nombre,
            precio = producto.precio.toInt(),
            categoria = producto.categoria,
            contiene = contiene,
            stockOk = stock.disponible && unidades > 0,
            mensajeStock = if (stock.disponible && unidades > 0) {
                "Disponible: hasta $unidades unidad(es)"
            } else {
                "PRODUCTO AGOTADO.\n${stock.mensaje}"
            },
            unidadesDisponibles = unidades,
            promoProducto = promoTexto,
            avisosDescuento = avisos
        )
    }

    fun etiquetaTipoEntrega(clave: String): String = when (clave) {
        "domicilio" -> "Despacho a domicilio"
        "local" -> "Comer en el local"
        else -> "Retiro en tienda"
    }

    private fun construirBoleta(
        pedidoId: Int,
        lineasUi: List<LineaCarritoUi>,
        totales: com.example.pizzeriaapp.util.ResultadoTotales,
        estado: String,
        repartidor: String,
        tipoEntrega: String,
        numeroAtencion: Int
    ): BoletaUi {
        val descuentos = mutableListOf<BoletaDescuentoUi>()
        totales.detallePromoProductos.forEach { p ->
            descuentos.add(
                BoletaDescuentoUi(
                    etiqueta = "${p.etiqueta} ${p.nombre} (-${p.porcentaje}%)",
                    monto = p.montoDescuento.toInt()
                )
            )
        }
        if (totales.aplicaDescuentoCantidad) {
            descuentos.add(
                BoletaDescuentoUi("Por cantidad (>3 u.)", totales.descuentoCantidad.toInt())
            )
        }
        if (totales.aplicaPromoFecha) {
            descuentos.add(
                BoletaDescuentoUi("Promo por fechas", totales.descuentoFecha.toInt())
            )
        }
        val total = totales.total.toInt()
        val tipoTxt = etiquetaTipoEntrega(tipoEntrega)
        val fechaHora = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        return BoletaUi(
            pedidoId = pedidoId,
            titulo = "Boleta pedido #$pedidoId",
            estado = estado,
            repartidor = repartidor,
            tipoEntrega = tipoTxt,
            claveEntrega = tipoEntrega,
            numeroAtencion = numeroAtencion,
            fechaHora = fechaHora,
            lineas = lineasUi.map {
                BoletaLineaUi(
                    nombre = it.nombre,
                    cantidad = it.cantidad,
                    precioUnitario = it.precioUnitario,
                    subtotal = it.subtotal,
                    notas = it.notas
                )
            },
            subtotal = totales.subtotal.toInt(),
            descuentos = descuentos,
            baseAntesIva = totales.despuesDescuentoFecha.toInt(),
            iva = totales.impuesto.toInt(),
            total = total,
            resumenAviso =
                "Pedido #$pedidoId creado.\n" +
                    "N° de atención: $numeroAtencion\n" +
                    "Entrega: $tipoTxt\n" +
                    "Total a pagar: $$total"
        )
    }

    fun detallesBebidas(): List<ProductoDetalleUi> =
        listOf("coca-cola", "sprite", "jugo").map { detalleProducto(it) }

    /** Aviso al entrar a un producto si no hay stock. */
    fun avisarSiAgotado(clave: String) {
        val d = detalleProducto(clave)
        if (!d.stockOk) {
            avisar("Se agotó: ${d.nombre}.\n${d.mensajeStock}")
        }
    }

    fun avisarSiBebidasAgotadas() {
        val bebidas = detallesBebidas()
        val agotadas = bebidas.filter { !it.stockOk }
        if (agotadas.size == bebidas.size) {
            avisar("Se agotaron todas las bebidas.")
        } else if (agotadas.isNotEmpty()) {
            avisar(
                "Algunas bebidas se agotaron:\n" +
                    agotadas.joinToString("\n") { "- ${it.nombre}" }
            )
        }
    }

    fun agregarAlCarrito(clave: String, cantidadTexto: String, notas: String) {
        viewModelScope.launch(Dispatchers.Default) {
            if (RepositorioPizzeria.ultimoCliente() == null) {
                avisar("Primero registra un cliente")
                return@launch
            }
            val cantidad = cantidadTexto.trim().toIntOrNull()
            if (cantidad == null || !Validaciones.esCantidadValida(cantidad)) {
                avisar("Cantidad inválida (1 a 100)")
                return@launch
            }
            val producto = RepositorioPizzeria.productoPorNombre(clave)
            val stock = RepositorioPizzeria.verificarStockProducto(producto, cantidad)
            if (!stock.disponible) {
                avisar("No hay stock suficiente para ${producto.nombre}.\n${stock.mensaje}")
                return@launch
            }
            val linea = LineaCarritoUi(
                id = siguienteIdLinea++,
                claveProducto = clave,
                nombre = producto.nombre,
                cantidad = cantidad,
                precioUnitario = producto.precio.toInt(),
                notas = notas.trim()
            )
            val actual = _carrito.value.orEmpty().toMutableList()
            actual.add(linea)
            _carrito.postValue(actual)
            // Sin diálogo pesado: solo actualiza el carrito (más fluido en emulador)
        }
    }

    fun eliminarDelCarrito(idLinea: Long) {
        val actual = _carrito.value.orEmpty().filterNot { it.id == idLinea }
        _carrito.value = actual
    }

    fun limpiarCarrito() {
        _carrito.value = emptyList()
    }

    /**
     * Cierra la venta actual en pantalla cliente.
     * Limpia carrito y boleta; el historial (clientes/pedidos) se conserva para el trabajador.
     */
    fun cerrarVentaActual() {
        _carrito.value = emptyList()
        _ultimaBoleta.value = null
        limpiarMensaje()
    }

    /** Reposición de un ingrediente al inventario del día. */
    fun pedirIngrediente(
        nombre: String,
        cantidad: Int = AppConstantes.CANTIDAD_REPOSICION_DEFAULT
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val msg = RepositorioPizzeria.pedirIngrediente(nombre, cantidad)
            refrescarResumen()
            avisar(msg)
        }
    }

    /**
     * Confirma el cierre del día: el stock actual queda fijo como inicio del siguiente.
     */
    fun confirmarCierreInventarioDia() {
        viewModelScope.launch(Dispatchers.Default) {
            val msg = RepositorioPizzeria.confirmarCierreInventarioDia()
            refrescarResumen()
            avisar(msg)
        }
    }

    /**
     * Registra reclamo en el sistema (trabajador puede verlo y marcarlo solucionado).
     * @return código único REC-…
     */
    fun registrarReclamo(
        pedidoId: Int,
        comoRecibio: String,
        estadoProducto: String
    ): String {
        val reclamo = RepositorioPizzeria.registrarReclamo(
            pedidoId = pedidoId,
            comoRecibio = comoRecibio,
            estadoProducto = estadoProducto
        )
        refrescarResumen()
        return reclamo.codigo
    }

    fun marcarReclamoSolucionado(codigo: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val msg = RepositorioPizzeria.marcarReclamoSolucionado(codigo)
            refrescarResumen()
            avisar(msg)
        }
    }

    fun confirmarCierreRecaudacionDia() {
        viewModelScope.launch(Dispatchers.Default) {
            val msg = RepositorioPizzeria.confirmarCierreRecaudacionDia()
            refrescarResumen()
            avisar(msg)
        }
    }

    /**
     * Confirma el carrito como pedido.
     * @param tipoEntrega retiro | domicilio | local
     */
    fun confirmarPedidoCarrito(tipoEntrega: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val cliente = RepositorioPizzeria.ultimoCliente()
            if (cliente == null) {
                avisar("Primero registra un cliente")
                return@launch
            }
            val lineasUi = _carrito.value.orEmpty()
            if (lineasUi.isEmpty()) {
                avisar("Tu resumen de pedido está vacío. Agrega productos primero.")
                return@launch
            }

            val lineas = lineasUi.map {
                RepositorioPizzeria.productoPorNombre(it.claveProducto) to it.cantidad
            }
            val notasCombinadas = lineasUi
                .mapNotNull { l ->
                    l.notas.takeIf { it.isNotBlank() }?.let { "${l.nombre}: $it" }
                }
                .joinToString(" | ")
                .ifBlank { null }

            val resultado = RepositorioPizzeria.crearPedidoCompleto(
                cliente = cliente,
                lineas = lineas,
                notas = notasCombinadas,
                fecha = LocalDate.now(),
                tipoEntrega = tipoEntrega
            )
            val pedido = resultado.pedido
            val totales = Calculos.calcularDesdeLineas(lineas, pedido.fecha)
            refrescarResumen()

            if (!resultado.stockOk) {
                _ultimaBoleta.postValue(null)
                avisar(
                    "Pedido #${pedido.id} CANCELADO por stock.\n${resultado.mensajeStock}"
                )
            } else {
                _carrito.postValue(emptyList())
                val repartidorTxt = when (tipoEntrega) {
                    "domicilio" -> resultado.repartidorNombre ?: "Sin repartidor disponible"
                    "local" -> "No aplica (comer en el local)"
                    else -> "No aplica (retiro en tienda)"
                }
                val boleta = construirBoleta(
                    pedidoId = pedido.id,
                    lineasUi = lineasUi,
                    totales = totales,
                    estado = pedido.estado,
                    repartidor = repartidorTxt,
                    tipoEntrega = tipoEntrega,
                    numeroAtencion = pedido.numeroAtencion
                )
                _ultimaBoleta.postValue(boleta)
                avisar(boleta.resumenAviso)
            }
        }
    }

    fun registrarCliente(
        nombre: String,
        telefono: String,
        correo: String,
        direccion: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val nombreOk = nombre.trim()
            val correoOk = correo.trim()
            val telefonoNorm = Validaciones.normalizarTelefono(telefono)
            val direccionOk = direccion.trim()

            when {
                !Validaciones.esNombreValido(nombreOk) ->
                    avisar("Nombre inválido (solo letras)")
                !Validaciones.esTelefonoValido(telefonoNorm) ->
                    avisar("Teléfono inválido (9 dígitos)")
                !Validaciones.esCorreoValido(correoOk) ->
                    avisar(
                        "Correo inválido. Debe ser como: nombre@correo.com " +
                            "(ejemplo: lisbeth@gmail.com)"
                    )
                direccionOk.isBlank() ->
                    avisar("Ingresa una dirección")
                else -> {
                    val existente = RepositorioPizzeria.buscarCliente(nombreOk, correoOk)
                    val pedidosPrevios = if (existente != null) {
                        RepositorioPizzeria.contarPedidosCliente(nombreOk, correoOk)
                    } else {
                        0
                    }
                    val cliente = Cliente(
                        nombre = nombreOk,
                        contacto = telefonoNorm,
                        telefono = telefonoNorm,
                        correo = correoOk,
                        direccion = direccionOk
                    )
                    RepositorioPizzeria.agregarCliente(cliente)
                    refrescarResumen()
                    avisar(
                        if (existente != null) {
                            val pedidosTxt = when (pedidosPrevios) {
                                0 -> "Aún no cuenta con pedidos anteriores."
                                1 -> "Cuenta con 1 pedido anterior."
                                else -> "Cuenta con $pedidosPrevios pedidos anteriores."
                            }
                            "Este cliente ya estaba registrado.\n" +
                                "Se actualizaron sus datos (sin duplicar).\n" +
                                pedidosTxt + "\n" +
                                "Total clientes: ${RepositorioPizzeria.totalClientes()}"
                        } else {
                            "Cliente registrado. Total clientes: ${RepositorioPizzeria.totalClientes()}"
                        }
                    )
                }
            }
        }
    }

    /**
     * Pedido completo S3: producto + cantidad + notas, stock, boleta, repartidor.
     */
    fun registrarPedido(
        tipoProducto: String,
        cantidadTexto: String,
        notas: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val cliente = RepositorioPizzeria.ultimoCliente()
            if (cliente == null) {
                avisar("Primero registra un cliente")
                return@launch
            }

            val cantidad = cantidadTexto.trim().toIntOrNull()
            if (cantidad == null || !Validaciones.esCantidadValida(cantidad)) {
                avisar("Cantidad inválida (1 a 100)")
                return@launch
            }

            val producto: Producto = RepositorioPizzeria.productoPorNombre(tipoProducto)
            val resultado = RepositorioPizzeria.crearPedidoCompleto(
                cliente = cliente,
                lineas = listOf(producto to cantidad),
                notas = notas.trim().ifBlank { null },
                fecha = LocalDate.now()
            )
            val pedido = resultado.pedido
            val totales = Calculos.calcularDesdeLineas(
                listOf(producto to cantidad),
                pedido.fecha
            )
            refrescarResumen()

            if (!resultado.stockOk) {
                avisar(
                    "Pedido #${pedido.id} CANCELADO por stock.\n${resultado.mensajeStock}"
                )
            } else {
                avisar(
                    "Pedido #${pedido.id} creado.\n" +
                        "Producto: ${producto.nombre} x$cantidad\n" +
                        "Estado: ${pedido.estado}\n" +
                        "Repartidor: ${resultado.repartidorNombre ?: "Sin asignar"}\n" +
                        "Subtotal: $${totales.subtotal.toInt()}\n" +
                        "Desc. promo: -$${totales.descuentoPromoProducto.toInt()}\n" +
                        "Desc. cantidad: -$${totales.descuentoCantidad.toInt()}\n" +
                        "Desc. fecha: -$${totales.descuentoFecha.toInt()}\n" +
                        "IVA: $${totales.impuesto.toInt()}\n" +
                        "TOTAL: $${totales.total.toInt()}"
                )
            }
        }
    }

    fun avanzarEstado(nuevoEstado: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val id = RepositorioPizzeria.ultimoPedido()?.id
            if (id == null) {
                avisar("No hay pedidos para actualizar")
                return@launch
            }
            val msg = RepositorioPizzeria.cambiarEstadoPedido(id, nuevoEstado)
            refrescarResumen()
            avisar(msg)
        }
    }

    private fun boletaTexto(lineas: List<Pair<Producto, Int>>, fecha: LocalDate): String {
        val t = Calculos.calcularDesdeLineas(lineas, fecha)
        return "Subtotal $${t.subtotal.toInt()} | Promo -$${t.descuentoPromoProducto.toInt()} | " +
            "Cant -$${t.descuentoCantidad.toInt()} | Fecha -$${t.descuentoFecha.toInt()} | " +
            "IVA $${t.impuesto.toInt()} | TOTAL $${t.total.toInt()}"
    }

    private fun refrescarResumen() {
        val pedidosUi = RepositorioPizzeria.pedidos.map { pedido ->
            val agrupado = pedido.productos
                .groupingBy { it.nombre }
                .eachCount()
            val lineas = agrupado.map { (nombre, cant) ->
                val prod = pedido.productos.first { it.nombre == nombre }
                prod to cant
            }
            val totales = Calculos.calcularDesdeLineas(lineas, pedido.fecha)
            PedidoItemUi(
                id = pedido.id,
                clienteNombre = pedido.cliente.nombre,
                producto = agrupado.entries.joinToString { "${it.key} x${it.value}" },
                cantidad = pedido.cantidad,
                estado = pedido.estado,
                repartidor = pedido.etiquetaRepartidor(),
                total = totales.total.toInt(),
                boleta = boletaTexto(lineas, pedido.fecha)
            )
        }

        val hoy = LocalDate.now()
        val ventasHoy = RepositorioPizzeria.pedidosDelDia(hoy).map { pedido ->
            val agrupado = pedido.productos.groupingBy { it.nombre }.eachCount()
            val reclamo = RepositorioPizzeria.reclamoPorPedido(pedido.id)
            VentaClienteUi(
                pedidoId = pedido.id,
                numeroAtencion = pedido.numeroAtencion,
                clienteNombre = pedido.cliente.nombre,
                telefono = pedido.cliente.telefono,
                correo = pedido.cliente.correo,
                direccion = pedido.cliente.direccion,
                productos = agrupado.entries.joinToString { "${it.key} x${it.value}" },
                tipoEntregaClave = pedido.tipoEntrega,
                tipoEntregaTexto = etiquetaTipoEntrega(pedido.tipoEntrega),
                total = RepositorioPizzeria.totalPedido(pedido),
                estadoPedido = pedido.estado,
                tieneProblema = reclamo != null,
                reclamoCodigo = reclamo?.codigo,
                reclamoSolucionado = reclamo?.solucionado == true,
                comoRecibio = reclamo?.comoRecibio,
                estadoProducto = reclamo?.estadoProducto
            )
        }.sortedByDescending { it.pedidoId }

        val umbral = AppConstantes.UMBRAL_STOCK_BAJO
        val inicio = RepositorioPizzeria.inventarioInicioDia()
            .entries
            .sortedBy { it.key }
            .map { IngredienteStockUi(it.key, it.value, it.value <= umbral) }
        val actual = RepositorioPizzeria.inventarioActual()
            .entries
            .sortedBy { it.key }
            .map { IngredienteStockUi(it.key, it.value, it.value <= umbral) }
        val inventarioTxt = actual.joinToString("\n") { "  - ${it.nombre}: ${it.cantidad}" }

        val cierreHoy = RepositorioPizzeria.cierreRecaudacion(hoy)
        val mes = java.time.YearMonth.now()
        val fmtCierre = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val localeEs = java.util.Locale.forLanguageTag("es-CL")
        val fmtHoy = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fmtMes = DateTimeFormatter.ofPattern("MMMM yyyy", localeEs)

        _resumen.postValue(
            ResumenUi(
                totalClientes = RepositorioPizzeria.totalClientes(),
                totalPedidos = RepositorioPizzeria.totalPedidos(),
                totalPedidosHoy = ventasHoy.size,
                ultimoClienteNombre = RepositorioPizzeria.ultimoCliente()?.nombre
                    ?: "Sin clientes registrados",
                ultimoPedidoTotal = pedidosUi.lastOrNull()?.let { "$${it.total}" } ?: "Sin pedidos",
                clientes = RepositorioPizzeria.clientes.map {
                    ClienteItemUi(it.nombre, it.correo, it.telefono, it.direccion)
                },
                pedidos = pedidosUi,
                ventasHoy = ventasHoy,
                problemasPendientes = ventasHoy.count {
                    it.tieneProblema && !it.reclamoSolucionado
                },
                inventarioInicio = inicio,
                inventarioActual = actual,
                inventarioTexto = inventarioTxt,
                ultimoPedidoId = RepositorioPizzeria.ultimoPedido()?.id,
                recaudacionHoy = RepositorioPizzeria.recaudacionDelDia(hoy),
                recaudacionHoyCerrada = cierreHoy != null,
                recaudacionHoyFechaCierre = cierreHoy?.confirmadoEn?.format(fmtCierre),
                recaudacionMes = RepositorioPizzeria.recaudacionDelMes(mes),
                mesEtiqueta = hoy.format(fmtMes),
                fechaHoyEtiqueta = hoy.format(fmtHoy)
            )
        )
    }
}
