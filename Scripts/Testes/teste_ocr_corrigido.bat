@echo off
echo.
echo ========================================
echo TESTE DO SISTEMA OCR CORRIGIDO
echo ========================================
echo.

echo 1. Verificando instalacao do Tesseract...
if exist "C:\Program Files\Tesseract-OCR\tesseract.exe" (
    echo ✓ Tesseract encontrado em: C:\Program Files\Tesseract-OCR\tesseract.exe
    "C:\Program Files\Tesseract-OCR\tesseract.exe" --version
) else (
    echo ✗ Tesseract NAO encontrado em C:\Program Files\Tesseract-OCR\
)

echo.
echo 2. Verificando arquivos de dados do Tesseract...
if exist "C:\Program Files\Tesseract-OCR\tessdata\eng.traineddata" (
    echo ✓ Dados de treinamento em ingles encontrados
) else (
    echo ✗ Dados de treinamento em ingles NAO encontrados
)

echo.
echo 3. Configurando variaveis de ambiente...
set TESSDATA_PREFIX=C:\Program Files\Tesseract-OCR\tessdata
echo ✓ TESSDATA_PREFIX configurado para: %TESSDATA_PREFIX%

echo.
echo 4. Compilando o projeto...
cd /d "c:\Development\projeto-portaria-1.5\projeto-portaria"
call mvn compile -q

if %ERRORLEVEL% EQU 0 (
    echo ✓ Compilacao realizada com sucesso
    
    echo.
    echo 5. Executando teste do sistema...
    call mvn exec:java -Dexec.mainClass="com.ghg.App" -Dexec.args="--test-ocr" -q
    
) else (
    echo ✗ Erro na compilacao
)

echo.
echo ========================================
echo TESTE CONCLUIDO
echo ========================================
pause
