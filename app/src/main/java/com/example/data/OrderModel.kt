package com.example.data

import com.google.firebase.firestore.DocumentId

data class OrderModel(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val clientName: String = "",
    val brigadeiro: Int = 0,
    val pacoca: Int = 0,
    val ninho: Int = 0,
    val ninhoNutella: Int = 0,
    val maracuja: Int = 0,
    val outro: Int = 0,
    val unitPriceSimple: Double = 7.0,
    val unitPriceSpecial: Double = 8.0,
    val paymentMethod: String = "PIX",
    val isPaid: Boolean = false,
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: String = ""
) {
    val totalQuantity: Int
        get() = brigadeiro + pacoca + ninho + ninhoNutella + maracuja + outro

    val totalPrice: Double
        get() {
            val simpleCount = brigadeiro + pacoca + ninho + maracuja + outro
            val specialCount = ninhoNutella
            return (simpleCount * unitPriceSimple) + (specialCount * unitPriceSpecial)
        }
}
