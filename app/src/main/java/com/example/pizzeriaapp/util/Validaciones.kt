package com.example.pizzeriaapp.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object Validaciones {
    /** Regex de correo (patrón de la clase Semana 3). */
    val REGEX_CORREO = Regex("""[\w.+-]+@[\w-]+(\.[\w-]+)+""")

    /** Formato de fecha dd/MM/yyyy (clase + PPT). */
    val REGEX_FECHA = Regex("""(\d{2})/(\d{2})/(\d{4})""")

    private val FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Periodo de promociones por rango de fechas. */
    val PROMO_INICIO: LocalDate = LocalDate.of(2026, 11, 1)
    val PROMO_FIN: LocalDate = LocalDate.of(2026, 11, 30)

    fun esCorreoValido(correo: String): Boolean =
        REGEX_CORREO.matches(correo.trim())

    /**
     * Celular Chile: 9 dígitos (ej. 912345678).
     * Reutilizado desde Semana 2.
     */
    fun normalizarTelefono(texto: String): String {
        var digitos = texto.filter { it.isDigit() }
        if (digitos.startsWith("56") && digitos.length >= 11) {
            digitos = digitos.removePrefix("56")
        }
        if (digitos.startsWith("0") && digitos.length >= 9) {
            digitos = digitos.removePrefix("0")
        }
        return digitos
    }

    fun esTelefonoValido(digitos: String): Boolean =
        digitos.length == 9 && digitos.all { it.isDigit() }

    /**
     * Formato uniforme exigido en Semana 3: +XX (XXX) XXX-XXXX
     * Ejemplo Chile: 912345678 → +56 (912) 345-6789
     */
    /**
     * Formato uniforme para celular chileno (9 dígitos).
     * El enunciado S3 pide un estilo uniforme tipo +XX (...);
     * aquí usamos la forma habitual en Chile: +56 9 1234 5678
     * Ejemplo: 973529841 → +56 9 7352 9841
     */
    fun formatearTelefono(digitos: String, codigoPais: String = "56"): String {
        val d = normalizarTelefono(digitos)
        require(esTelefonoValido(d)) { "Teléfono inválido para formatear" }
        // 9 dígitos: 9 + 4 + 4  →  +56 9 7352 9841
        val movil = d.substring(0, 1)
        val bloque1 = d.substring(1, 5)
        val bloque2 = d.substring(5, 9)
        return "+$codigoPais $movil $bloque1 $bloque2"
    }

    /**
     * Valida formato dd/MM/yyyy con Regex y ranges de día/mes/año.
     */
    fun parsearFecha(texto: String): LocalDate? {
        val r = REGEX_FECHA.matchEntire(texto.trim()) ?: return null
        val (d, m, a) = r.destructured
        val dia = d.toInt()
        val mes = m.toInt()
        val anio = a.toInt()
        if (dia !in 1..31 || mes !in 1..12 || anio !in 1900..2100) return null
        return try {
            LocalDate.of(anio, mes, dia)
        } catch (_: Exception) {
            null
        }
    }

    fun formatearFecha(fecha: LocalDate): String = fecha.format(FORMATO_FECHA)

    /** True si la fecha está dentro del periodo de promociones (inclusive). */
    fun estaEnPeriodoPromo(fecha: LocalDate): Boolean =
        !fecha.isBefore(PROMO_INICIO) && !fecha.isAfter(PROMO_FIN)

    fun esCantidadValida(cantidad: Int): Boolean = cantidad in 1..100

    /**
     * Nombre de cliente: no vacío, solo letras/espacios/tildes,
     * sin números ni símbolos, y al menos una letra.
     */
    fun esNombreValido(texto: String): Boolean {
        val t = texto.trim()
        if (t.isEmpty()) return false
        if (!t.any { it.isLetter() }) return false
        return t.all { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }
    }
}
