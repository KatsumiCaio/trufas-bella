package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.OrderModel
import com.example.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(viewModel: OrderViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val unpaidOrders = orders.filter { !it.isPaid }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recibos (Pedidos Pendentes)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (unpaidOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum pedido pendente para gerar recibos", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(unpaidOrders, key = { it.id }) { order ->
                    ReceiptCard(
                        order = order,
                        onShare = { shareOnWhatsApp(context, order) },
                        onCopy = {
                            val receiptText = buildReceiptText(order)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Recibo Trufas Bella", receiptText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Recibo copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptCard(
    order: OrderModel,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    val flavorsText = order.flavorQuantities
        .filter { it.value > 0 }
        .entries
        .joinToString(", ") { "${it.value} ${it.key}" }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRUFAS BELLA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.createdAt.ifBlank { "Hoje" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = order.clientName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = flavorsText.ifBlank { "Nenhum sabor especificado" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${order.totalQuantity} und",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "R$ %.2f".format(order.totalPrice),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE3F2FD),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CHAVE PIX (CELULAR)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                    Text("496.373.588-11", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
                }
            }

            if (order.notes.isNotBlank()) {
                Text(
                    text = "Obs: ${order.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar")
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", color = Color.White)
                }
            }
        }
    }
}

fun buildReceiptText(order: OrderModel): String {
    val flavorsList = order.flavorQuantities
        .filter { it.value > 0 }
        .entries
        .joinToString(", ") { "${it.value} ${it.key}" }

    return "*TRUFAS BELLA - RECIBO DE ENCOMENDA*\n\n" +
            "Cliente: *${order.clientName}*\n" +
            "Sabores: $flavorsList\n" +
            "Quantidade: *${order.totalQuantity} und*\n" +
            "Valor Total: *R$ %.2f*\n".format(order.totalPrice) +
            "Status: *${if (order.isPaid) "PAGO (${order.paymentMethod})" else "PENDENTE"}*\n\n" +
            "🔑 *PIX para Pagamento:*\n496.373.588-11\n\n" +
            "_Agradecemos a preferência!_ 🍫"
}
