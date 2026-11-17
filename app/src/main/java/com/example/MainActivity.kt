package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.OrderModel
import com.example.ui.screens.AddEditOrderDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FlavorsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ReceiptsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.TrufasBellaTheme
import com.example.ui.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrufasBellaTheme {
                var isUnlocked by remember { mutableStateOf(false) }

                if (!isUnlocked) {
                    AuthScreen(onAuthSuccess = {
                        isUnlocked = true
                    })
                } else {
                    var selectedTab by remember { mutableIntStateOf(0) }
                    var showAddDialog by remember { mutableStateOf(false) }
                    var orderToEdit by remember { mutableStateOf<OrderModel?>(null) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedidos") },
                                    label = { Text("Pedidos") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.Category, contentDescription = "Sabores") },
                                    label = { Text("Sabores") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Recibos") },
                                    label = { Text("Recibos") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Relatórios") },
                                    label = { Text("Relatórios") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (selectedTab) {
                                0 -> OrdersScreen(
                                    viewModel = viewModel,
                                    onAddNewClick = {
                                        orderToEdit = null
                                        showAddDialog = true
                                    },
                                    onEditOrder = { order ->
                                        orderToEdit = order
                                        showAddDialog = true
                                    }
                                )
                                1 -> FlavorsScreen(viewModel = viewModel)
                                2 -> ReceiptsScreen(viewModel = viewModel)
                                3 -> ReportsScreen(viewModel = viewModel)
                            }
                        }

                        if (showAddDialog) {
                            AddEditOrderDialog(
                                orderToEdit = orderToEdit,
                                onDismiss = {
                                    showAddDialog = false
                                    orderToEdit = null
                                },
                                onSave = { clientName, brigadeiro, pacoca, ninho, ninhoNutella, maracuja, outro, unitPriceSimple, unitPriceSpecial, paymentMethod, isPaid, notes ->
                                    val flavorQuantities = mapOf(
                                        "Brigadeiro" to brigadeiro,
                                        "Paçoca" to pacoca,
                                        "Ninho" to ninho,
                                        "Ninho c/ Nutella" to ninhoNutella,
                                        "Maracujá" to maracuja,
                                        "Outro" to outro
                                    ).filterValues { it > 0 }

                                    val totalPrice = ((brigadeiro + pacoca + ninho + maracuja + outro) * unitPriceSimple) + (ninhoNutella * unitPriceSpecial)

                                    if (orderToEdit == null) {
                                        viewModel.insertOrder(
                                            clientName = clientName,
                                            flavorQuantities = flavorQuantities,
                                            totalPrice = totalPrice,
                                            paymentMethod = paymentMethod,
                                            isPaid = isPaid,
                                            notes = notes
                                        )
                                    } else {
                                        val updated = orderToEdit!!.copy(
                                            clientName = clientName.trim().uppercase(),
                                            flavorQuantities = flavorQuantities,
                                            totalPrice = totalPrice,
                                            paymentMethod = paymentMethod,
                                            isPaid = isPaid,
                                            notes = notes
                                        )
                                        viewModel.updateOrder(updated)
                                    }
                                    showAddDialog = false
                                    orderToEdit = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
