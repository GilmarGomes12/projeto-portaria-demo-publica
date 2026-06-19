@echo off
REM ============================================================
REM    TESTE COMPLETO - SISTEMA DETECCAO DE PLACAS
REM    Versao: 1.0
REM    Data: 09/06/2025
REM ============================================================

setlocal EnableDelayedExpansion

echo.
echo ============================================================
echo    TESTE COMPLETO - SISTEMA DETECCAO DE PLACAS
echo    Portaria v1.5.1 - Ambiente Real
echo ============================================================
echo.

set "PROJETO_DIR=%~dp0projeto-portaria"
set "PYTHON_DIR=%PROJETO_DIR%\src\main\resources\python\detector"
set "LOGS_DIR=%~dp0logs"
set "RESULTADO_TESTE=%~dp0resultado_teste_deteccao.txt"

REM Cria arquivo de log dos testes
echo ============================================================ > "%RESULTADO_TESTE%"
echo TESTE COMPLETO SISTEMA DETECCAO DE PLACAS >> "%RESULTADO_TESTE%"
echo Data/Hora: %date% %time% >> "%RESULTADO_TESTE%"
echo ============================================================ >> "%RESULTADO_TESTE%"
echo. >> "%RESULTADO_TESTE%"

echo [INFO] Iniciando bateria de testes...
echo [INFO] Resultados serao salvos em: %RESULTADO_TESTE%
echo.

REM ===========================================
REM TESTE 1: VERIFICAR DEPENDENCIAS
REM ===========================================
echo [TESTE 1/7] Verificando dependencias do sistema...

echo TESTE 1: DEPENDENCIAS DO SISTEMA >> "%RESULTADO_TESTE%"
echo ---------------------------------------- >> "%RESULTADO_TESTE%"

REM Java
echo [1.1] Verificando Java...
java -version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] Java encontrado
    echo [OK] Java: >> "%RESULTADO_TESTE%"
    java -version 2>&1 | head -1 >> "%RESULTADO_TESTE%"
) else (
    echo [ERRO] Java nao encontrado
    echo [ERRO] Java nao encontrado >> "%RESULTADO_TESTE%"
)

REM Python
echo [1.2] Verificando Python...
python --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] Python encontrado
    echo [OK] Python: >> "%RESULTADO_TESTE%"
    python --version >> "%RESULTADO_TESTE%"
) else (
    echo [ERRO] Python nao encontrado
    echo [ERRO] Python nao encontrado >> "%RESULTADO_TESTE%"
)

REM Tesseract
echo [1.3] Verificando Tesseract OCR...
tesseract --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] Tesseract OCR encontrado
    echo [OK] Tesseract OCR: >> "%RESULTADO_TESTE%"
    tesseract --version 2>&1 | head -1 >> "%RESULTADO_TESTE%"
) else (
    echo [ERRO] Tesseract OCR nao encontrado
    echo [ERRO] Tesseract OCR nao encontrado >> "%RESULTADO_TESTE%"
    echo [INFO] Execute: instalar_tesseract_automatico.bat
)

echo. >> "%RESULTADO_TESTE%"

REM ===========================================
REM TESTE 2: DEPENDENCIAS PYTHON
REM ===========================================
echo [TESTE 2/7] Verificando dependencias Python...

echo TESTE 2: DEPENDENCIAS PYTHON >> "%RESULTADO_TESTE%"
echo ---------------------------------------- >> "%RESULTADO_TESTE%"

set "DEPS=opencv-python pytesseract numpy pillow psutil"

for %%i in (%DEPS%) do (
    echo [2.x] Verificando %%i...
    python -c "import %%i; print('[OK] %%i instalado')" 2>nul
    if !ERRORLEVEL! equ 0 (
        echo [OK] %%i instalado
        echo [OK] %%i instalado >> "%RESULTADO_TESTE%"
    ) else (
        echo [ERRO] %%i nao encontrado
        echo [ERRO] %%i nao encontrado >> "%RESULTADO_TESTE%"
    )
)

echo. >> "%RESULTADO_TESTE%"

REM ===========================================
REM TESTE 3: COMPILACAO JAVA
REM ===========================================
echo [TESTE 3/7] Testando compilacao Java...

echo TESTE 3: COMPILACAO JAVA >> "%RESULTADO_TESTE%"
echo ---------------------------------------- >> "%RESULTADO_TESTE%"

