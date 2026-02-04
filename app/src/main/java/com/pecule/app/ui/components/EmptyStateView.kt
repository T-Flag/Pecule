package com.pecule.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pecule.app.ui.theme.PeculeTheme

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 56.dp,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 400),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .alpha(alpha)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewWithActionPreview() {
    PeculeTheme {
        EmptyStateView(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Rien à afficher",
            subtitle = "Ajoutez votre première dépense avec le bouton +",
            actionLabel = "Ajouter une dépense",
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewFixedPreview() {
    PeculeTheme {
        EmptyStateView(
            icon = Icons.Default.Repeat,
            title = "Pas de charges fixes",
            subtitle = "Les charges fixes sont dupliquées automatiquement à chaque nouveau cycle"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewVariablePreview() {
    PeculeTheme {
        EmptyStateView(
            icon = Icons.Default.ShoppingBag,
            title = "Pas de dépenses variables",
            subtitle = "Ajoutez vos dépenses quotidiennes ici"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewIncomesPreview() {
    PeculeTheme {
        EmptyStateView(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Pas de revenus",
            subtitle = "Ajoutez vos revenus supplémentaires ici"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewStatisticsPreview() {
    PeculeTheme {
        EmptyStateView(
            icon = Icons.Default.BarChart,
            title = "Pas encore de statistiques",
            subtitle = "Les graphiques apparaîtront après vos premières dépenses"
        )
    }
}
