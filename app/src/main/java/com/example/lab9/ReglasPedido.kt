package com.example.lab9

sealed interface ResultadoPedido {
    data class Exito(val pedido: List<LineaPedido>) : ResultadoPedido
    data class Rechazado(val motivo: String) : ResultadoPedido
}

fun agregarAlPedido(
    pedido: List<LineaPedido>,
    instrumento: Instrumento,
    cantidad: Int = 1
): ResultadoPedido {
    if (cantidad <= 0) {
        return ResultadoPedido.Rechazado("La cantidad debe ser mayor a cero.")
    }

    val lineaActual = pedido.find { it.instrumentoId == instrumento.id }
    val cantidadActual = lineaActual?.cantidad ?: 0
    val nuevaCantidad = cantidadActual + cantidad

    if (nuevaCantidad > instrumento.stock) {
        return ResultadoPedido.Rechazado(
            "Solo hay ${instrumento.stock} unidades disponibles."
        )
    }

    val nuevoPedido = if (lineaActual != null) {
        pedido.map {
            if (it.instrumentoId == instrumento.id) {
                it.copy(cantidad = nuevaCantidad)
            } else {
                it
            }
        }
    } else {
        pedido + LineaPedido(instrumento.id, cantidad)
    }

    return ResultadoPedido.Exito(nuevoPedido)
} // <-- ESTE cierre le faltaba a agregarAlPedido

fun disminuirEnPedido(
    pedido: List<LineaPedido>,
    instrumentoId: String
): List<LineaPedido> {
    val lineaActual = pedido.find { it.instrumentoId == instrumentoId }
        ?: return pedido

    val nuevaCantidad = lineaActual.cantidad - 1

    return if (nuevaCantidad <= 0) {
        pedido.filterNot { it.instrumentoId == instrumentoId }
    } else {
        pedido.map {
            if (it.instrumentoId == instrumentoId) {
                it.copy(cantidad = nuevaCantidad)
            } else {
                it
            }
        }
    }
}

fun eliminarDelPedido(
    pedido: List<LineaPedido>,
    instrumentoId: String
): List<LineaPedido> {
    return pedido.filterNot { it.instrumentoId == instrumentoId }
}

fun calcularSubtotal(linea: LineaPedido, instrumentos: List<Instrumento>): Double {
    val instrumento = instrumentos.find { it.id == linea.instrumentoId }
        ?: return 0.0
    return instrumento.precio * linea.cantidad
}

fun calcularTotal(pedido: List<LineaPedido>, instrumentos: List<Instrumento>): Double {
    return pedido.sumOf { linea ->
        calcularSubtotal(linea, instrumentos)
    }
}