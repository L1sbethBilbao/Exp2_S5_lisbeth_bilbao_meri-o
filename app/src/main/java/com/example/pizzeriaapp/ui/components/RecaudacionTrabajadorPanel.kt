package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PanelRecaudacionDia(
    recaudacionHoy: Int,
    cerrada: Boolean,
    fechaCierre: String?,
    recaudacionMes: Int,
    mesEtiqueta: String,
    fechaHoyEtiqueta: String,
    onConfirmarCierre: () -> Unit
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
            Text(
                "PIZZERÍA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Recaudación del día", fontWeight = FontWeight.Bold)
            Text(
                text = fechaHoyEtiqueta,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            Text("Monto del día (en vivo)", fontWeight = FontWeight.SemiBold)
            Text(
                text = "$$recaudacionHoy",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (cerrada) {
                    "Día CERRADO · confirmado $fechaCierre"
                } else {
                    "Día ABIERTO · se actualiza con cada venta"
                },
                color = if (cerrada) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider()

            Text("Acumulado del mes ($mesEtiqueta)", fontWeight = FontWeight.SemiBold)
            Text(
                text = "$$recaudacionMes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Incluye días cerrados del mes" +
                    if (!cerrada) " + lo recaudado hoy (aún abierto)." else ".",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!cerrada) {
                Button(
                    onClick = onConfirmarCierre,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Confirmar recaudación del día y cerrar")
                }
            }
        }
    }
}
