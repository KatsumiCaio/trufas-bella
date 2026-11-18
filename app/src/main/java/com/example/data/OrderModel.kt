package com.example.data

import com.google.firebase.firestore.DocumentId

data class OrderModel(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val clientName: String = "",
    val flavorQuantities: Map<String, Int> = emptyMap(), // flavorName -> quantity
    val totalPrice: Double = 0.0,
    val unitPriceSimple: Double = 7.0,
    val unitPriceSpecial: Double = 8.0,
    val paymentMethod: String = "PIX",
    val isPaid: Boolean = false,
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: String = "",
    val weekOfMonth: Int = 1,
    val monthKey: String = ""
) {
    val totalQuantity: Int
        get() = flavorQuantities.values.sum()

    val brigadeiro: Int
        get() = flavorQuantities["Brigadeiro"] ?: 0

    val pacoca: Int
        get() = flavorQuantities["Paçoca"] ?: 0

    val ninho: Int
        get() = flavorQuantities["Ninho"] ?: 0

    val ninhoNutella: Int
        get() = flavorQuantities["Ninho c/ Nutella"] ?: 0

    val maracuja: Int
        get() = flavorQuantities["Maracujá"] ?: 0

    val outro: Int
        get() = flavorQuantities["Outro"] ?: 0
}
