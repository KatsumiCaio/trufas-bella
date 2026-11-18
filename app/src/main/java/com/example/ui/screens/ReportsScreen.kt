package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.OrderModel
import com.example.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: OrderViewModel) {
    val orders by viewModel.allOrders.collectAsState()

    val totalRevenue = orders.sumOf { it.totalPrice }
    val totalTruffles = orders.sumOf { it.totalQuantity }
    val totalPaid = orders.filter { it.isPaid }.sumOf { it.totalPrice }
    val totalPending = orders.filter { !it.isPaid }.sumOf { it.totalPrice }

    // Aggregate client purchases (Ranking)
    val clientRanking = orders.groupBy { it.clientName }
        .map { (client, list) ->
            client to list.sumOf { it.totalQuantity }
        }
        .sortedByDescending { it.second }

    // Group orders month by month, summing up weeks of the month
    val ordersByMonth = orders.groupBy { order ->
        if (order.monthKey.isNotBlank()) order.monthKey else {
            val fmt = SimpleDateFormat("MM/yyyy", Locale("pt", "BR"))
            fmt.format(Date(order.dateMillis))
        }
    }.toSortedMap(compareByDescending { it })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório Mês a Mês & Gráficos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Revenue Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Resumo Geral", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Geral", style = MaterialTheme.typography.bodySmall)
                                Text("R$ %.2f".format(totalRevenue), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Trufas", style = MaterialTheme.typography.bodySmall)
                                Text("$totalTruffles und", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF2E7D32)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Recebido: R$ %.2f".format(totalPaid), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFFE65100)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pendente: R$ %.2f".format(totalPending), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Month by Month Breakdown (Summing weeks of each month)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Relatório Mês a Mês (por Semanas)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (ordersByMonth.isEmpty()) {
                            Text("Nenhum pedido registrado para relatórios.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ordersByMonth.forEach { (monthKey, monthOrders) ->
                                    val monthRevenue = monthOrders.sumOf { it.totalPrice }
                                    val monthTruffles = monthOrders.sumOf { it.totalQuantity }
                                    val weeksGrouped = monthOrders.groupBy { it.weekOfMonth }.toSortedMap()

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Mês: $monthKey", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text("R$ %.2f ($monthTruffles und)".format(monthRevenue), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                                            }

                                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                                            weeksGrouped.forEach { (weekNum, weekOrders) ->
                                                val weekRevenue = weekOrders.sumOf { it.totalPrice }
                                                val weekTruffles = weekOrders.sumOf { it.totalQuantity }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("Semana $weekNum", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                        Text("${weekOrders.size} encomendas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("R$ %.2f".format(weekRevenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                        Text("$weekTruffles trufas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Truffle Type Chart Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TruffleTypeChart(orders = orders)
                    }
                }
            }

            // Client Ranking Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clientes que Mais Compraram", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (clientRanking.isEmpty()) {
                            Text("Nenhum dado de cliente ainda.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                clientRanking.forEachIndexed { index, (client, qty) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (index == 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${index + 1}º",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(32.dp)
                                            )
                                            Text(
                                                text = client,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Text(
                                            text = "$qty trufas",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TruffleTypeChart(orders: List<OrderModel>) {
    val flavorSalesMap = mutableMapOf<String, Int>()
    orders.forEach { order ->
        order.flavorQuantities.forEach { (name, qty) ->
            flavorSalesMap[name] = (flavorSalesMap[name] ?: 0) + qty
        }
    }
    val flavorList = flavorSalesMap.entries.map { it.key to it.value }.sortedByDescending { it.second }
    val maxFlavorCount = flavorList.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Vendas por Tipo de Trufa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (flavorList.isEmpty()) {
            Text("Nenhuma venda registrada ainda.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                flavorList.forEach { (flavor, count) ->
                    val fraction = (count.toFloat() / maxFlavorCount).coerceIn(0.05f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(flavor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("$count und", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}
