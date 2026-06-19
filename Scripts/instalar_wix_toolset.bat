@echo off
REM Script para instalação automática do WiX Toolset v3.11.2
REM Este script baixa e instala o WiX Toolset necessário para criar instaladores MSI

echo ====================================================
echo Instalador Automático - WiX Toolset v3.11.2
echo ====================================================
echo.

REM Verifica se já está instalado
where candle.exe >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo ✅ WiX Toolset já está instalado.
    echo.
    candle.exe -help | findstr "version"
    echo.
    goto :end
)

echo Baixando WiX Toolset v3.11.2...
echo.

REM Cria diretório temporário
set TEMP_DIR=%TEMP%\wix_install
if not exist "%TEMP_DIR%" mkdir "%TEMP_DIR%"

REM URL do WiX Toolset v3.11.2
set WIX_URL=https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311.exe

REM Baixa o instalador usando PowerShell
echo Fazendo download do WiX Toolset...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%WIX_URL%' -OutFile '%TEMP_DIR%\wix311.exe'}"

if not exist "%TEMP_DIR%\wix311.exe" (
    echo ⚠️ ERRO: Não foi possível baixar o WiX Toolset.
    echo Verifique sua conexão com a internet e tente novamente.
    echo.
    echo Você também pode baixar manualmente em:
    echo https://wixtoolset.org/releases/
    echo.
    pause
    exit /b 1
)

echo.
echo Instalando WiX Toolset...
echo Por favor, aceite os termos da licença na janela que será aberta.
echo.

REM Executa o instalador
start /wait "%TEMP_DIR%\wix311.exe"

REM Aguarda um momento para a instalação completar
timeout /t 3 /nobreak >nul

REM Verifica se a instalação foi bem-sucedida
echo.
echo Verificando instalação...

REM Adiciona possíveis caminhos do WiX ao PATH temporariamente para verificação
set "PATH=%PATH%;C:\Program Files (x86)\WiX Toolset v3.11\bin"
set "PATH=%PATH%;C:\Program Files\WiX Toolset v3.11\bin"

where candle.exe >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo ✅ WiX Toolset instalado com sucesso!
    echo.
    candle.exe -help | findstr "version"
    echo.
    echo ⚠️ IMPORTANTE: Feche e reabra o PowerShell/CMD para que as alterações
    echo               no PATH tenham efeito.
    echo.
) else (
    echo ⚠️ AVISO: A instalação pode ter sido concluída, mas o WiX não foi
    echo           encontrado no PATH do sistema.
    echo.
    echo Verifique se o WiX foi instalado em:
    echo - C:\Program Files (x86)\WiX Toolset v3.11\bin
    echo - C:\Program Files\WiX Toolset v3.11\bin
    echo.
    echo Se o diretório existir, adicione-o manualmente ao PATH do sistema:
    echo 1. Abra as Configurações do Sistema
    echo 2. Vá em Sistema ^> Sobre ^> Configurações avançadas do sistema
    echo 3. Clique em "Variáveis de Ambiente"
    echo 4. Edite a variável PATH e adicione o caminho do WiX
    echo.
)

REM Limpa arquivos temporários
if exist "%TEMP_DIR%\wix311.exe" del "%TEMP_DIR%\wix311.exe"
if exist "%TEMP_DIR%" rmdir "%TEMP_DIR%"

:end
echo.
echo Instalação concluída.
pause
