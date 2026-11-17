package com.nebulax.app.ui

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Mode cinéma immersif : plein écran (barres système masquées), écran
 * toujours allumé, scènes animées. Il s'agit d'un affichage sur l'écran du
 * téléphone, pas d'une projection optique.
 */
@Composable
fun CinemaScreen(onExit: () -> Unit) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val ctrl = WindowCompat.getInsetsController(window, view)
        ctrl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ctrl.hide(WindowInsetsCompat.Type.systemBars())
        view.keepScreenOn = true
        val lp = window.attributes
        val oldBrightness = lp.screenBrightness
        onDispose {
            ctrl.show(WindowInsetsCompat.Type.systemBars())
            view.keepScreenOn = false
            window.attributes = window.attributes.also { it.screenBrightness = oldBrightness }
        }
    }

    val scenes = listOf("Hyperespace", "Nébuleuse", "Aurore holographique")
    var scene by remember { mutableIntStateOf(0) }
    var showHud by remember { mutableStateOf(true) }
    LaunchedEffect(scene, showHud) {
        if (showHud) {
            delay(3500); showHud = false
        }
    }

    val t = rememberInfiniteTransition(label = "cinema")
    val time by t.animateFloat(0f, 1000f, infiniteRepeatable(tween(1_000_000, easing = LinearEasing)), label = "time")
    val stars = remember { List(260) { Triple(Random.nextFloat() * 2f - 1f, Random.nextFloat() * 2f - 1f, Random.nextFloat()) } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { scene = (scene + 1) % scenes.size; showHud = true },
                    onDoubleTap = { onExit() },
                    onLongPress = { showHud = true },
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            when (scene) {
                0 -> hyperspace(stars, time)
                1 -> nebula(time)
                else -> aurora(time)
            }
            // Lignes de balayage (effet holographique)
            var y = (time * 40f) % 6f
            while (y < size.height) {
                drawLine(Color.White.copy(alpha = 0.035f), Offset(0f, y), Offset(size.width, y), 1f)
                y += 6f
            }
        }
        if (showHud) {
            Column(Modifier.align(Alignment.BottomCenter).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(scenes[scene].uppercase(), color = Neb.Blue, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Text("Toucher : scène suivante · Double toucher : quitter", color = Neb.Dim, fontSize = 13.sp)
            }
        }
    }
}

private fun DrawScope.hyperspace(stars: List<Triple<Float, Float, Float>>, time: Float) {
    val c = center
    val speed = 0.25f
    stars.forEach { (sx, sy, sz) ->
        val z = 1f - ((sz + time * speed) % 1f)
        val k = 1f / (z * 4f + 0.05f)
        val x = c.x + sx * k * size.minDimension * 0.12f
        val y = c.y + sy * k * size.minDimension * 0.12f
        val kPrev = 1f / ((z + 0.03f) * 4f + 0.05f)
        val px = c.x + sx * kPrev * size.minDimension * 0.12f
        val py = c.y + sy * kPrev * size.minDimension * 0.12f
        val a = (1f - z).coerceIn(0f, 1f)
        val col = if (sx > 0) Neb.Blue else Neb.Violet
        drawLine(col.copy(alpha = a), Offset(px, py), Offset(x, y), strokeWidth = 1f + 3f * a)
    }
    drawCircle(Brush.radialGradient(listOf(Neb.Violet.copy(alpha = 0.35f), Color.Transparent), c, size.minDimension * 0.3f), size.minDimension * 0.3f, c)
}

private fun DrawScope.nebula(time: Float) {
    val cols = listOf(Neb.Violet, Neb.Blue, Neb.Pink, Neb.BlueDeep, Neb.Violet)
    cols.forEachIndexed { i, col ->
        val a = time * (0.08f + i * 0.02f) + i * 1.3f
        val p = Offset(center.x + cos(a) * size.width * 0.3f, center.y + sin(a * 0.8f) * size.height * 0.25f)
        val r = size.maxDimension * (0.35f + 0.05f * sin(a * 2))
        drawCircle(Brush.radialGradient(listOf(col.copy(alpha = 0.45f), Color.Transparent), p, r), r, p)
    }
}

private fun DrawScope.aurora(time: Float) {
    for (band in 0 until 5) {
        val col = if (band % 2 == 0) Neb.Blue else Neb.Violet
        var x = 0f
        val baseY = size.height * (0.25f + band * 0.12f)
        while (x < size.width) {
            val y = baseY + sin(x / 140f + time * (0.6f + band * 0.15f)) * 60f
            val h = 120f + 80f * sin(x / 90f + time + band)
            drawLine(
                Brush.verticalGradient(listOf(Color.Transparent, col.copy(alpha = 0.35f), Color.Transparent), y - h, y + h),
                Offset(x, y - h), Offset(x, y + h), strokeWidth = 6f,
            )
            x += 7f
        }
    }
}
