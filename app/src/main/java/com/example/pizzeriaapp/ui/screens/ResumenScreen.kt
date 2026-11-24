package com.example.pizzeriaapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzeriaapp.ui.theme.PizzeriaAppTheme
import com.example.pizzeriaapp.viewmodel.PizzeriaViewModel

/**
 * Router principal: elige cliente / login / trabajador.
 * El estado de formularios vive en cada pantalla (mejor rendimiento al escribir).
 */
@Composable
fun ResumenScreen(
    modifier: Modifier = Modifier,
    viewModel: PizzeriaViewModel = viewModel()
) {
    var modo by remember { mutableStateOf("cliente") }

    when (modo) {
        "login" -> PantallaLoginTrabajador(
            modifier = modifier,
            onEntrarOk = { modo = "trabajador" },
            onVolver = { modo = "cliente" }
        )

        "trabajador" -> TrabajadorPanelScreen(
            modifier = modifier,
            viewModel = viewModel,
            onCerrarSesion = { modo = "cliente" }
        )

        else -> ClientePanelScreen(
            modifier = modifier,
            viewModel = viewModel,
            onSoyTrabajador = { modo = "login" }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResumenScreenPreview() {
    PizzeriaAppTheme {
        ResumenScreen()
    }
}
