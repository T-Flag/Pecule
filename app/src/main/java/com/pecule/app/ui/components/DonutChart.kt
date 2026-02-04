package com.pecule.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.domain.CategoryInitializer
import com.pecule.app.ui.theme.PeculeTheme
import java.text.NumberFormat
import java.util.Locale

object CategoryColors {
    fun getColor(category: CategoryEntity): Color {
        return Color(category.color)
    }
}

@Composable
fun DonutChart(
    data: Map<CategoryEntity, Double>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 32.dp
) {
    val total = data.values.sum()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    val animationProgress by animateFloatAsState(
        targetValue = if (total > 0) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "donut_animation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (total > 0) {
            Canvas(modifier = Modifier.size(size)) {
                var startAngle = -90f
                val stroke = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Butt
                )

                data.forEach { (category, amount) ->
                    val sweepAngle = (amount / total * 360f * animationProgress).toFloat()
                    drawArc(
                        color = CategoryColors.getColor(category),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = stroke
                    )
                    startAngle += sweepAngle
                }
            }
        } else {
            Canvas(modifier = Modifier.size(size)) {
                drawArc(
                    color = Color.LightGray,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx())
                )
            }
        }

        Text(
            text = currencyFormat.format(total * animationProgress),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    val categories = CategoryInitializer.DEFAULT_CATEGORIES
    PeculeTheme {
        DonutChart(
            data = mapOf(
                categories[1] to 250.0,  // Alimentation
                categories[2] to 100.0,  // Transport
                categories[3] to 800.0,  // Logement
                categories[4] to 150.0,  // Factures
                categories[5] to 75.0    // Loisirs
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartEmptyPreview() {
    PeculeTheme {
        DonutChart(data = emptyMap())
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartSingleCategoryPreview() {
    val housingCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Logement" }!!
    PeculeTheme {
        DonutChart(
            data = mapOf(housingCategory to 1000.0)
        )
    }
}
