package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.OrderViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryScreen(viewModel: OrderViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val flavors by viewModel.allFlavors.collectAsState()

    val currentWeekNum = remember {
        Calendar.getInstance(Locale("pt", "BR")).get(Calendar.WEEK_OF_MONTH)
    }
    var selectedWeek by remember { mutableStateOf("Semana $currentWeekNum") } // "Semana 1", "Semana 2", etc. or "Todas"

    val weekFilteredOrders = orders.filter { order ->
        when (selectedWeek) {
            "Semana 1" -> order.weekOfMonth == 1
            "Semana 2" -> order.weekOfMonth == 2
            "Semana 3" -> order.weekOfMonth == 3
            "Semana 4" -> order.weekOfMonth == 4
            "Semana 5" -> order.weekOfMonth == 5
            else -> true
        }
    }

    // Calculate sum per flavor
    val flavorSummary = remember(weekFilteredOrders, flavors) {
        val map = mutableMapOf<String, Int>()
        // Initialize with all registered flavors
        flavors.forEach { flavor ->
            map[flavor.name] = 0
        }
        // Also include any flavor found in orders that might not be in flavors list
        weekFilteredOrders.forEach { order ->
            order.flavorQuantities.forEach { (flavorName, qty) ->
                map[flavorName] = (map[flavorName] ?: 0) + qty
            }
        }
        map.entries.sortedByDescending { it.value }
    }

    val totalTrufflesWeek = weekFilteredOrders.sumOf { it.totalQuantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Resumo de Produção da Semana", fontWeight = FontWeight.Bold)
                        Text("Total de trufas por sabor ($selectedWeek)", style = MaterialTheme.typography.bodySmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Week Filter Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Filtrar Semana:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Semana 1", "Semana 2", "Semana 3", "Semana 4", "Todas").forEach { week ->
                            FilterChip(
                                selected = selectedWeek == week,
                                onClick = { selectedWeek = week },
                                label = { Text(week) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Total Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Produção ($selectedWeek):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("$totalTrufflesWeek trufas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Flavor List
            if (flavorSummary.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum sabor cadastrado ou pedido na semana.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(flavorSummary) { (flavorName, count) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = flavorName.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (count > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
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
