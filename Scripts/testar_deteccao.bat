@echo off
REM Script para executar o teste de detecção de placas
REM Usa Python 3.12 que tem todas as dependências instaladas

set PYTHON_PATH=C:\Development\Tools\Python\python312\python.exe

echo ========================================
echo      TESTE DE DETECCAO DE PLACAS
echo ========================================
echo.

if not exist "%PYTHON_PATH%" (
    echo ERRO: Python nao encontrado em %PYTHON_PATH%
    echo Execute primeiro: configurar_python_final.bat
    pause
    exit /b 1
)

%PYTHON_PATH% teste_deteccao.py %*
pause
