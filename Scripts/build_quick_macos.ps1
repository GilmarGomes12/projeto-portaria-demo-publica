#!/usr/bin/env pwsh
# ============================================================================
# Build Rápido para MacOS - Sistema de Portaria v1.5.1
# Gera JAR Light e distribui automaticamente
# ============================================================================

# Configurações
$PROJECT_ROOT = "/Users/gilmargomes/Desktop/projeto-portaria-1.5"
$MAVEN_PROJECT = "$PROJECT_ROOT/projeto-portaria"
$MACOS_DIST = "$PROJECT_ROOT/Distribucao/MacOS"

# Cores
$GREEN = "Green"
$YELLOW = "Yellow"
$CYAN = "Cyan"
$RED = "Red"

function Write-Status {
    param([string]$Message, [string]$Color = "White")
    Write-Host "🔄 $Message" -ForegroundColor $Color
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor $GREEN
}

function Write-Error {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor $RED
}

# Header
Write-Host "===============================================" -ForegroundColor $CYAN
Write-Host "    BUILD RÁPIDO MACOS - PORTARIA v1.5.1" -ForegroundColor $CYAN
Write-Host "===============================================" -ForegroundColor $CYAN

# Navega para o projeto
Set-Location $MAVEN_PROJECT

# Build com pom-light
Write-Status "Compilando versão Light..." $YELLOW
mvn clean package -f pom-light.xml -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Error "Falha na compilação"
    exit 1
}

# Localiza JAR
$JAR_FILE = Get-ChildItem -Path "target" -Filter "projeto-portaria-light-*.jar" | Select-Object -First 1

if (-not $JAR_FILE) {
    Write-Error "JAR Light não foi gerado"
    exit 1
}

# Cria pasta bin se necessário
$BIN_DIR = "$MACOS_DIST/bin"
if (-not (Test-Path $BIN_DIR)) {
    New-Item -Path $BIN_DIR -ItemType Directory -Force | Out-Null
}

# Copia JAR
Copy-Item $JAR_FILE.FullName -Destination "$BIN_DIR/sistema-portaria-light.jar" -Force

$JAR_SIZE = [math]::Round($JAR_FILE.Length / 1MB, 2)
Write-Success "JAR Light distribuído ($JAR_SIZE MB)"

# Torna scripts executáveis (simulação - será feito no macOS)
Write-Success "Distribuição MacOS atualizada!"
Write-Host ""
Write-Host "💡 PRÓXIMOS PASSOS NO MACOS:" -ForegroundColor $YELLOW
Write-Host "   cd Distribucao/MacOS" -ForegroundColor "White"
Write-Host "   chmod +x *.sh" -ForegroundColor "White"
Write-Host "   ./instalar_sistema.sh" -ForegroundColor "White"
