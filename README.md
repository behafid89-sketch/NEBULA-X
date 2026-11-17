# NEBULA X

Application Android native (Kotlin + Jetpack Compose) pour Samsung Galaxy S24 Ultra, Android 8 à 15.

## Fonctions (réellement implémentées)
- Interface futuriste noir / bleu / violet, fond spatial animé, panneaux en verre holographique.
- Cœur lumineux animé (battement, halo, anneaux holographiques, particules) qui réagit à la voix.
- **Assistante vocale Nebula** en français : reconnaissance vocale Android (fr-FR) + réponses parlées (Text-to-Speech).
  Commandes : heure, date, batterie, lampe torche (allumer / éteindre), blague, aide, ouvrir cinéma / hologramme / gestes.
  La compréhension fonctionne par mots-clés (pas de grand modèle de langage).
- **Mode cinéma immersif** : plein écran, écran toujours allumé, 3 scènes animées (hyperespace, nébuleuse, aurore).
- **Hologramme 3D interactif** : 4 formes en fil de fer, rotation au doigt, zoom à deux doigts.
- **Commandes gestuelles (expérimental)** : détection de mouvement par la caméra frontale (balayage gauche / droite).

## Non inclus (transparence)
- Pas de projection optique : le Galaxy S24 Ultra n'a pas de projecteur, tout s'affiche sur l'écran.
- Réalité augmentée (ARCore) : pas encore.

## Compilation
GitHub Actions exécute `gradle assembleDebug` à chaque envoi sur `main` et publie `NEBULA-X.apk` dans l'onglet **Releases**.
