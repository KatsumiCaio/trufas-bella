package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatórios & Gráficos", fontWeight = FontWeight.Bold) },
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
            // Revenue Summary Card
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
                        Text("Faturamento Geral", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

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

            // Daily Revenue Chart Card
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
                        DailyRevenueChart(orders = orders)
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
fun DailyRevenueChart(orders: List<OrderModel>) {
    val revenueByDay = orders.groupBy { it.createdAt.ifBlank { "Hoje" } }
        .mapValues { (_, list) -> list.sumOf { it.totalPrice } }
        .entries.toList()
        .sortedBy { it.key }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Faturamento Total por Dia (R$)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (revenueByDay.isEmpty()) {
            Text("Nenhum dado de faturamento por dia.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            return
        }

        val maxRevenue = revenueByDay.maxOfOrNull { it.value }?.coerceAtLeast(10.0) ?: 10.0
        val primaryColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height - 16.dp.toPx()
                val barWidth = (width / (revenueByDay.size * 2)).coerceIn(12.dp.toPx(), 40.dp.toPx())
                val totalBarsWidth = barWidth * revenueByDay.size
                val spacing = (width - totalBarsWidth) / (revenueByDay.size + 1).coerceAtLeast(1)

                revenueByDay.forEachIndexed { index, (_, revenue) ->
                    val barHeight = (revenue.toFloat() / maxRevenue.toFloat()) * height
                    val left = spacing + index * (barWidth + spacing)
                    val top = height - barHeight

                    drawRect(
                        color = primaryColor,
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(4f))
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            revenueByDay.forEach { (day, revenue) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("R$ %.0f".format(revenue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
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
