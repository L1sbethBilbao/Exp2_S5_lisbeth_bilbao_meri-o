package com.example.pizzeriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.PizzaPepperoni
import com.example.pizzeriaapp.ui.theme.PizzeriaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizzeriaAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaInicio(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaInicio(modifier: Modifier = Modifier) {
    // Ejemplo usando la lógica de semanas anteriores (Producto, Pedido, Usuario/Cliente)
    val cliente = Cliente(
        nombre = "Ana Pérez",
        contacto = "912345678",
        telefono = "912345678",
        correo = "ana@duoc.cl",
        direccion = "Av. Siempre Viva 123"
    )
    val pizza = PizzaPepperoni()
    val pedido = Pedido(
        id = 1,
        cliente = cliente,
        productos = listOf(pizza),
        cantidad = 1
    )
    val totales = pedido.calcularTotales()

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Bienvenidos a la mejor pizzería de la ciudad")
        Text(
            text = "Cliente: ${cliente.nombre}",
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(text = "Producto: ${pizza.nombre} ($${pizza.precio.toInt()})")
        Text(text = "Estado pedido: ${pedido.estado}")
        Text(text = "Total estimado: $${totales.total.toInt()}")
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaInicioPreview() {
    PizzeriaAppTheme {
        PantallaInicio()
    }
}
