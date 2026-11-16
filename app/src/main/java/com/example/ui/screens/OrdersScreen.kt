package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderEntity
import com.example.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrderViewModel,
    onAddNewClick: () -> Unit,
    onEditOrder: (OrderEntity) -> Unit
) {
    val orders by viewModel.allOrders.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") } // Todos, Pendentes, Pagos
    val context = LocalContext.current

    val filteredOrders = orders.filter { order ->
        val matchesSearch = order.clientName.contains(searchQuery, ignoreCase = true) ||
                order.notes.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Pendentes" -> !order.isPaid
            "Pagos" -> order.isPaid
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalRevenue = orders.sumOf { it.totalPrice }
    val totalTruffles = orders.sumOf { it.totalQuantity }
    val pendingCount = orders.count { !it.isPaid }

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
            // Summary Dashboard Header Card
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

            // Search Bar & Filters
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Pendentes", "Pagos").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List of Orders
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
                                viewModel.updateOrder(order.copy(isPaid = !order.isPaid, paymentMethod = if (!order.isPaid) "PIX" else "Pendente"))
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
    order: OrderEntity,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsAppShare: () -> Unit
) {
    val flavorsText = buildString {
        val list = mutableListOf<String>()
        if (order.brigadeiro >  0) list.add("${order.brigadeiro} Brig.")
        if (order.pacoca > 0) list.add("${order.pacoca} Paç.")
        if (order.ninho > 0) list.add("${order.ninho} Ninho")
        if (order.ninhoNutella > 0) list.add("${order.ninhoNutella} Ninho+Nut.")
        if (order.maracuja > 0) list.add("${order.maracuja} Marac.")
        if (order.outro > 0) list.add("${order.outro} Outro")
        append(list.joinToString(", "))
    }

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
            // Header: Client Name & Payment Badge
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

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
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
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Flavors description
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

            // Footer: Qty, Total Price & Actions
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
                    // WhatsApp share button
                    IconButton(onClick = onWhatsAppShare, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar WhatsApp", tint = Color(0xFF25D366))
                    }
                    // Toggle paid status
                    IconButton(onClick = onTogglePaid, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (order.isPaid) Icons.Default.MoneyOff else Icons.Default.CheckCircle,
                            contentDescription = "Alterar status pagamento",
                            tint = if (order.isPaid) MaterialTheme.colorScheme.outline else Color(0xFF2E7D32)
                        )
                    }
                    // Edit
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                    }
                    // Delete
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

fun shareOnWhatsApp(context: Context, order: OrderEntity) {
    val flavorsList = buildString {
        if (order.brigadeiro > 0) append("${order.brigadeiro} Brigadeiro, ")
        if (order.pacoca > 0) append("${order.pacoca} Paçoca, ")
        if (order.ninho > 0) append("${order.ninho} Ninho, ")
        if (order.ninhoNutella > 0) append("${order.ninhoNutella} Ninho c/ Nutella, ")
        if (order.maracuja > 0) append("${order.maracuja} Maracujá, ")
        if (order.outro > 0) append("${order.outro} Outro, ")
    }.trimEnd(',', ' ')

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
