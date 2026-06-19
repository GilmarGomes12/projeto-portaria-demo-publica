@echo off
chcp 65001 > nul
title Criador de Instaladores - Sistema Portaria v1.5.1

echo ======================================================
echo   Criador de Instaladores - Sistema Portaria v1.5.1
echo ======================================================
echo.

REM Define variaveis
set PROJECT_DIR=c:\Development\projeto-portaria-1.5
set DIST_DIR=%PROJECT_DIR%\Distribucao\v1.5.1
set INSTALADORES_DIR=%PROJECT_DIR%\Instaladores
set DATE_STAMP=%date:~6,4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set DATE_STAMP=%DATE_STAMP: =0%

REM Verifica se distribuicao existe
if not exist "%DIST_DIR%" (
    echo ❌ ERRO: Diretorio de distribuicao nao encontrado!
    echo    Caminho: %DIST_DIR%
    echo    Execute primeiro o script de compilacao.
    echo.
    pause
    exit /b 1
)

REM Cria diretorio de instaladores
if not exist "%INSTALADORES_DIR%" (
    mkdir "%INSTALADORES_DIR%"
    echo ✅ Diretorio de instaladores criado.
)

echo 🔨 Criando instaladores...
echo.

REM 1. Instalador ZIP
echo 1️⃣ Criando instalador ZIP...
set ZIP_FILE=%INSTALADORES_DIR%\SistemaPortaria_v1.5.1_Setup_%DATE_STAMP%.zip
powershell -Command "Compress-Archive -Path '%DIST_DIR%\*' -DestinationPath '%ZIP_FILE%' -Force" 2>nul
if exist "%ZIP_FILE%" (
    echo    ✅ ZIP criado: %ZIP_FILE%
) else (
    echo    ❌ Falha ao criar ZIP
)
echo.

REM 2. Instalador Batch Interativo
echo 2️⃣ Criando instalador batch...
set BAT_FILE=%INSTALADORES_DIR%\Instalar_SistemaPortaria_v1.5.1_%DATE_STAMP%.bat

echo @echo off > "%BAT_FILE%"
echo chcp 65001 ^> nul >> "%BAT_FILE%"
echo title Sistema de Portaria v1.5.1 - Instalador >> "%BAT_FILE%"
echo. >> "%BAT_FILE%"
echo echo ================================================== >> "%BAT_FILE%"
echo echo   Sistema de Portaria v1.5.1 - Instalador >> "%BAT_FILE%"
echo echo ================================================== >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo. >> "%BAT_FILE%"
echo set DEFAULT_DIR=C:\Programas\SistemaPortaria >> "%BAT_FILE%"
echo set /p INSTALL_DIR="Diretorio de instalacao [%%DEFAULT_DIR%%]: " >> "%BAT_FILE%"
echo if "%%INSTALL_DIR%%"=="" set INSTALL_DIR=%%DEFAULT_DIR%% >> "%BAT_FILE%"
echo. >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo echo Instalando em: %%INSTALL_DIR%% >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo. >> "%BAT_FILE%"
echo if not exist "%%INSTALL_DIR%%" ^( >> "%BAT_FILE%"
echo     echo Criando diretorio... >> "%BAT_FILE%"
echo     mkdir "%%INSTALL_DIR%%" 2^>nul >> "%BAT_FILE%"
echo     if errorlevel 1 ^( >> "%BAT_FILE%"
echo         echo ERRO: Nao foi possivel criar o diretorio. >> "%BAT_FILE%"
echo         pause >> "%BAT_FILE%"
echo         exit /b 1 >> "%BAT_FILE%"
echo     ^) >> "%BAT_FILE%"
echo ^) >> "%BAT_FILE%"
echo. >> "%BAT_FILE%"
echo echo Copiando arquivos... >> "%BAT_FILE%"
echo xcopy /E /Y /Q "." "%%INSTALL_DIR%%\" ^>nul 2^>^&1 >> "%BAT_FILE%"
echo if errorlevel 1 ^( >> "%BAT_FILE%"
echo     echo ERRO: Falha ao copiar arquivos. >> "%BAT_FILE%"
echo     pause >> "%BAT_FILE%"
echo     exit /b 1 >> "%BAT_FILE%"
echo ^) >> "%BAT_FILE%"
echo. >> "%BAT_FILE%"
echo echo Criando atalho na Area de Trabalho... >> "%BAT_FILE%"
echo powershell -WindowStyle Hidden -Command "try { $ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut('%%USERPROFILE%%\Desktop\Sistema de Portaria.lnk'^); $s.TargetPath = '%%INSTALL_DIR%%\Sistema-Portaria-v1.5.1.jar'; $s.WorkingDirectory = '%%INSTALL_DIR%%'; $s.Save(^) } catch { }" 2^>nul >> "%BAT_FILE%"
echo. >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo echo ✅ Instalacao concluida com sucesso! >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo echo 📁 Sistema instalado em: %%INSTALL_DIR%% >> "%BAT_FILE%"
echo echo 🖥️  Atalho criado na Area de Trabalho >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo echo Para executar o sistema: >> "%BAT_FILE%"
echo echo 1. Clique no atalho da Area de Trabalho >> "%BAT_FILE%"
echo echo 2. Ou execute: %%INSTALL_DIR%%\Sistema-Portaria-v1.5.1.jar >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo echo ⚠️  REQUISITO: Java 11+ deve estar instalado >> "%BAT_FILE%"
echo echo. >> "%BAT_FILE%"
echo pause >> "%BAT_FILE%"

if exist "%BAT_FILE%" (
    echo    ✅ Instalador batch criado: %BAT_FILE%
) else (
    echo    ❌ Falha ao criar instalador batch
)
echo.

