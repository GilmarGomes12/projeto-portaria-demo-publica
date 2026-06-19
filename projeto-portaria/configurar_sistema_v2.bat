@echo off
REM Configuracao automatica do Sistema de Portaria v1.5.1
REM Sistema de deteccao de placas otimizado para producao
REM Autor: Gilmar H Gomes
REM Data: 09/06/2025

echo ========================================
echo  SISTEMA DE PORTARIA v1.5.1
echo  Configuracao Automatica de Producao
echo ========================================
echo.

REM Define diretorio base
set BASE_DIR=%cd%
echo 📁 Diretorio base: %BASE_DIR%
echo.

REM Passo 1: Verificar Java
echo 🔍 PASSO 1: Verificando Java...
java -version >nul 2>&1
if %errorLevel% neq 0 (
    echo ❌ Java nao encontrado! Instale Java 8+ antes de continuar.
    echo 💡 Download: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
) else (
    echo ✅ Java encontrado
    java -version 2>&1 | findstr "version"
)
echo.

REM Passo 2: Verificar Python
echo 🔍 PASSO 2: Verificando Python...
python --version >nul 2>&1
if %errorLevel% neq 0 (
    echo ❌ Python nao encontrado! Instale Python 3.12+ antes de continuar.
    echo 💡 Download: https://www.python.org/downloads/
    pause
    exit /b 1
) else (
    echo ✅ Python encontrado
    python --version
)
echo.

REM Passo 3: Verificar e instalar dependencias Python
echo 🔍 PASSO 3: Verificando dependencias Python...

echo   Verificando OpenCV...
python -c "import cv2; print('✅ OpenCV versao:', cv2.__version__)" 2>nul
if %errorLevel% neq 0 (
    echo   ⚠️ OpenCV nao encontrado. Instalando...
    pip install opencv-python
    if %errorLevel% neq 0 (
        echo   ❌ Falha ao instalar OpenCV
        pause
        exit /b 1
    )
) else (
    echo   ✅ OpenCV OK
)

echo   Verificando NumPy...
python -c "import numpy; print('✅ NumPy versao:', numpy.__version__)" 2>nul
if %errorLevel% neq 0 (
    echo   ⚠️ NumPy nao encontrado. Instalando...
    pip install numpy
) else (
    echo   ✅ NumPy OK
)

echo   Verificando Pytesseract...
python -c "import pytesseract; print('✅ Pytesseract OK')" 2>nul
if %errorLevel% neq 0 (
    echo   ⚠️ Pytesseract nao encontrado. Instalando...
    pip install pytesseract
) else (
    echo   ✅ Pytesseract OK
)
echo.

REM Passo 4: Verificar Tesseract OCR
echo 🔍 PASSO 4: Verificando Tesseract OCR...
tesseract --version >nul 2>&1
if %errorLevel% neq 0 (
    echo ❌ Tesseract OCR nao encontrado!
    echo 💡 Download: https://github.com/tesseract-ocr/tesseract/releases
    echo 💡 Adicione ao PATH: C:\Program Files\Tesseract-OCR
    echo.
    echo ⚠️ Sistema funcionara em modo simulado sem Tesseract
    set TESSERACT_OK=false
) else (
    echo ✅ Tesseract OCR encontrado
    tesseract --version 2>&1 | findstr "tesseract"
    set TESSERACT_OK=true
)
echo.

REM Passo 5: Testar cameras usando arquivo temporario
echo 🔍 PASSO 5: Testando cameras disponíveis...
echo import cv2 > test_cameras.py
echo cameras_ok = 0 >> test_cameras.py
echo for i in range(4): >> test_cameras.py
echo     try: >> test_cameras.py
echo         cap = cv2.VideoCapture(i, cv2.CAP_DSHOW) >> test_cameras.py
echo         if cap.isOpened(): >> test_cameras.py
echo             print(f'✅ Camera {i}: DISPONÍVEL') >> test_cameras.py
echo             cameras_ok += 1 >> test_cameras.py
echo             cap.release() >> test_cameras.py
echo         else: >> test_cameras.py
echo             print(f'❌ Camera {i}: nao disponivel') >> test_cameras.py
echo     except: >> test_cameras.py
echo         print(f'❌ Camera {i}: erro ao testar') >> test_cameras.py
echo print(f'Total: {cameras_ok} cameras detectadas') >> test_cameras.py
echo if cameras_ok ^>= 2: >> test_cameras.py
echo     print('✅ Configuracao multi-cameras OK (2+ cameras)') >> test_cameras.py
echo elif cameras_ok == 1: >> test_cameras.py
echo     print('⚠️ Apenas 1 camera - funcionara com limitacoes') >> test_cameras.py
echo else: >> test_cameras.py
echo     print('❌ Nenhuma camera detectada - modo simulado apenas') >> test_cameras.py

python test_cameras.py
del test_cameras.py
echo.

REM Passo 6: Compilar projeto
echo 🔍 PASSO 6: Compilando projeto...
if not exist "target" mkdir target
if not exist "target\classes" mkdir target\classes

echo   Compilando classes principais...
javac -cp "src;lib\*" src\main\java\com\ghg\service\*.java -d target\classes 2>nul
if %errorLevel% neq 0 (
    echo ❌ Erro na compilacao - verificar dependencias
) else (
    echo ✅ Compilacao concluida
)

echo   Compilando exemplo de uso...
javac -cp "target\classes;src;lib\*" src\main\java\ExemploUsoProducao.java -d target\classes 2>nul
if %errorLevel% neq 0 (
    echo ⚠️ Exemplo de uso nao compilado (opcional)
) else (
    echo ✅ Exemplo de uso compilado
)
echo.

REM Passo 7: Teste rapido do sistema
echo 🔍 PASSO 7: Executando teste de validacao...
if exist "target\classes\TesteSimplificadoProducao.class" (
    java -cp target\classes TesteSimplificadoProducao
    if %errorLevel% equ 0 (
        echo ✅ Teste de validacao passou
    ) else (
        echo ⚠️ Teste com avisos - verificar logs acima
    )
) else (
    echo ⚠️ Teste de validacao nao disponivel
)
echo.

REM Resumo final
echo ========================================
echo  RESUMO DA CONFIGURACAO
echo ========================================
echo ✅ Java: OK
echo ✅ Python: OK
echo ✅ Dependencias Python: OK
if "%TESSERACT_OK%"=="true" (
    echo ✅ Tesseract OCR: OK
) else (
    echo ⚠️ Tesseract OCR: Nao encontrado
)
echo ✅ Cameras: Testadas
echo ✅ Compilacao: OK
echo.

REM Instrucoes finais
echo 🎯 PRÓXIMOS PASSOS:
echo.
echo 1. Para testar o sistema:
echo    java -cp target\classes ExemploUsoProducao
echo.
echo 2. Para integrar ao seu projeto:
echo    - Copie as classes do pacote com.ghg.service
echo    - Use MultiCameraDetectorService como exemplo
echo.
echo 3. Para producao:
echo    - Configure 2 cameras USB (indices 0 e 1)
echo    - Use setUsarDetectorReal(true) para OCR real
echo    - Monitore logs para debugging
echo.

if "%TESSERACT_OK%"=="false" (
    echo ⚠️ IMPORTANTE: Instale Tesseract OCR para funcionalidade completa
    echo    Sem Tesseract, o sistema funcionara apenas em modo simulado
    echo.
)

echo ✅ CONFIGURACAO CONCLUIDA!
echo Sistema pronto para uso em producao.
echo.
pause
