# Script de demarrage complet - VideoGamePlatform
param(
    [switch]$SkipAvro,
    [switch]$SkipDocker,
    [switch]$SkipServices,
    [switch]$NoLogs
)

function Write-Step { param([string]$Message); Write-Host "`n========================================" -ForegroundColor Cyan; Write-Host $Message -ForegroundColor Cyan; Write-Host "========================================" -ForegroundColor Cyan }
function Write-Success { param([string]$Message); Write-Host " $Message" -ForegroundColor Green }
function Write-Error { param([string]$Message); Write-Host " $Message" -ForegroundColor Red }
function Write-Info { param([string]$Message); Write-Host " $Message" -ForegroundColor Yellow }

Write-Host "`nVideoGamePlatform Startup Script" -ForegroundColor Magenta
Write-Host "================================" -ForegroundColor Magenta

# Verification Java
Write-Step "Verification des prerequis"
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Success "Java trouve: $javaVersion"
} catch {
    Write-Error "Java non trouve. Installez Java 23+"
    exit 1
}

# Verification Docker
if (-not $SkipDocker) {
    try {
        docker ps | Out-Null
        Write-Success "Docker est demarre"
    } catch {
        Write-Error "Docker n est pas demarre. Lancez Docker Desktop."
        exit 1
    }
}

# Etape 1: Compilation Avro
if (-not $SkipAvro) {
    Write-Step "Etape 1: Compilation des schemas Avro"
    Push-Location ".\common\avro-schemas"
    try {
        Write-Info "Compilation..."
        .\gradlew build
        if ($LASTEXITCODE -eq 0) { Write-Success "Schemas Avro compiles avec succes" } else { Write-Error "Echec compilation Avro"; Pop-Location; exit 1 }
    } finally {
        Pop-Location
    }
} else {
    Write-Info "Compilation Avro ignoree (option SkipAvro)"
}

# Etape 2: Docker
if (-not $SkipDocker) {
    Write-Step "Etape 2: Demarrage de l infrastructure Docker"
    Push-Location ".\docker"
    try {
        Write-Info "Demarrage des conteneurs..."
        docker compose up -d
        if ($LASTEXITCODE -eq 0) { Write-Success "Conteneurs Docker demarres" } else { Write-Error "Echec demarrage Docker"; Pop-Location; exit 1 }
    } finally {
        Pop-Location
    }
    
    # Health checks
    Write-Step "Etape 3: Verification de l infrastructure"
    Write-Info "Attente de Kafka (30-60 secondes)..."
    $maxRetries = 30; $retries = 0; $kafkaReady = $false
    while ($retries -lt $maxRetries -and -not $kafkaReady) {
        try {
            $kafkaStatus = docker ps --filter "name=kafka" --format "{{.Status}}"
            if ($kafkaStatus -match "Up") {
                $connection = Test-NetConnection -ComputerName localhost -Port 9092 -WarningAction SilentlyContinue -InformationLevel Quiet
                if ($connection) { $kafkaReady = $true; Write-Success "Kafka est pret" }
            }
        } catch {}
        if (-not $kafkaReady) { Write-Host "." -NoNewline; Start-Sleep -Seconds 2; $retries++ }
    }
    if (-not $kafkaReady) { Write-Error "`nKafka n est pas pret"; exit 1 }
} else {
    Write-Info "Demarrage Docker ignore (option SkipDocker)"
}

# Etape 4: Services
if (-not $SkipServices) {
    Write-Step "Etape 4: Demarrage des services"
    if (-not $NoLogs) {
        if (-not (Test-Path ".\logs")) { New-Item -ItemType Directory -Path ".\logs" | Out-Null; Write-Info "Dossier logs cree" }
    }
    
    Write-Info "Demarrage de Platform Service (port 8082)..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd .\services\platform-service-java; .\gradlew run --console=plain 2>&1 | Tee-Object -FilePath '..\..\logs\platform-service.log'" -WindowStyle Normal
    Write-Success "Platform Service demarre (fenetre separee)"
    Start-Sleep -Seconds 3
    
    Write-Info "Demarrage de Publisher Service (port 8083)..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd .\services\publisher-service-java; .\gradlew run --console=plain 2>&1 | Tee-Object -FilePath '..\..\logs\publisher-service.log'" -WindowStyle Normal
    Write-Success "Publisher Service demarre (fenetre separee)"
    Start-Sleep -Seconds 3
    
    Write-Info "Demarrage de Analytics Service (Kafka Streams)..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd .\services\analytics-service-kotlin; .\gradlew run --console=plain 2>&1 | Tee-Object -FilePath '..\..\logs\analytics-service.log'" -WindowStyle Normal
    Write-Success "Analytics Service demarre (fenetre separee)"
    Start-Sleep -Seconds 3
    
    Write-Info "Demarrage de Player Simulator (JavaFX)..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd .\services\player-simulator-java; .\gradlew run --console=plain 2>&1 | Tee-Object -FilePath '..\..\logs\player-simulator.log'" -WindowStyle Normal
    Write-Success "Player Simulator demarre (fenetre separee)"
    
    Write-Info "`nAttente du demarrage complet des services (25 secondes)..."
    Start-Sleep -Seconds 25
} else {
    Write-Info "Demarrage des services ignore (option SkipServices)"
}

# Affichage final
Write-Step "VideoGamePlatform - Pret !"
Write-Host "`n URLs d acces:" -ForegroundColor Cyan
Write-Host "   Kafka UI:         http://localhost:8080" -ForegroundColor White
Write-Host "   PgAdmin:          http://localhost:5050 (admin@admin.com / admin)" -ForegroundColor White
Write-Host "   Schema Registry:  http://localhost:8081" -ForegroundColor White
Write-Host "   Platform API:     http://localhost:8082" -ForegroundColor White
Write-Host "   Publisher API:    http://localhost:8083" -ForegroundColor White
Write-Host "`n Services demarres:" -ForegroundColor Cyan
Write-Host "   Platform Service   (REST API - port 8082)" -ForegroundColor White
Write-Host "   Publisher Service  (REST API + JavaFX UI - port 8083)" -ForegroundColor White
Write-Host "   Analytics Service  (Kafka Streams - pas de port)" -ForegroundColor White
Write-Host "   Player Simulator   (JavaFX Desktop App)" -ForegroundColor White
if (-not $NoLogs) {
    Write-Host "`n Fichiers logs:" -ForegroundColor Cyan
    Write-Host "   .\logs\platform-service.log" -ForegroundColor White
    Write-Host "   .\logs\publisher-service.log" -ForegroundColor White
    Write-Host "   .\logs\analytics-service.log" -ForegroundColor White
    Write-Host "   .\logs\player-simulator.log" -ForegroundColor White
}
Write-Host "`n Tout est pret ! Bon developpement " -ForegroundColor Green
Write-Host "`nPour arreter: .\stop-stack.ps1" -ForegroundColor Yellow
