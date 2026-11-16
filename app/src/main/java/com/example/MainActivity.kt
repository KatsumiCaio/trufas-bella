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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.OrderModel
import com.example.ui.screens.AddEditOrderDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ReceiptsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.TrufasBellaTheme
import com.example.ui.viewmodel.OrderViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrufasBellaTheme {
                var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                    }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(listener)
                    }
                }

                if (currentUser == null) {
                    AuthScreen(onAuthSuccess = {
                        currentUser = Firebase.auth.currentUser
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
                                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Recibos") },
                                    label = { Text("Recibos") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
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
                                1 -> ReceiptsScreen(viewModel = viewModel)
                                2 -> ReportsScreen(viewModel = viewModel)
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
                                    if (orderToEdit == null) {
                                        viewModel.insertOrder(
                                            clientName, brigadeiro, pacoca, ninho, ninhoNutella, maracuja, outro,
                                            unitPriceSimple, unitPriceSpecial, paymentMethod, isPaid, notes
                                        )
                                    } else {
                                        val updated = orderToEdit!!.copy(
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
