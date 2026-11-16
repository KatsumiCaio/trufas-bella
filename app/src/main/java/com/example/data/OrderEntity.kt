package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val clientName: String,
    val brigadeiro: Int = 0,
    val pacoca: Int = 0,
    val ninho: Int = 0,
    val ninhoNutella: Int = 0,
    val maracuja: Int = 0,
    val outro: Int = 0,
    val unitPriceSimple: Double = 7.0,
    val unitPriceSpecial: Double = 8.0,
    val paymentMethod: String = "PIX", // PIX, Dinheiro, Pendente
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
