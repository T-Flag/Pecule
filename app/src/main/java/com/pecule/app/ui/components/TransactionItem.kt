package com.pecule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.domain.CategoryInitializer
import com.pecule.app.domain.Transaction
import com.pecule.app.ui.theme.PeculeTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val icon = getCategoryIcon(transaction.category, transaction.isExpense)
    val amountColor = if (transaction.isExpense) {
        Color(0xFFE57373) // Rouge pour dépense
    } else {
        Color(0xFF81C784) // Vert pour revenu
    }
    val amountPrefix = if (transaction.isExpense) "-" else "+"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = transaction.category?.name ?: "Revenu",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Label and date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatDate(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Amount
        Text(
            text = "$amountPrefix${formatAmount(transaction.amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}

private fun getCategoryIcon(category: CategoryEntity?, isExpense: Boolean): ImageVector {
    if (!isExpense) {
        return Icons.Filled.AddCircle
    }
    return when (category?.icon) {
        "payments" -> Icons.Filled.Payments
        "restaurant" -> Icons.Filled.Restaurant
        "directions_car" -> Icons.Filled.DirectionsCar
        "home" -> Icons.Filled.Home
        "receipt_long" -> Icons.Filled.Receipt
        "sports_esports" -> Icons.Filled.SportsEsports
        "medical_services" -> Icons.Filled.MedicalServices
        "shopping_bag" -> Icons.Filled.ShoppingBag
        else -> Icons.Filled.MoreHoriz
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.FRANCE)
    return date.format(formatter)
}

private fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    return formatter.format(amount)
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemExpensePreview() {
    val foodCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Alimentation" }
    PeculeTheme {
        TransactionItem(
            transaction = Transaction(
                id = 1,
                label = "Courses Carrefour",
                amount = 85.50,
                date = LocalDate.of(2025, 1, 15),
                isExpense = true,
                isFixed = false,
                category = foodCategory
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemIncomePreview() {
    PeculeTheme {
        TransactionItem(
            transaction = Transaction(
                id = 2,
                label = "Vente Leboncoin",
                amount = 150.00,
                date = LocalDate.of(2025, 1, 20),
                isExpense = false,
                isFixed = false,
                category = null
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionItemDarkPreview() {
    val entertainmentCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Loisirs" }
    PeculeTheme(darkTheme = true) {
        TransactionItem(
            transaction = Transaction(
                id = 1,
                label = "Restaurant",
                amount = 42.00,
                date = LocalDate.of(2025, 1, 18),
                isExpense = true,
                isFixed = false,
                category = entertainmentCategory
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
