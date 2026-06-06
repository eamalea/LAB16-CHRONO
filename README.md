# ServiceChronometreJava – Foreground Service + Bound Service

Application Android démontrant l’utilisation d’un **Foreground Service** avec notification persistante et d’un **Bound Service** pour communiquer avec l’activité.  
Le chronomètre continue de tourner même quand l’application est fermée.

## Fonctionnalités

- Démarrage d’un chronomètre dans un service de premier plan (Foreground Service)
- Notification persistante mise à jour chaque seconde
- Communication bidirectionnelle via Bound Service (callback temps réel)
- Arrêt propre du service avec nettoyage des ressources
- Gestion de la permission `POST_NOTIFICATIONS` (Android 13+)
- Affichage au format `MM:SS`

## Prérequis

- Android Studio Hedgehog+
- SDK minimum : API 24 (Android 7.0)
- Émulateur ou appareil avec API 26+ recommandé pour tester le Foreground Service

## Installation

1. Clonez le dépôt
2. Ouvrez le projet dans Android Studio
3. Synchronisez Gradle
4. Lancez l’application

## Utilisation

- Cliquez sur **DÉMARRER** : le service se lance, une notification apparaît, le chronomètre tourne.
- Fermez l’application (glisser vers le haut) : la notification reste, le chronomètre continue.
- Rouvrez l’application : le temps affiché est synchronisé avec le service.
- Cliquez sur **ARRÊTER** : le service s’arrête, la notification disparaît.

## Structure du code

- `ChronometreService.java` : Foreground Service + gestion du temps + notification
- `MainActivity.java` : UI + liaison avec le service via `ServiceConnection`
- `activity_main.xml` : deux boutons et un TextView

## Améliorations apportées

- Utilisation de `Handler` + `Runnable` (léger et adapté au thread principal)
- Interface `OnTimeChangeListener` pour des mises à jour UI en direct
- Gestion des permissions notification (Android 13+)
- Logs détaillés pour suivre le cycle de vie (`Log.d`)
- `PendingIntent` dans la notification pour revenir à l’activité

## Points clés à retenir

- `startForeground()` est **obligatoire** depuis Android 8.0 pour un service visible.
- `START_STICKY` : redémarrage automatique si le système tue le service.
- `foregroundServiceType="dataSync"` requis dans le manifeste (API 34+).
- Toujours `unbindService` dans `onDestroy` pour éviter les fuites mémoire.
