package com.example.expensestracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensestracker.ui.theme.ExpensesTrackerTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var expenseDatabaseHelper: ExpenseDatabaseHelper
    private lateinit var itemsDatabaseHelper: ItemsDatabaseHelper

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expenseDatabaseHelper = ExpenseDatabaseHelper(this)
        itemsDatabaseHelper = ItemsDatabaseHelper(this)

        setContent {
            ExpensesTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DashboardScreen(
                        onAddExpenseClick = { startActivity(Intent(applicationContext, AddExpensesActivity::class.java)) },
                        onSetLimitClick = { startActivity(Intent(applicationContext, SetLimitActivity::class.java)) },
                        onViewRecordsClick = { startActivity(Intent(applicationContext, ViewRecordsActivity::class.java)) },
                        expenseDatabaseHelper = expenseDatabaseHelper,
                        itemsDatabaseHelper = itemsDatabaseHelper
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onAddExpenseClick: () -> Unit,
    onSetLimitClick: () -> Unit,
    onViewRecordsClick: () -> Unit,
    expenseDatabaseHelper: ExpenseDatabaseHelper,
    itemsDatabaseHelper: ItemsDatabaseHelper
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Header
        DashboardHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Stats
        QuickStats(expenseDatabaseHelper)

        Spacer(modifier = Modifier.height(24.dp))

        // Action Cards
        ActionCards(
            onAddExpenseClick = onAddExpenseClick,
            onSetLimitClick = onSetLimitClick,
            onViewRecordsClick = onViewRecordsClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transactions
        RecentTransactions(itemsDatabaseHelper)
    }
}

@Composable
fun DashboardHeader() {
    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    Column {
        Text(
            text = "Expense Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Text(
            text = currentDate,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun QuickStats(expenseDatabaseHelper: ExpenseDatabaseHelper) {
    val remainingAmount = remember {
        expenseDatabaseHelper.getExpenseAmount(1) ?: 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        elevation = 4.dp,
        backgroundColor = Color(0xFF3498DB)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Available Balance",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "₹ $remainingAmount",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActionCards(
    onAddExpenseClick: () -> Unit,
    onSetLimitClick: () -> Unit,
    onViewRecordsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionCard(
            title = "Add\nExpense",
            backgroundColor = Color(0xFF2ECC71),
            onClick = onAddExpenseClick,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        ActionCard(
            title = "Set\nLimit",
            backgroundColor = Color(0xFFE74C3C),
            onClick = onSetLimitClick,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        ActionCard(
            title = "View\nRecords",
            backgroundColor = Color(0xFF9B59B6),
            onClick = onViewRecordsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp,
        backgroundColor = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentTransactions(itemsDatabaseHelper: ItemsDatabaseHelper) {
    val recentItems = remember {
        itemsDatabaseHelper.getAllItems().takeLast(3)
    }

    Column {
        Text(
            text = "Recent Transactions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        recentItems.forEach { item ->
            TransactionItem(item)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TransactionItem(item: Items) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.itemName ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Qty: ${item.quantity}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "₹ ${item.cost}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE74C3C)
            )
        }
    }
}