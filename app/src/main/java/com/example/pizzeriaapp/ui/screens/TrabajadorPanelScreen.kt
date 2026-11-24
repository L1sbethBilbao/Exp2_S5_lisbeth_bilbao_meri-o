package com.example.pizzeriaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.ui.components.BotonCierreDiaInventario
import com.example.pizzeriaapp.ui.components.FichaVentaClienteBoleta
import com.example.pizzeriaapp.ui.components.FiltroClientesTrabajador
import com.example.pizzeriaapp.ui.components.FiltrosClientesDia
import com.example.pizzeriaapp.ui.components.ListaInventarioActual
import com.example.pizzeriaapp.ui.components.ListaInventarioFijo
import com.example.pizzeriaapp.ui.components.PanelRecaudacionDia
import com.example.pizzeriaapp.ui.components.SeccionColapsable
import com.example.pizzeriaapp.ui.components.filtrarVentasHoy
import com.example.pizzeriaapp.util.AppConstantes
import com.example.pizzeriaapp.viewmodel.IngredienteStockUi
import com.example.pizzeriaapp.viewmodel.PizzeriaViewModel
import com.example.pizzeriaapp.viewmodel.ResumenUi

@Composable
fun TrabajadorPanelScreen(
    modifier: Modifier = Modifier,
    viewModel: PizzeriaViewModel,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val resumen by viewModel.resumen.observeAsState(ResumenUi())
    val mensaje by viewModel.mensaje.observeAsState("")
    val mensajeId by viewModel.mensajeId.observeAsState(0)

    var inventarioExpandido by remember { mutableStateOf(true) }
    var clientesExpandido by remember { mutableStateOf(true) }
    var recaudacionExpandida by remember { mutableStateOf(true) }
    var filtroClientes by remember { mutableStateOf(FiltroClientesTrabajador.TODOS) }

    var resaltarClientes by remember { mutableStateOf(false) }
    var resaltarPedidos by remember { mutableStateOf(false) }
    var mostrarAviso by remember { mutableStateOf(false) }
    var textoAviso by remember { mutableStateOf("") }

    var mostrarStockBajo by remember { mutableStateOf(false) }
    var bajosMostrados by remember { mutableStateOf<List<IngredienteStockUi>>(emptyList()) }
    var ultimaFirmaBajos by remember { mutableStateOf("") }

    var mostrarCierreDia by remember { mutableStateOf(false) }
    var mostrarCierreRecaudacion by remember { mutableStateOf(false) }

    LaunchedEffect(mensajeId, mensaje) {
        if (mensajeId == 0 || mensaje.isBlank()) return@LaunchedEffect
        if (mensaje.startsWith("Cliente registrado") ||
            mensaje.startsWith("Este cliente ya estaba registrado")
        ) {
            resaltarClientes = true
        }
        if (mensaje.startsWith("Pedido #") ||
            mensaje.startsWith("Reposición:") ||
            mensaje.startsWith("Recaudación") ||
            mensaje.startsWith("Reclamo")
        ) {
            resaltarPedidos = true
        }
        textoAviso = mensaje
        mostrarAviso = true
        Toast.makeText(context, mensaje.take(80), Toast.LENGTH_SHORT).show()
        viewModel.limpiarMensaje()
    }

    LaunchedEffect(resumen.inventarioActual) {
        val bajos = resumen.inventarioActual.filter { it.esBajo }
        val firma = bajos.joinToString("|") { "${it.nombre}:${it.cantidad}" }
        if (bajos.isNotEmpty() && firma != ultimaFirmaBajos) {
            ultimaFirmaBajos = firma
            bajosMostrados = bajos
            mostrarStockBajo = true
        }
        if (bajos.isEmpty()) {
            ultimaFirmaBajos = ""
            mostrarStockBajo = false
        }
    }

    if (mostrarAviso) {
        AlertDialog(
            onDismissRequest = { mostrarAviso = false },
            title = { Text("Aviso") },
            text = { Text(textoAviso) },
            confirmButton = {
                TextButton(onClick = { mostrarAviso = false }) { Text("Aceptar") }
            }
        )
    }

    if (mostrarStockBajo && bajosMostrados.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { mostrarStockBajo = false },
            title = { Text("Quedan pocos ingredientes") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Estos ingredientes están en ${AppConstantes.UMBRAL_STOCK_BAJO} " +
                            "o menos. Puedes pedir reposición:"
                    )
                    bajosMostrados.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "${item.nombre}: ${item.cantidad}",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { viewModel.pedirIngrediente(item.nombre) },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("Pedir +${AppConstantes.CANTIDAD_REPOSICION_DEFAULT}")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarStockBajo = false }) { Text("Cerrar") }
            }
        )
    }

    if (mostrarCierreDia) {
        AlertDialog(
            onDismissRequest = { mostrarCierreDia = false },
            title = { Text("Confirmar cierre de día") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Revisa el inventario final. Si coincide con lo que realmente " +
                            "quedó en la tienda, confirma. Ese stock quedará fijo como " +
                            "inicio del día siguiente."
                    )
                    Text("Inventario final:", fontWeight = FontWeight.Bold)
                    resumen.inventarioActual.forEach { item ->
                        Text("• ${item.nombre}: ${item.cantidad}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarCierreDia = false
                    viewModel.confirmarCierreInventarioDia()
                }) { Text("Sí, es correcto") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCierreDia = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarCierreRecaudacion) {
        AlertDialog(
            onDismissRequest = { mostrarCierreRecaudacion = false },
            title = { Text("Confirmar recaudación del día") },
            text = {
                Text(
                    "¿Confirmas que el monto recaudado hoy es " +
                        "$${resumen.recaudacionHoy}? " +
                        "Quedará cerrado con fecha y no se podrá modificar."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarCierreRecaudacion = false
                    viewModel.confirmarCierreRecaudacionDia()
                }) { Text("Sí, confirmar monto") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCierreRecaudacion = false }) { Text("Cancelar") }
            }
        )
    }

    val ventasFiltradas = remember(resumen.ventasHoy, filtroClientes) {
        filtrarVentasHoy(resumen.ventasHoy, filtroClientes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bienvenidos a la mejor pizzería de la ciudad",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Panel del trabajador", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        ) { Text("Cerrar sesión") }

        Text("Resumen del sistema (en vivo)", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val colorClientes =
                    if (resaltarClientes) MaterialTheme.colorScheme.primary else Color.Unspecified
                val pesoClientes =
                    if (resaltarClientes) FontWeight.Bold else FontWeight.Normal
                val colorPedidos =
                    if (resaltarPedidos) MaterialTheme.colorScheme.primary else Color.Unspecified
                val pesoPedidos =
                    if (resaltarPedidos) FontWeight.Bold else FontWeight.Normal

                Text(
                    text = "Fecha: ${resumen.fechaHoyEtiqueta}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Clientes registrados: ${resumen.totalClientes}",
                    color = colorClientes,
                    fontWeight = pesoClientes
                )
                Text(
                    "Pedidos del día: ${resumen.totalPedidosHoy}",
                    color = colorPedidos,
                    fontWeight = pesoPedidos
                )
                Text(
                    "Pedidos totales: ${resumen.totalPedidos}",
                    color = colorPedidos,
                    fontWeight = pesoPedidos
                )
                Text(
                    "Problemas pendientes: ${resumen.problemasPendientes}",
                    color = if (resumen.problemasPendientes > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Unspecified
                    },
                    fontWeight = if (resumen.problemasPendientes > 0) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
                Text(
                    "Último cliente: ${resumen.ultimoClienteNombre}",
                    color = colorClientes,
                    fontWeight = pesoClientes
                )
                Text(
                    "Último pedido: ${resumen.ultimoPedidoTotal}",
                    color = colorPedidos,
                    fontWeight = pesoPedidos
                )
                Text(
                    "Recaudado hoy: $${resumen.recaudacionHoy}" +
                        if (resumen.recaudacionHoyCerrada) " (cerrado)" else " (abierto)",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        SeccionColapsable(
            titulo = "1. Inventario de la tienda",
            completada = false,
            expandida = inventarioExpandido,
            onTituloClick = { inventarioExpandido = !inventarioExpandido }
        ) {
            ListaInventarioFijo(
                titulo = "Inicio del día (fijo)",
                items = resumen.inventarioInicio
            )
            ListaInventarioActual(
                titulo = "Inventario actual / fin del día (en curso)",
                items = resumen.inventarioActual,
                onPedir = { nombre -> viewModel.pedirIngrediente(nombre) }
            )
            BotonCierreDiaInventario(onClick = { mostrarCierreDia = true })
        }

        SeccionColapsable(
            titulo = "2. Clientes del día",
            completada = false,
            expandida = clientesExpandido,
            onTituloClick = { clientesExpandido = !clientesExpandido }
        ) {
            FiltrosClientesDia(
                filtro = filtroClientes,
                onFiltro = { filtroClientes = it },
                pendientesProblemas = resumen.problemasPendientes
            )
            Text(
                when (filtroClientes) {
                    FiltroClientesTrabajador.TODOS ->
                        "Todos los clientes que compraron hoy"
                    FiltroClientesTrabajador.PROBLEMAS ->
                        "Clientes con problemas de pedido (llamar y resolver)"
                    FiltroClientesTrabajador.DOMICILIO ->
                        "Clientes con delivery a domicilio"
                    FiltroClientesTrabajador.LOCAL ->
                        "Clientes que comieron en el local"
                    FiltroClientesTrabajador.RETIRO ->
                        "Clientes con retiro en tienda"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (ventasFiltradas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "No hay registros en este filtro.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                ventasFiltradas.forEach { venta ->
                    FichaVentaClienteBoleta(
                        venta = venta,
                        onConfirmarSolucion = { codigo ->
                            viewModel.marcarReclamoSolucionado(codigo)
                        }
                    )
                }
            }
        }

        SeccionColapsable(
            titulo = "3. Recaudación",
            completada = resumen.recaudacionHoyCerrada,
            expandida = recaudacionExpandida,
            onTituloClick = { recaudacionExpandida = !recaudacionExpandida }
        ) {
            PanelRecaudacionDia(
                recaudacionHoy = resumen.recaudacionHoy,
                cerrada = resumen.recaudacionHoyCerrada,
                fechaCierre = resumen.recaudacionHoyFechaCierre,
                recaudacionMes = resumen.recaudacionMes,
                mesEtiqueta = resumen.mesEtiqueta,
                fechaHoyEtiqueta = resumen.fechaHoyEtiqueta,
                onConfirmarCierre = { mostrarCierreRecaudacion = true }
            )
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}
