# Réservation d'Espaces de Travail

Une application mobile Android permettant de réserver des espaces de travail de manière simple et efficace.

## Description du Projet

Cette application offre une plateforme complète de gestion et de réservation d'espaces de travail avec deux interfaces principales :

### Partie Admin
Les administrateurs d'espaces de travail peuvent :
- Ajouter et gérer leurs espaces de travail
- Suivre les commandes et réservations en temps réel
- Consulter le revenus

### Partie Client
Les clients peuvent :
- Consulter la liste des espaces de travail disponibles
- Voir les détails de chaque espace (équipements, localisation, prix)
- Effectuer des réservations facilement
- Gérer leurs réservations

## Technologie

- **Langage** : Java
- **Plateforme** : Android
- **IDE** : Android Studio
- **Base de données** : Realm
  
## Fonctionnalités Principales

### Fonctionnalités Générales
- Authentification et gestion des profils utilisateur
- Interface intuitive et conviviale
- Gestion des préférences utilisateur

### Fonctionnalités Admin
- Gestion des espaces de travail (création, modification, suppression)
- Suivi et gestion des commandes en temps réel
- Analyse des revenus 
- Gestion des utilisateurs de l'espace

### Fonctionnalités Client
- Recherche et filtrage des espaces disponibles
- Affichage détaillé des workspaces
- Système de réservation intuitif
- Historique des réservations
- Gestion des favoris

## Démarrage Rapide

### Prérequis
- Android Studio (dernière version)
- JDK 11 ou supérieur
- Android SDK API 21+

### Installation

1. Clonez le dépôt :
```bash
git clone https://github.com/yourusername/workspace-reservation.git
cd workspace-reservation
```

2. Ouvrez le projet dans Android Studio

3. Synchronisez les dépendances Gradle

4. Compilez et exécutez l'application sur un émulateur ou un appareil réel

## Structure du Projet

```
MyApplication8/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/myapplication/
│   │   │   │       ├── ui/
│   │   │   │       │   ├── admin/          # Interfaces administrateur
│   │   │   │       │   ├── client/         # Interfaces client
│   │   │   │       │   └── login/          # Interfaces partagées
│   │   │   │       ├── model/              # Classes métier
│   │   │   │       └── utils/              # Utilitaires
│   │   │   └── res/
│   │   │       ├── layout/                 # Fichiers de mise en page
│   │   │       ├── drawable/               # Ressources graphiques
│   │   │       └── values/                 # Strings, couleurs, styles
│   └── build.gradle
└── README.md
```

## Utilisation

### Pour les Administrateurs
1. Créez un compte ou connectez-vous
2. Gérez vos espaces et consultez les revenues
3. Gérez et consultez les reservations


### Pour les Clients
1. Créez un compte ou connectez-vous
2. Explorez les espaces disponibles
3. Sélectionnez un espace et complétez votre réservation
4. Gérez vos reservations


## Auteurs

- Ayoub KADDOURI
- Mohamed Yassine Reghioui Hamzaoui
- Youssef Ben Omar
- Mohammed Nour Moussa

