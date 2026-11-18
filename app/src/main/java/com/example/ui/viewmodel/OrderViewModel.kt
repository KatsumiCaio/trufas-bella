package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FlavorModel
import com.example.data.FirestoreRepository
import com.example.data.OrderModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirestoreRepository(application)

    val allOrders: StateFlow<List<OrderModel>> = repository.observeOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allFlavors: StateFlow<List<FlavorModel>> = repository.observeFlavors()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertOrder(
        clientName: String,
        flavorQuantities: Map<String, Int>,
        totalPrice: Double,
        paymentMethod: String,
        isPaid: Boolean,
        notes: String,
        weekOfMonth: Int
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val formattedDate = dateFormat.format(Date())
            repository.addOrder(
                clientName = clientName,
                flavorQuantities = flavorQuantities,
                totalPrice = totalPrice,
                paymentMethod = paymentMethod,
                isPaid = isPaid,
                notes = notes,
                createdAt = formattedDate,
                weekOfMonth = weekOfMonth
            )
        }
    }

    fun updateOrder(order: OrderModel, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateOrder(order)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteOrder(order: OrderModel) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    fun addFlavor(name: String, price: Double, isSpecial: Boolean) {
        viewModelScope.launch {
            repository.addFlavor(name, price, isSpecial)
        }
    }

    fun updateFlavor(flavor: FlavorModel) {
        viewModelScope.launch {
            repository.updateFlavor(flavor)
        }
    }

    fun deleteFlavor(flavor: FlavorModel) {
        viewModelScope.launch {
            repository.deleteFlavor(flavor)
        }
    }

    fun seedDefaultFlavors() {
        viewModelScope.launch {
            repository.seedDefaultFlavors()
        }
    }

    fun seedInitialData() {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val today = dateFormat.format(Date())
            val sample = listOf(
                OrderModel(clientName = "BRUNO", flavorQuantities = mapOf("Brigadeiro" to 3, "Ninho" to 2, "Outro" to 1), totalPrice = 42.0, paymentMethod = "PIX", isPaid = true, notes = "Pagou via PIX", createdAt = today, weekOfMonth = 1),
                OrderModel(clientName = "DOUGLAS", flavorQuantities = mapOf("Paçoca" to 4, "Ninho c/ Nutella" to 2, "Maracujá" to 3), totalPrice = 65.0, paymentMethod = "Dinheiro", isPaid = true, notes = "Entregue no trabalho", createdAt = today, weekOfMonth = 1),
                OrderModel(clientName = "MARIANA", flavorQuantities = mapOf("Brigadeiro" to 2, "Ninho" to 4), totalPrice = 42.0, paymentMethod = "Pendente", isPaid = false, notes = "Vai pagar semana que vem", createdAt = today, weekOfMonth = 2),
                OrderModel(clientName = "CAMILA", flavorQuantities = mapOf("Ninho c/ Nutella" to 3, "Paçoca" to 2), totalPrice = 38.0, paymentMethod = "PIX", isPaid = true, notes = "Pix ok", createdAt = today, weekOfMonth = 2)
            )
            sample.forEach {
                repository.addOrder(
                    clientName = it.clientName,
                    flavorQuantities = it.flavorQuantities,
                    totalPrice = it.totalPrice,
                    paymentMethod = it.paymentMethod,
                    isPaid = it.isPaid,
                    notes = it.notes,
                    createdAt = it.createdAt,
                    weekOfMonth = it.weekOfMonth
                )
            }
        }
    }
}
