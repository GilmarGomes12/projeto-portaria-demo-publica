#!/usr/bin/env pwsh
# ============================================================================
# Script Unificado para Build e Distribuição MacOS
# Sistema de Portaria v1.5.1 - Simplificado e Otimizado
# ============================================================================

param(
    [switch]$Light,      # Usar pom-light.xml (JAR menor)
    [switch]$Deploy,     # Copiar automaticamente para distribuição MacOS
    [switch]$Package,    # Criar estrutura completa de instalação
    [switch]$Clean       # Limpar builds anteriores
)

# Configurações
$PROJECT_ROOT = "/Users/gilmargomes/Documents/projeto-portaria-1.5-main"
$MAVEN_PROJECT = "$PROJECT_ROOT/projeto-portaria"
$MACOS_DIST = "$PROJECT_ROOT/Distribucao/MacOS"
$VERSION = "1.5.1"

# Cores para output
$RED = "Red"
$GREEN = "Green"
$YELLOW = "Yellow"
$CYAN = "Cyan"
$WHITE = "White"

function Write-Step {
    param([string]$Message, [string]$Color = $WHITE)
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

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️ $Message" -ForegroundColor $YELLOW
}

# Header
Clear-Host
Write-Host "============================================================" -ForegroundColor $CYAN
Write-Host "      BUILD AUTOMATIZADO MACOS - SISTEMA PORTARIA" -ForegroundColor $CYAN
Write-Host "                     Versão $VERSION" -ForegroundColor $CYAN
Write-Host "============================================================" -ForegroundColor $CYAN
Write-Host ""

# Verifica pré-requisitos
Write-Step "Verificando pré-requisitos..." $YELLOW

if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    Write-Error "Maven não encontrado. Instale o Maven e tente novamente."
    exit 1
}

if (-not (Get-Command "java" -ErrorAction SilentlyContinue)) {
    Write-Error "Java não encontrado. Instale o Java 21+ e tente novamente."
    exit 1
}

Write-Success "Pré-requisitos verificados"

# Navega para o diretório do projeto
Set-Location $MAVEN_PROJECT

# Limpeza (se solicitado)
if ($Clean) {
    Write-Step "Limpando builds anteriores..." $YELLOW
    mvn clean | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Limpeza concluída"
    } else {
        Write-Error "Falha na limpeza"
        exit 1
    }
}

# Determina qual POM usar
$POM_FILE = if ($Light) { "pom-light.xml" } else { "pom.xml" }
$JAR_TYPE = if ($Light) { "LIGHT" } else { "COMPLETO" }
$JAR_SIZE = if ($Light) { "~18MB" } else { "~920MB" }

Write-Step "Compilando projeto ($JAR_TYPE - $JAR_SIZE)..." $YELLOW
Write-Host "   📁 POM: $POM_FILE" -ForegroundColor $WHITE

# Compilação
mvn clean package -f $POM_FILE -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Error "Falha na compilação"
    exit 1
}

# Localiza o JAR gerado
$JAR_PATTERN = if ($Light) { "projeto-portaria-light-*.jar" } else { "projeto-portaria-*.jar" }
$JAR_FILE = Get-ChildItem -Path "target" -Filter $JAR_PATTERN | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $JAR_FILE) {
    Write-Error "JAR não foi gerado em target/"
    exit 1
}

$JAR_PATH = $JAR_FILE.FullName
$JAR_NAME = $JAR_FILE.Name
$JAR_SIZE_MB = [math]::Round($JAR_FILE.Length / 1MB, 2)

Write-Success "Compilação concluída"
Write-Host "   📦 JAR: $JAR_NAME ($JAR_SIZE_MB MB)" -ForegroundColor $WHITE

