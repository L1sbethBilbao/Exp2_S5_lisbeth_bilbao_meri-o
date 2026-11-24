package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.viewmodel.LineaCarritoUi
import com.example.pizzeriaapp.viewmodel.ProductoDetalleUi

private class CampoPedidoEstado(inicial: String = "") {
    var texto by mutableStateOf(inicial)
}

@Composable
private fun CampoPedidoAislado(
    estado: CampoPedidoEstado,
    label: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = estado.texto,
        onValueChange = { estado.texto = it },
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DetallePizzaPanel(
    detalle: ProductoDetalleUi,
    onVolver: () -> Unit,
    onAgregar: (cantidad: String, notas: String) -> Unit
) {
    val cantidad = remember(detalle.clave) { CampoPedidoEstado("1") }
    val notas = remember(detalle.clave) { CampoPedidoEstado("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(detalle.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Precio: $${detalle.precio}")
            if (detalle.promoProducto != null) {
                Text(
                    "Promo: ${detalle.promoProducto}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Contiene: ${detalle.contiene}")
            Text(
                text = detalle.mensajeStock,
                color = if (detalle.stockOk) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.Bold
            )
            AvisosDescuento(detalle.avisosDescuento)
            CampoPedidoAislado(cantidad, "Cantidad (1-100)", detalle.stockOk)
            CampoPedidoAislado(notas, "Notas (opcional)", detalle.stockOk)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Volver")
                }
                Button(
                    onClick = { onAgregar(cantidad.texto, notas.texto) },
                    enabled = detalle.stockOk,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("Agregar") }
            }
        }
    }
}

@Composable
fun BebidasPanel(
    bebidas: List<ProductoDetalleUi>,
    onVolver: () -> Unit,
    onAgregar: (clave: String, cantidad: String, notas: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Bebidas disponibles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        bebidas.forEach { bebida ->
            val cantidad = remember(bebida.clave) { CampoPedidoEstado("1") }
            val notas = remember(bebida.clave) { CampoPedidoEstado("") }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(bebida.nombre, fontWeight = FontWeight.Bold)
                    Text("Precio: $${bebida.precio}")
                    if (bebida.promoProducto != null) {
                        Text(
                            "Promo: ${bebida.promoProducto}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = bebida.mensajeStock,
                        color = if (bebida.stockOk) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    AvisosDescuento(bebida.avisosDescuento)
                    CampoPedidoAislado(cantidad, "Cantidad", bebida.stockOk)
                    CampoPedidoAislado(notas, "Notas (opcional)", bebida.stockOk)
                    Button(
                        onClick = { onAgregar(bebida.clave, cantidad.texto, notas.texto) },
                        enabled = bebida.stockOk,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text("Agregar ${bebida.nombre}") }
                }
            }
        }
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Volver al menú de productos")
        }
    }
}

@Composable
fun ResumenCarritoPanel(
    carrito: List<LineaCarritoUi>,
    onEliminar: (Long) -> Unit,
    onConfirmar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Resumen de tu pedido",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (carrito.isEmpty()) {
            Text("Aún no agregaste productos.")
        } else {
            carrito.forEach { linea ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${linea.nombre} x${linea.cantidad}", fontWeight = FontWeight.Bold)
                        Text("Precio unitario: $${linea.precioUnitario}")
                        Text("Subtotal: $${linea.subtotal}")
                        if (linea.notas.isNotBlank()) {
                            Text("Nota: ${linea.notas}")
                        }
                        TextButton(onClick = { onEliminar(linea.id) }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Text(
                "Total estimado: $${carrito.sumOf { it.subtotal }}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Button(
            onClick = onConfirmar,
            modifier = Modifier.fillMaxWidth(),
            enabled = carrito.isNotEmpty(),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Confirmar pedido")
        }
    }
}
