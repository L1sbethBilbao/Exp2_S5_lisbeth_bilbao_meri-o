package com.example.pizzeriaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pizzeriaapp.ui.components.BebidasPanel
import com.example.pizzeriaapp.ui.components.BoletaColapsable
import com.example.pizzeriaapp.ui.components.BotonProductoMenu
import com.example.pizzeriaapp.ui.components.DetallePizzaPanel
import com.example.pizzeriaapp.ui.components.ContadorSeguimientoPanel
import com.example.pizzeriaapp.ui.components.FormularioCliente
import com.example.pizzeriaapp.ui.components.FormularioReclamo
import com.example.pizzeriaapp.ui.components.ResumenCarritoPanel
import com.example.pizzeriaapp.ui.components.SeccionColapsable
import com.example.pizzeriaapp.viewmodel.BoletaUi
import com.example.pizzeriaapp.viewmodel.PizzeriaViewModel

/**
 * Pantalla cliente.
 * Mientras escribes el registro, NO se compone el árbol de pedido/boleta/seguimiento
 * (evita saturar el Main thread / Choreographer).
 */
@Composable
fun ClientePanelScreen(
    modifier: Modifier = Modifier,
    viewModel: PizzeriaViewModel,
    onSoyTrabajador: () -> Unit
) {
    val context = LocalContext.current
    val mensaje by viewModel.mensaje.observeAsState("")
    val mensajeId by viewModel.mensajeId.observeAsState(0)

    var formResetKey by remember { mutableIntStateOf(0) }
    var nombrePendiente by remember { mutableStateOf("") }
    var clienteHecho by remember { mutableStateOf(false) }
    var clienteExpandido by remember { mutableStateOf(true) }
    var nombreBienvenida by remember { mutableStateOf("") }

    var mostrarAviso by remember { mutableStateOf(false) }
    var tituloAviso by remember { mutableStateOf("Aviso") }
    var textoAviso by remember { mutableStateOf("") }

    // Se activa solo tras registrar: ahí sí montamos pedido + LiveData pesados.
    var pedidoListoParaMostrar by remember { mutableStateOf(false) }
    // Reenvío de mensajes de pedido al subárbol (evita doble observe + race al limpiar).
    var pedidoMsgId by remember { mutableIntStateOf(0) }
    var pedidoMsg by remember { mutableStateOf("") }

    LaunchedEffect(mensajeId, mensaje) {
        if (mensajeId == 0 || mensaje.isBlank()) return@LaunchedEffect

        val esClienteOk = mensaje.startsWith("Cliente registrado") ||
            mensaje.startsWith("Este cliente ya estaba registrado")
        val esPedidoOk = mensaje.startsWith("Pedido #") &&
            !mensaje.contains("CANCELADO", ignoreCase = true)

        val esValidacionCampo = mensaje.startsWith("Nombre inválido") ||
            mensaje.startsWith("Teléfono inválido") ||
            mensaje.startsWith("Correo inválido") ||
            mensaje.startsWith("Ingresa una dirección")

        if (!esValidacionCampo) {
            tituloAviso = when {
                mensaje.contains("CANCELADO", ignoreCase = true) -> "Pedido cancelado"
                mensaje.startsWith("Este cliente ya estaba registrado") -> "Cliente ya registrado"
                mensaje.startsWith("Se agotó") ||
                    mensaje.startsWith("Se agotaron") ||
                    mensaje.startsWith("Algunas bebidas") ||
                    mensaje.startsWith("No hay stock") -> "Sin stock / agotado"
                esClienteOk || esPedidoOk -> "Registro exitoso"
                else -> "Revisa los datos"
            }
            textoAviso = mensaje
            mostrarAviso = true
            Toast.makeText(context, mensaje.take(80), Toast.LENGTH_SHORT).show()
        } else {
            clienteExpandido = true
            Toast.makeText(context, mensaje.take(80), Toast.LENGTH_SHORT).show()
        }

        if (esClienteOk) {
            nombreBienvenida = nombrePendiente
            formResetKey++
            clienteHecho = true
            clienteExpandido = false
            pedidoListoParaMostrar = true
        }

        // Pedido / stock / cantidad: el hijo reacciona a esta copia local.
        pedidoMsg = mensaje
        pedidoMsgId = mensajeId

        viewModel.limpiarMensaje()
    }

    if (mostrarAviso) {
        AlertDialog(
            onDismissRequest = { mostrarAviso = false },
            title = { Text(tituloAviso) },
            text = { Text(textoAviso) },
            confirmButton = {
                TextButton(onClick = { mostrarAviso = false }) { Text("Aceptar") }
            }
        )
    }

    // Sin scroll mientras se escribe el registro: scroll + IME relayout = lag por tecla.
    val columnaModifier = if (pedidoListoParaMostrar) {
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(24.dp)
    }

    Column(
        modifier = columnaModifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onSoyTrabajador,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = MaterialTheme.shapes.small,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Soy trabajador", style = MaterialTheme.typography.labelMedium)
            }
        }
        Text(
            text = "Bienvenidos a la mejor pizzería de la ciudad",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Pedido del cliente", style = MaterialTheme.typography.titleMedium)

        // Árbol LIVIANO: solo el formulario con estado local.
        SeccionColapsable(
            titulo = "1. Registrar cliente",
            completada = clienteHecho,
            expandida = clienteExpandido,
            onTituloClick = { clienteExpandido = !clienteExpandido }
        ) {
            FormularioCliente(
                resetKey = formResetKey,
                onRegistrar = { n, t, c, d ->
                    nombrePendiente = n
                    viewModel.registrarCliente(n, t, c, d)
                }
            )
        }

        if (clienteHecho && nombreBienvenida.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "¡Bienvenida/o, $nombreBienvenida!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Ya puedes armar tu pedido abajo.",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Pedido/boleta/seguimiento: se montan DESPUÉS del registro (deferred composition).
        if (pedidoListoParaMostrar) {
            ClientePedidoSeguimiento(
                viewModel = viewModel,
                nombreBienvenida = nombreBienvenida,
                pedidoMsgId = pedidoMsgId,
                pedidoMsg = pedidoMsg,
                onCerrarVenta = {
                    viewModel.cerrarVentaActual()
                    formResetKey++
                    nombrePendiente = ""
                    clienteHecho = false
                    clienteExpandido = true
                    nombreBienvenida = ""
                    pedidoListoParaMostrar = false
                    pedidoMsg = ""
                    pedidoMsgId = 0
                    mostrarAviso = false
                    Toast.makeText(
                        context,
                        "Venta finalizada. Puedes registrar al siguiente cliente.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

/**
 * Subárbol pesado (LiveData de carrito/boleta + paneles).
 * Separado para que escribir en el formulario no comparta scope de recomposición
 * con estas suscripciones.
 */
@Composable
private fun ClientePedidoSeguimiento(
    viewModel: PizzeriaViewModel,
    nombreBienvenida: String,
    pedidoMsgId: Int,
    pedidoMsg: String,
    onCerrarVenta: () -> Unit
) {
    val context = LocalContext.current
    val resumen by viewModel.resumen.observeAsState(
        com.example.pizzeriaapp.viewmodel.ResumenUi()
    )
    val carrito by viewModel.carrito.observeAsState(emptyList())
    val ultimaBoleta by viewModel.ultimaBoleta.observeAsState(null)

    var pedidoHecho by remember { mutableStateOf(false) }
    var pedidoExpandido by remember { mutableStateOf(true) }
    var vistaPedido by remember { mutableStateOf("menu") }
    var boletaExpandida by remember { mutableStateOf(false) }
    var faseSeguimiento by remember { mutableStateOf("idle") }
    var faseTrasAlerta by remember { mutableStateOf("confirmar") }
    var errorCantidad by remember { mutableStateOf<String?>(null) }
    var errorPedidoCliente by remember { mutableStateOf<String?>(null) }

    var mostrarConfirmarPedido by remember { mutableStateOf(false) }
    var mostrarTipoEntrega by remember { mutableStateOf(false) }
    var mostrarAlertaSeguimiento by remember { mutableStateOf(false) }
    var tituloAlertaSeguimiento by remember { mutableStateOf("") }
    var textoAlertaSeguimiento by remember { mutableStateOf("") }

    var comoRecibioGuardado by remember { mutableStateOf("") }
    var estadoProductoGuardado by remember { mutableStateOf("") }
    var codigoReclamo by remember { mutableStateOf("") }
    var fechaHoraReclamo by remember { mutableStateOf("") }

    LaunchedEffect(pedidoMsgId, pedidoMsg) {
        if (pedidoMsgId == 0 || pedidoMsg.isBlank()) return@LaunchedEffect

        when {
            pedidoMsg.startsWith("Cantidad inválida") -> {
                errorCantidad = "Ingresa un número entre 1 y 100"
                pedidoExpandido = true
            }
            pedidoMsg.startsWith("Primero registra un cliente") -> {
                errorPedidoCliente = "Primero registra un cliente arriba"
                pedidoExpandido = true
            }
            pedidoMsg.contains("CANCELADO", ignoreCase = true) -> {
                errorCantidad = "Revisa cantidad o stock disponible"
                pedidoExpandido = true
            }
        }

        val esPedidoOk = pedidoMsg.startsWith("Pedido #") &&
            !pedidoMsg.contains("CANCELADO", ignoreCase = true)
        if (esPedidoOk) {
            errorCantidad = null
            errorPedidoCliente = null
            vistaPedido = "menu"
            // Primero boleta abierta + preparación (no comprimir ni saltar a entrega).
            boletaExpandida = true
            pedidoHecho = true
            pedidoExpandido = false
            faseSeguimiento = "preparacion"
        }
    }

    if (mostrarConfirmarPedido) {
        val totalCarrito = carrito.sumOf { it.subtotal }
        val resumenTexto = carrito.joinToString("\n") { l ->
            val nota = if (l.notas.isBlank()) "" else "\n   Nota: ${l.notas}"
            "• ${l.nombre} x${l.cantidad} = $${l.subtotal}$nota"
        } + "\n\nTotal estimado: $$totalCarrito"
        AlertDialog(
            onDismissRequest = { mostrarConfirmarPedido = false },
            title = { Text("Confirmar pedido") },
            text = { Text(resumenTexto) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmarPedido = false
                    mostrarTipoEntrega = true
                }) { Text("Sí, confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarPedido = false }) { Text("Seguir editando") }
            }
        )
    }

    if (mostrarTipoEntrega) {
        AlertDialog(
            onDismissRequest = { mostrarTipoEntrega = false },
            title = { Text("Tipo de entrega") },
            text = { Text("¿Cómo quieres recibir tu pedido?") },
            confirmButton = {},
            dismissButton = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        mostrarTipoEntrega = false
                        viewModel.confirmarPedidoCarrito("retiro")
                    }) { Text("Retiro en local") }
                    TextButton(onClick = {
                        mostrarTipoEntrega = false
                        viewModel.confirmarPedidoCarrito("domicilio")
                    }) { Text("Delivery a domicilio") }
                    TextButton(onClick = {
                        mostrarTipoEntrega = false
                        viewModel.confirmarPedidoCarrito("local")
                    }) { Text("Comer en el local") }
                    TextButton(onClick = { mostrarTipoEntrega = false }) { Text("Cancelar") }
                }
            }
        )
    }

    if (mostrarAlertaSeguimiento) {
        AlertDialog(
            onDismissRequest = {
                mostrarAlertaSeguimiento = false
                faseSeguimiento = faseTrasAlerta
            },
            title = { Text(tituloAlertaSeguimiento) },
            text = { Text(textoAlertaSeguimiento) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarAlertaSeguimiento = false
                    faseSeguimiento = faseTrasAlerta
                }) { Text("Aceptar") }
            }
        )
    }

    SeccionColapsable(
        titulo = "2. Agregar pedido",
        completada = pedidoHecho,
        expandida = pedidoExpandido,
        onTituloClick = { pedidoExpandido = !pedidoExpandido }
    ) {
        if (errorPedidoCliente != null) {
            Text(
                errorPedidoCliente!!,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        if (errorCantidad != null) {
            Text(
                errorCantidad!!,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }

        when (vistaPedido) {
            "pepperoni", "margarita", "hawaiana" -> {
                val detallePizza = remember(vistaPedido) {
                    viewModel.detalleProducto(vistaPedido)
                }
                DetallePizzaPanel(
                    detalle = detallePizza,
                    onVolver = { vistaPedido = "menu" },
                    onAgregar = { cant, notas ->
                        errorCantidad = null
                        errorPedidoCliente = null
                        viewModel.agregarAlCarrito(vistaPedido, cant, notas)
                        Toast.makeText(
                            context,
                            "Agregado al resumen del pedido",
                            Toast.LENGTH_SHORT
                        ).show()
                        vistaPedido = "menu"
                    }
                )
            }
            "bebidas" -> {
                val listaBebidas = remember(vistaPedido) {
                    viewModel.detallesBebidas()
                }
                BebidasPanel(
                    bebidas = listaBebidas,
                    onVolver = { vistaPedido = "menu" },
                    onAgregar = { clave, cant, notas ->
                        errorCantidad = null
                        errorPedidoCliente = null
                        viewModel.agregarAlCarrito(clave, cant, notas)
                        Toast.makeText(
                            context,
                            "Agregado al resumen del pedido",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
            else -> {
                Text(
                    "Elige un producto para ver detalle, precio, stock y descuentos:",
                    style = MaterialTheme.typography.bodyMedium
                )
                val promoPepperoni = remember {
                    viewModel.detalleProducto("pepperoni").promoProducto
                }
                val promoMargarita = remember {
                    viewModel.detalleProducto("margarita").promoProducto
                }
                val promoHawaiana = remember {
                    viewModel.detalleProducto("hawaiana").promoProducto
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonProductoMenu(
                        titulo = "Pepperoni",
                        promo = promoPepperoni,
                        onClick = {
                            vistaPedido = "pepperoni"
                            viewModel.avisarSiAgotado("pepperoni")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BotonProductoMenu(
                        titulo = "Margarita",
                        promo = promoMargarita,
                        onClick = {
                            vistaPedido = "margarita"
                            viewModel.avisarSiAgotado("margarita")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonProductoMenu(
                        titulo = "Hawaiana",
                        promo = promoHawaiana,
                        onClick = {
                            vistaPedido = "hawaiana"
                            viewModel.avisarSiAgotado("hawaiana")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BotonProductoMenu(
                        titulo = "Bebidas",
                        promo = null,
                        onClick = {
                            vistaPedido = "bebidas"
                            viewModel.avisarSiBebidasAgotadas()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                ResumenCarritoPanel(
                    carrito = carrito,
                    onEliminar = { viewModel.eliminarDelCarrito(it) },
                    onConfirmar = {
                        if (carrito.isEmpty()) {
                            errorPedidoCliente = "Agrega productos antes de confirmar"
                        } else {
                            mostrarConfirmarPedido = true
                        }
                    }
                )
            }
        }
    }

    ultimaBoleta?.let { boleta ->
        SeguimientoPostBoleta(
            boleta = boleta,
            nombreBienvenida = nombreBienvenida.ifBlank { resumen.ultimoClienteNombre },
            boletaExpandida = boletaExpandida,
            onToggleBoleta = { boletaExpandida = !boletaExpandida },
            faseSeguimiento = faseSeguimiento,
            onFase = { faseSeguimiento = it },
            onMostrarAlerta = { titulo, texto, siguiente ->
                tituloAlertaSeguimiento = titulo
                textoAlertaSeguimiento = texto
                faseTrasAlerta = siguiente
                mostrarAlertaSeguimiento = true
                faseSeguimiento = "alerta"
            },
            comoRecibioGuardado = comoRecibioGuardado,
            estadoProductoGuardado = estadoProductoGuardado,
            onReclamoDatos = { como, estado ->
                comoRecibioGuardado = como
                estadoProductoGuardado = estado
            },
            codigoReclamo = codigoReclamo,
            fechaHoraReclamo = fechaHoraReclamo,
            onGenerarReclamo = {
                codigoReclamo = viewModel.registrarReclamo(
                    pedidoId = boleta.pedidoId,
                    comoRecibio = comoRecibioGuardado,
                    estadoProducto = estadoProductoGuardado
                )
                fechaHoraReclamo = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            },
            onCerrarVenta = onCerrarVenta
        )
    }
}

@Composable
private fun SeguimientoPostBoleta(
    boleta: BoletaUi,
    nombreBienvenida: String,
    boletaExpandida: Boolean,
    onToggleBoleta: () -> Unit,
    faseSeguimiento: String,
    onFase: (String) -> Unit,
    onMostrarAlerta: (String, String, String) -> Unit,
    comoRecibioGuardado: String,
    estadoProductoGuardado: String,
    onReclamoDatos: (String, String) -> Unit,
    codigoReclamo: String,
    fechaHoraReclamo: String,
    onGenerarReclamo: () -> Unit,
    onCerrarVenta: () -> Unit
) {
    val esDomicilio = boleta.claveEntrega == "domicilio"

    BoletaColapsable(
        boleta = boleta,
        expandida = boletaExpandida,
        onToggle = onToggleBoleta
    )

    // 1) Preparación (30 s) debajo de la boleta abierta.
    if (faseSeguimiento == "preparacion") {
        ContadorSeguimientoPanel(
            claveFase = "prep-${boleta.pedidoId}",
            activo = true,
            titulo = "Tu pedido está en preparación",
            mensaje = "Estamos preparando tu pedido. Esto tomará unos 30 segundos.",
            onTermino = {
                if (esDomicilio) {
                    onMostrarAlerta(
                        "Pedido listo para envío",
                        "Tu pedido ya terminó de prepararse.\n" +
                            "Ahora se lo irán a dejar a tu casa con el repartidor.",
                        "entrega"
                    )
                } else {
                    onMostrarAlerta(
                        "Pedido listo",
                        "Tu pedido ya está listo.\n" +
                            "Por favor estate atento/a a tu N° de atención.",
                        "confirmar"
                    )
                }
            }
        )
    }

    // 2) Entrega a domicilio (otros 30 s) — solo delivery.
    if (faseSeguimiento == "entrega") {
        ContadorSeguimientoPanel(
            claveFase = "entrega-${boleta.pedidoId}",
            activo = true,
            titulo = "Repartidor en camino",
            mensaje = "El repartidor está llevando tu pedido a domicilio. " +
                "Tiempo estimado: 30 segundos.",
            onTermino = {
                onMostrarAlerta(
                    "Pedido en tu domicilio",
                    "El repartidor debería estar llegando.\n" +
                        "Estate atento/a al llamado del repartidor.",
                    "confirmar"
                )
            }
        )
    }

    if (faseSeguimiento == "confirmar") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "¿Recibiste tu pedido correcto y completo?",
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { onFase("gracias") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) { Text("Sí, recibí mi pedido correctamente") }
                OutlinedButton(
                    onClick = { onFase("problema") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) { Text("No, mi pedido no fue lo esperado") }
            }
        }
    }

    if (faseSeguimiento == "gracias") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Gracias por comprar con nosotros, $nombreBienvenida!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Hasta pronto. Te esperamos de nuevo.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Button(
                    onClick = onCerrarVenta,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Finalizar venta y atender otro cliente")
                }
            }
        }
    }

    if (faseSeguimiento == "problema" ||
        faseSeguimiento == "reclamo_form" ||
        faseSeguimiento == "reclamo_revision" ||
        faseSeguimiento == "reclamo_doc"
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Lo sentimos, $nombreBienvenida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Registramos que tu pedido no fue lo esperado. " +
                        "Un trabajador te contactará para ayudarte. " +
                        "Gracias por avisarnos.",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        if (faseSeguimiento == "problema") {
            Button(
                onClick = { onFase("reclamo_form") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) { Text("Hacer reclamo del producto") }
        }

        if (faseSeguimiento == "reclamo_form") {
            FormularioReclamo(
                onRevisar = { como, estado ->
                    onReclamoDatos(como, estado)
                    onFase("reclamo_revision")
                }
            )
        }

        if (faseSeguimiento == "reclamo_revision") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Confirma que la información es correcta",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Cliente: $nombreBienvenida")
                    Text("Pedido: #${boleta.pedidoId}")
                    Text("Cómo lo recibiste:\n$comoRecibioGuardado")
                    Text("Estado del producto:\n$estadoProductoGuardado")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onFase("reclamo_form") },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) { Text("Corregir") }
                        Button(
                            onClick = {
                                onGenerarReclamo()
                                onFase("reclamo_doc")
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) { Text("Confirmar") }
                    }
                }
            }
        }

        if (faseSeguimiento == "reclamo_doc") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "COPIA DE RECLAMO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    Text(
                        "Código único: $codigoReclamo",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Fecha y hora: $fechaHoraReclamo")
                    Text("Cliente: $nombreBienvenida")
                    Text("Pedido: #${boleta.pedidoId}")
                    Text("N° atención: ${boleta.numeroAtencion}")
                    HorizontalDivider()
                    Text("Cómo recibió el producto", fontWeight = FontWeight.SemiBold)
                    Text(comoRecibioGuardado)
                    Text("Estado del producto", fontWeight = FontWeight.SemiBold)
                    Text(estadoProductoGuardado)
                    HorizontalDivider()
                    Text(
                        "Nos contactaremos contigo de inmediato. " +
                            "Guarda este documento: es tu copia del reclamo " +
                            "y el código $codigoReclamo es único.",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { /* futuro */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) { Text("Mandar a correo") }
                    Button(
                        onClick = onCerrarVenta,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Finalizar reclamo y atender otro cliente")
                    }
                }
            }
        }
    }
}
