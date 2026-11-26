package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale
import com.example.data.OrderModel
import com.example.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentOrdersScreen(
    viewModel: OrderViewModel,
    onAddNewClick: () -> Unit,
    onEditOrder: (OrderModel) -> Unit
) {
    val orders by viewModel.allOrders.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val currentWeekNum = remember {
        Calendar.getInstance(Locale("pt", "BR")).get(Calendar.WEEK_OF_MONTH)
    }
    
    var selectedWeekFilter by remember { mutableStateOf("Semana $currentWeekNum") } // Default to current week of month
    val context = LocalContext.current

    val filteredOrders = orders.filter { order ->
        val matchesSearch = order.clientName.contains(searchQuery, ignoreCase = true) ||
                order.notes.contains(searchQuery, ignoreCase = true) ||
                order.flavorQuantities.keys.any { it.contains(searchQuery, ignoreCase = true) }

        val matchesWeek = when (selectedWeekFilter) {
            "Semana 1" -> order.weekOfMonth == 1
            "Semana 2" -> order.weekOfMonth == 2
            "Semana 3" -> order.weekOfMonth == 3
            "Semana 4" -> order.weekOfMonth == 4
            "Semana 5" -> order.weekOfMonth == 5
            else -> true // "Todas"
        }

        matchesSearch && matchesWeek
    }

    val totalRevenue = filteredOrders.sumOf { it.totalPrice }
    val totalTruffles = filteredOrders.sumOf { it.totalQuantity }
    val pendingCount = filteredOrders.count { !it.isPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Encomendas Atuais", fontWeight = FontWeight.Bold)
                        Text("Semana Atual (Semana $currentWeekNum)", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    if (orders.isEmpty()) {
                        IconButton(onClick = { viewModel.seedInitialData() }) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Carregar Dados de Exemplo")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Encomenda")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Vendas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Text("R$ %.2f".format(totalRevenue), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier
                        .height(32.dp)
                        .width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Trufas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Text("$totalTruffles und", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier
                        .height(32.dp)
                        .width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pendentes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Text("$pendingCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (pendingCount > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar cliente, sabor ou obs...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Semana 1", "Semana 2", "Semana 3", "Semana 4", "Todas").forEach { filter ->
                        FilterChip(
                            selected = selectedWeekFilter == filter,
                            onClick = { selectedWeekFilter = filter },
                            label = { Text(filter) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhuma encomenda para $selectedWeekFilter", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                        if (orders.isEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.seedInitialData() }) {
                                Text("Carregar Dados de Exemplo")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        OrderItemCard(
                            order = order,
                            onTogglePaid = {
                                val newIsPaid = !order.isPaid
                                val updated = order.copy(
                                    isPaid = newIsPaid,
                                    paymentMethod = if (newIsPaid) "PIX" else "Pendente"
                                )
                                viewModel.updateOrder(updated) {
                                    Toast.makeText(
                                        context,
                                        if (newIsPaid) "Pedido marcado como PAGO!" else "Pedido marcado como PENDENTE",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onEdit = { onEditOrder(order) },
                            onDelete = { viewModel.deleteOrder(order) },
                            onWhatsAppShare = {
                                shareOnWhatsApp(context, order)
                            }
                        )
                    }
                }
            }
        }
    }
}
