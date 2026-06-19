@echo off
REM Script para executar a detecção pontual de placa
REM Usa Python 3.12 que tem todas as dependências instaladas

set PYTHON_PATH=C:\Development\Tools\Python\python312\python.exe

if not exist "%PYTHON_PATH%" (
    echo ERRO: Python nao encontrado em %PYTHON_PATH%
    echo Execute primeiro: configurar_python_final.bat
    pause
    exit /b 1
)

if "%1"=="" (
    echo ERRO: Camera ou imagem nao especificada
    echo.
    echo Uso: detector_placa.bat [CAMERA_OU_IMAGEM]
    echo.
    echo Exemplos:
    echo   detector_placa.bat 0                        ^(camera USB^)
    echo   detector_placa.bat "C:\imagens\placa.jpg"   ^(imagem^)
    echo.
    pause
    exit /b 1
)

%PYTHON_PATH% detector_placa_final.py %1
