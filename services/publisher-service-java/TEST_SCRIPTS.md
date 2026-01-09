# 🧪 Scripts de Test - Publisher Service

Ce fichier contient des commandes de test pour le Publisher Service.

## 📋 Prérequis

- Service démarré sur http://localhost:8082
- `curl` et `jq` installés

### Installation de jq (Windows)

```powershell
# Via Chocolatey
choco install jq

# Ou télécharger depuis https://stedolan.github.io/jq/download/
```

---

## 🚀 Tests Basiques

### 1. Health Check

```bash
curl http://localhost:8082/actuator/health
```

**Résultat attendu:**
```json
{
  "status": "UP"
}
```

---

### 2. Statistiques Globales

```bash
curl http://localhost:8082/api/admin/stats | jq
```

**Résultat attendu:**
```json
{
  "totalGames": 20,
  "totalPatches": 5,
  "totalCrashes": 0,
  "totalReviews": 0
}
```

---

## 🎮 Tests GAMES

### Lister tous les jeux

```bash
curl http://localhost:8082/api/games | jq
```

### Compter les jeux

```bash
curl -s http://localhost:8082/api/games | jq 'length'
```

### Récupérer le premier jeu

```bash
curl -s http://localhost:8082/api/games | jq '.[0]'
```

### Rechercher par titre

```bash
curl "http://localhost:8082/api/games/search?title=Call" | jq
```

### Détails d'un jeu spécifique

```bash
# Windows PowerShell
$gameId = (curl -s http://localhost:8082/api/games | ConvertFrom-Json)[0].id
curl http://localhost:8082/api/games/$gameId | jq

# Linux/Mac/Git Bash
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl http://localhost:8082/api/games/$GAME_ID | jq
```

---

## 🔧 Tests PATCHES

### Publier un patch

```bash
# Windows PowerShell
$gameId = (curl -s http://localhost:8082/api/games | ConvertFrom-Json)[0].id
curl -X POST http://localhost:8082/api/games/$gameId/patch `
  -H "Content-Type: application/json" `
  -d '{\"changelog\": \"- Fixed critical memory leak\\n- Improved rendering performance\\n- Fixed crash on startup\"}' | jq

# Linux/Mac/Git Bash
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl -X POST http://localhost:8082/api/games/$GAME_ID/patch \
  -H "Content-Type: application/json" \
  -d '{"changelog": "- Fixed critical memory leak\n- Improved rendering performance\n- Fixed crash on startup"}' | jq
```

**Résultat attendu:**
```json
{
  "success": true,
  "message": "Patch publié avec succès",
  "patch": {
    "id": "uuid-...",
    "gameId": "...",
    "version": "1.0.1",
    "previousVersion": "1.0.0",
    "changelog": "...",
    "patchSize": 150234567,
    "releaseDate": "2025-12-28T15:30:00"
  }
}
```

### Patch avec changelog automatique

```bash
# Windows PowerShell
$gameId = (curl -s http://localhost:8082/api/games | ConvertFrom-Json)[0].id
curl -X POST http://localhost:8082/api/games/$gameId/patch `
  -H "Content-Type: application/json" `
  -d '{}' | jq

# Linux/Mac/Git Bash
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl -X POST http://localhost:8082/api/games/$GAME_ID/patch \
  -H "Content-Type: application/json" \
  -d '{}' | jq
```

### Historique des patches

```bash
# Windows PowerShell
$gameId = (curl -s http://localhost:8082/api/games | ConvertFrom-Json)[0].id
curl http://localhost:8082/api/games/$gameId/patches | jq

# Linux/Mac/Git Bash
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl http://localhost:8082/api/games/$GAME_ID/patches | jq
```

---

## 📝 Tests METADATA

### Mettre à jour les métadonnées

```bash
# Windows PowerShell
$gameId = (curl -s http://localhost:8082/api/games | ConvertFrom-Json)[0].id
curl -X PUT http://localhost:8082/api/games/$gameId/metadata `
  -H "Content-Type: application/json" `
  -d '{\"genre\": \"Action-Shooter\", \"description\": \"Fast-paced multiplayer shooter game\"}' | jq

# Linux/Mac/Git Bash
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl -X PUT http://localhost:8082/api/games/$GAME_ID/metadata \
  -H "Content-Type: application/json" \
  -d '{"genre": "Action-Shooter", "description": "Fast-paced multiplayer shooter game"}' | jq
```

