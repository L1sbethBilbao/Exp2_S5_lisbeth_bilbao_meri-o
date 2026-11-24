package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pizzeriaapp.viewmodel.VentaClienteUi

enum class FiltroClientesTrabajador {
    TODOS,
    PROBLEMAS,
    DOMICILIO,
    LOCAL,
    RETIRO
}

fun filtrarVentasHoy(
    ventas: List<VentaClienteUi>,
    filtro: FiltroClientesTrabajador
): List<VentaClienteUi> = when (filtro) {
    FiltroClientesTrabajador.TODOS -> ventas
    FiltroClientesTrabajador.PROBLEMAS -> ventas.filter { it.tieneProblema }
        .sortedBy { it.reclamoSolucionado }
    FiltroClientesTrabajador.DOMICILIO -> ventas.filter { it.tipoEntregaClave == "domicilio" }
    FiltroClientesTrabajador.LOCAL -> ventas.filter { it.tipoEntregaClave == "local" }
    FiltroClientesTrabajador.RETIRO -> ventas.filter { it.tipoEntregaClave == "retiro" }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltrosClientesDia(
    filtro: FiltroClientesTrabajador,
    onFiltro: (FiltroClientesTrabajador) -> Unit,
    pendientesProblemas: Int
) {
    // FlowRow: si no caben en una línea, pasan a la siguiente (no se cortan).
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filtro == FiltroClientesTrabajador.TODOS,
            onClick = { onFiltro(FiltroClientesTrabajador.TODOS) },
            label = { Text("Todos hoy") }
        )
        FilterChip(
            selected = filtro == FiltroClientesTrabajador.PROBLEMAS,
            onClick = { onFiltro(FiltroClientesTrabajador.PROBLEMAS) },
            label = {
                Text(
                    if (pendientesProblemas > 0) {
                        "Problemas ($pendientesProblemas)"
                    } else {
                        "Problemas"
                    }
                )
            }
        )
        FilterChip(
            selected = filtro == FiltroClientesTrabajador.DOMICILIO,
            onClick = { onFiltro(FiltroClientesTrabajador.DOMICILIO) },
            label = { Text("Delivery") }
        )
        FilterChip(
            selected = filtro == FiltroClientesTrabajador.LOCAL,
            onClick = { onFiltro(FiltroClientesTrabajador.LOCAL) },
            label = { Text("En local") }
        )
        FilterChip(
            selected = filtro == FiltroClientesTrabajador.RETIRO,
            onClick = { onFiltro(FiltroClientesTrabajador.RETIRO) },
            label = { Text("Retiro") }
        )
    }
}

/** Ficha estilo boleta para una venta/cliente del día. */
@Composable
fun FichaVentaClienteBoleta(
    venta: VentaClienteUi,
    onConfirmarSolucion: (codigo: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "PIZZERÍA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Pedido N° ${venta.pedidoId}", fontWeight = FontWeight.Bold)
                    Text(venta.tipoEntregaTexto, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "N° ATENCIÓN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${venta.numeroAtencion}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        lineHeight = 38.sp
                    )
                }
            }

            HorizontalDivider()

            Text("Cliente", fontWeight = FontWeight.Bold)
            Text(venta.clienteNombre)
            Text("Tel: ${venta.telefono}")
            Text("Correo: ${venta.correo}")
            Text("Dir: ${venta.direccion}")

            HorizontalDivider()

            Text("Detalle", fontWeight = FontWeight.Bold)
            Text(venta.productos)
            Text("Estado pedido: ${venta.estadoPedido}")
            Text(
                "TOTAL: $${venta.total}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (venta.tieneProblema) {
                HorizontalDivider()
                val colorProblema = if (venta.reclamoSolucionado) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                Text(
                    if (venta.reclamoSolucionado) {
                        "Reclamo ${venta.reclamoCodigo} · SOLUCIONADO"
                    } else {
                        "Reclamo ${venta.reclamoCodigo} · PENDIENTE"
                    },
                    fontWeight = FontWeight.Bold,
                    color = colorProblema
                )
                if (!venta.comoRecibio.isNullOrBlank()) {
                    Text("Cómo recibió: ${venta.comoRecibio}")
                }
                if (!venta.estadoProducto.isNullOrBlank()) {
                    Text("Estado producto: ${venta.estadoProducto}")
                }
                if (!venta.reclamoSolucionado && venta.reclamoCodigo != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onConfirmarSolucion(venta.reclamoCodigo) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Confirmar que sí se solucionó")
                    }
                }
            }
        }
    }
}
