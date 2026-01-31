param([switch]$KeepDocker)
function Write-Step { param([string]$Message); Write-Host "`n========================================" -ForegroundColor Cyan; Write-Host $Message -ForegroundColor Cyan; Write-Host "========================================" -ForegroundColor Cyan }
function Write-Success { param([string]$Message); Write-Host " $Message" -ForegroundColor Green }
function Write-Error { param([string]$Message); Write-Host " $Message" -ForegroundColor Red }
function Write-Info { param([string]$Message); Write-Host " $Message" -ForegroundColor Yellow }

Write-Host "`nVideoGamePlatform Stop Script" -ForegroundColor Red
Write-Host "=============================" -ForegroundColor Red

Write-Step "Etape 1: Arret des services Gradle"
Write-Info "Recherche des processus Java (Gradle)..."
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Info "Trouve $($javaProcesses.Count) processus Java"
    foreach ($process in $javaProcesses) {
        try {
            $commandLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($process.Id)").CommandLine
            if ($commandLine -match "gradle") {
                Write-Info "Arret du processus Java (PID: $($process.Id))"
                Stop-Process -Id $process.Id -Force
                Write-Success "Processus $($process.Id) arrete"
            }
        } catch {
            Write-Info "Impossible d arreter le processus $($process.Id)"
        }
    }
} else {
    Write-Info "Aucun processus Java en cours d execution"
}

Start-Sleep -Seconds 2

if (-not $KeepDocker) {
    Write-Step "Etape 2: Arret de l infrastructure Docker"
    $dockerPath = ".\docker"
    if (Test-Path $dockerPath) {
        Push-Location $dockerPath
        try {
            Write-Info "Arret des conteneurs Docker..."
            docker compose down
            if ($LASTEXITCODE -eq 0) { Write-Success "Conteneurs Docker arretes" } else { Write-Error "Erreur lors de l arret de Docker" }
        } finally {
            Pop-Location
        }
    } else {
        Write-Error "Dossier docker non trouve: $dockerPath"
    }
} else {
    Write-Info "Infrastructure Docker conservee (option KeepDocker)"
}

Write-Step "Etape 3: Nettoyage"
$lockFiles = Get-ChildItem -Path "." -Filter "*.lock" -Recurse -ErrorAction SilentlyContinue
if ($lockFiles.Count -gt 0) {
    Write-Info "Fichiers de verrouillage Gradle trouves: $($lockFiles.Count)"
    Write-Info "(Non supprimes - faites-le manuellement si necessaire)"
}

Write-Host "`n Arret termine !" -ForegroundColor Green
Write-Host "`n Remarques:" -ForegroundColor Cyan
Write-Host "   Si des fenetres PowerShell sont encore ouvertes, fermez-les manuellement" -ForegroundColor White
Write-Host "   Les donnees Docker sont conservees (volumes)" -ForegroundColor White
Write-Host "   Pour supprimer aussi les volumes: cd docker && docker compose down -v" -ForegroundColor White
