package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pizzeriaapp.viewmodel.BoletaUi

@Composable
fun BoletaColapsable(
    boleta: BoletaUi,
    expandida: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (expandida) {
                        "${boleta.titulo} · cerrar"
                    } else {
                        "${boleta.titulo} · ver"
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "Atención: te llamaremos por el N° ${boleta.numeroAtencion}. " +
                    "Mantente pendiente de ese número.",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        if (expandida) {
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
                            Text(
                                "Boleta N° ${boleta.pedidoId}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "N° ATENCIÓN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${boleta.numeroAtencion}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                                lineHeight = 44.sp
                            )
                        }
                    }
                    Text("Fecha y hora: ${boleta.fechaHora}")
                    Text("Entrega: ${boleta.tipoEntrega}", fontWeight = FontWeight.SemiBold)
                    Text("Estado: ${boleta.estado}")
                    Text(
                        text = "Repartidor: ${boleta.repartidor}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    Text("Detalle", fontWeight = FontWeight.Bold)
                    boleta.lineas.forEach { linea ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BoletaFila(
                                izquierda = "${linea.nombre}  ×${linea.cantidad}",
                                derecha = "$${linea.subtotal}",
                                negrita = true
                            )
                            Text(
                                "$${linea.precioUnitario} c/u",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (linea.notas.isNotBlank()) {
                                Text(
                                    "Nota: ${linea.notas}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    BoletaFila("Subtotal", "$${boleta.subtotal}")
                    if (boleta.descuentos.isEmpty()) {
                        BoletaFila("Descuentos", "$0")
                    } else {
                        Text("Descuentos", fontWeight = FontWeight.SemiBold)
                        boleta.descuentos.forEach { d ->
                            BoletaFila(
                                izquierda = d.etiqueta,
                                derecha = "-$${d.monto}",
                                colorDerecha = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    HorizontalDivider()

                    BoletaFila("Neto", "$${boleta.baseAntesIva}")
                    BoletaFila("IVA (19%)", "$${boleta.iva}")

                    HorizontalDivider(thickness = 2.dp)

                    BoletaFila(
                        izquierda = "TOTAL A PAGAR",
                        derecha = "$${boleta.total}",
                        negrita = true,
                        colorDerecha = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* futuro: pagar */ },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Pagar")
                        }
                        OutlinedButton(
                            onClick = { /* futuro: enviar boleta al correo */ },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("¿Enviar boleta al correo?")
                        }
                    }
                }
            }
        }
    }
}
