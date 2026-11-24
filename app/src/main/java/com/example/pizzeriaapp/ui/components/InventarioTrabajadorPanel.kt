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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.util.AppConstantes
import com.example.pizzeriaapp.viewmodel.IngredienteStockUi

@Composable
fun ListaInventarioFijo(
    titulo: String,
    items: List<IngredienteStockUi>
) {
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
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(
                "Este inventario no cambia durante el día.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (items.isEmpty()) {
                Text("Sin datos")
            } else {
                items.forEach { item ->
                    Text("• ${item.nombre}: ${item.cantidad}")
                }
            }
        }
    }
}

@Composable
fun ListaInventarioActual(
    titulo: String,
    items: List<IngredienteStockUi>,
    onPedir: (nombre: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(
                "Se actualiza al preparar pedidos y al pedir reposición. " +
                    "Umbral de aviso: ${AppConstantes.UMBRAL_STOCK_BAJO} o menos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (items.isEmpty()) {
                Text("Sin datos")
            } else {
                items.forEach { item ->
                    FilaIngredienteActual(
                        item = item,
                        onPedir = { onPedir(item.nombre) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaIngredienteActual(
    item: IngredienteStockUi,
    onPedir: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "• ${item.nombre}: ${item.cantidad}" +
                if (item.esBajo) "  (bajo)" else "",
            modifier = Modifier.weight(1f),
            color = if (item.esBajo) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (item.esBajo) FontWeight.Bold else FontWeight.Normal
        )
        if (item.esBajo) {
            Button(
                onClick = onPedir,
                shape = MaterialTheme.shapes.small,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Pedir")
            }
        }
    }
}

@Composable
fun BotonCierreDiaInventario(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Text("Terminar día y confirmar inventario final")
    }
}
