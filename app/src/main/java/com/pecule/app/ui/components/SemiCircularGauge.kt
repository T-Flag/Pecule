package com.pecule.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pecule.app.ui.theme.PeculeTheme

@Composable
fun SemiCircularGauge(
    percentage: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.outlineVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    val clampedPercentage = percentage.coerceIn(0f, 1f)

    val animatedPercentage by animateFloatAsState(
        targetValue = clampedPercentage,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "gauge_animation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
    ) {
        val strokeWidth = 24.dp.toPx()
        val arcSize = Size(
            width = size.width - strokeWidth,
            height = (size.height - strokeWidth / 2) * 2
        )
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        // Background arc
        drawArc(
            color = backgroundColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc with animation
        if (animatedPercentage > 0f) {
            drawArc(
                color = progressColor,
                startAngle = 180f,
                sweepAngle = 180f * animatedPercentage,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SemiCircularGaugePreview() {
    PeculeTheme {
        SemiCircularGauge(percentage = 0.5f)
    }
}

@Preview(showBackground = true)
@Composable
private fun SemiCircularGaugeFullPreview() {
    PeculeTheme {
        SemiCircularGauge(percentage = 1f)
    }
}

@Preview(showBackground = true)
@Composable
private fun SemiCircularGaugeEmptyPreview() {
    PeculeTheme {
        SemiCircularGauge(percentage = 0f)
    }
}
