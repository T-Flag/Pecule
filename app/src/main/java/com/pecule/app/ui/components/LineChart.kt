package com.pecule.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pecule.app.domain.BalancePoint
import com.pecule.app.ui.theme.PeculeTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LineChart(
    data: List<BalancePoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    pointColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (data.isEmpty()) return

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM", Locale.FRANCE)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(start = 50.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height

            // Calculate min/max values for Y axis
            val minBalance = data.minOf { it.balance }
            val maxBalance = data.maxOf { it.balance }
            val range = if (maxBalance == minBalance) 1.0 else maxBalance - minBalance
            val padding = range * 0.1 // Add 10% padding
            val yMin = minBalance - padding
            val yMax = maxBalance + padding
            val yRange = yMax - yMin

            // Check if balance crosses zero
            val crossesZero = minBalance < 0 && maxBalance > 0

            // Calculate points
            val points = data.mapIndexed { index, point ->
                val x = if (data.size == 1) chartWidth / 2 else (index.toFloat() / (data.size - 1)) * chartWidth
                val y = chartHeight - ((point.balance - yMin) / yRange * chartHeight).toFloat()
                Offset(x, y)
            }

            // Draw zero line if balance crosses zero
            if (crossesZero) {
                val zeroY = chartHeight - ((0 - yMin) / yRange * chartHeight).toFloat()
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, zeroY),
                    end = Offset(chartWidth, zeroY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Draw filled area under the curve
            if (points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, chartHeight)
                    lineTo(points.first().x, points.first().y)
                    points.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    lineTo(points.last().x, chartHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, fillColor.copy(alpha = 0f)),
                        startY = 0f,
                        endY = chartHeight
                    )
                )
            }

            // Draw line connecting points
            if (points.size > 1) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3f)
                )
            }

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = pointColor,
                    radius = 6f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = point
                )
            }

            // Draw Y axis labels
            drawYAxisLabels(yMin, yMax, chartHeight, textColor, currencyFormat)

            // Draw X axis labels (dates)
            drawXAxisLabels(data, points, chartHeight, textColor, dateFormatter)
        }
    }
}

private fun DrawScope.drawYAxisLabels(
    yMin: Double,
    yMax: Double,
    chartHeight: Float,
    textColor: Color,
    currencyFormat: NumberFormat
) {
    val paint = android.graphics.Paint().apply {
        color = textColor.hashCode()
        textSize = 24f
        textAlign = android.graphics.Paint.Align.RIGHT
    }

    val steps = 3
    for (i in 0..steps) {
        val value = yMin + (yMax - yMin) * i / steps
        val y = chartHeight - (i.toFloat() / steps * chartHeight)

        drawContext.canvas.nativeCanvas.drawText(
            formatCompact(value),
            -8f,
            y + 8f,
            paint
        )
    }
}

private fun DrawScope.drawXAxisLabels(
    data: List<BalancePoint>,
    points: List<Offset>,
    chartHeight: Float,
    textColor: Color,
    dateFormatter: DateTimeFormatter
) {
    if (data.isEmpty()) return

    val paint = android.graphics.Paint().apply {
        color = textColor.hashCode()
        textSize = 22f
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // Show first, middle, and last dates if more than 3 points
    val indicesToShow = when {
        data.size <= 3 -> data.indices.toList()
        data.size <= 5 -> listOf(0, data.size - 1)
        else -> listOf(0, data.size / 2, data.size - 1)
    }

    indicesToShow.forEach { index ->
        val point = points[index]
        val date = data[index].date

        drawContext.canvas.nativeCanvas.drawText(
            date.format(dateFormatter),
            point.x,
            chartHeight + 40f,
            paint
        )
    }
}

private fun formatCompact(value: Double): String {
    return when {
        value >= 1000 || value <= -1000 -> String.format(Locale.FRANCE, "%.0fk€", value / 1000)
        else -> String.format(Locale.FRANCE, "%.0f€", value)
    }
}

@Composable
fun BalanceHistoryCard(
    balanceHistory: List<BalancePoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Évolution du solde",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (balanceHistory.size <= 1) {
                Text(
                    text = "Pas assez de données pour afficher le graphique",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LineChart(
                    data = balanceHistory,
                    height = 180.dp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LineChartPreview() {
    PeculeTheme {
        LineChart(
            data = listOf(
                BalancePoint(LocalDate.of(2025, 1, 25), 2500.0),
                BalancePoint(LocalDate.of(2025, 1, 27), 2300.0),
                BalancePoint(LocalDate.of(2025, 1, 29), 2600.0),
                BalancePoint(LocalDate.of(2025, 2, 1), 2100.0),
                BalancePoint(LocalDate.of(2025, 2, 5), 2400.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LineChartNegativePreview() {
    PeculeTheme {
        LineChart(
            data = listOf(
                BalancePoint(LocalDate.of(2025, 1, 25), 500.0),
                BalancePoint(LocalDate.of(2025, 1, 27), 200.0),
                BalancePoint(LocalDate.of(2025, 1, 29), -100.0),
                BalancePoint(LocalDate.of(2025, 2, 1), -300.0),
                BalancePoint(LocalDate.of(2025, 2, 5), 100.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceHistoryCardPreview() {
    PeculeTheme {
        BalanceHistoryCard(
            balanceHistory = listOf(
                BalancePoint(LocalDate.of(2025, 1, 25), 2500.0),
                BalancePoint(LocalDate.of(2025, 1, 27), 2300.0),
                BalancePoint(LocalDate.of(2025, 1, 29), 2600.0),
                BalancePoint(LocalDate.of(2025, 2, 1), 2100.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceHistoryCardEmptyPreview() {
    PeculeTheme {
        BalanceHistoryCard(
            balanceHistory = listOf(
                BalancePoint(LocalDate.of(2025, 1, 25), 2500.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
