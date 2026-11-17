package com.nebulax.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cœur lumineux animé : battement, halo, anneaux holographiques en rotation
 * et particules en orbite. [energy] (0..1) amplifie l'effet, par exemple
 * selon le volume de la voix pendant l'écoute.
 */
@Composable
fun HoloHeart(modifier: Modifier = Modifier, energy: Float = 0f) {
    val t = rememberInfiniteTransition(label = "heart")
    val beat by t.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1100
                1f at 0
                1.12f at 120 using FastOutSlowInEasing
                0.98f at 260
                1.08f at 380
                1f at 640
            }
        ),
        label = "beat",
    )
    val spin by t.animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "spin")
    val scan by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "scan")

    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f
        val s = beat + energy * 0.15f

        // Halo
        drawCircle(
            Brush.radialGradient(listOf(Neb.Violet.copy(alpha = 0.45f + energy * 0.3f), Neb.Blue.copy(alpha = 0.12f), Color.Transparent), center = c, radius = r * 1.0f * s),
            radius = r * 1.0f * s, center = c,
        )

        // Anneaux holographiques (ellipses inclinées en rotation)
        for (i in 0 until 3) {
            rotate(spin * (if (i % 2 == 0) 1f else -0.7f) + i * 60f, c) {
                drawOval(
                    brush = Brush.sweepGradient(listOf(Neb.Blue, Neb.Violet, Neb.Pink, Neb.Blue), center = c),
                    topLeft = Offset(c.x - r * 0.92f, c.y - r * 0.3f),
                    size = Size(r * 1.84f, r * 0.6f),
                    style = Stroke(width = 2.2f),
                    alpha = 0.55f,
                )
            }
        }

        // Particules en orbite
        for (k in 0 until 14) {
            val a = Math.toRadians((spin * 1.6 + k * (360.0 / 14))).toFloat()
            val rr = r * (0.75f + 0.12f * sin(a * 3))
            drawCircle(
                if (k % 2 == 0) Neb.Blue else Neb.Violet,
                radius = 2.5f + (k % 3),
                center = Offset(c.x + cos(a) * rr, c.y + sin(a) * rr * 0.45f),
                alpha = 0.85f,
            )
        }

        // Cœur
        scale(s, c) {
            val heart = heartPath(c, r * 0.48f)
            drawPath(heart, Brush.radialGradient(listOf(Neb.Pink.copy(alpha = 0.5f), Color.Transparent), center = c, radius = r * 0.8f))
            drawPath(heart, Brush.linearGradient(listOf(Neb.Blue, Neb.Violet, Neb.Pink), start = Offset(c.x - r * 0.5f, c.y - r * 0.5f), end = Offset(c.x + r * 0.5f, c.y + r * 0.5f)))
            drawPath(heart, Color.White.copy(alpha = 0.7f), style = Stroke(width = 3f))
            scanLine(c, r * 0.48f, scan)
        }
    }
}

private fun DrawScope.scanLine(c: Offset, r: Float, p: Float) {
    val y = c.y - r * 0.9f + p * r * 1.9f
    drawLine(Color.White.copy(alpha = 0.35f), Offset(c.x - r * 1.05f, y), Offset(c.x + r * 1.05f, y), strokeWidth = 2f)
}

fun heartPath(c: Offset, r: Float): Path = Path().apply {
    moveTo(c.x, c.y + r * 0.95f)
    cubicTo(c.x - r * 1.45f, c.y + r * 0.1f, c.x - r * 1.05f, c.y - r * 1.15f, c.x, c.y - r * 0.45f)
    cubicTo(c.x + r * 1.05f, c.y - r * 1.15f, c.x + r * 1.45f, c.y + r * 0.1f, c.x, c.y + r * 0.95f)
    close()
}
