package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = { 
                    Column {
                        Text("Etiquetas & Recibos (Pendentes)", fontWeight = FontWeight.Bold)
                        Text("Pedidos pagos saem automaticamente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                },
                actions = {
                    if (unpaidOrders.isNotEmpty()) {
                        IconButton(onClick = { printAllSeparationLabels(context, unpaidOrders) }) {
                            Icon(Icons.Default.Print, contentDescription = "Imprimir Folha de Separação")
                        }
                    }
                },
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
                    Text("Nenhum pedido pendente para etiquetas/recibos", color = MaterialTheme.colorScheme.outline)
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
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Folha de Separação Física", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("${unpaidOrders.size} etiquetas prontas para impressão em lote", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Button(
                                onClick = { printAllSeparationLabels(context, unpaidOrders) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Imprimir Todas")
                            }
                        }
                    }
                }

                items(unpaidOrders, key = { it.id }) { order ->
                    SeparationLabelCard(
                        order = order,
                        onShare = { shareOnWhatsApp(context, order) },
                        onCopy = {
                            val receiptText = buildReceiptText(order)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Etiqueta Trufas Bella", receiptText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Etiqueta copiada para a área de transferência!", Toast.LENGTH_SHORT).show()
                        },
                        onPrint = {
                            printSeparationLabel(context, order)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SeparationLabelCard(
    order: OrderModel,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onPrint: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: TRUFAS BELLA & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRUFAS BELLA - ETIQUETA DE SEPARAÇÃO",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                )
                Text(
                    text = order.createdAt.ifBlank { "Hoje" },
                    style = TextStyle(fontSize = 11.sp, color = Color.Gray)
                )
            }

            // Tabular Grid matching the reference image layout
            // Columns: NOME | BRIGADEIRO | PAÇOCA | NINHO | NI/C | MARACUJÁ | OUTRO | QTD | VALOR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .border(width = 0.5.dp, color = Color.Black)
                ) {
                    TableHeaderCell("NOME", modifier = Modifier.weight(2f))
                    TableDivider()
                    TableHeaderCell("BRIG.", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableHeaderCell("PAÇOCA", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableHeaderCell("NINHO", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableHeaderCell("NI/C", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableHeaderCell("MARAC.", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableHeaderCell("OUTRO", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableHeaderCell("QTD", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableHeaderCell("VALOR", modifier = Modifier.weight(1.2f))
                }

                // Table Values Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.5.dp, color = Color.Black)
                ) {
                    TableCell(order.clientName, modifier = Modifier.weight(2f), bold = true)
                    TableDivider()
                    TableCell(if (order.brigadeiro > 0) order.brigadeiro.toString() else "", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableCell(if (order.pacoca > 0) order.pacoca.toString() else "", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableCell(if (order.ninho > 0) order.ninho.toString() else "", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableCell(if (order.ninhoNutella > 0) order.ninhoNutella.toString() else "", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableCell(if (order.maracuja > 0) order.maracuja.toString() else "", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableCell(if (order.outro > 0) order.outro.toString() else "", modifier = Modifier.weight(1f))
                    TableDivider()
                    TableCell(order.totalQuantity.toString(), modifier = Modifier.weight(1f), bold = true)
                    TableDivider()
                    TableCell("R$ %.2f".format(order.totalPrice), modifier = Modifier.weight(1.2f), bold = true)
                }

                // PIX Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F0F0))
                        .border(width = 0.5.dp, color = Color.Black)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PIX:",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                    Text(
                        text = "496.373.588-11",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                }
            }

            if (order.notes.isNotBlank()) {
                Text(
                    text = "Obs: ${order.notes}",
                    style = TextStyle(fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar", style = TextStyle(fontSize = 12.sp))
                }

                OutlinedButton(
                    onClick = onPrint,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Imprimir", style = TextStyle(fontSize = 12.sp))
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", color = Color.White, style = TextStyle(fontSize = 12.sp))
                }
            }
        }
    }
}

@Composable
fun RowScope.TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun RowScope.TableCell(text: String, modifier: Modifier = Modifier, bold: Boolean = false) {
    Box(
        modifier = modifier
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 11.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = Color.Black),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun TableDivider() {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .fillMaxHeight()
            .background(Color.Black)
    )
}

fun printAllSeparationLabels(context: Context, orders: List<OrderModel>) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
    if (printManager == null) {
        Toast.makeText(context, "Impressão não suportada neste dispositivo", Toast.LENGTH_SHORT).show()
        return
    }

    val rowsHtml = orders.joinToString(separator = "<br/><hr style='border: 1px dashed #666; margin: 20px 0;'/><br/>") { order ->
        """
        <div class="label-block">
            <div class="header-info">
                <span><b>TRUFAS BELLA - ETIQUETA DE SEPARAÇÃO</b></span>
                <span>Data: ${order.createdAt.ifBlank { "Hoje" }}</span>
            </div>
            <table>
                <tr>
                    <th>NOME</th>
                    <th>BRIG.</th>
                    <th>PAÇOCA</th>
                    <th>NINHO</th>
                    <th>NI/C</th>
                    <th>MARAC.</th>
                    <th>OUTRO</th>
                    <th>QTD</th>
                    <th>VALOR</th>
                </tr>
                <tr>
                    <td><b>${order.clientName}</b></td>
                    <td>${if (order.brigadeiro > 0) order.brigadeiro.toString() else ""}</td>
                    <td>${if (order.pacoca > 0) order.pacoca.toString() else ""}</td>
                    <td>${if (order.ninho > 0) order.ninho.toString() else ""}</td>
                    <td>${if (order.ninhoNutella > 0) order.ninhoNutella.toString() else ""}</td>
                    <td>${if (order.maracuja > 0) order.maracuja.toString() else ""}</td>
                    <td>${if (order.outro > 0) order.outro.toString() else ""}</td>
                    <td><b>${order.totalQuantity}</b></td>
                    <td><b>R$ %.2f</b></td>
                </tr>
            </table>
            <div class="pix">PIX: 496.373.588-11</div>
            ${if (order.notes.isNotBlank()) "<div class='obs'>Obs: ${order.notes}</div>" else ""}
        </div>
        """.trimIndent().format(order.totalPrice)
    }

    val htmlContent = """
        <html>
        <head>
            <style>
                body { font-family: monospace; font-size: 11px; padding: 10px; color: #000; }
                .label-block { margin-bottom: 25px; page-break-inside: avoid; }
                .header-info { display: flex; justify-content: space-between; font-weight: bold; margin-bottom: 3px; font-size: 10px; }
                table { width: 100%; border-collapse: collapse; margin-top: 2px; }
                th, td { border: 1px solid black; padding: 6px; text-align: center; }
                th { background-color: #dcdcdc; font-size: 10px; }
                td { font-size: 11px; }
                .pix { font-weight: bold; margin-top: 3px; border: 1px solid black; padding: 5px; background-color: #f0f0f0; font-size: 10px; }
                .obs { font-size: 10px; margin-top: 2px; font-style: italic; }
            </style>
        </head>
        <body>
            $rowsHtml
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                createPrintDocumentAdapter("Folha_Etiquetas_Separacao").let { adapter ->
                    printManager.print("Folha_Etiquetas_Separacao", adapter, PrintAttributes.Builder().build())
                }
            }
        }
        loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }
}

fun printSeparationLabel(context: Context, order: OrderModel) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
    if (printManager == null) {
        Toast.makeText(context, "Impressão não suportada neste dispositivo", Toast.LENGTH_SHORT).show()
        return
    }

    val htmlContent = """
        <html>
        <head>
            <style>
                body { font-family: monospace; font-size: 12px; padding: 10px; }
                table { width: 100%; border-collapse: collapse; margin-top: 5px; }
                th, td { border: 1px solid black; padding: 6px; text-align: center; }
                th { background-color: #ddd; font-size: 11px; }
                .title { font-weight: bold; font-size: 14px; margin-bottom: 5px; }
                .pix { font-weight: bold; margin-top: 5px; border: 1px solid black; padding: 5px; }
            </style>
        </head>
        <body>
            <div class="title">TRUFAS BELLA - ETIQUETA DE SEPARAÇÃO</div>
            <div>Data: ${order.createdAt.ifBlank { "Hoje" }}</div>
            <table>
                <tr>
                    <th>NOME</th>
                    <th>BRIG.</th>
                    <th>PAÇOCA</th>
                    <th>NINHO</th>
                    <th>NI/C</th>
                    <th>MARAC.</th>
                    <th>OUTRO</th>
                    <th>QTD</th>
                    <th>VALOR</th>
                </tr>
                <tr>
                    <td><b>${order.clientName}</b></td>
                    <td>${if (order.brigadeiro > 0) order.brigadeiro.toString() else ""}</td>
                    <td>${if (order.pacoca > 0) order.pacoca.toString() else ""}</td>
                    <td>${if (order.ninho > 0) order.ninho.toString() else ""}</td>
                    <td>${if (order.ninhoNutella > 0) order.ninhoNutella.toString() else ""}</td>
                    <td>${if (order.maracuja > 0) order.maracuja.toString() else ""}</td>
                    <td>${if (order.outro > 0) order.outro.toString() else ""}</td>
                    <td><b>${order.totalQuantity}</b></td>
                    <td><b>R$ %.2f</b></td>
                </tr>
            </table>
            <div class="pix">PIX: 496.373.588-11</div>
            ${if (order.notes.isNotBlank()) "<div>Obs: ${order.notes}</div>" else ""}
        </body>
        </html>
    """.trimIndent().format(order.totalPrice)

    val webView = WebView(context).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                createPrintDocumentAdapter("Etiqueta_${order.clientName}".replace(" ", "_")).let { adapter ->
                    printManager.print("Etiqueta_${order.clientName}", adapter, PrintAttributes.Builder().build())
                }
            }
        }
        loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }
}

fun buildReceiptText(order: OrderModel): String {
    val flavorsList = order.flavorQuantities
        .filter { it.value > 0 }
        .entries
        .joinToString(", ") { "${it.value} ${it.key}" }

    return "*TRUFAS BELLA - ETIQUETA DE SEPARAÇÃO*\n\n" +
            "Cliente: *${order.clientName}*\n" +
            "Sabores: $flavorsList\n" +
            "Quantidade: *${order.totalQuantity} und*\n" +
            "Valor Total: *R$ %.2f*\n".format(order.totalPrice) +
            "Status: *PENDENTE*\n\n" +
            "🔑 *PIX para Pagamento:*\n496.373.588-11\n\n" +
            "_Agradecemos a preferência!_ 🍫"
}
