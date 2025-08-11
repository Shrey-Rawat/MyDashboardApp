package com.mydashboardapp.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

data class ChartData(
    val label: String,
    val value: Float,
    val color: Color = Color.Blue
)

@Composable
fun BarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
    showValues: Boolean = true,
    animate: Boolean = true
) {
    val animationDuration = if (animate) 1000 else 0
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "bar_chart_animation"
    )
    
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val actualMaxValue = maxValue ?: data.maxOfOrNull { it.value } ?: 1f
    
    val contentDescriptions = data.joinToString(", ") { "${it.label}: ${it.value}" }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .semantics {
                contentDescription = "Bar chart showing: $contentDescriptions"
            }
    ) {
        val barWidth = size.width / data.size * 0.7f
        val spacing = size.width / data.size * 0.3f
        val maxHeight = size.height * 0.8f
        
        data.forEachIndexed { index, item ->
            val barHeight = (item.value / actualMaxValue) * maxHeight * animatedProgress
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight
            
            // Draw bar
            drawRoundRect(
                color = item.color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            
            // Draw value on top of bar
            if (showValues) {
                val textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                val textLayoutResult = textMeasurer.measure(
                    text = item.value.toString(),
                    style = textStyle
                )
                
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x + (barWidth - textLayoutResult.size.width) / 2,
                        y - textLayoutResult.size.height - 8.dp.toPx()
                    )
                )
            }
            
            // Draw label
            val labelStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 10.sp
            )
            val labelLayoutResult = textMeasurer.measure(
                text = item.label,
                style = labelStyle
            )
            
            drawText(
                textLayoutResult = labelLayoutResult,
                topLeft = Offset(
                    x + (barWidth - labelLayoutResult.size.width) / 2,
                    size.height - labelLayoutResult.size.height
                )
            )
        }
    }
}

@Composable
fun PieChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    centerRadius: Float = 0.3f,
    animate: Boolean = true
) {
    val animationDuration = if (animate) 1000 else 0
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "pie_chart_animation"
    )
    
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val contentDescriptions = data.joinToString(", ") { 
        "${it.label}: ${(it.value / total * 100).toInt()}%" 
    }
    
    Canvas(
        modifier = modifier
            .size(200.dp)
            .semantics {
                contentDescription = "Pie chart showing: $contentDescriptions"
            }
    ) {
        val radius = minOf(size.width, size.height) / 2
        val center = Offset(size.width / 2, size.height / 2)
        var currentAngle = -90f // Start from top
        
        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f * animatedProgress
            
            drawArc(
                color = item.color,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2)
            )
            
            currentAngle += sweepAngle
        }
        
        // Draw center hole for donut effect
        if (centerRadius > 0f) {
            drawCircle(
                color = Color.White,
                radius = radius * centerRadius,
                center = center
            )
        }
    }
}

@Composable
fun LineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    animate: Boolean = true
) {
    val animationDuration = if (animate) 1500 else 0
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "line_chart_animation"
    )
    
    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = maxValue - minValue
    
    val contentDescription = "Line chart with ${data.size} data points, " +
            "ranging from $minValue to $maxValue"
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        if (data.size < 2) return@Canvas
        
        val stepX = size.width / (data.size - 1)
        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range * size.height)
            Offset(x, y)
        }
        
        // Draw animated path
        for (i in 0 until (points.size - 1)) {
            if (i < points.size * animatedProgress - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
        
        // Draw points
        points.forEachIndexed { index, point ->
            if (index < points.size * animatedProgress) {
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChartsPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Bar Chart", style = MaterialTheme.typography.headlineSmall)
        BarChart(
            data = listOf(
                ChartData("Jan", 100f, Color.Blue),
                ChartData("Feb", 150f, Color.Green),
                ChartData("Mar", 80f, Color.Red),
                ChartData("Apr", 200f, Color.Yellow)
            )
        )
        
        Text("Pie Chart", style = MaterialTheme.typography.headlineSmall)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            PieChart(
                data = listOf(
                    ChartData("A", 30f, Color.Blue),
                    ChartData("B", 40f, Color.Green),
                    ChartData("C", 20f, Color.Red),
                    ChartData("D", 10f, Color.Yellow)
                )
            )
        }
        
        Text("Line Chart", style = MaterialTheme.typography.headlineSmall)
        LineChart(
            data = listOf(10f, 20f, 15f, 30f, 25f, 40f, 35f)
        )
    }
}
