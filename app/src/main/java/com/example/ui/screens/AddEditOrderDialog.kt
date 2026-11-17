package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.FlavorModel
import com.example.data.OrderModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderDialog(
    orderToEdit: OrderModel? = null,
    flavors: List<FlavorModel>,
    onDismiss: () -> Unit,
    onSave: (
        clientName: String,
        flavorQuantities: Map<String, Int>,
        totalPrice: Double,
        paymentMethod: String,
        isPaid: Boolean,
        notes: String
    ) -> Unit,
    onSeedFlavors: () -> Unit
) {
    var clientName by remember { mutableStateOf(orderToEdit?.clientName ?: "") }
    var flavorCounts by remember { mutableStateOf(orderToEdit?.flavorQuantities ?: emptyMap<String, Int>()) }

    var paymentMethod by remember { mutableStateOf(orderToEdit?.paymentMethod ?: "PIX") }
    var isPaid by remember { mutableStateOf(orderToEdit?.isPaid ?: false) }
    var notes by remember { mutableStateOf(orderToEdit?.notes ?: "") }

    var expandedPayment by remember { mutableStateOf(false) }
    val paymentOptions = listOf("PIX", "Dinheiro", "Pendente")

    val totalQty = flavorCounts.values.sum()
    val totalPrice = flavorCounts.entries.sumOf { (flavorName, qty) ->
        val price = flavors.find { it.name == flavorName }?.price ?: 7.0
        qty * price
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (orderToEdit == null) "Nova Encomenda" else "Editar Encomenda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nome do Cliente *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sabores de Trufas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (flavors.isEmpty()) {
                        TextButton(onClick = onSeedFlavors) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Carregar Padrões")
                        }
                    }
                }

                if (flavors.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum sabor cadastrado. Clique em Carregar Padrões ou adicione na aba Sabores.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    flavors.forEach { flavor ->
                        val count = flavorCounts[flavor.name] ?: 0
                        FlavorCounterRow(
                            label = if (flavor.isSpecial) "${flavor.name} (Especial - R$ %.2f)".format(flavor.price) else "${flavor.name} (R$ %.2f)".format(flavor.price),
                            count = count
                        ) { newCount ->
                            flavorCounts = flavorCounts.toMutableMap().apply {
                                if (newCount > 0) put(flavor.name, newCount) else remove(flavor.name)
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPayment,
                        onExpandedChange = { expandedPayment = !expandedPayment },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pagamento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayment) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPayment,
                            onDismissRequest = { expandedPayment = false }
                        ) {
                            paymentOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        paymentMethod = option
                                        isPaid = (option != "Pendente")
                                        expandedPayment = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Checkbox(
                            checked = isPaid,
                            onCheckedChange = { isPaid = it },
                            modifier = Modifier.testTag("is_paid_checkbox")
                        )
                        Text("Pago", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (ex: vai pagar semana que vem)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Quantidade Total:", style = MaterialTheme.typography.bodySmall)
                            Text("$totalQty unidades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Valor Total:", style = MaterialTheme.typography.bodySmall)
                            Text("R$ %.2f".format(totalPrice), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && totalQty > 0) {
                                onSave(
                                    clientName,
                                    flavorCounts,
                                    totalPrice,
                                    paymentMethod,
                                    isPaid,
                                    notes
                                )
                            }
                        },
                        enabled = clientName.isNotBlank() && totalQty > 0,
                        modifier = Modifier.testTag("save_order_button")
                    ) {
                        Text("Salvar Encomenda")
                    }
                }
            }
        }
    }
}

@Composable
fun FlavorCounterRow(label: String, count: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Menos", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 24.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = { onCountChange(count + 1) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Mais", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
