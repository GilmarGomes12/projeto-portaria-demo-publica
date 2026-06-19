@echo off
echo "Iniciando teste de integração Python..."

cd /d "c:\Development\projeto-portaria-1.5\projeto-portaria"

echo "Verificando estrutura do projeto..."
if exist "src\main\resources\python\detector\monitor_portao.py" (
    echo "✓ monitor_portao.py encontrado"
) else (
    echo "✗ monitor_portao.py não encontrado"
)

if exist "src\main\resources\python\detector\detector_placa_final.py" (
    echo "✓ detector_placa_final.py encontrado"
) else (
    echo "✗ detector_placa_final.py não encontrado"
)

if exist "target\classes\com\ghg\test\TestePythonSimples.class" (
    echo "✓ TestePythonSimples.class compilado"
) else (
    echo "✗ TestePythonSimples.class não encontrado"
)

echo "Tentando executar o teste..."
java -cp "target\classes" com.ghg.test.TestePythonSimples

pause
