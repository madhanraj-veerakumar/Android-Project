package com.example.expensestracker.ui // Adjust package name based on your project structure

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensestracker.Items
import com.example.expensestracker.ItemsDatabaseHelper
import com.example.expensestracker.DateRange // Assuming you have DateRange class in your project

// Composable function to display the records
@Composable
fun ViewRecordsContent(
    itemsDatabaseHelper: ItemsDatabaseHelper,
    searchQuery: String,
    dateRange: DateRange?,
    onDeleteClick: (Items) -> Unit
) {
    val items = remember(searchQuery, dateRange) {
        itemsDatabaseHelper.getFilteredItems(searchQuery, dateRange)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                ExpenseItem(
                    item = item,
                    onDeleteClick = { onDeleteClick(item) }
                )
            }
        }

        if (items.isEmpty()) {
            item {
                Text(
                    text = "No expenses found",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}

// Define ExpenseItem for individual records inside the same file or another file
@Composable
fun ExpenseItem(
    item: Items,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.itemName ?: "",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Row(modifier = Modifier.padding(top = 4.dp)) {
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
