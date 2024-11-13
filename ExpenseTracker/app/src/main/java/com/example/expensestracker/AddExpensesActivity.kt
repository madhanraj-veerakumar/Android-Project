package com.example.expensestracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class AddExpensesActivity : ComponentActivity() {
    private lateinit var itemsDatabaseHelper: ItemsDatabaseHelper
    private lateinit var expenseDatabaseHelper: ExpenseDatabaseHelper

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsDatabaseHelper = ItemsDatabaseHelper(this)
        expenseDatabaseHelper = ExpenseDatabaseHelper(this)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                AddExpensesScreen(
                    itemsDatabaseHelper = itemsDatabaseHelper,
                    expenseDatabaseHelper = expenseDatabaseHelper,
                    onNavigateToSetLimit = { startActivity(Intent(applicationContext, SetLimitActivity::class.java)) },
                    onNavigateToViewRecords = { startActivity(Intent(applicationContext, ViewRecordsActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
fun AddExpensesScreen(
    itemsDatabaseHelper: ItemsDatabaseHelper,
    expenseDatabaseHelper: ExpenseDatabaseHelper,
    onNavigateToSetLimit: () -> Unit,
    onNavigateToViewRecords: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        AddExpensesHeader()

        // Form Content
        AddExpensesForm(
            itemsDatabaseHelper = itemsDatabaseHelper,
            expenseDatabaseHelper = expenseDatabaseHelper
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Navigation
        BottomNavigation(
            onNavigateToSetLimit = onNavigateToSetLimit,
            onNavigateToViewRecords = onNavigateToViewRecords
        )
    }
}

@Composable
fun AddExpensesHeader() {
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
            text = "Add New Expense",
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
fun AddExpensesForm(
    itemsDatabaseHelper: ItemsDatabaseHelper,
    expenseDatabaseHelper: ExpenseDatabaseHelper
) {
    val context = LocalContext.current
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Item Name Field
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF3498DB),
                        focusedLabelColor = Color(0xFF3498DB)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Field
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF3498DB),
                        focusedLabelColor = Color(0xFF3498DB)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cost Field
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost (₹)") },
                    modifier = Modifier.fillMaxWidth(),
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

                // Submit Button
                Button(
                    onClick = {
                        if (itemName.isNotEmpty() && quantity.isNotEmpty() && cost.isNotEmpty()) {
                            val items = Items(
                                id = null,
                                itemName = itemName,
                                quantity = quantity,
                                cost = cost
                            )

                            val limit = expenseDatabaseHelper.getExpenseAmount(1)
                            val actualValue = limit?.minus(cost.toInt())

                            val expense = Expense(
                                id = 1,
                                amount = actualValue?.toString()
                            )

                            if (actualValue != null) {
                                if (actualValue < 1) {
                                    Toast.makeText(context, "Expense Limit Exceeded!", Toast.LENGTH_SHORT).show()
                                } else {
                                    expenseDatabaseHelper.updateExpense(expense)
                                    itemsDatabaseHelper.insertItems(items)
                                    Toast.makeText(context, "Expense Added Successfully!", Toast.LENGTH_SHORT).show()
                                    // Clear fields
                                    itemName = ""
                                    quantity = ""
                                    cost = ""
                                }
                            }
                        } else {
                            error = "Please fill all fields"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2ECC71)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Add Expense",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(
    onNavigateToSetLimit: () -> Unit,
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
                onClick = onNavigateToSetLimit,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE74C3C)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Set Limit",
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