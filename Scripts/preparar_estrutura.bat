@echo off
echo [INFO] Verificando e criando estrutura de distribuicoes...

:: Criar diretórios bin se não existirem
mkdir "c:\Development\projeto-portaria-1.5\Distribucao\Windows\bin" 2>nul
mkdir "c:\Development\projeto-portaria-1.5\Distribucao\Linux\bin" 2>nul
mkdir "c:\Development\projeto-portaria-1.5\Distribucao\MacOS\bin" 2>nul

echo [INFO] Diretorios criados com sucesso!

:: Verificar se Maven está disponível
mvn --version
if %errorlevel% neq 0 (
    echo [ERRO] Maven nao encontrado. Instale Maven primeiro.
    pause
    exit /b 1
)

echo [INFO] Maven encontrado. Prosseguindo...
echo [INFO] Estrutura de diretorios preparada para sincronizacao.

pause
