@echo off
echo ===============================================================================
echo        SCRIPT RAPIDO DE COMPILACAO - SISTEMA PORTARIA v1.5.1
echo ===============================================================================

:: Navegar para diretório do projeto Maven
cd /d "c:\Development\projeto-portaria-1.5\projeto-portaria"

:: Verificar se Maven está instalado
mvn --version >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] Maven nao encontrado. Instale Maven primeiro.
    echo Baixe em: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

:: Limpar e compilar
echo [INFO] Limpando compilacoes anteriores...
mvn clean

echo [INFO] Compilando projeto...
mvn package -DskipTests

:: Verificar se JAR foi gerado
if exist "target\*.jar" (
    echo [SUCCESS] JAR compilado com sucesso!
    dir target\*.jar
) else (
    echo [ERRO] JAR nao foi gerado
)

pause
