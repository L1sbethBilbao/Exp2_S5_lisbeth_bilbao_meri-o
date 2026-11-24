package com.example.pizzeriaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.util.AppConstantes

private class LoginCampoEstado {
    var texto by mutableStateOf("")
}

@Composable
private fun CampoLogin(
    estado: LoginCampoEstado,
    label: String,
    isError: Boolean,
    password: Boolean = false,
    supporting: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = estado.texto,
        onValueChange = { estado.texto = it },
        label = { Text(label) },
        isError = isError,
        visualTransformation = if (password) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        supportingText = supporting,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PantallaLoginTrabajador(
    modifier: Modifier = Modifier,
    onEntrarOk: () -> Unit,
    onVolver: () -> Unit
) {
    val usuario = remember { LoginCampoEstado() }
    val clave = remember { LoginCampoEstado() }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Acceso trabajador",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Ingresa tu usuario y clave")
        CampoLogin(
            estado = usuario,
            label = "Usuario",
            isError = error != null
        )
        CampoLogin(
            estado = clave,
            label = "Clave",
            isError = error != null,
            password = true,
            supporting = error?.let { msg ->
                {
                    Text(msg, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Button(
            onClick = {
                if (usuario.texto.trim().equals(AppConstantes.USUARIO_TRABAJADOR, ignoreCase = true) &&
                    clave.texto == AppConstantes.CLAVE_TRABAJADOR
                ) {
                    onEntrarOk()
                } else {
                    error = "Usuario o clave incorrectos"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Entrar")
        }
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Volver")
        }
        Text(
            text = "Prueba: usuario \"${AppConstantes.USUARIO_TRABAJADOR}\" / " +
                "clave \"${AppConstantes.CLAVE_TRABAJADOR}\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
