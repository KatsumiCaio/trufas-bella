package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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

    // --- Flavors Management ---
    fun observeFlavors(): Flow<List<FlavorModel>> = callbackFlow {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            trySend(emptyList())
            close(e)
            return@callbackFlow
        }

        val ref = db.collection("users").document(uid).collection("flavors")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(FlavorModel::class.java)
                if (list.isEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultFlavors(uid)
                    }
                } else {
                    trySend(list)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    private suspend fun seedDefaultFlavors(uid: String) {
        val defaults = listOf(
            FlavorModel(name = "Brigadeiro", price = 7.0, isSpecial = false),
            FlavorModel(name = "Paçoca", price = 7.0, isSpecial = false),
            FlavorModel(name = "Ninho", price = 7.0, isSpecial = false),
            FlavorModel(name = "Ninho c/ Nutella", price = 8.0, isSpecial = true),
            FlavorModel(name = "Maracujá", price = 7.0, isSpecial = false),
            FlavorModel(name = "Outro", price = 7.0, isSpecial = false)
        )
        val batch = db.batch()
        val colRef = db.collection("users").document(uid).collection("flavors")
        defaults.forEach { flavor ->
            val docRef = colRef.document()
            batch.set(docRef, flavor.copy(id = docRef.id, userId = uid))
        }
        batch.commit().await()
    }

    suspend fun updateFlavor(flavor: FlavorModel) {
        val uid = requireUserId()
        require(flavor.id.isNotBlank())
        db.collection("users").document(uid).collection("flavors")
            .document(flavor.id)
            .set(flavor.copy(userId = uid))
            .await()
    }

    suspend fun addFlavor(name: String, price: Double, isSpecial: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("flavors").document()
        val flavor = FlavorModel(
            id = docRef.id,
            userId = uid,
            name = name.trim(),
            price = price,
            isSpecial = isSpecial
        )
        docRef.set(flavor).await()
    }

    suspend fun deleteFlavor(flavor: FlavorModel) {
        val uid = requireUserId()
        require(flavor.id.isNotBlank())
        db.collection("users").document(uid).collection("flavors")
            .document(flavor.id)
            .delete()
            .await()
    }
}
