package com.example.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.util.Validaciones

/**
 * Holder fuera de composición: el padre NO lee .texto al componer,
 * así cada tecla solo invalida UN campo (no los 4 OutlinedTextField).
 */
private class CampoTextoEstado {
    var texto by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
}

/**
 * Un solo TextField con estado propio → recomposición mínima por tecla.
 */
@Composable
private fun CampoTextoAislado(
    estado: CampoTextoEstado,
    label: String,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = estado.texto,
        onValueChange = {
            estado.texto = it
            if (estado.error != null) estado.error = null
        },
        label = { Text(label) },
        isError = estado.error != null,
        supportingText = estado.error?.let { msg ->
            { Text(msg) }
        },
        singleLine = singleLine,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Formulario de registro optimizado para teclado.
 * Estado por campo + el padre no observa el texto en composition.
 */
@Composable
fun FormularioCliente(
    resetKey: Int,
    onRegistrar: (nombre: String, telefono: String, correo: String, direccion: String) -> Unit
) {
    val nombre = remember(resetKey) { CampoTextoEstado() }
    val telefono = remember(resetKey) { CampoTextoEstado() }
    val correo = remember(resetKey) { CampoTextoEstado() }
    val direccion = remember(resetKey) { CampoTextoEstado() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CampoTextoAislado(nombre, "Nombre")
        CampoTextoAislado(telefono, "Teléfono (9 dígitos)")
        CampoTextoAislado(correo, "Correo (ej: nombre@gmail.com)")
        CampoTextoAislado(direccion, "Dirección", singleLine = false)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    nombre.error = null
                    telefono.error = null
                    correo.error = null
                    direccion.error = null

                    val nombreOk = nombre.texto.trim()
                    val correoOk = correo.texto.trim()
                    val telefonoNorm = Validaciones.normalizarTelefono(telefono.texto)
                    val direccionOk = direccion.texto.trim()
                    when {
                        !Validaciones.esNombreValido(nombreOk) ->
                            nombre.error = "Solo letras (sin números ni símbolos)"
                        !Validaciones.esTelefonoValido(telefonoNorm) ->
                            telefono.error = "Debe tener 9 dígitos (solo números)"
                        !Validaciones.esCorreoValido(correoOk) ->
                            correo.error = "Usa un correo con @ (ej: lisbeth@gmail.com)"
                        direccionOk.isBlank() ->
                            direccion.error = "Completa la dirección"
                        else -> onRegistrar(nombreOk, telefonoNorm, correoOk, direccionOk)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Registrar") }
            OutlinedButton(
                onClick = {
                    nombre.texto = ""
                    telefono.texto = ""
                    correo.texto = ""
                    direccion.texto = ""
                    nombre.error = null
                    telefono.error = null
                    correo.error = null
                    direccion.error = null
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small
            ) { Text("Limpiar") }
        }
    }
}
