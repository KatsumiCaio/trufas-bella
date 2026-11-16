package com.example.data

import com.google.firebase.firestore.DocumentId

data class FlavorModel(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val price: Double = 7.0,
    val isSpecial: Boolean = false
)
