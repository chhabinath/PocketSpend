package com.chhabinath.pocketspend.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chhabinath.pocketspend.viewmodel.ExpenseViewModel

@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {

    var merchant by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("Groceries")
    }

    var date by remember {
        mutableStateOf("2026-10-04")
    }

    val categories = listOf(
        "Food",
        "Groceries",
        "Transport",
        "Shopping",
        "Bills",
        "Entertainment",
        "Healthcare",
        "Other"
    )

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            TextButton(
                onClick = onBack
            ) {
                Text("Cancel")
            }

            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // Merchant
        OutlinedTextField(
            value = merchant,
            onValueChange = {
                merchant = it
            },
            label = {
                Text("Merchant")
            },
            placeholder = {
                Text("e.g. DMART")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Amount
        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
            },
            label = {
                Text("Amount")
            },
            placeholder = {
                Text("e.g. 450")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Category
        Column {

            OutlinedButton(
                onClick = {
                    categoryExpanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Category: $category"
                )
            }

            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = {
                    categoryExpanded = false
                }
            ) {

                categories.forEach { item ->

                    DropdownMenuItem(
                        text = {
                            Text(item)
                        },
                        onClick = {

                            category = item
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Date
        OutlinedTextField(
            value = date,
            onValueChange = {
                date = it
            },
            label = {
                Text("Date")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        // Save
        Button(
            onClick = {

                val amountValue = amount.toDoubleOrNull()

                if (
                    merchant.isNotBlank() &&
                    amountValue != null &&
                    amountValue > 0
                ) {

                    viewModel.addExpense(
                        merchant = merchant.trim(),
                        amount = amountValue,
                        category = category,
                        date = date
                    )

                    onBack()
                }

            },
            modifier = Modifier.fillMaxWidth(),
            enabled = merchant.isNotBlank() &&
                    amount.toDoubleOrNull() != null &&
                    amount.toDoubleOrNull()!! > 0
        ) {

            Text("Save Expense")
        }
    }
}