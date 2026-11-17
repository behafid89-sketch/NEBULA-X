package com.nebulax.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nebulax.app.voice.Destination

private data class Tile(val emoji: String, val title: String, val sub: String, val dest: Destination, val accent: Color)

@Composable
fun HomeScreen(status: String, onOpen: (Destination) -> Unit) {
    val tiles = listOf(
        Tile("🎙️", "Assistante", "Voix française", Destination.ASSISTANT, Neb.Blue),
        Tile("🎬", "Cinéma", "Mode immersif", Destination.CINEMA, Neb.Violet),
        Tile("🔷", "Hologramme", "3D interactive", Destination.HOLO3D, Neb.Pink),
        Tile("✋", "Gestes", "Caméra", Destination.GESTURES, Neb.BlueDeep),
    )
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("SYSTÈME EN LIGNE", color = Neb.Blue, fontSize = 12.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        NebTitle("NEBULA X", 38)
        HoloHeart(
            Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.95f)
                .aspectRatio(1f)
                .padding(8.dp)
        )
        Text(status, color = Neb.Dim, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        tiles.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().widthIn(max = 600.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { t ->
                    HoloPanel(Modifier.weight(1f).height(130.dp), accent = t.accent, onClick = { onOpen(t.dest) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(t.emoji, fontSize = 38.sp)
                            Text(t.title, color = Neb.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(t.sub, color = Neb.Dim, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}
