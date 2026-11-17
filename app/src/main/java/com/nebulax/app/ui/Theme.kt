package com.nebulax.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

object Neb {
    val Black = Color(0xFF03030C)
    val Deep = Color(0xFF0A0826)
    val Blue = Color(0xFF2EE6FF)
    val BlueDeep = Color(0xFF1B5CFF)
    val Violet = Color(0xFF9B5CFF)
    val Pink = Color(0xFFFF4FD8)
    val Text = Color(0xFFEAF2FF)
    val Dim = Color(0xFF8E9BC4)
    val Glass = Color(0x1A8FB4FF)
    val GlassBorder = Color(0x5545D5FF)
}

@Composable
fun NebulaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Neb.Blue,
            secondary = Neb.Violet,
            background = Neb.Black,
            surface = Neb.Deep,
            onPrimary = Neb.Black,
            onBackground = Neb.Text,
            onSurface = Neb.Text,
        ),
        content = content,
    )
}

private data class Star(val x: Float, val y: Float, val r: Float, val phase: Float)

/** Fond spatial : dégradé noir/bleu/violet, nébuleuses et étoiles scintillantes. */
@Composable
fun SpaceBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val stars = remember { List(110) { Star(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2.2f + 0.6f, Random.nextFloat() * 6.28f) } }
    val t = rememberInfiniteTransition(label = "space")
    val time by t.animateFloat(0f, 6.2832f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "time")
    val drift by t.animateFloat(0f, 1f, infiniteRepeatable(tween(14000), RepeatMode.Reverse), label = "drift")
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Neb.Black, Neb.Deep, Color(0xFF120A33))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                Brush.radialGradient(listOf(Neb.Violet.copy(alpha = 0.28f), Color.Transparent), center = Offset(size.width * (0.15f + 0.1f * drift), size.height * 0.2f), radius = size.width * 0.8f),
                radius = size.width * 0.8f, center = Offset(size.width * (0.15f + 0.1f * drift), size.height * 0.2f),
            )
            drawCircle(
                Brush.radialGradient(listOf(Neb.BlueDeep.copy(alpha = 0.25f), Color.Transparent), center = Offset(size.width * (0.9f - 0.1f * drift), size.height * 0.8f), radius = size.width * 0.9f),
                radius = size.width * 0.9f, center = Offset(size.width * (0.9f - 0.1f * drift), size.height * 0.8f),
            )
            stars.forEach { s ->
                val a = 0.35f + 0.65f * ((kotlin.math.sin(time + s.phase) + 1f) / 2f)
                drawCircle(Color.White.copy(alpha = a), radius = s.r, center = Offset(s.x * size.width, s.y * size.height))
            }
        }
        content()
    }
}

/** Panneau en verre holographique cliquable. */
@Composable
fun HoloPanel(
    modifier: Modifier = Modifier,
    accent: Color = Neb.Blue,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    var m = modifier
        .clip(shape)
        .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), Neb.Violet.copy(alpha = 0.08f))))
        .border(1.5.dp, Brush.linearGradient(listOf(accent.copy(alpha = 0.9f), Neb.Violet.copy(alpha = 0.5f))), shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Box(m.padding(16.dp), contentAlignment = Alignment.Center, content = content)
}

@Composable
fun NebTitle(text: String, size: Int = 28) {
    Text(text, color = Neb.Text, fontSize = size.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
}
