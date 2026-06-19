# Script para criacao de instalador - Sistema de Portaria v1.5.1
# Versao simplificada e robusta

param(
    [switch]$SemWix = $false
)

# Configuracoes
$DATE = Get-Date -Format "yyyyMMdd_HHmmss"
$PROJECT_DIR = "c:\Development\projeto-portaria-1.5"
$DIST_DIR = "$PROJECT_DIR\Distribucao\v1.5.1"
$INSTALADORES_DIR = "$PROJECT_DIR\Instaladores"

# Funcoes
function Write-ColorText {
    param([string]$Text, [string]$Color = "White")
    Write-Host $Text -ForegroundColor $Color
}

function Test-WixToolset {
    try {
        $null = Get-Command candle.exe -ErrorAction SilentlyContinue
        $null = Get-Command light.exe -ErrorAction SilentlyContinue
        return $true
    } catch {
        return $false
    }
}

# Inicio do script
Clear-Host
Write-ColorText "======================================================" "Cyan"
Write-ColorText "  Script de Criacao de Instaladores - v1.5.1" "Cyan"
Write-ColorText "======================================================" "Cyan"
Write-Host ""

# Verifica diretorios
if (-not (Test-Path $DIST_DIR)) {
    Write-ColorText "ERRO: Diretorio de distribuicao nao encontrado!" "Red"
    Write-ColorText "Caminho: $DIST_DIR" "Red"
    Write-ColorText "Execute primeiro o script de compilacao." "Yellow"
    exit 1
}

if (-not (Test-Path $INSTALADORES_DIR)) {
    New-Item -Path $INSTALADORES_DIR -ItemType Directory -Force | Out-Null
    Write-ColorText "Diretorio de instaladores criado." "Green"
}

# Verifica WiX Toolset
$wixDisponivel = Test-WixToolset
if ($SemWix) {
    $wixDisponivel = $false
    Write-ColorText "WiX Toolset desabilitado via parametro." "Yellow"
} elseif (-not $wixDisponivel) {
    Write-ColorText "WiX Toolset nao encontrado - apenas instaladores basicos." "Yellow"
    
    $installWixScript = "$PROJECT_DIR\Scripts\instalar_wix_toolset.bat"
    if (Test-Path $installWixScript) {
        Write-ColorText "Script de instalacao do WiX encontrado: $installWixScript" "Cyan"
        Write-Host "Deseja instalar o WiX Toolset agora? (s/N): " -NoNewline
        $resposta = Read-Host
        if ($resposta -eq 's' -or $resposta -eq 'S') {
            Start-Process -FilePath $installWixScript -Wait
            $wixDisponivel = Test-WixToolset
        }
    }
}

Write-Host ""

# 1. Instalador ZIP
Write-ColorText "1. Criando instalador ZIP..." "White"
$zipPath = "$INSTALADORES_DIR\SistemaPortaria_v1.5.1_Setup_$DATE.zip"
try {
    Compress-Archive -Path "$DIST_DIR\*" -DestinationPath $zipPath -Force
    Write-ColorText "   OK: $zipPath" "Green"
} catch {
    Write-ColorText "   ERRO: $_" "Red"
}

# 2. Instalador Batch
Write-ColorText "2. Criando instalador batch..." "White"
$batchContent = @'
@echo off
chcp 65001 > nul 2>&1
title Sistema de Portaria v1.5.1 - Instalador

echo ====================================================
echo   Sistema de Portaria v1.5.1 - Instalador
echo ====================================================
echo.

set DEFAULT_DIR=C:\Programas\SistemaPortaria
set /p INSTALL_DIR="Diretorio de instalacao [%DEFAULT_DIR%]: "
if "%INSTALL_DIR%"=="" set INSTALL_DIR=%DEFAULT_DIR%

echo.
echo Instalando em: %INSTALL_DIR%
echo.

if not exist "%INSTALL_DIR%" (
    echo Criando diretorio...
    mkdir "%INSTALL_DIR%" 2>nul
    if errorlevel 1 (
        echo ERRO: Nao foi possivel criar o diretorio.
        pause
        exit /b 1
    )
)

echo Copiando arquivos...
xcopy /E /Y /Q "." "%INSTALL_DIR%\" >nul 2>&1
if errorlevel 1 (
    echo ERRO: Falha ao copiar arquivos.
    pause
    exit /b 1
)

echo Criando atalhos...
powershell -WindowStyle Hidden -Command "try { $ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut('%USERPROFILE%\Desktop\Sistema de Portaria.lnk'); $s.TargetPath = '%INSTALL_DIR%\Sistema-Portaria-v1.5.1.jar'; $s.WorkingDirectory = '%INSTALL_DIR%'; $s.Save() } catch { }" 2>nul

echo.
echo Instalacao concluida!
echo Sistema instalado em: %INSTALL_DIR%
echo Atalho criado na Area de Trabalho
echo.
pause
'@

