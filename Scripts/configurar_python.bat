@echo off
echo ========================================
echo   CONFIGURACAO SISTEMA DETECCAO PLACAS
echo ========================================
echo.

echo [1/3] Verificando Python...
python --version
if %errorlevel% neq 0 (
    echo ERRO: Python nao encontrado!
    echo Instale o Python 3.7+ antes de continuar.
    pause
    exit /b 1
)

echo [2/3] Instalando dependencias Python...
cd /d "%~dp0"
pip install -r requirements.txt
if %errorlevel% neq 0 (
    echo ERRO: Falha ao instalar dependencias!
    pause
    exit /b 1
)

echo [3/3] Verificando Tesseract OCR...
where tesseract >nul 2>&1
if %errorlevel% neq 0 (
    echo AVISO: Tesseract OCR nao encontrado no PATH.
    echo.
    echo Baixe e instale o Tesseract OCR de:
    echo https://github.com/UB-Mannheim/tesseract/wiki
    echo.
    echo Depois edite o arquivo detector_placa_final.py
    echo e descomente a linha com o caminho do tesseract.exe
) else (
    echo Tesseract OCR encontrado: OK
)

echo.
echo ========================================
echo   CONFIGURACAO CONCLUIDA!
echo ========================================
echo.
echo Para testar:
echo   python teste_deteccao.py 1    (camera USB)
echo   python teste_deteccao.py 3    (imagem)
echo.
echo Para monitoramento continuo:
echo   python monitor_portao.py 0    (camera USB)
echo   python monitor_portao.py "rtsp://..."  (camera IP)
echo.
pause
