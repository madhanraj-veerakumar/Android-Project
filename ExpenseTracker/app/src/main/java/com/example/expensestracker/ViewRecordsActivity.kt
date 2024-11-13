package com.example.expensestracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.example.expensestracker.ui.ViewRecordsContent
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Rest of your enums and data classes remain the same
enum class SortOrder {
    DATE_DESC,
    AMOUNT_DESC
}

data class DateRange(
    val startDate: Date,
    val endDate: Date
)

class ViewRecordsActivity : ComponentActivity() {
    private lateinit var itemsDatabaseHelper: ItemsDatabaseHelper

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsDatabaseHelper = ItemsDatabaseHelper(this)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                ViewRecordsScreen(
                    itemsDatabaseHelper = itemsDatabaseHelper,
                    onNavigateToAddExpenses = { startActivity(Intent(applicationContext, AddExpensesActivity::class.java)) },
                    onNavigateToSetLimit = { startActivity(Intent(applicationContext, SetLimitActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
fun ViewRecordsScreen(
    itemsDatabaseHelper: ItemsDatabaseHelper,
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToSetLimit: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<Items?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf<DateRange?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        ViewRecordsHeader(
            onFilterClick = { showFilterDialog = true },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )

        ViewRecordsContent(
            itemsDatabaseHelper = itemsDatabaseHelper,
            searchQuery = searchQuery,
            dateRange = selectedDateRange,
            onDeleteClick = { item ->
                selectedItem = item
                showDeleteDialog = true
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        ViewRecordsBottomNavigation(
            onNavigateToAddExpenses = onNavigateToAddExpenses,
            onNavigateToSetLimit = onNavigateToSetLimit,
            onClearAllClick = {
                selectedItem = null
                showDeleteDialog = true
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            selectedItem = selectedItem,
            onConfirm = {
                if (selectedItem == null) {
                    itemsDatabaseHelper.deleteAllItems()
                } else {
                    selectedItem?.id?.let { itemsDatabaseHelper.deleteItem(it) }
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showFilterDialog) {
        DateFilterDialog(
            onDismiss = { showFilterDialog = false },
            onApply = { range ->
                selectedDateRange = range
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    selectedItem: Items?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (selectedItem == null) "Clear All Records" else "Delete Record",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (selectedItem == null)
                    "Are you sure you want to clear all expense records? This action cannot be undone."
                else
                    "Are you sure you want to delete this expense record? This action cannot be undone."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE74C3C))
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun DateFilterDialog(
    onDismiss: () -> Unit,
    onApply: (DateRange) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Filter by Date Range", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (DD/MM/YYYY)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isError = error != null
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (DD/MM/YYYY)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isError = error != null
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val start = dateFormat.parse(startDate)
                        val end = dateFormat.parse(endDate)
                        if (start != null && end != null) {
                            if (start.after(end)) {
                                error = "Start date must be before end date"
                            } else {
                                onApply(DateRange(start, end))
                                onDismiss()
                            }
                        } else {
                            error = "Invalid date format"
                        }
                    } catch (e: Exception) {
                        error = "Invalid date format"
                    }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ViewRecordsHeader(
    onFilterClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF9B59B6))
            .padding(16.dp)
    ) {
        Text(
            text = "Expense Records",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search expenses...", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                cursorColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
            ),
            trailingIcon = {
                IconButton(onClick = onFilterClick) {
                    Image(
                        painter = painterResource(id = R.drawable.filter_icon), // Replace with your PNG file
                        contentDescription = "Filter",
                        modifier = Modifier.size(24.dp) // Adjust size as needed
                    )
                }
            }
        )
    }
}

@Composable
fun ExpenseItem(
    item: Items,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.itemName ?: "",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Quantity: ${item.quantity}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "₹${item.cost}",
                    fontSize = 14.sp,
                    color = Color(0xFF2ECC71),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick = onDeleteClick
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color(0xFFE74C3C)
            )
        }
    }
}

@Composable
fun ViewRecordsBottomNavigation(
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToSetLimit: () -> Unit,
    onClearAllClick: () -> Unit
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

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClearAllClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE74C3C)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Clear All",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onNavigateToSetLimit,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9B59B6)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Set Limit",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}