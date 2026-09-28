
package com.example.lab9

enum class BillingType {
    CF,
    NIT
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    BANK_TRANSFER
}

data class CheckoutUiState(
    val fullName: String = "",
    val phone: String = "",
    val nit: String = "",
    val businessName: String = "",

    val billingType: BillingType = BillingType.CF,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,

    val isFullNameTouched: Boolean = false,
    val isPhoneTouched: Boolean = false,
    val isNitTouched: Boolean = false,
    val isBusinessNameTouched: Boolean = false
) {
    // Errores visibles: solo aparecen después de editar el campo.

    val fullNameError: String?
        get() = if (isFullNameTouched) {
            validateFullName(fullName)
        } else {
            null
        }

    val phoneError: String?
        get() = if (isPhoneTouched) {
            validatePhoneNumber(phone)
        } else {
            null
        }

    val nitError: String?
        get() = if (billingType == BillingType.NIT && isNitTouched) {
            validateNit(nit)
        } else {
            null
        }

    val businessNameError: String?
        get() = if (
            billingType == BillingType.NIT &&
            isBusinessNameTouched
        ) {
            validateBusinessName(businessName)
        } else {
            null
        }

    // La validez se calcula incluso si los errores están ocultos.

    val isFormValid: Boolean
        get() {
            val contactIsValid =
                validateFullName(fullName) == null &&
                        validatePhoneNumber(phone) == null

            val billingIsValid =
                billingType == BillingType.CF ||
                        (
                                validateNit(nit) == null &&
                                        validateBusinessName(businessName) == null
                                )

            return contactIsValid && billingIsValid
        }
}