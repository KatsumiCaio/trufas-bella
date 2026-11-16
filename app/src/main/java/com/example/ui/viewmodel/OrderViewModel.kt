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
            repository.addOrder(
                clientName = clientName,
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
                createdAt = formattedDate
            )
        }
    }

    fun updateOrder(order: OrderModel) {
        viewModelScope.launch {
            repository.updateOrder(order)
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

    fun seedInitialData() {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val today = dateFormat.format(Date())
            val sample = listOf(
                OrderModel(clientName = "BRUNO", brigadeiro = 3, ninho = 2, outro = 1, paymentMethod = "PIX", isPaid = true, notes = "Pagou via PIX", createdAt = today),
                OrderModel(clientName = "DOUGLAS", pacoca = 4, ninhoNutella = 2, maracuja = 3, paymentMethod = "Dinheiro", isPaid = true, notes = "Entregue no trabalho", createdAt = today),
                OrderModel(clientName = "MARIANA", brigadeiro = 2, ninho = 4, paymentMethod = "Pendente", isPaid = false, notes = "Vai pagar semana que vem", createdAt = today),
                OrderModel(clientName = "CAMILA", ninhoNutella = 3, pacoca = 2, paymentMethod = "PIX", isPaid = true, notes = "Pix ok", createdAt = today)
            )
            sample.forEach {
                repository.addOrder(
                    clientName = it.clientName,
                    brigadeiro = it.brigadeiro,
                    pacoca = it.pacoca,
                    ninho = it.ninho,
                    ninhoNutella = it.ninhoNutella,
                    maracuja = it.maracuja,
                    outro = it.outro,
                    unitPriceSimple = it.unitPriceSimple,
                    unitPriceSpecial = it.unitPriceSpecial,
                    paymentMethod = it.paymentMethod,
                    isPaid = it.isPaid,
                    notes = it.notes,
                    createdAt = it.createdAt
                )
            }
        }
    }
}
