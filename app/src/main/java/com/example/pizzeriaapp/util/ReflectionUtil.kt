package com.example.pizzeriaapp.util

/**
 * Lista propiedades y métodos de una clase usando reflection (Semana 3).
 * Adaptado a Android con reflection de Java.
 */
object ReflectionUtil {

    fun listarMiembros(clase: Class<*>): String {
        val sb = StringBuilder()
        sb.appendLine("Clase: ${clase.simpleName}")
        sb.appendLine("  Campos:")
        val fields = clase.declaredFields.sortedBy { it.name }
        if (fields.isEmpty()) {
            sb.appendLine("    (ninguno)")
        } else {
            for (f in fields) {
                sb.appendLine("    - ${f.name}: ${f.type.simpleName}")
            }
        }
        sb.appendLine("  Métodos:")
        val methods = clase.declaredMethods.sortedBy { it.name }
        if (methods.isEmpty()) {
            sb.appendLine("    (ninguno declarado)")
        } else {
            for (m in methods) {
                val params = m.parameterTypes.joinToString(", ") { it.simpleName }
                sb.appendLine("    - ${m.name}($params): ${m.returnType.simpleName}")
            }
        }
        return sb.toString().trimEnd()
    }
}
