# Script de Compilacao Simples - Sistema de Portaria v1.5.1

$PROJECT_DIR = "c:\Development\projeto-portaria-1.5"
$SRC_DIR = "$PROJECT_DIR\src"
$DIST_DIR = "$PROJECT_DIR\Distribucao\v1.5.1"

Write-Host "Compilando Sistema de Portaria v1.5.1..." -ForegroundColor Cyan

# Verifica se o código fonte existe
if (-not (Test-Path $SRC_DIR)) {
    Write-Host "ERRO: Diretório de código fonte não encontrado: $SRC_DIR" -ForegroundColor Red
    exit 1
}

# Procura pelo arquivo JAR já compilado
$jarFiles = Get-ChildItem -Path $PROJECT_DIR -Name "*.jar" -Recurse
if ($jarFiles.Count -eq 0) {
    Write-Host "ERRO: Nenhum arquivo JAR encontrado. Compile o projeto primeiro." -ForegroundColor Red
    Write-Host "Use sua IDE (IntelliJ IDEA, Eclipse, etc.) ou Maven/Gradle para compilar." -ForegroundColor Yellow
    exit 1
}

# Encontra o JAR mais recente
$latestJar = Get-ChildItem -Path $PROJECT_DIR -Name "*.jar" -Recurse | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$jarPath = $latestJar.FullName

Write-Host "JAR encontrado: $jarPath" -ForegroundColor Green

# Cria estrutura de distribuição
if (-not (Test-Path $DIST_DIR)) {
    New-Item -Path $DIST_DIR -ItemType Directory -Force | Out-Null
}

# Copia o JAR para a distribuição
$targetJar = "$DIST_DIR\Sistema-Portaria-v1.5.1.jar"
Copy-Item -Path $jarPath -Destination $targetJar -Force
Write-Host "JAR copiado para: $targetJar" -ForegroundColor Green

Write-Host "Compilação concluída!" -ForegroundColor Green
