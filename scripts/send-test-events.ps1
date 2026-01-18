# Script pour envoyer des événements de test vers Kafka

Write-Host "==== Envoi d'événements de test vers Kafka ====" -ForegroundColor Cyan
Write-Host ""

# 1. Nouvel utilisateur
Write-Host "1. Envoi d'un événement user-registered..." -ForegroundColor Yellow
$userEvent = @'
{"userId":"user-001","username":"john_doe","email":"john@example.com","registrationTimestamp":1705598400000}
'@

echo $userEvent | docker exec -i kafka kafka-console-producer `
    --bootstrap-server localhost:9092 `
    --topic user-registered `
    --property "parse.key=true" `
    --property "key.separator=:" `
    --property "key=user-001"

Write-Host "✓ Utilisateur envoyé" -ForegroundColor Green
Start-Sleep -Seconds 1

# 2. Nouveau jeu (métadonnées)
Write-Host "2. Envoi d'un événement game-metadata-updated..." -ForegroundColor Yellow
$gameMetadata = @'
{"gameId":"game-001","title":"Cyberpunk 2077","publisher":"CD Projekt Red","genre":"RPG","platform":"PC","description":"Open-world RPG game","eventTimestamp":1705598400000}
'@

echo $gameMetadata | docker exec -i kafka kafka-console-producer `
    --bootstrap-server localhost:9092 `
    --topic game-metadata-updated `
    --property "parse.key=true" `
    --property "key.separator=:" `
    --property "key=game-001"

Write-Host "✓ Métadonnées du jeu envoyées" -ForegroundColor Green
Start-Sleep -Seconds 1

# 3. Patch de jeu
Write-Host "3. Envoi d'un événement game-patched..." -ForegroundColor Yellow
$patchEvent = @'
{"gameId":"game-001","version":"1.5.2","patchTimestamp":1705598400000}
'@

echo $patchEvent | docker exec -i kafka kafka-console-producer `
    --bootstrap-server localhost:9092 `
    --topic game-patched `
    --property "parse.key=true" `
    --property "key.separator=:" `
    --property "key=game-001"

Write-Host "✓ Patch envoyé" -ForegroundColor Green
Start-Sleep -Seconds 1

Write-Host ""
Write-Host "==== Tous les événements ont été envoyés ====" -ForegroundColor Green
Write-Host ""
Write-Host "Vérifiez les logs du Platform Service pour voir les événements traités"
