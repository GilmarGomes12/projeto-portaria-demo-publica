# Script de Compilação e Distribuição Completa - v1.5.1
# Este script executa todo o processo de compilação, geração de instaladores e validação
# Data: 31/05/2025

# Configura a codificação para UTF-8 e tratamento de erros
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"

# Define um arquivo de log para capturar todo o processo
$LOG_FILE = "c:\Development\projeto-portaria-1.5\logs\distribuicao_v1.5.1_completa_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"

# Função para registrar mensagens no console e no arquivo de log
function Write-LogMessage {
    param(
        [string]$Message,
        [string]$ForegroundColor = "White"
    )
    
    Write-Host $Message -ForegroundColor $ForegroundColor
    Add-Content -Path $LOG_FILE -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - $Message"
}

# Cabeçalho
Clear-Host
Write-LogMessage "╔═══════════════════════════════════════════════════════════════════╗" "Cyan"
Write-LogMessage "║ Sistema de Portaria v1.5.1 - Processo de Distribuição Completo    ║" "Cyan"
Write-LogMessage "║ Data: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')                             ║" "Cyan"
Write-LogMessage "╚═══════════════════════════════════════════════════════════════════╝" "Cyan"
Write-LogMessage ""

# Define diretórios principais
$PROJECT_DIR = "c:\Development\projeto-portaria-1.5"
$SCRIPTS_DIR = "$PROJECT_DIR\Scripts"

# Etapa 1: Verificar pré-requisitos
Write-LogMessage "► ETAPA 1: Verificando pré-requisitos..." "Yellow"

# Verifica se Maven está instalado
try {
    $mvnVersion = (mvn --version)
    Write-LogMessage "✓ Maven encontrado: $($mvnVersion[0])" "Green"
} catch {
    Write-LogMessage "❌ Maven não encontrado. Impossível continuar." "Red"
    Write-LogMessage "Por favor, instale o Maven e tente novamente." "Red"
    exit 1
}

# Verifica se Java está instalado
try {
    $javaVersion = (java --version)
    Write-LogMessage "✓ Java encontrado: $($javaVersion[0])" "Green"
} catch {
    Write-LogMessage "❌ Java não encontrado. Impossível continuar." "Red"
    Write-LogMessage "Por favor, instale o Java 21 ou superior e tente novamente." "Red"
    exit 1
}

# Verifica se o arquivo pom.xml está atualizado para a versão 1.5.1
$pomContent = Get-Content -Path "$PROJECT_DIR\projeto-portaria\pom.xml" -Raw
if ($pomContent -match '<version>1.5.1</version>') {
    Write-LogMessage "✓ Arquivo POM.xml está com a versão correta (1.5.1)" "Green"
} else {
    Write-LogMessage "❌ Arquivo POM.xml não contém a versão 1.5.1." "Red"
    Write-LogMessage "Por favor, atualize o arquivo POM.xml antes de continuar." "Red"
    exit 1
}

Write-LogMessage "► Pré-requisitos verificados com sucesso." "Green"
Write-LogMessage ""

# Etapa 2: Compilar o projeto
Write-LogMessage "► ETAPA 2: Compilando o projeto..." "Yellow"
Write-LogMessage "Executando script de compilação..."

try {
    & "$SCRIPTS_DIR\compilar_v1.5.1.ps1" | Tee-Object -Append $LOG_FILE
    
    if ($LASTEXITCODE -ne 0) {
        Write-LogMessage "❌ Falha na compilação do projeto. Verifique os logs para mais detalhes." "Red"
        exit 1
    }
    
    Write-LogMessage "✓ Projeto compilado com sucesso." "Green"
} catch {
    Write-LogMessage "❌ Erro ao executar o script de compilação: $_" "Red"
    exit 1
}

Write-LogMessage ""

# Etapa 3: Criar instaladores
Write-LogMessage "► ETAPA 3: Criando instaladores..." "Yellow"
Write-LogMessage "Executando script de criação de instaladores..."

try {
    & "$SCRIPTS_DIR\criar_instalador_v1.5.1.ps1" | Tee-Object -Append $LOG_FILE
    
    if ($LASTEXITCODE -ne 0) {
        Write-LogMessage "❌ Falha na criação dos instaladores. Verifique os logs para mais detalhes." "Red"
        exit 1
    }
    
    Write-LogMessage "✓ Instaladores criados com sucesso." "Green"
} catch {
    Write-LogMessage "❌ Erro ao executar o script de criação de instaladores: $_" "Red"
    exit 1
}

Write-LogMessage ""

# Etapa 4: Testar o sistema compilado
Write-LogMessage "► ETAPA 4: Testando o sistema compilado..." "Yellow"

# Verifica se o JAR existe
$JAR_PATH = "$PROJECT_DIR\Distribucao\v1.5.1\Sistema-Portaria-v1.5.1.jar"
if (-not (Test-Path $JAR_PATH)) {
    Write-LogMessage "❌ Arquivo JAR não encontrado em: $JAR_PATH" "Red"
    exit 1
} else {
    Write-LogMessage "✓ Arquivo JAR encontrado: $JAR_PATH" "Green"
}

# Teste de integridade do JAR (verifica se o arquivo pode ser listado)
try {
    $jarOutput = jar -tf $JAR_PATH | Select-Object -First 5
    Write-LogMessage "✓ JAR válido. Primeiros 5 arquivos na estrutura:" "Green"
    foreach ($line in $jarOutput) {
        Write-LogMessage "  - $line" "Gray"
    }
} catch {
    Write-LogMessage "❌ Falha ao verificar o conteúdo do JAR. Arquivo pode estar corrompido." "Red"
    exit 1
}

