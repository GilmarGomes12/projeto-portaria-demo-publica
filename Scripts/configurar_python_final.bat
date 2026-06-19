@echo off
REM Script para configurar o ambiente Python para detecção de placas
REM Versão corrigida com Python 3.12

echo ========================================
echo   CONFIGURACAO AMBIENTE PYTHON
echo   Para Deteccao de Placas - Versao Final
echo ========================================
echo.

REM Define o caminho do Python correto
set PYTHON_PATH=C:\Development\Tools\Python\python312\python.exe

echo Verificando instalacao do Python...
%PYTHON_PATH% --version
if %errorlevel% neq 0 (
    echo ERRO: Python nao encontrado em %PYTHON_PATH%
    echo Verifique se o Python 3.12 esta instalado neste caminho.
    pause
    exit /b 1
)

echo.
echo Verificando dependencias existentes...
%PYTHON_PATH% -c "import sys; print('Python path:', sys.executable)"

echo.
echo Instalando dependencias necessarias...
echo - OpenCV (Computer Vision)
echo - pytesseract (OCR)
echo - Pillow (Manipulacao de imagens)

%PYTHON_PATH% -m pip install --upgrade pip
%PYTHON_PATH% -m pip install opencv-python
%PYTHON_PATH% -m pip install pytesseract
%PYTHON_PATH% -m pip install Pillow

echo.
echo Testando importacoes...
%PYTHON_PATH% -c "import cv2; print('✅ OpenCV versao:', cv2.__version__)"
if %errorlevel% neq 0 (
    echo ❌ ERRO: OpenCV nao foi instalado corretamente
    pause
    exit /b 1
)

%PYTHON_PATH% -c "import pytesseract; print('✅ pytesseract importado com sucesso')"
if %errorlevel% neq 0 (
    echo ❌ ERRO: pytesseract nao foi instalado corretamente
    pause
    exit /b 1
)

echo.
echo ========================================
echo   CONFIGURACAO CONCLUIDA COM SUCESSO!
echo ========================================
echo.
echo Para executar os scripts, use:
echo 1. testar_deteccao.bat - Para testes interativos
echo 2. monitor_portao.bat - Para monitoramento continuo
echo 3. detector_placa.bat - Para deteccao pontual
echo.
pause
