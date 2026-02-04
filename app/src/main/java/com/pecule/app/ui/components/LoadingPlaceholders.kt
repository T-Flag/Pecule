package com.pecule.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pecule.app.ui.theme.PeculeTheme

@Composable
fun BalanceCardPlaceholder(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Greeting placeholder
            ShimmerEffect(
                modifier = Modifier
                    .width(150.dp)
                    .height(20.dp),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Balance placeholder
            ShimmerEffect(
                modifier = Modifier
                    .width(200.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gauge placeholder (semi-circle)
            ShimmerEffect(
                modifier = Modifier
                    .size(180.dp, 90.dp),
                shape = RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Percentage text placeholder
            ShimmerEffect(
                modifier = Modifier
                    .width(100.dp)
                    .height(16.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Composable
fun TransactionItemPlaceholder(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon placeholder
        ShimmerEffect(
            modifier = Modifier.size(48.dp),
            shape = CircleShape
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Label and date placeholder
        Column(modifier = Modifier.weight(1f)) {
            ShimmerEffect(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerEffect(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }

        // Amount placeholder
        ShimmerEffect(
            modifier = Modifier
                .width(70.dp)
                .height(16.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun DonutChartPlaceholder(
    modifier: Modifier = Modifier,
    size: Int = 200
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ShimmerEffect(
            modifier = Modifier.size(size.dp),
            shape = CircleShape
        )
    }
}

@Composable
fun TransactionListPlaceholder(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(itemCount) {
            TransactionItemPlaceholder()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceCardPlaceholderPreview() {
    PeculeTheme {
        BalanceCardPlaceholder(
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemPlaceholderPreview() {
    PeculeTheme {
        TransactionItemPlaceholder(
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionListPlaceholderPreview() {
    PeculeTheme {
        TransactionListPlaceholder(
            itemCount = 3,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPlaceholderPreview() {
    PeculeTheme {
        DonutChartPlaceholder(
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BalanceCardPlaceholderDarkPreview() {
    PeculeTheme(darkTheme = true) {
        BalanceCardPlaceholder(
            modifier = Modifier.padding(16.dp)
        )
    }
}
