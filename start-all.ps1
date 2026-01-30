<#
Start all services in separate PowerShell windows (Windows-friendly).
Run from repository root: .\start-all.ps1
#>
param()

$services = @(
    @{ name = 'platform-service-java'; path = 'services\\platform-service-java' },
    @{ name = 'publisher-service-java'; path = 'services\\publisher-service-java' },
    @{ name = 'player-simulator-java'; path = 'services\\player-simulator-java' },
    @{ name = 'analytics-service-kotlin'; path = 'services\\analytics-service-kotlin' }
)

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition

# Ensure logs folder exists
$logsDir = Join-Path $root 'logs'
if (-not (Test-Path $logsDir)) { New-Item -ItemType Directory -Path $logsDir | Out-Null }

foreach ($s in $services) {
    $serviceDir = Join-Path $root $s.path
    $gradlew = Join-Path $serviceDir 'gradlew.bat'

    if (-not (Test-Path $serviceDir)) {
        Write-Warning "Service folder not found: $serviceDir - skipping"
        continue
    }

    if (-not (Test-Path $gradlew)) {
        Write-Warning "Gradle wrapper not found in $serviceDir - skipping"
        continue
    }

    Write-Host "Starting $($s.name) (logs -> $logsDir\\$($s.name).log)"

    $cmd = "& '$gradlew' ':app:bootRun'"
    $logFile = Join-Path $logsDir ("$($s.name).log")

    Start-Process -FilePath powershell.exe -ArgumentList '-NoExit','-Command',$cmd -WorkingDirectory $serviceDir -WindowStyle Normal
}

Write-Host 'All start commands issued.'