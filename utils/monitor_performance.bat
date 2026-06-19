@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   MONITOR DE PERFORMANCE - PORTARIA v1.5.1
echo ========================================
echo.

set "LOG_FILE=logs\portaria.log"
set "TEMP_FILE=%TEMP%\portaria_stats.tmp"

if not exist "%LOG_FILE%" (
    echo ❌ ERRO: Arquivo de log nao encontrado
    echo    Execute o sistema primeiro para gerar logs
    pause
    exit /b 1
)

echo [1/4] Analisando deteccoes de placas...
findstr /c:"Placa detectada" "%LOG_FILE%" > "%TEMP_FILE%" 2>nul
if exist "%TEMP_FILE%" (
    for /f %%a in ('type "%TEMP_FILE%" ^| find /c /v ""') do set "TOTAL_DETECCOES=%%a"
) else (
    set "TOTAL_DETECCOES=0"
)

echo [2/4] Calculando estatisticas de confianca...
set "ALTA_CONFIANCA=0"
set "MEDIA_CONFIANCA=0"
set "BAIXA_CONFIANCA=0"

for /f "tokens=*" %%i in ('findstr /c:"confiança:" "%LOG_FILE%" 2^>nul') do (
    set "line=%%i"
    for /f "tokens=*" %%j in ("!line!") do (
        echo %%j | findstr /r "confiança: [0-9][0-9]%%" >nul
        if !errorlevel! equ 0 (
            for /f "tokens=2 delims=()" %%k in ("%%j") do (
                set "conf=%%k"
                set "conf=!conf:confiança: =!"
                set "conf=!conf:%%=!"
                if !conf! geq 90 (
                    set /a "ALTA_CONFIANCA+=1"
                ) else if !conf! geq 75 (
                    set /a "MEDIA_CONFIANCA+=1"
                ) else (
                    set /a "BAIXA_CONFIANCA+=1"
                )
            )
        )
    )
)

echo [3/4] Verificando status de autorizacoes...
findstr /c:"AUTORIZADO:" "%LOG_FILE%" > "%TEMP_FILE%" 2>nul
if exist "%TEMP_FILE%" (
    for /f %%a in ('type "%TEMP_FILE%" ^| find /c /v ""') do set "TOTAL_AUTORIZACOES=%%a"
) else (
    set "TOTAL_AUTORIZACOES=0"
)

findstr /c:"NÃO AUTORIZADO:" "%LOG_FILE%" > "%TEMP_FILE%" 2>nul
if exist "%TEMP_FILE%" (
    for /f %%a in ('type "%TEMP_FILE%" ^| find /c /v ""') do set "TOTAL_NAO_AUTORIZADOS=%%a"
) else (
    set "TOTAL_NAO_AUTORIZADOS=0"
)

echo [4/4] Verificando pool de conexoes...
findstr /c:"Conexão thread-local" "%LOG_FILE%" > "%TEMP_FILE%" 2>nul
if exist "%TEMP_FILE%" (
    for /f %%a in ('type "%TEMP_FILE%" ^| find /c /v ""') do set "RECONEXOES=%%a"
) else (
    set "RECONEXOES=0"
)

echo.
echo ========================================
echo   RELATORIO DE PERFORMANCE
echo ========================================
echo.
echo 📊 DETECCAO DE PLACAS:
echo    Total de deteccoes: %TOTAL_DETECCOES%
echo    Alta confianca (90-100%%): %ALTA_CONFIANCA%
echo    Media confianca (75-89%%): %MEDIA_CONFIANCA%
echo    Baixa confianca (^<75%%): %BAIXA_CONFIANCA%
echo.
echo 🔐 AUTORIZACOES:
echo    Veiculos autorizados: %TOTAL_AUTORIZACOES%
echo    Veiculos nao autorizados: %TOTAL_NAO_AUTORIZADOS%
echo.
echo 🔗 POOL DE CONEXOES:
echo    Reconexoes realizadas: %RECONEXOES%
if %RECONEXOES% gtr 50 (
    echo    ⚠️  ATENCAO: Muitas reconexoes detectadas
)
echo.
echo 📈 EFICIENCIA GERAL:
if %TOTAL_DETECCOES% gtr 0 (
    set /a "EFICIENCIA=(%ALTA_CONFIANCA%+%MEDIA_CONFIANCA%)*100/%TOTAL_DETECCOES%"
    echo    Taxa de deteccao valida: !EFICIENCIA!%%
    if !EFICIENCIA! geq 90 (
        echo    ✅ EXCELENTE performance
    ) else if !EFICIENCIA! geq 75 (
        echo    ✅ BOA performance
    ) else (
        echo    ⚠️  Performance pode ser melhorada
    )
) else (
    echo    Nenhuma deteccao registrada
)

echo.
echo ========================================
echo   DATA/HORA: %date% %time%
echo ========================================

if exist "%TEMP_FILE%" del "%TEMP_FILE%"
echo.
pause
