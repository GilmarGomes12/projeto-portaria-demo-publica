@echo off
REM Script para executar o monitor contínuo do portão
REM Usa Python 3.12 que tem todas as dependências instaladas

set PYTHON_PATH=C:\Development\Tools\Python\python312\python.exe

if not exist "%PYTHON_PATH%" (
    echo ERRO: Python nao encontrado em %PYTHON_PATH%
    echo Execute primeiro: configurar_python_final.bat
    pause
    exit /b 1
)

if "%1"=="" (
    echo ERRO: URL da camera nao fornecida
    echo.
    echo Uso: monitor_portao.bat [URL_CAMERA]
    echo.
    echo Exemplos:
    echo   monitor_portao.bat 0                                    ^(camera USB^)
    echo   monitor_portao.bat "rtsp://admin:senha@192.168.1.100/stream"
    echo   monitor_portao.bat "http://192.168.1.100:8080/video"
    echo.
    pause
    exit /b 1
)

echo ========================================
echo     MONITOR CONTINUO DO PORTAO
echo ========================================
echo Camera: %1
echo Pressione Ctrl+C para parar
echo.

%PYTHON_PATH% monitor_portao.py %1
