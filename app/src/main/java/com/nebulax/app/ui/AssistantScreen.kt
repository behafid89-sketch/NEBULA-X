package com.nebulax.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nebulax.app.voice.VoiceAssistant

@Composable
fun AssistantScreen(va: VoiceAssistant, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) va.startListening() else va.speak("Sans le micro, je ne peux pas vous entendre. Vous pouvez l'autoriser dans les paramètres.")
    }
    fun mic() {
        if (va.listening) {
            va.stopListening(); return
        }
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) va.startListening()
        else askMic.launch(Manifest.permission.RECORD_AUDIO)
    }
    val micScale by animateFloatAsState(if (va.listening) 1.08f + va.level * 0.25f else 1f, label = "mic")

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopBar("Assistante Nebula", onBack)
        HoloHeart(
            Modifier.widthIn(max = 380.dp).fillMaxWidth(0.8f).aspectRatio(1f),
            energy = if (va.listening) va.level else 0f,
        )
        HoloPanel(Modifier.fillMaxWidth().widthIn(max = 640.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (va.heard.isNotBlank()) {
                    Text("« ${va.heard} »", color = Neb.Blue, fontSize = 16.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                }
                Text(va.reply, color = Neb.Text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Quelle heure est-il ?", "Allume la lampe", "Raconte une blague", "Mode cinéma", "Aide").forEach { s ->
                Text(
                    s,
                    color = Neb.Text,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Neb.GlassBorder, RoundedCornerShape(18.dp))
                        .clickable { va.handle(s) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(
            Modifier
                .size(108.dp)
                .scale(micScale)
                .clip(CircleShape)
                .background(Brush.linearGradient(if (va.listening) listOf(Neb.Pink, Neb.Violet) else listOf(Neb.Blue, Neb.Violet)))
                .clickable { mic() },
            contentAlignment = Alignment.Center,
        ) {
            Text(if (va.listening) "■" else "🎙️", fontSize = 44.sp, color = Neb.Black)
        }
        Spacer(Modifier.height(6.dp))
        Text(if (va.listening) "J'écoute…" else "Touchez pour parler", color = Neb.Dim, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.5.dp, Neb.GlassBorder, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) { Text("←", color = Neb.Text, fontSize = 24.sp) }
        Spacer(Modifier.size(14.dp))
        Text(title, color = Neb.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}
