package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Campos del reclamo con estado LOCAL para no trabar el teclado.
 */
@Composable
fun FormularioReclamo(
    onRevisar: (comoRecibio: String, estadoProducto: String) -> Unit
) {
    var comoRecibio by remember { mutableStateOf("") }
    var estadoProducto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Reclamo del producto", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = comoRecibio,
                onValueChange = {
                    comoRecibio = it
                    error = null
                },
                label = { Text("¿Cómo recibiste el producto?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = MaterialTheme.shapes.small
            )
            OutlinedTextField(
                value = estadoProducto,
                onValueChange = {
                    estadoProducto = it
                    error = null
                },
                label = { Text("Estado del producto al recibirlo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = MaterialTheme.shapes.small
            )
            if (error != null) {
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = {
                    if (comoRecibio.isBlank() || estadoProducto.isBlank()) {
                        error = "Completa ambos campos del reclamo"
                    } else {
                        onRevisar(comoRecibio, estadoProducto)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Revisar reclamo")
            }
        }
    }
}
