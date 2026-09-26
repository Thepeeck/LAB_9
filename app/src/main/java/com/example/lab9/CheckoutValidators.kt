package com.example.lab9

fun validateFullName(value: String): String? {
    val name = value.trim()

    return when {
        name.any { it.isDigit() } ->
            "El nombre no debe contener números."

        name.count { it.isLetter() } < 3 ->
            "Ingrese al menos 3 letras."

        else -> null
    }
}

fun validatePhoneNumber(value: String): String? {
    val phone = value.trim()

    return if (
        phone.length == 8 &&
        phone.all { it in '0'..'9' }
    ) {
        null
    } else {
        "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones."
    }
}

fun validateNit(value: String): String? {
    val nit = value.trim()

    return if (
        nit.length >= 5 &&
        nit.all { it in '0'..'9' }
    ) {
        null
    } else {
        "Ingrese al menos 5 dígitos, sin espacios ni guiones."
    }
}

fun validateBusinessName(value: String): String? {
    val businessName = value.trim()

    return if (businessName.length >= 3) {
        null
    } else {
        "Ingrese al menos 3 caracteres."
    }
}