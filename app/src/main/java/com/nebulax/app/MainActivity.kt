package com.nebulax.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nebulax.app.ui.AssistantScreen
import com.nebulax.app.ui.CinemaScreen
import com.nebulax.app.ui.GestureScreen
import com.nebulax.app.ui.Holo3DScreen
import com.nebulax.app.ui.HomeScreen
import com.nebulax.app.ui.NebulaTheme
import com.nebulax.app.ui.SpaceBackground
import com.nebulax.app.voice.Destination
import com.nebulax.app.voice.VoiceAssistant

class MainActivity : ComponentActivity() {

    private lateinit var assistant: VoiceAssistant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        assistant = VoiceAssistant(this)

        setContent {
            NebulaTheme {
                var screen by remember { mutableStateOf(Destination.HOME) }
                assistant.onNavigate = { d -> screen = d }
                BackHandler(enabled = screen != Destination.HOME) { screen = Destination.HOME }
                val home = { screen = Destination.HOME }

                SpaceBackground {
                    AnimatedContent(
                        targetState = screen,
                        transitionSpec = { (fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.96f)) togetherWith fadeOut(tween(200)) },
                        label = "nav",
                    ) { s ->
                        when (s) {
                            Destination.HOME -> HomeScreen(
                                status = if (assistant.ttsReady) "Nebula est prête. Choisissez un module." else "Initialisation de la voix…",
                                onOpen = { screen = it },
                            )
                            Destination.ASSISTANT -> AssistantScreen(assistant, onBack = home)
                            Destination.CINEMA -> CinemaScreen(onExit = home)
                            Destination.HOLO3D -> Holo3DScreen(onBack = home)
                            Destination.GESTURES -> GestureScreen(onBack = home, speak = { assistant.speak(it) })
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::assistant.isInitialized) assistant.stopListening()
    }

    override fun onDestroy() {
        if (::assistant.isInitialized) assistant.release()
        super.onDestroy()
    }
}