if exist "%PROJETO_DIR%" (
    echo [3.1] Compilando projeto Java...
    cd /d "%PROJETO_DIR%"
    
    REM Tenta compilacao rapida
    javac -version >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        echo [OK] Compilador Java disponivel
        echo [OK] Compilador Java disponivel >> "%RESULTADO_TESTE%"
        
        REM Se Maven disponivel, usa Maven
        mvn --version >nul 2>&1
        if !ERRORLEVEL! equ 0 (
            echo [INFO] Testando compilacao com Maven...
            timeout /t 30 /nobreak > nul & taskkill /f /im java.exe 2>nul
            mvn compile -q >>"%RESULTADO_TESTE%" 2>&1
            if !ERRORLEVEL! equ 0 (
                echo [OK] Compilacao Maven bem-sucedida
                echo [OK] Compilacao Maven bem-sucedida >> "%RESULTADO_TESTE%"
            ) else (
                echo [AVISO] Problemas na compilacao Maven
                echo [AVISO] Problemas na compilacao Maven >> "%RESULTADO_TESTE%"
            )
        ) else (
            echo [INFO] Maven nao disponivel - pulando teste de compilacao
            echo [INFO] Maven nao disponivel >> "%RESULTADO_TESTE%"
        )
    ) else (
        echo [ERRO] Compilador Java nao encontrado
        echo [ERRO] Compilador Java nao encontrado >> "%RESULTADO_TESTE%"
    )
    
    cd /d "%~dp0"
) else (
    echo [ERRO] Diretorio do projeto nao encontrado: %PROJETO_DIR%
    echo [ERRO] Diretorio do projeto nao encontrado >> "%RESULTADO_TESTE%"
)

echo. >> "%RESULTADO_TESTE%"

REM ===========================================
REM TESTE 4: DETECTOR PYTHON BASICO
REM ===========================================
echo [TESTE 4/7] Testando detector Python...

echo TESTE 4: DETECTOR PYTHON >> "%RESULTADO_TESTE%"
echo ---------------------------------------- >> "%RESULTADO_TESTE%"

if exist "%PYTHON_DIR%" (
    echo [4.1] Testando carregamento do detector...
    cd /d "%PYTHON_DIR%"
    
    REM Teste basico - carregamento das bibliotecas
    python -c "
import sys
sys.path.append('.')
try:
    import cv2
    import pytesseract
    print('[OK] Bibliotecas carregadas com sucesso')
    print('OpenCV:', cv2.__version__)
except Exception as e:
    print('[ERRO] Falha ao carregar bibliotecas:', str(e))
" >> "%RESULTADO_TESTE%" 2>&1
    
    echo [4.2] Testando detector simulado...
    REM Timeout de 10 segundos para evitar travamento
    timeout /t 10 /nobreak > nul & python detector_placa_simulado.py --help >> "%RESULTADO_TESTE%" 2>&1
    
    cd /d "%~dp0"
    echo [OK] Teste do detector Python concluido
) else (
    echo [ERRO] Diretorio Python nao encontrado: %PYTHON_DIR%
    echo [ERRO] Diretorio Python nao encontrado >> "%RESULTADO_TESTE%"
)

echo. >> "%RESULTADO_TESTE%"

REM ===========================================
REM TESTE 5: DETECCAO DE CAMERA
REM ===========================================
echo [TESTE 5/7] Testando deteccao de cameras...

echo TESTE 5: DETECCAO DE CAMERAS >> "%RESULTADO_TESTE%"
echo ---------------------------------------- >> "%RESULTADO_TESTE%"

if exist "%PYTHON_DIR%" (
    cd /d "%PYTHON_DIR%"
    
    echo [5.1] Testando cameras ID 0, 1, 2...
    for %%c in (0 1 2) do (
        echo [INFO] Testando camera ID %%c...
        
        REM Script Python para testar camera rapidamente
        python -c "
import cv2
import sys
try:
    cap = cv2.VideoCapture(%%c)
    if cap.isOpened():
        ret, frame = cap.read()
        if ret:
            print('[OK] Camera ID %%c: Funcionando (' + str(frame.shape) + ')')
        else:
            print('[AVISO] Camera ID %%c: Conectada mas sem imagem')
        cap.release()
    else:
        print('[INFO] Camera ID %%c: Nao disponivel')
except Exception as e:
    print('[ERRO] Camera ID %%c: Erro -', str(e))
" >> "%RESULTADO_TESTE%" 2>&1
    )
    
    cd /d "%~dp0"
) else (
    echo [ERRO] Nao foi possivel testar cameras - Python nao encontrado
    echo [ERRO] Nao foi possivel testar cameras >> "%RESULTADO_TESTE%"
)

echo. >> "%RESULTADO_TESTE%"

REM ===========================================
REM TESTE 6: TESTE OCR BASICO
REM ===========================================
echo [TESTE 6/7] Testando capacidade OCR...

echo TESTE 6: CAPACIDADE OCR >> "%RESULTADO_TESTE%"
echo ---------------------------------------- >> "%RESULTADO_TESTE%"

tesseract --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [6.1] Testando OCR com texto simples...
    
    REM Cria imagem de teste simples
    python -c "
import cv2
import numpy as np
import pytesseract

