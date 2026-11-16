package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.OrderEntity
import com.example.data.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OrderRepository

    val allOrders: StateFlow<List<OrderEntity>>

    init {
        val orderDao = AppDatabase.getDatabase(application).orderDao()
        repository = OrderRepository(orderDao)
        allOrders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insertOrder(
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
        notes: String
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val formattedDate = dateFormat.format(Date())
            val order = OrderEntity(
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
                createdAt = formattedDate
            )
            repository.insert(order)
        }
    }

    fun updateOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.update(order)
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.delete(order)
        }
    }

    fun seedInitialData() {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val today = dateFormat.format(Date())
            val sample = listOf(
                OrderEntity(clientName = "BRUNO", brigadeiro = 3, ninho = 2, outro = 1, paymentMethod = "PIX", isPaid = true, notes = "Pagou via PIX", createdAt = today),
                OrderEntity(clientName = "DOUGLAS", pacoca = 4, ninhoNutella = 2, maracuja = 3, paymentMethod = "Dinheiro", isPaid = true, notes = "Entregue no trabalho", createdAt = today),
                OrderEntity(clientName = "MARIANA", brigadeiro = 2, ninho = 4, paymentMethod = "Pendente", isPaid = false, notes = "Vai pagar semana que vem", createdAt = today),
                OrderEntity(clientName = "CAMILA", ninhoNutella = 3, pacoca = 2, paymentMethod = "PIX", isPaid = true, notes = "Pix ok", createdAt = today)
            )
            sample.forEach { repository.insert(it) }
        }
    }
}
