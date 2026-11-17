package com.nebulax.app.voice

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import java.text.Normalizer
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class Destination { HOME, ASSISTANT, CINEMA, HOLO3D, GESTURES }

data class Answer(val text: String, val destination: Destination? = null)

/**
 * Compréhension des commandes par mots-clés, entièrement hors ligne.
 * Facile à étendre : ajouter une règle dans [answer].
 */
class Brain(private val context: Context) {

    private var torchOn = false
    private val jokes = listOf(
        "Pourquoi les étoiles ne se perdent jamais ? Parce qu'elles suivent la Voie lactée.",
        "Que dit un astronaute amoureux ? Tu es mon étoile, je suis dans la lune.",
        "Pourquoi le téléphone porte-t-il des lunettes ? Parce qu'il a perdu ses contacts.",
        "Quel est le comble pour un robot ? Avoir un cœur d'artichaut.",
    )

    fun answer(raw: String): Answer {
        val q = normalize(raw)
        fun has(vararg words: String) = words.any { q.contains(it) }

        return when {
            has("aide", "que sais tu", "que peux tu", "quoi faire") -> Answer(
                "Je peux donner l'heure, la date, le niveau de batterie, allumer ou éteindre la lampe, raconter une blague, " +
                    "et ouvrir le mode cinéma, l'hologramme 3D ou les gestes."
            )
            has("bonjour", "salut", "coucou", "hello", "bonsoir") -> Answer(greeting())
            has("qui es tu", "comment tu t appelles", "ton nom", "tu es qui") ->
                Answer("Je suis Nebula, l'assistante de NEBULA X. Je fonctionne sur votre Galaxy.")
            has("heure") -> {
                val t = LocalTime.now()
                Answer("Il est ${t.hour} heures ${t.minute.toString().padStart(2, '0')}.")
            }
            has("date", "quel jour", "on est quel", "aujourd hui") -> {
                val d = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE))
                Answer("Nous sommes le $d.")
            }
            has("batterie", "charge") -> {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                Answer(if (pct in 0..100) "La batterie est à $pct pour cent." else "Je ne peux pas lire la batterie.")
            }
            has("lampe", "torche", "lumiere", "flash") -> {
                val off = has("eteins", "eteindre", "eteint", "arrete", "coupe")
                Answer(setTorch(!off))
            }
            has("blague", "fais moi rire", "drole") -> Answer(jokes.random())
            has("cinema", "film", "immersif") -> Answer("Lancement du mode cinéma immersif.", Destination.CINEMA)
            has("hologramme", "3d", "trois d") -> Answer("Voici l'hologramme 3D. Faites-le tourner avec le doigt.", Destination.HOLO3D)
            has("geste", "camera", "main") -> Answer("Mode gestes activé. Passez la main devant la caméra frontale.", Destination.GESTURES)
            has("accueil", "menu", "retour") -> Answer("Retour à l'accueil.", Destination.HOME)
            has("merci") -> Answer("Avec plaisir.")
            has("ca va", "comment vas tu", "comment tu vas") -> Answer("Mes circuits brillent ! Et vous ?")
            has("projet", "projection", "mur", "plafond") -> Answer(
                "Votre téléphone n'a pas de projecteur optique : je ne peux pas projeter d'image sur un mur. " +
                    "Le mode cinéma offre une expérience immersive sur l'écran."
            )
            else -> Answer("Je n'ai pas encore appris cette commande. Dites « aide » pour savoir ce que je sais faire.")
        }
    }

    private fun greeting(): String {
        val h = LocalTime.now().hour
        return when {
            h < 5 -> "Bonsoir. Il est tard, que puis-je faire pour vous ?"
            h < 18 -> "Bonjour ! Que puis-je faire pour vous ?"
            else -> "Bonsoir ! Que puis-je faire pour vous ?"
        }
    }

    private fun setTorch(on: Boolean): String {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
                ?: return "Je ne trouve pas de lampe sur cet appareil."
            cm.setTorchMode(id, on)
            torchOn = on
            if (on) "Lampe allumée." else "Lampe éteinte."
        } catch (e: Exception) {
            "Impossible d'utiliser la lampe pour le moment."
        }
    }

    fun torchOff() {
        if (torchOn) setTorch(false)
    }

    private fun normalize(s: String): String =
        Normalizer.normalize(s.lowercase(Locale.FRANCE), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