# Deploy para MacOS (se solicitado)
if ($Deploy -or $Package) {
    Write-Step "Preparando distribuição MacOS..." $YELLOW
    
    # Cria pasta bin se não existir
    $BIN_DIR = "$MACOS_DIST/bin"
    if (-not (Test-Path $BIN_DIR)) {
        New-Item -Path $BIN_DIR -ItemType Directory -Force | Out-Null
        Write-Host "   📁 Pasta bin criada" -ForegroundColor $WHITE
    }
    
    # Copia JAR com nome padronizado
    $TARGET_JAR = if ($Light) { "sistema-portaria-light.jar" } else { "sistema-portaria.jar" }
    Copy-Item $JAR_PATH -Destination "$BIN_DIR/$TARGET_JAR" -Force
    Write-Success "JAR copiado para distribuição MacOS"
    Write-Host "   📍 Local: $BIN_DIR/$TARGET_JAR" -ForegroundColor $WHITE
    
    # Atualiza o script executar_sistema.sh para apontar para o JAR correto
    $EXEC_SCRIPT = "$MACOS_DIST/executar_sistema.sh"
    if (Test-Path $EXEC_SCRIPT) {
        $content = Get-Content $EXEC_SCRIPT -Raw
        $newContent = $content -replace 'JAR_FILE="\$SCRIPT_DIR/bin/sistema-portaria\.jar"', "JAR_FILE=`"`$SCRIPT_DIR/bin/$TARGET_JAR`""
        Set-Content $EXEC_SCRIPT -Value $newContent -Encoding UTF8
        Write-Success "Script executar_sistema.sh atualizado"
    } else {
        Write-Warning "Script executar_sistema.sh não encontrado, usando executar_simples.sh"
    }
}

# Criar pacote completo (se solicitado)
if ($Package) {
    Write-Step "Criando pacote completo MacOS..." $YELLOW
    
    $PACKAGE_NAME = "Sistema-Portaria-v$VERSION-MacOS-$(if ($Light) { 'Light' } else { 'Complete' }).zip"
    $PACKAGE_PATH = "$PROJECT_ROOT\Distribucao\Pacotes\$PACKAGE_NAME"
    
    # Cria pasta de pacotes se não existir
    $PACKAGES_DIR = "$PROJECT_ROOT/Distribucao/Pacotes"
    if (-not (Test-Path $PACKAGES_DIR)) {
        New-Item -Path $PACKAGES_DIR -ItemType Directory -Force | Out-Null
    }
    
    # Compacta a distribuição MacOS
    if (Test-Path $PACKAGE_PATH) {
        Remove-Item $PACKAGE_PATH -Force
    }
    
    Compress-Archive -Path "$MACOS_DIST/*" -DestinationPath $PACKAGE_PATH -Force
    Write-Success "Pacote criado: $PACKAGE_NAME"
    
    $PACKAGE_SIZE_MB = [math]::Round((Get-Item $PACKAGE_PATH).Length / 1MB, 2)
    Write-Host "   📦 Tamanho: $PACKAGE_SIZE_MB MB" -ForegroundColor $WHITE
}

# Resumo final
Write-Host ""
Write-Host "============================================================" -ForegroundColor $CYAN
Write-Host "                    RESUMO DO BUILD" -ForegroundColor $CYAN
Write-Host "============================================================" -ForegroundColor $CYAN
Write-Host "✅ Tipo: $JAR_TYPE ($JAR_SIZE_MB MB)" -ForegroundColor $GREEN
Write-Host "✅ JAR: $JAR_NAME" -ForegroundColor $GREEN
if ($Deploy -or $Package) {
    Write-Host "✅ Distribuição: Atualizada" -ForegroundColor $GREEN
}
if ($Package) {
    Write-Host "✅ Pacote: $PACKAGE_NAME" -ForegroundColor $GREEN
}

Write-Host ""
Write-Host "💡 PRÓXIMOS PASSOS:" -ForegroundColor $YELLOW
Write-Host "   1. Testar localmente: ./Distribucao/MacOS/executar_simples.sh" -ForegroundColor $WHITE
if ($Package) {
    Write-Host "   2. Distribuir pacote: Distribucao/Pacotes/$PACKAGE_NAME" -ForegroundColor $WHITE
}
Write-Host "   3. Em MacOS: chmod +x *.sh && ./executar_simples.sh" -ForegroundColor $WHITE

Write-Host ""
Write-Host "🚀 Build concluído com sucesso!" -ForegroundColor $GREEN
