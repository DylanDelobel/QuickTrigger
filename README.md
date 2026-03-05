# QuickTrigger

Un mod Fabric qui ajoute des boutons de téléportation rapide directement dans l'inventaire Minecraft.

---

## Fonctionnalités

### Bouton Spawn
Un bouton boussole au-dessus de l'inventaire pour se téléporter instantanément au spawn du serveur.

### Boutons Home
Jusqu'à **4 maisons** accessibles en un clic, colorées selon le rang :

| # | Couleur | Rôle requis | Commande |
|---|---------|-------------|----------|
| Home #1 | 🔵 Bleu | Tous les joueurs | `/trigger home` |
| Home #2 | 🟢 Vert | Mineur | `/trigger home set 2` |
| Home #3 | 🟠 Orange | Architecte | `/trigger home set 3` |
| Home #4 | 🟣 Violet | Dragon | `/trigger home set 4` |

Les maisons verrouillées s'affichent en **lit gris** avec un tooltip indiquant le rôle Discord requis pour les débloquer.

---

## Installation

### Prérequis
- Minecraft **1.21.11**
- [Fabric Loader](https://fabricmc.net/) **≥ 0.18.0**
- [Fabric API](https://modrinth.com/mod/fabric-api) **0.139.x+1.21.11**

### Côté client
Copier `quicktrigger-1.0.0.jar` dans le dossier `mods/` de votre installation Minecraft.

### Côté serveur
Copier le **même `.jar`** dans le dossier `mods/` du serveur Fabric.

> Le mod est **optionnel côté client** — les joueurs sans le mod jouent normalement, ils n'ont simplement pas les boutons dans l'inventaire.

---

## Configuration serveur

### Scoreboard `homes.limit`
Le serveur envoie automatiquement le nombre de maisons disponibles à chaque joueur via un canal custom (`quicktrigger:data`).

Créer l'objectif et définir la limite par joueur :
```
/scoreboard objectives add homes.limit dummy
/scoreboard players set <joueur> homes.limit <valeur>
```

Valeurs possibles : `1` (défaut), `2`, `3`, `4`.

### Triggers requis
Le mod envoie des commandes `/trigger` — les objectifs doivent exister sur le serveur :
```
/scoreboard objectives add spawn trigger
/scoreboard objectives add home trigger
```

Un datapack ou plugin doit ensuite écouter ces triggers pour effectuer les téléportations.

---

## Build

### Prérequis
- Java **21**
- Gradle (inclus via le wrapper)

```bash
./gradlew build
```

Le `.jar` final se trouve dans `build/libs/quicktrigger-1.0.0.jar`.

---

## Compatibilité

| Environnement | Supporté |
|---------------|----------|
| Client seul (singleplayer) | ✅ |
| Client sur serveur Fabric | ✅ (avec envoi de `homes.limit`) |
| Client sur serveur Fabric (sans mod serveur) | ✅ (1 home par défaut) |
| Client vanilla sur serveur Fabric | ✅ (mod ignoré) |

---

## Structure du projet

```
src/main/java/com/quicktrigger/
├── QuickTrigger.java          # Entrypoint serveur — envoie homes.limit au JOIN
├── QuickTriggerClient.java    # Entrypoint client — affiche les boutons
└── QuickTriggerPayloads.java  # Payload partagé pour le canal custom S2C
```
