# MediCare Self-Downloading Maven & Runner Script

$ErrorActionPreference = "Stop"

# 1. Check if mvn is installed in the system PATH
$systemMvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($systemMvn) {
    Write-Host "========================================================" -ForegroundColor Cyan
    Write-Host "System Maven detected: $($systemMvn.Source)" -ForegroundColor Green
    Write-Host "Starting MediCare Server..." -ForegroundColor Cyan
    Write-Host "========================================================" -ForegroundColor Cyan
    & mvn spring-boot:run
    exit
}

# 2. Setup local Maven directory
$localMavenDir = Join-Path $PSScriptRoot ".maven"
$extractedDir = Join-Path $localMavenDir "apache-maven-3.9.6"
$localMvnCmd = Join-Path $extractedDir "bin\mvn.cmd"

if (-not (Test-Path $localMvnCmd)) {
    Write-Host "========================================================" -ForegroundColor Yellow
    Write-Host "Maven is not installed on this system." -ForegroundColor Red
    Write-Host "Downloading Apache Maven 3.9.6 automatically..." -ForegroundColor Yellow
    Write-Host "========================================================" -ForegroundColor Yellow
    
    if (Test-Path $localMavenDir) {
        Remove-Item -Recurse -Force $localMavenDir
    }
    New-Item -ItemType Directory -Path $localMavenDir | Out-Null
    
    $zipPath = Join-Path $PSScriptRoot "maven.zip"
    $url = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    
    Write-Host "Downloading from: $url" -ForegroundColor DarkGray
    Invoke-WebRequest -Uri $url -OutFile $zipPath
    
    Write-Host "Extracting Maven package..." -ForegroundColor Yellow
    Expand-Archive -Path $zipPath -DestinationPath $localMavenDir
    
    Remove-Item $zipPath
    Write-Host "Maven downloaded and extracted successfully to .maven/" -ForegroundColor Green
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Using local Maven: $localMvnCmd" -ForegroundColor Green
Write-Host "Starting MediCare Server..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

& $localMvnCmd spring-boot:run
