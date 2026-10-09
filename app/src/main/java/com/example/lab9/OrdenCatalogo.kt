package com.example.lab9

fun ordenarInstrumentos(
    instrumentos: List<Instrumento>,
    orden: String
): List<Instrumento> {
    return when (orden) {
        "price" -> instrumentos.sortedBy { it.precio }
        "name" -> instrumentos.sortedBy { it.nombre.lowercase() }
        else -> instrumentos
    }
}