---

## 💥 Tests CRASHES

### Lister tous les crashes

```bash
curl http://localhost:8082/api/crashes | jq
```

### Crashes d'un jeu spécifique

```bash
# Windows PowerShell
$gameId = (curl -s http://localhost:8082/api/games | ConvertFrom-Json)[0].id
curl http://localhost:8082/api/crashes/game/$gameId | jq

# Linux/Mac/Git Bash
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl http://localhost:8082/api/crashes/game/$GAME_ID | jq
```

### Statistiques de crashes

```bash
curl http://localhost:8082/api/crashes/stats | jq
```

---

## ⭐ Tests REVIEWS

### Lister toutes les statistiques

```bash
curl http://localhost:8082/api/reviews | jq
```

### Stats d'un jeu

```bash
# Windows PowerShell
$gameId = (curl -s http://localhost:8082/api/games | ConvertFrom-Json)[0].id
curl http://localhost:8082/api/reviews/game/$gameId | jq

# Linux/Mac/Git Bash
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
curl http://localhost:8082/api/reviews/game/$GAME_ID | jq
```

---

## 🛠️ Tests ADMIN

### Recharger VGSales

```bash
curl -X POST http://localhost:8082/api/admin/reload-vgsales | jq
```

### Simuler un patch aléatoire

```bash
curl -X POST http://localhost:8082/api/admin/simulate-patch | jq
```

**Résultat attendu:**
```json
{
  "success": true,
  "message": "Simulation de patch déclenchée"
}
```

---

## 🔄 Test de Workflow Complet

### Scénario: Publier un patch et vérifier

```bash
# 1. Récupérer un jeu
echo "=== Étape 1: Récupération d'un jeu ==="
GAME_ID=$(curl -s http://localhost:8082/api/games | jq -r '.[0].id')
GAME_TITLE=$(curl -s http://localhost:8082/api/games/$GAME_ID | jq -r '.title')
echo "Jeu sélectionné: $GAME_TITLE (ID: $GAME_ID)"

# 2. Vérifier la version actuelle
echo -e "\n=== Étape 2: Version actuelle ==="
curl -s http://localhost:8082/api/games/$GAME_ID | jq '.currentVersion'

# 3. Publier un patch
echo -e "\n=== Étape 3: Publication du patch ==="
curl -s -X POST http://localhost:8082/api/games/$GAME_ID/patch \
  -H "Content-Type: application/json" \
  -d '{"changelog": "Test patch workflow"}' | jq '.patch.version'

# 4. Vérifier la nouvelle version
echo -e "\n=== Étape 4: Nouvelle version ==="
curl -s http://localhost:8082/api/games/$GAME_ID | jq '.currentVersion'

# 5. Voir l'historique
echo -e "\n=== Étape 5: Historique des patches ==="
curl -s http://localhost:8082/api/games/$GAME_ID/patches | jq 'length'
```

---

## 📊 Monitoring avec Watch

### Surveillance en temps réel des stats

```bash
# Linux/Mac
watch -n 5 'curl -s http://localhost:8082/api/admin/stats | jq'

# Windows PowerShell
while($true) {
  Clear-Host
  curl -s http://localhost:8082/api/admin/stats | ConvertFrom-Json
  Start-Sleep -Seconds 5
}
```

---

## 🐛 Tests d'Erreurs

### Jeu inexistant

```bash
curl -X POST http://localhost:8082/api/games/fake-id/patch \
  -H "Content-Type: application/json" \
  -d '{"changelog": "test"}' | jq
```

**Résultat attendu:**
```json
{
  "success": false,
  "error": "Jeu introuvable: fake-id"
}
```

### Requête invalide

```bash
curl -X POST http://localhost:8082/api/games/123/patch \
  -H "Content-Type: application/json" \
  -d 'invalid json' | jq
```

---

## 🧹 Nettoyage

### Supprimer la base de données

```bash
# Windows
Remove-Item -Recurse -Force .\data\publisher-db*

# Linux/Mac
rm -rf ./data/publisher-db*
```

Redémarrez le service pour recréer la base.

---

## 📝 Notes

- Tous les IDs sont des UUIDs générés automatiquement
- Les timestamps sont en epoch milliseconds
- Les versions suivent le semantic versioning (MAJOR.MINOR.PATCH)
- La simulation automatique s'exécute toutes les 2 minutes

---

**Bon test ! 🚀**