Write-LogMessage "✓ Sistema testado com sucesso." "Green"
Write-LogMessage ""

# Etapa 5: Verificar documentação
Write-LogMessage "► ETAPA 5: Verificando documentação..." "Yellow"

# Lista a documentação crítica que deve estar presente
$DOCS_REQUIRED = @(
    "$PROJECT_DIR\Documentacao_OLD_20250604_190740\correcao_resource_leak_getConexao_31052025.md",
    "$PROJECT_DIR\Documentacao\_Arquivados\relatorio_executivo_correcoes_v1.5.1_31052025.md",
    "$PROJECT_DIR\Documentacao_OLD_20250604_190740\instrucoes_atualizacao_v1.5.1.md",
    "$PROJECT_DIR\Documentacao_OLD_20250604_190740\guia_desenvolvedores_correcoes_v1.5.1.md",
    "$PROJECT_DIR\Documentacao_OLD_20250604_190740\arquivos_compilados_v1.5.1.md"
)

$missingDocs = @()

foreach ($doc in $DOCS_REQUIRED) {
    if (Test-Path $doc) {
        Write-LogMessage "✓ Documentação encontrada: $($doc.Split('\')[-1])" "Green"
    } else {
        $missingDocs += $doc
        Write-LogMessage "❌ Documentação não encontrada: $($doc.Split('\')[-1])" "Red"
    }
}

if ($missingDocs.Count -gt 0) {
    Write-LogMessage "⚠️ Aviso: $($missingDocs.Count) documento(s) não encontrado(s)." "Yellow"
} else {
    Write-LogMessage "✓ Toda a documentação necessária foi encontrada." "Green"
}

Write-LogMessage ""

# Etapa 6: Preparar resumo final
Write-LogMessage "► ETAPA 6: Preparando resumo da distribuição..." "Yellow"

# Coleta informações sobre os arquivos gerados
$JAR_SIZE = "{0:N2} MB" -f ((Get-Item $JAR_PATH).Length / 1MB)
$INSTALADORES_DIR = "$PROJECT_DIR\Instaladores"
$INSTALADORES = Get-ChildItem -Path $INSTALADORES_DIR -Filter "SistemaPortaria_v1.5.1*" | 
                Select-Object Name, @{Name="Size(MB)"; Expression={"{0:N2}" -f ($_.Length / 1MB)}}

$DISTRIBUICAO_DIR = "$PROJECT_DIR\Distribucao"
$DISTRIBUICAO = Get-ChildItem -Path $DISTRIBUICAO_DIR -Filter "Sistema_Portaria_v1.5.1*" | 
                Select-Object Name, @{Name="Size(MB)"; Expression={"{0:N2}" -f ($_.Length / 1MB)}}

# Cria um relatório de distribuição
$RELATORIO = @"
# Resumo de Distribuição - Sistema de Portaria v1.5.1

**Data:** $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')
**Versão:** 1.5.1

## Arquivos Gerados

### Arquivo JAR Principal
- **Nome:** Sistema-Portaria-v1.5.1.jar
- **Tamanho:** $JAR_SIZE
- **Local:** $JAR_PATH

### Pacotes de Distribuição
$(($DISTRIBUICAO | ForEach-Object { "- **$($_.Name)** ($($_.("Size(MB)")) MB)" }) -join "`n")

### Instaladores
$(($INSTALADORES | ForEach-Object { "- **$($_.Name)** ($($_.("Size(MB)")) MB)" }) -join "`n")

## Documentação Incluída

- correcao_resource_leak_getConexao_31052025.md
- relatorio_executivo_correcoes_v1.5.1_31052025.md
- instrucoes_atualizacao_v1.5.1.md
- guia_desenvolvedores_correcoes_v1.5.1.md
- arquivos_compilados_v1.5.1.md

## Correções Aplicadas

1. **Correção de resource leak em PlacaDetector.java**
   - Adicionada liberação adequada de objetos Mat do OpenCV
   
2. **Substituição do método depreciado getConexao() por getConnection()**
   - Atualizado em 8 locais em VeiculoDAO.java
   
## Processo de Distribuição Concluído

Este relatório foi gerado automaticamente pelo script de distribuição completa.
O log detalhado da compilação está disponível em: $LOG_FILE
"@

$RELATORIO_PATH = "$PROJECT_DIR\Distribucao\relatorio_distribuicao_v1.5.1_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
$RELATORIO | Out-File -FilePath $RELATORIO_PATH -Encoding UTF8

Write-LogMessage "✓ Relatório de distribuição gerado em: $RELATORIO_PATH" "Green"
Write-LogMessage ""

# Mensagem final
Write-LogMessage "╔═══════════════════════════════════════════════════════════════════╗" "Cyan"
Write-LogMessage "║ ✅ PROCESSO DE DISTRIBUIÇÃO CONCLUÍDO COM SUCESSO!               ║" "Cyan"
Write-LogMessage "╚═══════════════════════════════════════════════════════════════════╝" "Cyan"
Write-LogMessage ""
Write-LogMessage "📁 JAR: $JAR_PATH" "White"
Write-LogMessage "📁 Instaladores: $INSTALADORES_DIR" "White"
Write-LogMessage "📄 Relatório: $RELATORIO_PATH" "White"
Write-LogMessage "📄 Log detalhado: $LOG_FILE" "White"
Write-LogMessage ""
Write-LogMessage "Para distribuir o sistema, utilize os instaladores gerados." "Yellow"
Write-LogMessage "Para instruções de atualização, consulte: instrucoes_atualizacao_v1.5.1.md" "Yellow"
Write-LogMessage ""
