# SCRIPT DE COMPILAÇÃO E SINCRONIZAÇÃO - SISTEMA PORTARIA v1.5.1
# Executa compilação Maven e sincroniza JAR para todas as plataformas

param(
    [switch]$SkipTests = $false
)

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "        COMPILAÇÃO E SINCRONIZAÇÃO - SISTEMA PORTARIA v1.5.1" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host

# Definir diretórios
$ProjetoDir = "c:\Development\projeto-portaria-1.5"
$MavenDir = "$ProjetoDir\projeto-portaria"
$DistDir = "$ProjetoDir\Distribucao"

Write-Host "[INFO] Verificando estrutura do projeto..." -ForegroundColor Yellow
if (-not (Test-Path $ProjetoDir)) {
    Write-Host "[ERRO] Diretório do projeto não encontrado: $ProjetoDir" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host "[INFO] Navegando para o diretório do Maven..." -ForegroundColor Yellow
Set-Location $MavenDir

Write-Host "[INFO] Limpando compilações anteriores..." -ForegroundColor Yellow
if (Test-Path "target") {
    Remove-Item -Path "target" -Recurse -Force
}

Write-Host "[INFO] Compilando o projeto com Maven..." -ForegroundColor Yellow
$mvnArgs = "clean", "package"
if ($SkipTests) {
    $mvnArgs += "-DskipTests"
}

Write-Host "[INFO] Executando: mvn $($mvnArgs -join ' ')" -ForegroundColor Green
$result = Start-Process -FilePath "mvn" -ArgumentList $mvnArgs -Wait -NoNewWindow -PassThru

if ($result.ExitCode -ne 0) {
    Write-Host "[ERRO] Falha na compilação do projeto" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Buscar JAR gerado
Write-Host "[INFO] Procurando JAR gerado..." -ForegroundColor Yellow
$jarFiles = Get-ChildItem -Path "target" -Filter "*.jar" | Where-Object { $_.Name -like "*dependencies*" -or $_.Name -like "*projeto-portaria*" }

if (-not $jarFiles) {
    Write-Host "[ERRO] Nenhum JAR foi gerado" -ForegroundColor Red
    Write-Host "[INFO] Arquivos no target:" -ForegroundColor Yellow
    Get-ChildItem -Path "target" -Filter "*.jar" | ForEach-Object { Write-Host "  - $($_.Name)" }
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Usar o JAR com dependências se disponível, senão o primeiro encontrado
$targetJar = $jarFiles | Where-Object { $_.Name -like "*dependencies*" } | Select-Object -First 1
if (-not $targetJar) {
    $targetJar = $jarFiles | Select-Object -First 1
}

Write-Host "[INFO] JAR encontrado: $($targetJar.Name)" -ForegroundColor Green
Write-Host "[INFO] Tamanho: $([math]::Round($targetJar.Length / 1MB, 2)) MB" -ForegroundColor Green

# Criar estrutura de diretórios
Write-Host "[INFO] Criando estrutura de diretórios..." -ForegroundColor Yellow
$dirs = @(
    "$DistDir\Windows\bin",
    "$DistDir\Linux\bin", 
    "$DistDir\MacOS\bin"
)

foreach ($dir in $dirs) {
    if (-not (Test-Path $dir)) {
        New-Item -Path $dir -ItemType Directory -Force | Out-Null
        Write-Host "[OK] Criado: $dir" -ForegroundColor Green
    }
}

# Copiar JAR para cada plataforma
Write-Host "[INFO] Sincronizando JAR para todas as plataformas..." -ForegroundColor Yellow

$destinations = @{
    "v1.5.1" = "$DistDir\v1.5.1\Sistema-Portaria-v1.5.1.jar"
    "Windows" = "$DistDir\Windows\bin\sistema-portaria.jar"
    "Linux" = "$DistDir\Linux\bin\sistema-portaria.jar"
    "MacOS" = "$DistDir\MacOS\bin\sistema-portaria.jar"
}

foreach ($platform in $destinations.Keys) {
    try {
        Copy-Item -Path $targetJar.FullName -Destination $destinations[$platform] -Force
        Write-Host "[OK] $platform atualizado" -ForegroundColor Green
    }
    catch {
        Write-Host "[ERRO] Falha ao copiar para $platform : $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Atualizar arquivo de versão
Write-Host "[INFO] Atualizando informações de versão..." -ForegroundColor Yellow
$versionInfo = @"
Sistema de Portaria - Versão 1.5.1
Data de Compilação: $(Get-Date -Format "dd/MM/yyyy HH:mm:ss")
Compilado por: $env:USERNAME
Tamanho do JAR: $([math]::Round($targetJar.Length / 1MB, 2)) MB
JAR fonte: $($targetJar.Name)
"@

$versionInfo | Out-File -FilePath "$DistDir\v1.5.1\versao.txt" -Encoding UTF8

# Verificar sincronização
Write-Host
Write-Host "[INFO] Verificando sincronização..." -ForegroundColor Yellow

foreach ($platform in $destinations.Keys) {
    $jarPath = $destinations[$platform]
    if (Test-Path $jarPath) {
        $jarSize = [math]::Round((Get-Item $jarPath).Length / 1MB, 2)
        Write-Host "[OK] $platform : $jarSize MB" -ForegroundColor Green
    }
    else {
        Write-Host "[AVISO] $platform : JAR não encontrado" -ForegroundColor Yellow
    }
}

Write-Host
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "                         SINCRONIZAÇÃO CONCLUÍDA" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "[INFO] Todas as distribuições foram sincronizadas com o JAR compilado" -ForegroundColor Green
Write-Host "[INFO] Tamanho do JAR: $([math]::Round($targetJar.Length / 1MB, 2)) MB" -ForegroundColor Green
Write-Host "[INFO] Para testar, execute os scripts de cada plataforma:" -ForegroundColor Yellow
Write-Host "        - Windows: Distribucao\Windows\executar_sistema.bat" -ForegroundColor White
Write-Host "        - Linux:   Distribucao\Linux\executar_sistema.sh" -ForegroundColor White
Write-Host "        - MacOS:   Distribucao\MacOS\executar_sistema.sh" -ForegroundColor White
Write-Host "===============================================================================" -ForegroundColor Cyan

Read-Host "Pressione Enter para continuar"