REM 3. Verifica WiX Toolset
echo 3️⃣ Verificando WiX Toolset para MSI...
where candle.exe >nul 2>&1
if %errorlevel% == 0 (
    echo    ✅ WiX Toolset encontrado - tentando criar MSI...
    
    REM Cria diretorio temporario
    set WIX_TEMP=%INSTALADORES_DIR%\temp_wix
    if exist "%WIX_TEMP%" rmdir /s /q "%WIX_TEMP%"
    mkdir "%WIX_TEMP%"
    
    REM Cria arquivo WXS simples
    set WXS_FILE=%WIX_TEMP%\sistema.wxs
    
    echo ^<?xml version='1.0' encoding='UTF-8'?^> > "%WXS_FILE%"
    echo ^<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi'^> >> "%WXS_FILE%"
    echo   ^<Product Id='*' Name='Sistema de Portaria' Version='1.5.1' >> "%WXS_FILE%"
    echo            Manufacturer='GHG Portaria' UpgradeCode='12345678-1234-1234-1234-123456789012' Language='1046'^> >> "%WXS_FILE%"
    echo     ^<Package Description='Sistema de Portaria' Manufacturer='GHG Portaria' >> "%WXS_FILE%"
    echo              InstallerVersion='200' Compressed='yes' /^> >> "%WXS_FILE%"
    echo     ^<Media Id='1' Cabinet='files.cab' EmbedCab='yes' /^> >> "%WXS_FILE%"
    echo. >> "%WXS_FILE%"
    echo     ^<Directory Id='TARGETDIR' Name='SourceDir'^> >> "%WXS_FILE%"
    echo       ^<Directory Id='ProgramFilesFolder'^> >> "%WXS_FILE%"
    echo         ^<Directory Id='INSTALLDIR' Name='SistemaPortaria'^> >> "%WXS_FILE%"
    echo           ^<Component Id='MainComponent' Guid='87654321-4321-4321-4321-210987654321'^> >> "%WXS_FILE%"
    echo             ^<File Id='JarFile' Name='Sistema-Portaria-v1.5.1.jar' >> "%WXS_FILE%"
    echo                   Source='%DIST_DIR%\Sistema-Portaria-v1.5.1.jar' KeyPath='yes' /^> >> "%WXS_FILE%"
    echo           ^</Component^> >> "%WXS_FILE%"
    echo         ^</Directory^> >> "%WXS_FILE%"
    echo       ^</Directory^> >> "%WXS_FILE%"
    echo     ^</Directory^> >> "%WXS_FILE%"
    echo. >> "%WXS_FILE%"
    echo     ^<Feature Id='Complete' Level='1'^> >> "%WXS_FILE%"
    echo       ^<ComponentRef Id='MainComponent' /^> >> "%WXS_FILE%"
    echo     ^</Feature^> >> "%WXS_FILE%"
    echo   ^</Product^> >> "%WXS_FILE%"
    echo ^</Wix^> >> "%WXS_FILE%"
    
    REM Compila MSI
    cd /d "%WIX_TEMP%"
    candle.exe sistema.wxs >nul 2>&1
    if %errorlevel% == 0 (
        set MSI_FILE=%INSTALADORES_DIR%\SistemaPortaria_v1.5.1_Setup_%DATE_STAMP%.msi
        light.exe -out "%MSI_FILE%" sistema.wixobj >nul 2>&1
        if %errorlevel% == 0 (
            echo    ✅ MSI criado: %MSI_FILE%
        ) else (
            echo    ❌ Falha ao gerar MSI
        )
    ) else (
        echo    ❌ Falha ao compilar WXS
    )
    
    REM Limpa temporarios
    cd /d "%PROJECT_DIR%\Scripts"
    if exist "%WIX_TEMP%" rmdir /s /q "%WIX_TEMP%"
    
) else (
    echo    ⚠️ WiX Toolset nao encontrado - MSI nao sera criado
    echo    Para instalar: execute instalar_wix_toolset.bat
)
echo.

REM 4. Pacote completo
echo 4️⃣ Criando pacote com todos os instaladores...
set PACOTE_FILE=%INSTALADORES_DIR%\SistemaPortaria_v1.5.1_TodosInstaladores_%DATE_STAMP%.zip

REM Lista arquivos para empacotar
set ARQUIVOS_TEMP=%TEMP%\instaladores_list.txt
dir /b "%INSTALADORES_DIR%\*%DATE_STAMP%*" > "%ARQUIVOS_TEMP%" 2>nul

REM Cria comando PowerShell para empacotar
set PS_CMD=powershell -Command "$files = @(); Get-Content '%ARQUIVOS_TEMP%' | ForEach { $files += '%INSTALADORES_DIR%\' + $_ }; Compress-Archive -Path $files -DestinationPath '%PACOTE_FILE%' -Force"
%PS_CMD% >nul 2>&1

if exist "%PACOTE_FILE%" (
    echo    ✅ Pacote criado: %PACOTE_FILE%
) else (
    echo    ❌ Falha ao criar pacote
)

REM Limpa arquivo temporario
if exist "%ARQUIVOS_TEMP%" del "%ARQUIVOS_TEMP%"

echo.
echo ======================================================
echo 🎉 PROCESSO CONCLUIDO!
echo ======================================================
echo.
echo 📁 Instaladores criados em: %INSTALADORES_DIR%
echo.
echo Arquivos criados nesta execucao:
dir /b "%INSTALADORES_DIR%\*%DATE_STAMP%*" 2>nul
echo.
echo ✅ Use qualquer um dos instaladores acima para distribuir o sistema.
echo.
pause
