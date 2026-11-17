package com.nebulax.app.gesture

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.abs

enum class Swipe { LEFT, RIGHT, UP, DOWN }

/**
 * Détection de gestes par la caméra frontale, sans IA externe :
 * on compare chaque image à la précédente (luminance sur une grille 32×24),
 * on calcule le centre du mouvement, puis on suit sa trajectoire.
 * Un déplacement rapide et ample → balayage gauche / droite / haut / bas.
 *
 * Limites : fonctionne mieux avec un bon éclairage et un fond immobile.
 */
class MotionGestureAnalyzer(
    private val mirror: Boolean = true,
    private val onMotion: (x: Float, y: Float, energy: Float) -> Unit,
    private val onSwipe: (Swipe) -> Unit,
) : ImageAnalysis.Analyzer {

    private val gw = 32
    private val gh = 24
    private var prev: FloatArray? = null
    private val track = ArrayDeque<Triple<Long, Float, Float>>()
    private var lastSwipe = 0L

    override fun analyze(image: ImageProxy) {
        try {
            val plane = image.planes[0]
            val buf = plane.buffer
            val rowStride = plane.rowStride
            val pixStride = plane.pixelStride
            val w = image.width
            val h = image.height
            val rot = image.imageInfo.rotationDegrees

            val cur = FloatArray(gw * gh)
            for (gy in 0 until gh) {
                for (gx in 0 until gw) {
                    val px = (gx * w) / gw + w / (gw * 2)
                    val py = (gy * h) / gh + h / (gh * 2)
                    val idx = py * rowStride + px * pixStride
                    cur[gy * gw + gx] = if (idx < buf.limit()) (buf.get(idx).toInt() and 0xFF).toFloat() else 0f
                }
            }

            val p = prev
            prev = cur
            if (p == null) return

            var sum = 0f
            var sx = 0f
            var sy = 0f
            for (i in cur.indices) {
                val d = abs(cur[i] - p[i])
                if (d > 28f) {
                    sum += d
                    sx += (i % gw) * d
                    sy += (i / gw) * d
                }
            }
            val energy = (sum / (gw * gh * 60f)).coerceIn(0f, 1f)
            if (sum < 2500f) {
                onMotion(0.5f, 0.5f, 0f)
                if (track.isNotEmpty() && System.currentTimeMillis() - track.last().first > 300) track.clear()
                return
            }

            // Coordonnées dans le repère de l'écran (portrait), caméra frontale miroir.
            var nx = (sx / sum) / gw
            var ny = (sy / sum) / gh
            when (rot) {
                90 -> { val t = nx; nx = 1f - ny; ny = t }
                270 -> { val t = nx; nx = ny; ny = 1f - t }
                180 -> { nx = 1f - nx; ny = 1f - ny }
            }
            if (mirror) nx = 1f - nx
            onMotion(nx, ny, energy)

            val now = System.currentTimeMillis()
            track.addLast(Triple(now, nx, ny))
            while (track.isNotEmpty() && now - track.first().first > 700) track.removeFirst()

            if (now - lastSwipe > 900 && track.size >= 3) {
                val dx = track.last().second - track.first().second
                val dy = track.last().third - track.first().third
                val swipe = when {
                    abs(dx) > 0.33f && abs(dx) > abs(dy) * 1.3f -> if (dx > 0) Swipe.RIGHT else Swipe.LEFT
                    abs(dy) > 0.33f && abs(dy) > abs(dx) * 1.3f -> if (dy > 0) Swipe.DOWN else Swipe.UP
                    else -> null
                }
                if (swipe != null) {
                    lastSwipe = now
                    track.clear()
                    onSwipe(swipe)
                }
            }
        } finally {
            image.close()
        }
    }
}