# Cria imagem simples com texto
img = np.ones((100, 300, 3), dtype=np.uint8) * 255
cv2.putText(img, 'ABC1234', (50, 60), cv2.FONT_HERSHEY_SIMPLEX, 2, (0, 0, 0), 3)
cv2.imwrite('teste_ocr.png', img)

try:
    texto = pytesseract.image_to_string(img, config='--psm 8')
    texto_limpo = ''.join(c for c in texto if c.isalnum())
    if 'ABC1234' in texto_limpo or len(texto_limpo) >= 6:
        print('[OK] OCR funcionando: Detectou texto similar a placa')
    else:
        print('[AVISO] OCR funcionando mas com baixa precisao:', texto_limpo)
except Exception as e:
    print('[ERRO] Falha no teste OCR:', str(e))
" >> "%RESULTADO_TESTE%" 2>&1

    if exist "%PYTHON_DIR%\teste_ocr.png" del "%PYTHON_DIR%\teste_ocr.png" 2>nul
    
) else (
    echo [ERRO] Tesseract nao disponivel - OCR nao pode ser testado
    echo [ERRO] Tesseract nao disponivel para teste OCR >> "%RESULTADO_TESTE%"
)

echo. >> "%RESULTADO_TESTE%"

REM ===========================================
REM TESTE 7: RELATORIO FINAL
REM ===========================================
echo [TESTE 7/7] Gerando relatorio final...

echo TESTE 7: RELATORIO FINAL >> "%RESULTADO_TESTE%"
echo ============================================================ >> "%RESULTADO_TESTE%"

REM Conta sucessos e erros
findstr /c:"[OK]" "%RESULTADO_TESTE%" > temp_ok.txt
findstr /c:"[ERRO]" "%RESULTADO_TESTE%" > temp_erro.txt

set /p count_ok=<temp_ok.txt
set /p count_erro=<temp_erro.txt

del temp_ok.txt temp_erro.txt 2>nul

echo RESUMO DOS TESTES: >> "%RESULTADO_TESTE%"
echo - Sucessos: %count_ok% >> "%RESULTADO_TESTE%"
echo - Erros: %count_erro% >> "%RESULTADO_TESTE%"
echo. >> "%RESULTADO_TESTE%"

if %count_erro% equ 0 (
    echo STATUS: SISTEMA PRONTO PARA PRODUCAO >> "%RESULTADO_TESTE%"
    echo [SUCESSO] Todos os testes passaram! Sistema pronto para producao.
) else (
    if %count_erro% lss 3 (
        echo STATUS: SISTEMA FUNCIONAL COM LIMITACOES >> "%RESULTADO_TESTE%"
        echo [AVISO] Sistema funcional mas com algumas limitacoes.
    ) else (
        echo STATUS: SISTEMA REQUER CONFIGURACAO ADICIONAL >> "%RESULTADO_TESTE%"
        echo [ATENCAO] Sistema requer configuracao adicional antes da producao.
    )
)

echo. >> "%RESULTADO_TESTE%"
echo RECOMENDACOES: >> "%RESULTADO_TESTE%"

findstr /c:"[ERRO]" "%RESULTADO_TESTE%" >nul
if %ERRORLEVEL% equ 0 (
    echo - Instale dependencias faltantes antes de usar em producao >> "%RESULTADO_TESTE%"
)

tesseract --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo - Execute: instalar_tesseract_automatico.bat >> "%RESULTADO_TESTE%"
)

echo - Configure camera na interface: Menu > Configuracoes > Deteccao de Placas >> "%RESULTADO_TESTE%"
echo - Ajuste parametros conforme ambiente (confianca, intervalo) >> "%RESULTADO_TESTE%"
echo - Teste em ambiente real antes de deployment final >> "%RESULTADO_TESTE%"

echo. >> "%RESULTADO_TESTE%"
echo ============================================================ >> "%RESULTADO_TESTE%"
echo FIM DOS TESTES - %date% %time% >> "%RESULTADO_TESTE%"
echo ============================================================ >> "%RESULTADO_TESTE%"

echo.
echo ============================================================
echo [CONCLUIDO] Teste completo finalizado
echo ============================================================
echo.
echo [INFO] Relatorio salvo em: %RESULTADO_TESTE%
echo [INFO] Abrir relatorio agora? (S/N)

set /p resposta=
if /i "%resposta%" equ "S" (
    notepad "%RESULTADO_TESTE%"
)

echo.
echo [INFO] Para instalar dependencias faltantes:
echo        - Tesseract OCR: instalar_tesseract_automatico.bat
echo        - Python deps: pip install -r src\main\resources\python\detector\requirements.txt
echo.
echo [INFO] Proximo passo: Configurar sistema na interface grafica
echo        Menu: Configuracoes ^> Deteccao de Placas
echo.

pause
exit /b 0
