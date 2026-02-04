package com.pecule.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.ui.theme.PeculeTheme
import java.text.NumberFormat
import java.util.Locale

object CategoryColors {
    val colors = mapOf(
        Category.SALARY to Color(0xFF4CAF50),
        Category.FOOD to Color(0xFFFF9800),
        Category.TRANSPORT to Color(0xFF2196F3),
        Category.HOUSING to Color(0xFF9C27B0),
        Category.UTILITIES to Color(0xFFFFEB3B),
        Category.ENTERTAINMENT to Color(0xFFE91E63),
        Category.HEALTH to Color(0xFF00BCD4),
        Category.SHOPPING to Color(0xFFFF5722),
        Category.OTHER to Color(0xFF607D8B)
    )

    fun getColor(category: Category): Color = colors[category] ?: Color.Gray
}

@Composable
fun DonutChart(
    data: Map<Category, Double>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 32.dp
) {
    val total = data.values.sum()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

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
                    val sweepAngle = (amount / total * 360f).toFloat()
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
            text = currencyFormat.format(total),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    PeculeTheme {
        DonutChart(
            data = mapOf(
                Category.FOOD to 250.0,
                Category.TRANSPORT to 100.0,
                Category.HOUSING to 800.0,
                Category.UTILITIES to 150.0,
                Category.ENTERTAINMENT to 75.0
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
    PeculeTheme {
        DonutChart(
            data = mapOf(Category.HOUSING to 1000.0)
        )
    }
}
