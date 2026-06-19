@echo off
echo ========================================
echo   TESTE DE EXECUCAO POS-CORRECAO
echo ========================================
echo.

echo Verificando se as classes estao compiladas...
if exist "target\classes\com\ghg\test\TesteIndependentePython.class" (
    echo [OK] TesteIndependentePython.class compilado
) else (
    echo [ERRO] TesteIndependentePython.class NAO encontrado
    goto fim
)

echo.
echo Tentando executar o teste...
echo.

java -cp target\classes com.ghg.test.TesteIndependentePython

echo.
echo ========================================
echo   TESTE CONCLUIDO
echo ========================================

:fim
pause
