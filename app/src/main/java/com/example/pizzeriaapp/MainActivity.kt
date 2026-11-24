package com.example.pizzeriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.pizzeriaapp.ui.screens.ResumenScreen
import com.example.pizzeriaapp.ui.theme.PizzeriaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sin enableEdgeToEdge: evita relayout completo en cada tecla
        // cuando el IME cambia insets con adjustResize.
        setContent {
            PizzeriaAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ResumenScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
