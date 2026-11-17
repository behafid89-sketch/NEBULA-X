package com.nebulax.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nebulax.app.gesture.MotionGestureAnalyzer
import com.nebulax.app.gesture.Swipe
import java.util.concurrent.Executors

private val panels = listOf(
    "🌌" to "Galaxie",
    "🪐" to "Saturne",
    "☄️" to "Comète",
    "🌠" to "Étoile filante",
    "🛰️" to "Satellite",
)

@Composable
fun GestureScreen(onBack: () -> Unit, speak: (String) -> Unit) {
    val ctx = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) ask.launch(Manifest.permission.CAMERA) }

    var index by remember { mutableIntStateOf(0) }
    var lastGesture by remember { mutableStateOf("Passez la main devant la caméra frontale, de gauche à droite.") }
    var hx by remember { mutableFloatStateOf(0.5f) }
    var hy by remember { mutableFloatStateOf(0.5f) }
    var energy by remember { mutableFloatStateOf(0f) }
    val tilt = remember { Animatable(0f) }

    LaunchedEffect(index) {
        tilt.snapTo(0f)
    }

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar("Commandes gestuelles", onBack)
        Spacer(Modifier.height(10.dp))
        if (!granted) {
            HoloPanel(Modifier.fillMaxWidth(), onClick = { ask.launch(Manifest.permission.CAMERA) }) {
                Text("Autorisez la caméra pour utiliser les gestes.\nTouchez ici.", color = Neb.Text, textAlign = TextAlign.Center)
            }
            return@Column
        }
        Box(
            Modifier
                .size(170.dp, 220.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, Neb.GlassBorder, RoundedCornerShape(22.dp))
        ) {
            CameraFeed(
                onMotion = { x, y, e -> hx = x; hy = y; energy = e },
                onSwipe = { s ->
                    when (s) {
                        Swipe.LEFT -> { index = (index + 1) % panels.size; lastGesture = "Geste vers la gauche → suivant" }
                        Swipe.RIGHT -> { index = (index - 1 + panels.size) % panels.size; lastGesture = "Geste vers la droite → précédent" }
                        Swipe.UP -> lastGesture = "Geste vers le haut"
                        Swipe.DOWN -> lastGesture = "Geste vers le bas"
                    }
                    if (s == Swipe.LEFT || s == Swipe.RIGHT) speak(panels[index].second)
                },
            )
            Canvas(Modifier.fillMaxSize()) {
                if (energy > 0f) {
                    val p = Offset(hx * size.width, hy * size.height)
                    drawCircle(Brush.radialGradient(listOf(Neb.Blue.copy(alpha = 0.8f), Color.Transparent), p, 60f), 60f, p)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            val prev = panels[(index - 1 + panels.size) % panels.size]
            val cur = panels[index]
            val next = panels[(index + 1) % panels.size]
            HoloCard(prev, false, -35f)
            HoloCard(cur, true, 0f)
            HoloCard(next, false, 35f)
        }
        Text(lastGesture, color = Neb.Blue, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Expérimental : détection de mouvement par la caméra. Fonctionne mieux avec un bon éclairage et un fond immobile.",
            color = Neb.Dim, fontSize = 12.sp, textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HoloCard(item: Pair<String, String>, focused: Boolean, rot: Float) {
    val anim = remember { Animatable(rot + if (rot == 0f) 0f else rot * 0.3f) }
    LaunchedEffect(item, rot) { anim.animateTo(rot, spring(dampingRatio = 0.6f)) }
    HoloPanel(
        Modifier
            .size(if (focused) 150.dp else 100.dp, if (focused) 200.dp else 140.dp)
            .graphicsLayer {
                rotationY = anim.value
                cameraDistance = 12f * density
                alpha = if (focused) 1f else 0.55f
            },
        accent = if (focused) Neb.Blue else Neb.Violet,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(item.first, fontSize = if (focused) 54.sp else 34.sp)
            Text(item.second, color = Neb.Text, fontSize = if (focused) 16.sp else 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CameraFeed(onMotion: (Float, Float, Float) -> Unit, onSwipe: (Swipe) -> Unit) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val main = remember { ContextCompat.getMainExecutor(ctx) }
    val worker = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    DisposableEffect(owner) {
        val future = ProcessCameraProvider.getInstance(ctx)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(worker, MotionGestureAnalyzer(
                mirror = true,
                onMotion = { x, y, e -> main.execute { onMotion(x, y, e) } },
                onSwipe = { s -> main.execute { onSwipe(s) } },
            ))
            try {
                provider.unbindAll()
                provider.bindToLifecycle(owner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
            } catch (_: Exception) {
            }
        }, main)
        onDispose {
            try {
                ProcessCameraProvider.getInstance(ctx).get().unbindAll()
            } catch (_: Exception) {
            }
            worker.shutdown()
        }
    }
    AndroidView({ previewView }, Modifier.fillMaxSize())
}
