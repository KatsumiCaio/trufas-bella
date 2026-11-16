package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val context: Context) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )
    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    fun observeOrders(): Flow<List<OrderModel>> = callbackFlow {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            trySend(emptyList())
            close(e)
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid).collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.toObjects(OrderModel::class.java)
                    trySend(orders)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun addOrder(
        clientName: String,
        brigadeiro: Int,
        pacoca: Int,
        ninho: Int,
        ninhoNutella: Int,
        maracuja: Int,
        outro: Int,
        unitPriceSimple: Double,
        unitPriceSpecial: Double,
        paymentMethod: String,
        isPaid: Boolean,
        notes: String,
        createdAt: String
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("orders").document()
        val order = OrderModel(
            id = docRef.id,
            userId = uid,
            clientName = clientName.trim().uppercase(),
            brigadeiro = brigadeiro,
            pacoca = pacoca,
            ninho = ninho,
            ninhoNutella = ninhoNutella,
            maracuja = maracuja,
            outro = outro,
            unitPriceSimple = unitPriceSimple,
            unitPriceSpecial = unitPriceSpecial,
            paymentMethod = paymentMethod,
            isPaid = isPaid,
            notes = notes,
            dateMillis = System.currentTimeMillis(),
            createdAt = createdAt
        )
        docRef.set(order).await()
    }

    suspend fun updateOrder(order: OrderModel) {
        val uid = requireUserId()
        require(order.id.isNotBlank())
        val docRef = db.collection("users").document(uid).collection("orders").document(order.id)
        docRef.set(order.copy(userId = uid)).await()
    }

    suspend fun deleteOrder(order: OrderModel) {
        val uid = requireUserId()
        require(order.id.isNotBlank())
        val docRef = db.collection("users").document(uid).collection("orders").document(order.id)
        docRef.delete().await()
    }
}
