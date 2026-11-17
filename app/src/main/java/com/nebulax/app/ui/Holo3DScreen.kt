package com.nebulax.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class V3(val x: Float, val y: Float, val z: Float)

class Shape3D(val name: String, val vertices: List<V3>, val edges: List<Pair<Int, Int>>)

/** Arêtes = paires de sommets à la distance minimale (pour les solides réguliers). */
private fun autoEdges(v: List<V3>): List<Pair<Int, Int>> {
    fun d(a: V3, b: V3) = sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) + (a.z - b.z) * (a.z - b.z))
    var min = Float.MAX_VALUE
    for (i in v.indices) for (j in i + 1 until v.size) min = minOf(min, d(v[i], v[j]))
    val out = mutableListOf<Pair<Int, Int>>()
    for (i in v.indices) for (j in i + 1 until v.size) if (d(v[i], v[j]) < min * 1.05f) out += i to j
    return out
}

val SHAPES: List<Shape3D> by lazy {
    val p = (1f + sqrt(5f)) / 2f
    val ico = listOf(
        V3(-1f, p, 0f), V3(1f, p, 0f), V3(-1f, -p, 0f), V3(1f, -p, 0f),
        V3(0f, -1f, p), V3(0f, 1f, p), V3(0f, -1f, -p), V3(0f, 1f, -p),
        V3(p, 0f, -1f), V3(p, 0f, 1f), V3(-p, 0f, -1f), V3(-p, 0f, 1f),
    ).map { V3(it.x / 1.9f, it.y / 1.9f, it.z / 1.9f) }
    val cube = buildList { for (x in listOf(-1f, 1f)) for (y in listOf(-1f, 1f)) for (z in listOf(-1f, 1f)) add(V3(x * 0.8f, y * 0.8f, z * 0.8f)) }
    val octa = listOf(V3(1.1f, 0f, 0f), V3(-1.1f, 0f, 0f), V3(0f, 1.1f, 0f), V3(0f, -1.1f, 0f), V3(0f, 0f, 1.1f), V3(0f, 0f, -1.1f))
    // Tore (anneau) : grille de points sur un donut
    val torusV = mutableListOf<V3>()
    val torusE = mutableListOf<Pair<Int, Int>>()
    val nu = 18; val nv = 8
    for (i in 0 until nu) for (j in 0 until nv) {
        val u = i * 2 * Math.PI.toFloat() / nu
        val w = j * 2 * Math.PI.toFloat() / nv
        torusV += V3((0.9f + 0.35f * cos(w)) * cos(u), 0.35f * sin(w), (0.9f + 0.35f * cos(w)) * sin(u))
        torusE += (i * nv + j) to (((i + 1) % nu) * nv + j)
        torusE += (i * nv + j) to (i * nv + (j + 1) % nv)
    }
    listOf(
        Shape3D("Icosaèdre", ico, autoEdges(ico)),
        Shape3D("Cube", cube, autoEdges(cube)),
        Shape3D("Octaèdre", octa, autoEdges(octa)),
        Shape3D("Anneau quantique", torusV, torusE),
    )
}

@Composable
fun Holo3DScreen(onBack: () -> Unit) {
    var rx by remember { mutableFloatStateOf(0.4f) }
    var ry by remember { mutableFloatStateOf(0.6f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var shapeIdx by remember { mutableIntStateOf(0) }
    val t = rememberInfiniteTransition(label = "holo3d")
    val auto by t.animateFloat(0f, (2 * Math.PI).toFloat(), infiniteRepeatable(tween(16000, easing = LinearEasing)), label = "auto")
    val shape = SHAPES[shapeIdx]

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar("Hologramme 3D · ${shape.name}", onBack)
        Canvas(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, z, _ ->
                        ry += pan.x / 220f
                        rx -= pan.y / 220f
                        zoom = (zoom * z).coerceIn(0.5f, 2.5f)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { shapeIdx = (shapeIdx + 1) % SHAPES.size })
                }
        ) {
            val c = center
            val scale = size.minDimension * 0.32f * zoom
            val ay = ry + auto
            val ax = rx
            fun project(v: V3): Triple<Offset, Float, Float> {
                // rotation Y puis X
                val x1 = v.x * cos(ay) + v.z * sin(ay)
                val z1 = -v.x * sin(ay) + v.z * cos(ay)
                val y2 = v.y * cos(ax) - z1 * sin(ax)
                val z2 = v.y * sin(ax) + z1 * cos(ax)
                val persp = 3.2f / (3.2f + z2)
                return Triple(Offset(c.x + x1 * scale * persp, c.y + y2 * scale * persp), z2, persp)
            }
            // socle holographique
            drawOval(
                Brush.radialGradient(listOf(Neb.Blue.copy(alpha = 0.35f), Color.Transparent), Offset(c.x, c.y + scale * 1.35f), scale),
                topLeft = Offset(c.x - scale, c.y + scale * 1.2f), size = androidx.compose.ui.geometry.Size(scale * 2f, scale * 0.3f),
            )
            val pts = shape.vertices.map { project(it) }
            shape.edges.forEach { (i, j) ->
                val a = pts[i]; val b = pts[j]
                val depth = ((a.second + b.second) / 2f + 1.2f) / 2.4f // 0 = devant, 1 = derrière
                val alpha = (1f - depth * 0.7f).coerceIn(0.2f, 1f)
                drawLine(Neb.Violet.copy(alpha = alpha * 0.35f), a.first, b.first, strokeWidth = 9f)
                drawLine(Brush.linearGradient(listOf(Neb.Blue, Neb.Violet), a.first, b.first), a.first, b.first, strokeWidth = 2.5f, alpha = alpha)
            }
            pts.forEach { (o, _, persp) -> drawCircle(Color.White, radius = 3.5f * persp, center = o, alpha = 0.9f) }
        }
        Text(
            "Glisser : faire tourner · Pincer : zoomer · Double toucher : changer de forme",
            color = Neb.Dim, fontSize = 13.sp, textAlign = TextAlign.Center,
        )
    }
}
