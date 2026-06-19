@echo off
setlocal EnableDelayedExpansion
echo ===============================================================================
echo        SCRIPT DE SINCRONIZACAO DAS DISTRIBUICOES - SISTEMA PORTARIA v1.5.1
echo ===============================================================================
echo.

:: Definir cores para o output
set "VERDE=echo."
set "VERMELHO=echo."
set "AZUL=echo."

:: Definir diretórios
set "PROJETO_DIR=c:\Development\projeto-portaria-1.5"
set "MAVEN_DIR=%PROJETO_DIR%\projeto-portaria"
set "DIST_DIR=%PROJETO_DIR%\Distribucao"
set "TARGET_JAR=%MAVEN_DIR%\target\projeto-portaria-1.5-jar-with-dependencies.jar"

echo [INFO] Verificando estrutura do projeto...
if not exist "%PROJETO_DIR%" (
    echo [ERRO] Diretorio do projeto nao encontrado: %PROJETO_DIR%
    pause
    exit /b 1
)

echo [INFO] Navegando para o diretorio do Maven...
cd /d "%MAVEN_DIR%"

echo [INFO] Compilando o projeto com Maven...
echo [INFO] Executando: mvn clean package
mvn clean package
if !errorlevel! neq 0 (
    echo [ERRO] Falha na compilacao do projeto
    pause
    exit /b 1
)

echo [INFO] Verificando se o JAR foi gerado...
if not exist "%TARGET_JAR%" (
    echo [ERRO] JAR nao foi gerado em: %TARGET_JAR%
    echo [INFO] Verificando arquivos no target:
    dir "%MAVEN_DIR%\target\*.jar"
    pause
    exit /b 1
)

:: Obter informações do JAR
for %%I in ("%TARGET_JAR%") do (
    set "JAR_SIZE=%%~zI"
    set "JAR_NAME=%%~nxI"
)

echo [INFO] JAR encontrado: !JAR_NAME!
echo [INFO] Tamanho: !JAR_SIZE! bytes

:: Criar estrutura de diretórios se não existir
echo [INFO] Criando estrutura de diretorios...
mkdir "%DIST_DIR%\Windows\bin" 2>nul
mkdir "%DIST_DIR%\Linux\bin" 2>nul
mkdir "%DIST_DIR%\MacOS\bin" 2>nul

:: Copiar JAR para cada plataforma
echo [INFO] Sincronizando JAR para todas as plataformas...

echo [INFO] - Copiando para v1.5.1...
copy "%TARGET_JAR%" "%DIST_DIR%\v1.5.1\Sistema-Portaria-v1.5.1.jar" >nul
if !errorlevel! equ 0 (
    echo [OK] v1.5.1 atualizado
) else (
    echo [ERRO] Falha ao copiar para v1.5.1
)

echo [INFO] - Copiando para Windows...
copy "%TARGET_JAR%" "%DIST_DIR%\Windows\bin\sistema-portaria.jar" >nul
if !errorlevel! equ 0 (
    echo [OK] Windows atualizado
) else (
    echo [ERRO] Falha ao copiar para Windows
)

echo [INFO] - Copiando para Linux...
copy "%TARGET_JAR%" "%DIST_DIR%\Linux\bin\sistema-portaria.jar" >nul
if !errorlevel! equ 0 (
    echo [OK] Linux atualizado
) else (
    echo [ERRO] Falha ao copiar para Linux
)

echo [INFO] - Copiando para MacOS...
copy "%TARGET_JAR%" "%DIST_DIR%\MacOS\bin\sistema-portaria.jar" >nul
if !errorlevel! equ 0 (
    echo [OK] MacOS atualizado
) else (
    echo [ERRO] Falha ao copiar para MacOS
)

:: Atualizar arquivo de versão
echo [INFO] Atualizando informacoes de versao...
echo Sistema de Portaria - Versao 1.5.1 > "%DIST_DIR%\v1.5.1\versao.txt"
echo Data de Compilacao: %date% %time% >> "%DIST_DIR%\v1.5.1\versao.txt"
echo Compilado por: %USERNAME% >> "%DIST_DIR%\v1.5.1\versao.txt"
echo Tamanho do JAR: !JAR_SIZE! bytes >> "%DIST_DIR%\v1.5.1\versao.txt"

:: Verificar sincronização
echo.
echo [INFO] Verificando sincronizacao...
for %%P in (v1.5.1 Windows\bin Linux\bin MacOS\bin) do (
    if exist "%DIST_DIR%\%%P\sistema-portaria.jar" (
        echo [OK] %%P: sistema-portaria.jar
    ) else if exist "%DIST_DIR%\%%P\Sistema-Portaria-v1.5.1.jar" (
        echo [OK] %%P: Sistema-Portaria-v1.5.1.jar
    ) else (
        echo [AVISO] %%P: JAR nao encontrado
    )
)

echo.
echo ===============================================================================
echo                         SINCRONIZACAO CONCLUIDA
echo ===============================================================================
echo [INFO] Todas as distribuicoes foram sincronizadas com o JAR compilado
echo [INFO] Tamanho do JAR: !JAR_SIZE! bytes
echo [INFO] Para testar, execute os scripts de cada plataforma:
echo        - Windows: Distribucao\Windows\executar_sistema.bat
echo        - Linux:   Distribucao\Linux\executar_sistema.sh
echo        - MacOS:   Distribucao\MacOS\executar_sistema.sh
echo ===============================================================================

pause
