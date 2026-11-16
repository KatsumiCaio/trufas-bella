package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.OrderEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderDialog(
    orderToEdit: OrderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        clientName: String,
        brigadeiro: Int,
        pacoca: Int,
        ninho: Int,
        ninhoNutella: Int,
        maracuja: Int,
        outro: Int,
        unitPriceSimple: Double,
        unitPriceSpecial: Double,
        paymentMethod: String,
        isPaid: Boolean,
        notes: String
    ) -> Unit
) {
    var clientName by remember { mutableStateOf(orderToEdit?.clientName ?: "") }
    var brigadeiro by remember { mutableStateOf(orderToEdit?.brigadeiro ?: 0) }
    var pacoca by remember { mutableStateOf(orderToEdit?.pacoca ?: 0) }
    var ninho by remember { mutableStateOf(orderToEdit?.ninho ?: 0) }
    var ninhoNutella by remember { mutableStateOf(orderToEdit?.ninhoNutella ?: 0) }
    var maracuja by remember { mutableStateOf(orderToEdit?.maracuja ?: 0) }
    var outro by remember { mutableStateOf(orderToEdit?.outro ?: 0) }

    var unitPriceSimpleStr by remember { mutableStateOf(orderToEdit?.unitPriceSimple?.toString() ?: "7.0") }
    var unitPriceSpecialStr by remember { mutableStateOf(orderToEdit?.unitPriceSpecial?.toString() ?: "8.0") }

    var paymentMethod by remember { mutableStateOf(orderToEdit?.paymentMethod ?: "PIX") }
    var isPaid by remember { mutableStateOf(orderToEdit?.isPaid ?: false) }
    var notes by remember { mutableStateOf(orderToEdit?.notes ?: "") }

    var expandedPayment by remember { mutableStateOf(false) }
    val paymentOptions = listOf("PIX", "Dinheiro", "Pendente")

    val simplePrice = unitPriceSimpleStr.toDoubleOrNull() ?: 7.0
    val specialPrice = unitPriceSpecialStr.toDoubleOrNull() ?: 8.0

    val totalQty = brigadeiro + pacoca + ninho + ninhoNutella + maracuja + outro
    val totalPrice = ((brigadeiro + pacoca + ninho + maracuja + outro) * simplePrice) + (ninhoNutella * specialPrice)

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
                // Title
                Text(
                    text = if (orderToEdit == null) "Nova Encomenda" else "Editar Encomenda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Client Name
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

                // Flavors Counters
                Text("Sabores de Trufas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                FlavorCounterRow("Brigadeiro (R$ $simplePrice)", brigadeiro) { brigadeiro = it }
                FlavorCounterRow("Paçoca (R$ $simplePrice)", pacoca) { pacoca = it }
                FlavorCounterRow("Ninho (R$ $simplePrice)", ninho) { ninho = it }
                FlavorCounterRow("Ninho c/ Nutella (Especial - R$ $specialPrice)", ninhoNutella) { ninhoNutella = it }
                FlavorCounterRow("Maracujá (R$ $simplePrice)", maracuja) { maracuja = it }
                FlavorCounterRow("Outro (R$ $simplePrice)", outro) { outro = it }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Prices customization toggle or inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unitPriceSimpleStr,
                        onValueChange = { unitPriceSimpleStr = it },
                        label = { Text("Preço Simples (R$)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitPriceSpecialStr,
                        onValueChange = { unitPriceSpecialStr = it },
                        label = { Text("Preço Especial (R$)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Payment Method & Status
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

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (ex: vai pagar semana que vem)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Live Summary Card
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

                // Buttons
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
                                    brigadeiro,
                                    pacoca,
                                    ninho,
                                    ninhoNutella,
                                    maracuja,
                                    outro,
                                    simplePrice,
                                    specialPrice,
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
