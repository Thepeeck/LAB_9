package com.example.lab9

data class OrderReceipt (
    val folio: String, val customerName: String, val billingType: BillingType, val paymentMethod: PaymentMethod, val total: Double
)