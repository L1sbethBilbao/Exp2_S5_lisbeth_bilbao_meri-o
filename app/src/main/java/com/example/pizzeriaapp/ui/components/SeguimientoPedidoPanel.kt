package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pizzeriaapp.util.AppConstantes
import kotlinx.coroutines.delay

/**
 * Contador aislado debajo de la boleta.
 * [claveFase] cambia entre preparación / entrega para reiniciar el timer.
 */
@Composable
fun ContadorSeguimientoPanel(
    claveFase: String,
    activo: Boolean,
    titulo: String,
    mensaje: String,
    onTermino: () -> Unit
) {
    val segundosEspera = AppConstantes.SEGUNDOS_ESPERA_PEDIDO
    var segundosRestantes by remember(claveFase) { mutableIntStateOf(segundosEspera) }
    val onTerminoActual by rememberUpdatedState(onTermino)

    LaunchedEffect(claveFase, activo) {
        if (!activo) return@LaunchedEffect
        segundosRestantes = segundosEspera
        for (s in segundosEspera downTo 1) {
            segundosRestantes = s
            delay(1000)
        }
        segundosRestantes = 0
        onTerminoActual()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = mensaje,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "⏳",
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (segundosRestantes > 0) {
                    "$segundosRestantes s"
                } else {
                    "Listo"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
