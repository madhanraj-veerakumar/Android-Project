package com.example.expensestracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class SetLimitActivity : ComponentActivity() {
    private lateinit var expenseDatabaseHelper: ExpenseDatabaseHelper

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expenseDatabaseHelper = ExpenseDatabaseHelper(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SetLimitScreen(
                        expenseDatabaseHelper = expenseDatabaseHelper,
                        onNavigateToAddExpenses = { startActivity(Intent(applicationContext, AddExpensesActivity::class.java)) },
                        onNavigateToViewRecords = { startActivity(Intent(applicationContext, ViewRecordsActivity::class.java)) }
                    )
                }
            }
        }
    }
}

@Composable
fun SetLimitScreen(
    expenseDatabaseHelper: ExpenseDatabaseHelper? = null,
    onNavigateToAddExpenses: () -> Unit = {},
    onNavigateToViewRecords: () -> Unit = {}
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var amountToConfirm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        SetLimitHeader()
        SetLimitForm(
            expenseDatabaseHelper = expenseDatabaseHelper,
            onConfirmAmount = { amount ->
                amountToConfirm = amount
                showConfirmDialog = true
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        SetLimitBottomNavigation(
            onNavigateToAddExpenses = onNavigateToAddExpenses,
            onNavigateToViewRecords = onNavigateToViewRecords
        )
    }

    if (showConfirmDialog) {
        ConfirmationDialog(
            amount = amountToConfirm,
            onConfirm = {
                expenseDatabaseHelper?.let { helper ->
                    val expense = Expense(
                        id = 1,
                        amount = amountToConfirm
                    )
                    helper.updateExpense(expense)
                }
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
fun SetLimitHeader() {
    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3498DB))
            .padding(16.dp)
    ) {
        Text(
            text = "Set Monthly Limit",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = currentDate,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun SetLimitForm(
    expenseDatabaseHelper: ExpenseDatabaseHelper?,
    onConfirmAmount: (String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val currentLimit = remember {
        expenseDatabaseHelper?.getExpenseAmount(1)?.toString() ?: "Not Set"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        LimitInfoCard(currentLimit = currentLimit)
        Spacer(modifier = Modifier.height(16.dp))
        NewLimitCard(
            amount = amount,
            error = error,
            onAmountChange = { newAmount ->
                amount = newAmount.filter { it.isDigit() }
                error = validateAmount(newAmount)
            },
            onSetLimit = {
                if (error.isEmpty() && amount.isNotEmpty()) {
                    onConfirmAmount(amount)
                } else if (amount.isEmpty()) {
                    error = "Please enter an amount"
                }
            }
        )
    }
}

@Composable
fun LimitInfoCard(currentLimit: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE74C3C)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Monthly Limit",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "₹ $currentLimit",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NewLimitCard(
    amount: String,
    error: String,
    onAmountChange: (String) -> Unit,
    onSetLimit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Set New Monthly Limit",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Enter Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF3498DB),
                    focusedLabelColor = Color(0xFF3498DB)
                )
            )

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSetLimit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2ECC71)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Set New Limit",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    amount: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm New Limit",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Are you sure you want to set ₹$amount as your new monthly limit?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2ECC71))
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetLimitBottomNavigation(
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToViewRecords: () -> Unit
) {
    BottomAppBar(
        backgroundColor = Color.White,
        modifier = Modifier.height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateToAddExpenses,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2ECC71)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Add Expenses",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onNavigateToViewRecords,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9B59B6)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "View Records",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun validateAmount(amount: String): String {
    return when {
        amount.isEmpty() -> "Please enter an amount"
        amount.toLongOrNull() == null -> "Please enter a valid amount"
        amount.toLong() <= 0 -> "Amount must be greater than 0"
        amount.toLong() > 1000000000 -> "Amount cannot exceed 1 billion"
        else -> ""
    }
}

@Preview(showBackground = true)
@Composable
fun SetLimitScreenPreview() {
    MaterialTheme {
        SetLimitScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SetLimitHeaderPreview() {
    MaterialTheme {
        SetLimitHeader()
    }
}

@Preview(showBackground = true)
@Composable
fun LimitInfoCardPreview() {
    MaterialTheme {
        LimitInfoCard(currentLimit = "50,000")
    }
}

@Preview(showBackground = true)
@Composable
fun NewLimitCardPreview() {
    MaterialTheme {
        NewLimitCard(
            amount = "1000",
            error = "",
            onAmountChange = {},
            onSetLimit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationDialogPreview() {
    MaterialTheme {
        ConfirmationDialog(
            amount = "50000",
            onConfirm = {},
            onDismiss = {}
        )
    }
}