$batchPath = "$INSTALADORES_DIR\Instalar_SistemaPortaria_v1.5.1.bat"
try {
    $batchContent | Out-File -FilePath $batchPath -Encoding UTF8 -Force
    Write-ColorText "   OK: $batchPath" "Green"
} catch {
    Write-ColorText "   ERRO: $_" "Red"
}

# 3. Instalador MSI (se WiX disponivel)
if ($wixDisponivel) {
    Write-ColorText "3. Criando instalador MSI..." "White"
    
    $WIX_DIR = "$INSTALADORES_DIR\temp_wix"
    if (Test-Path $WIX_DIR) {
        Remove-Item -Path $WIX_DIR -Recurse -Force
    }
    New-Item -Path $WIX_DIR -ItemType Directory -Force | Out-Null
    
    try {
        # GUIDs
        $productGuid = [System.Guid]::NewGuid().ToString().ToUpper()
        $upgradeGuid = [System.Guid]::NewGuid().ToString().ToUpper()
        $componentGuid = [System.Guid]::NewGuid().ToString().ToUpper()
        
        # Arquivo WXS simples
        $wxsContent = @"
<?xml version='1.0' encoding='UTF-8'?>
<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi'>
  <Product Id='$productGuid' Name='Sistema de Portaria' Version='1.5.1'
           Manufacturer='GHG Portaria' UpgradeCode='$upgradeGuid' Language='1046'>
    <Package Description='Sistema de Portaria' Manufacturer='GHG Portaria' 
             InstallerVersion='200' Compressed='yes' />
    <Media Id='1' Cabinet='files.cab' EmbedCab='yes' />
    
    <Directory Id='TARGETDIR' Name='SourceDir'>
      <Directory Id='ProgramFilesFolder'>
        <Directory Id='INSTALLDIR' Name='SistemaPortaria'>
          <Component Id='MainComponent' Guid='$componentGuid'>
            <File Id='JarFile' Name='Sistema-Portaria-v1.5.1.jar'
                  Source='$DIST_DIR\Sistema-Portaria-v1.5.1.jar' KeyPath='yes' />
          </Component>
        </Directory>
      </Directory>
    </Directory>
    
    <Feature Id='Complete' Level='1'>
      <ComponentRef Id='MainComponent' />
    </Feature>
  </Product>
</Wix>
"@

        $wxsFile = "$WIX_DIR\sistema.wxs"
        $wxsContent | Out-File -FilePath $wxsFile -Encoding UTF8 -Force
        
        # Compila MSI
        $oldLocation = Get-Location
        Set-Location -Path $WIX_DIR
        
        $null = & candle.exe sistema.wxs 2>&1
        if ($LASTEXITCODE -eq 0) {
            $msiPath = "$INSTALADORES_DIR\SistemaPortaria_v1.5.1_Setup_$DATE.msi"
            $null = & light.exe -out $msiPath sistema.wixobj 2>&1
            
            if ($LASTEXITCODE -eq 0 -and (Test-Path $msiPath)) {
                Write-ColorText "   OK: $msiPath" "Green"
            } else {
                Write-ColorText "   Falha ao gerar MSI" "Yellow"
                $msiPath = $null
            }
        } else {
            Write-ColorText "   Falha ao compilar WXS" "Yellow"
            $msiPath = $null
        }
        
        Set-Location -Path $oldLocation
        
    } catch {
        Write-ColorText "   ERRO: $_" "Red"
        $msiPath = $null
    } finally {
        if (Test-Path $WIX_DIR) {
            Remove-Item -Path $WIX_DIR -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
} else {
    Write-ColorText "3. Instalador MSI: Pulado (WiX nao disponivel)" "Yellow"
    $msiPath = $null
}

# 4. Pacote completo
Write-ColorText "4. Criando pacote completo..." "White"
$pacoteItems = @($zipPath, $batchPath)
if ($msiPath -and (Test-Path $msiPath)) {
    $pacoteItems += $msiPath
}

$pacotePath = "$INSTALADORES_DIR\SistemaPortaria_v1.5.1_TodosInstaladores_$DATE.zip"
try {
    Compress-Archive -Path $pacoteItems -DestinationPath $pacotePath -Force
    Write-ColorText "   OK: $pacotePath" "Green"
} catch {
    Write-ColorText "   ERRO: $_" "Red"
}

# Resumo
Write-Host ""
Write-ColorText "======================================================" "Green"
Write-ColorText "INSTALADORES CRIADOS COM SUCESSO!" "Green"
Write-ColorText "======================================================" "Green"
Write-Host ""
Write-ColorText "Instaladores criados em: $INSTALADORES_DIR" "Cyan"
Write-Host ""
if (Test-Path $zipPath) { Write-ColorText "ZIP:    $(Split-Path $zipPath -Leaf)" "White" }
if (Test-Path $batchPath) { Write-ColorText "BAT:    $(Split-Path $batchPath -Leaf)" "White" }
if ($msiPath -and (Test-Path $msiPath)) { Write-ColorText "MSI:    $(Split-Path $msiPath -Leaf)" "White" }
if (Test-Path $pacotePath) { Write-ColorText "PACOTE: $(Split-Path $pacotePath -Leaf)" "White" }
Write-Host ""
