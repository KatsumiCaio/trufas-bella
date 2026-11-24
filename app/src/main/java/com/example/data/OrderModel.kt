package com.example.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

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
    @get:PropertyName("isPaid")
    @field:PropertyName("isPaid")
    val isPaid: Boolean = false,
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: String = "",
    val weekOfMonth: Int = 1,
    val monthKey: String = ""
) {
    val totalQuantity: Int
        get() = flavorQuantities.values.sum()
}
