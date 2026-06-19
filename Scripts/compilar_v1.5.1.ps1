# Script de Compilação e Empacotamento - Sistema de Portaria v1.5.1

# Configura a data atual
$DATE = Get-Date -Format "yyyyMMdd_HHmmss"

# Define diretórios
$PROJECT_DIR = "c:\Development\projeto-portaria-1.5\projeto-portaria"
$BACKUP_DIR = "c:\Development\projeto-portaria-1.5\Backups"
$OUTPUT_DIR = "c:\Development\projeto-portaria-1.5\Distribucao"

# Cria backup do projeto antes da compilação
Write-Host "Criando backup do projeto atual..." -ForegroundColor Cyan
$BACKUP_ZIP = "$BACKUP_DIR\Sistema_Portaria_v1.5.1_fonte_$DATE.zip"
Compress-Archive -Path $PROJECT_DIR -DestinationPath $BACKUP_ZIP -Force
Write-Host "Backup do código fonte criado em: $BACKUP_ZIP" -ForegroundColor Green

# Navega para o diretório do projeto
Set-Location -Path $PROJECT_DIR
Write-Host "Compilando o projeto..." -ForegroundColor Cyan

# Atualiza a versão no pom.xml
Write-Host "Atualizando versão no pom.xml para 1.5.1..." -ForegroundColor Yellow
$pomPath = "$PROJECT_DIR\pom.xml"
$pomContent = Get-Content -Path $pomPath -Raw
$updatedPomContent = $pomContent -replace '<version>1.4-SNAPSHOT</version>', '<version>1.5.1</version>'
$updatedPomContent | Set-Content -Path $pomPath -Encoding UTF8

# Limpa compilações anteriores
Write-Host "Limpando compilações anteriores..." -ForegroundColor Yellow
mvn clean

# Compila o projeto
Write-Host "Compilando o código..." -ForegroundColor Yellow
mvn compile

# Roda os testes
Write-Host "Executando testes unitários..." -ForegroundColor Yellow
mvn test

# Verifica se os testes passaram
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️ ERRO: Os testes falharam. Compilação interrompida." -ForegroundColor Red
    exit 1
}

# Empacota o projeto
Write-Host "Empacotando o projeto em JAR..." -ForegroundColor Yellow
mvn package -DskipTests

# Verifica se o JAR foi gerado
$JAR_PATH = "$PROJECT_DIR\target\projeto-portaria-1.5.1.jar"
if (-not (Test-Path $JAR_PATH)) {
    # Tenta encontrar o JAR com outro nome caso a versão não tenha sido atualizada corretamente
    $possibleJars = Get-ChildItem -Path "$PROJECT_DIR\target" -Filter "projeto-portaria-*.jar"
    if ($possibleJars.Count -gt 0) {
        $JAR_PATH = $possibleJars[0].FullName
        Write-Host "JAR encontrado com nome diferente: $JAR_PATH" -ForegroundColor Yellow
    } else {
        Write-Host "⚠️ ERRO: JAR não foi gerado em $PROJECT_DIR\target" -ForegroundColor Red
        exit 1
    }
}

# Cria diretório para nova versão na pasta de distribuição
$DIST_VERSION_DIR = "$OUTPUT_DIR\v1.5.1"
if (-not (Test-Path $DIST_VERSION_DIR)) {
    New-Item -Path $DIST_VERSION_DIR -ItemType Directory -Force | Out-Null
}

# Copia o JAR para diretório de distribuição
Copy-Item $JAR_PATH -Destination "$DIST_VERSION_DIR\Sistema-Portaria-v1.5.1.jar" -Force
Write-Host "JAR copiado para distribuição: $DIST_VERSION_DIR\Sistema-Portaria-v1.5.1.jar" -ForegroundColor Green

# Copia a documentação para o diretório de distribuição
$DOCS_DIR = "$DIST_VERSION_DIR\docs"
if (-not (Test-Path $DOCS_DIR)) {
    New-Item -Path $DOCS_DIR -ItemType Directory -Force | Out-Null
}

# Copia documentação relevante (somente para a v1.5.1)
$DOCS_TO_COPY = @(
    "c:\Development\projeto-portaria-1.5\Documentacao_OLD_20250604_190740\correcao_resource_leak_getConexao_31052025.md",
    "c:\Development\projeto-portaria-1.5\Documentacao\_Arquivados\relatorio_executivo_correcoes_v1.5.1_31052025.md",
    "c:\Development\projeto-portaria-1.5\Documentacao_OLD_20250604_190740\instrucoes_atualizacao_v1.5.1.md"
)

foreach ($doc in $DOCS_TO_COPY) {
    if (Test-Path $doc) {
        Copy-Item $doc -Destination $DOCS_DIR -Force
        Write-Host "Documentação copiada: $doc" -ForegroundColor Green
    } else {
        Write-Host "⚠️ AVISO: Documento não encontrado: $doc" -ForegroundColor Yellow
    }
}

# Cria arquivo de versão com data de compilação
$VERSION_INFO = @"
Sistema de Portaria - Versão 1.5.1
Data de Compilação: $(Get-Date -Format "dd/MM/yyyy HH:mm:ss")
Compilado por: $(whoami)
"@

$VERSION_INFO | Out-File -FilePath "$DIST_VERSION_DIR\versao.txt" -Encoding utf8 -Force
Write-Host "Arquivo de versão criado" -ForegroundColor Green

# Atualiza o arquivo versão principal
$VERSION_INFO | Out-File -FilePath "$OUTPUT_DIR\versao.txt" -Encoding utf8 -Force
Write-Host "Arquivo de versão principal atualizado" -ForegroundColor Green

# Empacota a distribuição completa
$DIST_ZIP = "$OUTPUT_DIR\Sistema_Portaria_v1.5.1_$DATE.zip"
Compress-Archive -Path $DIST_VERSION_DIR\* -DestinationPath $DIST_ZIP -Force
Write-Host "Pacote de distribuição criado em: $DIST_ZIP" -ForegroundColor Green

# Cria também um backup da distribuição
Copy-Item $DIST_ZIP -Destination "$BACKUP_DIR\Sistema_Portaria_v1.5.1_$DATE.zip" -Force
Write-Host "Backup da distribuição criado em: $BACKUP_DIR\Sistema_Portaria_v1.5.1_$DATE.zip" -ForegroundColor Green

# Mensagem final
Write-Host ""
Write-Host "✅ Compilação e empacotamento da v1.5.1 concluídos com sucesso!" -ForegroundColor Green
Write-Host "📁 JAR: $DIST_VERSION_DIR\Sistema-Portaria-v1.5.1.jar" -ForegroundColor White
Write-Host "📁 Distribuição: $DIST_ZIP" -ForegroundColor White
Write-Host "📁 Backup: $BACKUP_ZIP" -ForegroundColor White
Write-Host ""
Write-Host "Para instalar, copie o arquivo JAR para o diretório de instalação ou use o pacote de distribuição." -ForegroundColor Cyan
Write-Host ""
