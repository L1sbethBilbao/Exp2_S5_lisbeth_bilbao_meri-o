package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Sección del flujo: al completarse se comprime y el título+check
 * se muestran dentro de un botón. Tocando se puede abrir o cerrar.
 */
@Composable
fun SeccionColapsable(
    titulo: String,
    completada: Boolean,
    expandida: Boolean,
    onTituloClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (completada) {
            Button(
                onClick = onTituloClick,
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
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sección lista",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTituloClick)
            )
        }
        if (expandida) {
            content()
        }
    }
}

/** Casilla con borde rojo y texto cuando hay error de validación. */
@Composable
fun CampoConError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { msg -> { Text(msg) } },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BotonProductoMenu(
    titulo: String,
    promo: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo)
            if (promo != null) {
                Text(promo, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun AvisosDescuento(avisos: List<String>) {
    if (avisos.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Descuentos", fontWeight = FontWeight.Bold)
            avisos.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun BoletaFila(
    izquierda: String,
    derecha: String,
    negrita: Boolean = false,
    colorDerecha: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = izquierda,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = derecha,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            color = colorDerecha
        )
    }
}
