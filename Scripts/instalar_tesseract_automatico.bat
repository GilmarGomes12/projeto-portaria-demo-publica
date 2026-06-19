@echo off
REM ============================================================
REM    INSTALADOR AUTOMATICO TESSERACT OCR - SISTEMA PORTARIA
REM    Versao: 1.0
REM    Data: 09/06/2025
REM ============================================================

setlocal EnableDelayedExpansion

echo.
echo ============================================================
echo    INSTALADOR AUTOMATICO TESSERACT OCR
echo    Sistema de Deteccao de Placas - Portaria v1.5.1
echo ============================================================
echo.

REM Verifica se está executando como administrador
net session >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERRO] Este script precisa ser executado como Administrador
    echo [INFO] Clique com botao direito e selecione "Executar como administrador"
    echo.
    pause
    exit /b 1
)

echo [INFO] Verificando sistema...

REM Verifica se Tesseract já está instalado
echo [1/5] Verificando instalacao existente...
where tesseract >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo [OK] Tesseract OCR ja esta instalado:
    tesseract --version 2>nul | findstr "tesseract"
    echo.
    echo [INFO] Deseja reinstalar? (S/N)
    set /p resposta=
    if /i "!resposta!" neq "S" (
        echo [INFO] Instalacao cancelada pelo usuario
        goto :verificar_python
    )
) else (
    echo [INFO] Tesseract OCR nao encontrado - procedendo com instalacao
)

echo.
echo [2/5] Baixando Tesseract OCR...

REM Cria diretorio temporario
set TEMP_DIR=%TEMP%\tesseract_install
if not exist "%TEMP_DIR%" mkdir "%TEMP_DIR%"

REM URLs de download (versoes mais recentes)
set TESSERACT_URL=https://github.com/UB-Mannheim/tesseract/releases/download/v5.3.0/tesseract-ocr-w64-setup-5.3.0.20221214.exe
set INSTALLER_FILE=%TEMP_DIR%\tesseract-installer.exe

echo [INFO] Baixando de: %TESSERACT_URL%
echo [INFO] Destino: %INSTALLER_FILE%

REM Baixa usando PowerShell (disponivel no Windows 7+)
powershell -Command "& {[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%TESSERACT_URL%' -OutFile '%INSTALLER_FILE%'}"

if not exist "%INSTALLER_FILE%" (
    echo [ERRO] Falha no download do Tesseract OCR
    echo [INFO] Por favor, baixe manualmente de:
    echo        https://github.com/UB-Mannheim/tesseract/wiki
    echo.
    pause
    exit /b 1
)

echo [OK] Download concluido com sucesso

echo.
echo [3/5] Instalando Tesseract OCR...

REM Instala silenciosamente com configuracoes otimizadas
echo [INFO] Executando instalador...
echo [INFO] Configuracoes: Adicionar ao PATH + Dados de idioma Portugues

"%INSTALLER_FILE%" /S /D="C:\Program Files\Tesseract-OCR"

REM Aguarda instalacao concluir
timeout /t 5 /nobreak >nul

echo [4/5] Verificando instalacao...

REM Adiciona ao PATH se necessario
set "TESSERACT_PATH=C:\Program Files\Tesseract-OCR"
echo %PATH% | findstr /i "tesseract" >nul
if %ERRORLEVEL% neq 0 (
    echo [INFO] Adicionando Tesseract ao PATH do sistema...
    setx PATH "%PATH%;%TESSERACT_PATH%" /M >nul 2>&1
    set "PATH=%PATH%;%TESSERACT_PATH%"
)

REM Testa instalacao
echo [INFO] Testando instalacao...
"%TESSERACT_PATH%\tesseract.exe" --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] Tesseract OCR instalado com sucesso!
    "%TESSERACT_PATH%\tesseract.exe" --version | findstr "tesseract"
) else (
    echo [AVISO] Tesseract instalado mas nao encontrado no PATH
    echo [INFO] Reinicie o computador para atualizar variaveis de ambiente
)

:verificar_python
echo.
echo [5/5] Verificando dependencias Python...

REM Verifica Python
python --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] Python encontrado:
    python --version
    
    REM Verifica pytesseract
    python -c "import pytesseract; print('[OK] pytesseract instalado')" 2>nul
    if %ERRORLEVEL% neq 0 (
        echo [INFO] Instalando pytesseract...
        pip install pytesseract
    )
    
    REM Verifica OpenCV
    python -c "import cv2; print('[OK] OpenCV instalado: v' + cv2.__version__)" 2>nul
    if %ERRORLEVEL% neq 0 (
        echo [INFO] Instalando OpenCV...
        pip install opencv-python
    )
    
) else (
    echo [AVISO] Python nao encontrado
    echo [INFO] Instale Python 3.8+ antes de continuar
)

echo.
echo ============================================================
echo [CONCLUIDO] Instalacao do ambiente OCR finalizada
echo ============================================================

REM Limpeza
if exist "%TEMP_DIR%" (
    echo [INFO] Limpando arquivos temporarios...
    rmdir /s /q "%TEMP_DIR%" 2>nul
)

echo.
echo [INFO] PROXIMO PASSO: Testar o sistema
echo.
echo Para testar, execute:
echo   1. Reinicie o computador (recomendado)
echo   2. Abra um novo prompt de comando
echo   3. Digite: tesseract --version
echo   4. Execute o sistema de portaria
echo.
echo [INFO] Configuracao da camera deve ser feita na interface do sistema
echo [INFO] Menu: Configuracoes ^> Deteccao de Placas
echo.

pause

echo [INFO] Deseja abrir a pasta do projeto? (S/N)
set /p resposta=
if /i "!resposta!" equ "S" (
    explorer "%~dp0"
)

exit /b 0
