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

fun isCurrentWeek(dateMillis: Long): Boolean {
    val calOrder = Calendar.getInstance(Locale("pt", "BR")).apply { timeInMillis = dateMillis }
    val calNow = Calendar.getInstance(Locale("pt", "BR"))
    return calOrder.get(Calendar.WEEK_OF_YEAR) == calNow.get(Calendar.WEEK_OF_YEAR) &&
           calOrder.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
}

fun isCurrentMonth(dateMillis: Long): Boolean {
    val calOrder = Calendar.getInstance(Locale("pt", "BR")).apply { timeInMillis = dateMillis }
    val calNow = Calendar.getInstance(Locale("pt", "BR"))
    return calOrder.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) &&
           calOrder.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrderViewModel,
    onAddNewClick: () -> Unit,
    onEditOrder: (OrderModel) -> Unit
) {
    val orders by viewModel.allOrders.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("Esta Semana") } // "Esta Semana", "Este Mês", "Todas"
    var selectedStatus by remember { mutableStateOf("Todos") } // "Todos", "Pendentes", "Pagos"
    val context = LocalContext.current

    val filteredOrders = orders.filter { order ->
        val matchesSearch = order.clientName.contains(searchQuery, ignoreCase = true) ||
                order.notes.contains(searchQuery, ignoreCase = true)
        
        val matchesPeriod = when (selectedPeriod) {
            "Esta Semana" -> isCurrentWeek(order.dateMillis)
            "Este Mês" -> isCurrentMonth(order.dateMillis)
            else -> true
        }

        val matchesStatus = when (selectedStatus) {
            "Pendentes" -> !order.isPaid
            "Pagos" -> order.isPaid
            else -> true
        }

        matchesSearch && matchesPeriod && matchesStatus
    }

    val periodFilteredOrders = orders.filter { order ->
        when (selectedPeriod) {
            "Esta Semana" -> isCurrentWeek(order.dateMillis)
            "Este Mês" -> isCurrentMonth(order.dateMillis)
            else -> true
        }
    }

    val totalRevenue = periodFilteredOrders.sumOf { it.totalPrice }
    val totalTruffles = periodFilteredOrders.sumOf { it.totalQuantity }
    val pendingCount = periodFilteredOrders.count { !it.isPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trufas Bella", fontWeight = FontWeight.Bold)
                        Text("Gestão de Encomendas e Entregas", style = MaterialTheme.typography.bodySmall)
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
                    placeholder = { Text("Pesquisar cliente ou observação...") },
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

                Text("Período:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Esta Semana", "Este Mês", "Todas").forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Status:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Pendentes", "Pagos").forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status) }
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
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhuma encomenda encontrada", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
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

@Composable
fun OrderItemCard(
    order: OrderModel,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsAppShare: () -> Unit
) {
    val flavorsText = order.flavorQuantities
        .filter { it.value > 0 }
        .entries
        .joinToString(", ") { "${it.value} ${it.key}" }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E1)
                ) {
                    Text(
                        text = if (order.isPaid) "PAGO (${order.paymentMethod})" else "PENDENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isPaid) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = flavorsText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Obs: ${order.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${order.totalQuantity} unidades", style = MaterialTheme.typography.bodySmall)
                    Text("R$ %.2f".format(order.totalPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onWhatsAppShare, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar WhatsApp", tint = Color(0xFF25D366))
                    }
                    IconButton(onClick = onTogglePaid, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (order.isPaid) Icons.Default.MoneyOff else Icons.Default.CheckCircle,
                            contentDescription = "Alterar status pagamento",
                            tint = if (order.isPaid) MaterialTheme.colorScheme.outline else Color(0xFF2E7D32)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

fun shareOnWhatsApp(context: Context, order: OrderModel) {
    val flavorsList = order.flavorQuantities
        .filter { it.value > 0 }
        .entries
        .joinToString(", ") { "${it.value} ${it.key}" }

    val text = "*TRUFAS BELLA - COMPROVANTE DE ENCOMENDA*\n\n" +
            "Cliente: *${order.clientName}*\n" +
            "Sabores: $flavorsList\n" +
            "Quantidade: *${order.totalQuantity} und*\n" +
            "Valor Total: *R$ %.2f*\n".format(order.totalPrice) +
            "Status: *${if (order.isPaid) "PAGO (${order.paymentMethod})" else "PENDENTE"}*\n\n" +
            "🔑 *PIX para Pagamento:*\n496.373.588-11\n\n" +
            "_Agradecemos a preferência!_ 🍫"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Compartilhar Recibo"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao abrir compartilhamento", Toast.LENGTH_SHORT).show()
    }
}
