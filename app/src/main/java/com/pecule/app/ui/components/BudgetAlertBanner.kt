package com.pecule.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pecule.app.domain.BudgetAlert
import com.pecule.app.domain.BudgetAlertLevel
import com.pecule.app.ui.theme.PeculeTheme

@Composable
fun BudgetAlertBanner(
    alert: BudgetAlert,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && alert.level != BudgetAlertLevel.NONE,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val backgroundColor = when (alert.level) {
            BudgetAlertLevel.WARNING -> Color(0xFFFFF3E0) // Light orange
            BudgetAlertLevel.DANGER -> Color(0xFFFFEBEE) // Light red
            BudgetAlertLevel.EXCEEDED -> Color(0xFFD32F2F).copy(alpha = 0.15f) // Dark red tint
            BudgetAlertLevel.NONE -> Color.Transparent
        }

        val iconColor = when (alert.level) {
            BudgetAlertLevel.WARNING -> Color(0xFFE65100) // Deep orange
            BudgetAlertLevel.DANGER -> Color(0xFFD32F2F) // Red
            BudgetAlertLevel.EXCEEDED -> Color(0xFFB71C1C) // Dark red
            BudgetAlertLevel.NONE -> Color.Transparent
        }

        val textColor = when (alert.level) {
            BudgetAlertLevel.WARNING -> Color(0xFFE65100)
            BudgetAlertLevel.DANGER -> Color(0xFFC62828)
            BudgetAlertLevel.EXCEEDED -> Color(0xFFB71C1C)
            BudgetAlertLevel.NONE -> Color.Transparent
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetAlertBannerWarningPreview() {
    PeculeTheme {
        BudgetAlertBanner(
            alert = BudgetAlert(
                level = BudgetAlertLevel.WARNING,
                message = "Attention, vous avez utilisé 65% de votre budget",
                percentage = 65
            ),
            visible = true,
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetAlertBannerDangerPreview() {
    PeculeTheme {
        BudgetAlertBanner(
            alert = BudgetAlert(
                level = BudgetAlertLevel.DANGER,
                message = "Budget critique : 90% utilisé",
                percentage = 90
            ),
            visible = true,
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetAlertBannerExceededPreview() {
    PeculeTheme {
        BudgetAlertBanner(
            alert = BudgetAlert(
                level = BudgetAlertLevel.EXCEEDED,
                message = "Budget dépassé ! 120% utilisé",
                percentage = 120
            ),
            visible = true,
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
