@echo off
REM Script de diagnóstico para problemas em produção
REM Sistema de Portaria v1.5.1 - SUPORTE A 2 PORTÕES

echo ============================================================
echo        DIAGNÓSTICO DE PROBLEMAS EM PRODUÇÃO
echo        Sistema de Detecção de Placas v1.5.1
echo        CONFIGURAÇÃO: 2 PORTÕES - 2 CÂMERAS
echo ============================================================
echo.

echo [INFO] Verificando status das câmeras disponíveis...
echo.

REM Verifica câmeras USB disponíveis para os 2 portões com DirectShow
echo === TESTE DE CÂMERAS DOS 2 PORTÕES ===
echo [INFO] Testando conectividade das câmeras para ENTRADA e SAÍDA

echo.
echo 📷 PORTÃO DE ENTRADA (Câmera 0):
python -c "import cv2; cap = cv2.VideoCapture(0, cv2.CAP_DSHOW); status = 'CONECTADA' if cap.isOpened() else 'FALHA DE CONEXÃO'; print(f'   Status: {status}'); cap.release()" 2>nul

echo.
echo 📷 PORTÃO DE SAÍDA (Câmera 1):
python -c "import cv2; cap = cv2.VideoCapture(1, cv2.CAP_DSHOW); status = 'CONECTADA' if cap.isOpened() else 'FALHA DE CONEXÃO'; print(f'   Status: {status}'); cap.release()" 2>nul

echo.
echo === VERIFICAÇÃO ADICIONAL DE CÂMERAS DISPONÍVEIS ===
for /L %%i in (0,1,2) do (
    echo Verificando câmera %%i (DirectShow)...
    python -c "import cv2; cap = cv2.VideoCapture(%%i, cv2.CAP_DSHOW); print(f'   Câmera %%i: {\"Disponível\" if cap.isOpened() else \"Indisponível\"}'); cap.release()" 2>nul
)

echo.
echo === VERIFICAÇÃO DE DEPENDÊNCIAS ===
python -c "import cv2; print(f'OpenCV: {cv2.__version__}')" 2>nul || echo OpenCV: NÃO INSTALADO
python -c "import pytesseract; print('Tesseract: OK')" 2>nul || echo Tesseract: NÃO INSTALADO
python -c "import numpy; print(f'NumPy: {numpy.__version__}')" 2>nul || echo NumPy: NÃO INSTALADO

echo.
echo === VERIFICAÇÃO DE LOGS ===
if exist "logs\portaria.log" (
    echo [INFO] Logs encontrados - últimas 10 linhas:
    tail -n 10 "logs\portaria.log" 2>nul || (
        powershell "Get-Content 'logs\portaria.log' -Tail 10"
    )
) else (
    echo [AVISO] Arquivo de log não encontrado em logs\portaria.log
)

echo.
echo === TESTE DE CONECTIVIDADE DE REDE ===
ping -n 1 192.168.1.1 >nul && echo Rede local: OK || echo Rede local: PROBLEMAS

echo.
echo === VERIFICAÇÃO DE PROCESSOS ===
tasklist | findstr /i "java" >nul && echo Java em execução: SIM || echo Java em execução: NÃO
tasklist | findstr /i "python" >nul && echo Python em execução: SIM || echo Python em execução: NÃO

echo.
echo === TESTE RÁPIDO DE DETECÇÃO PARA OS 2 PORTÕES ===
echo [INFO] Testando detecção para portão de ENTRADA...
timeout /t 2 >nul
python "%~dp0..\projeto-portaria\src\main\resources\python\detector\detector_placa_final.py" --camera 0 --backend directshow --timeout 5 2>nul && echo ✓ Teste ENTRADA: OK || echo ✗ Teste ENTRADA: FALHA

echo [INFO] Testando detecção para portão de SAÍDA...  
timeout /t 2 >nul
python "%~dp0..\projeto-portaria\src\main\resources\python\detector\detector_placa_final.py" --camera 1 --backend directshow --timeout 5 2>nul && echo ✓ Teste SAÍDA: OK || echo ✗ Teste SAÍDA: FALHA

echo.
echo === TESTE DE CONECTIVIDADE SIMULTÂNEA ===
echo [INFO] Verificando se ambas as câmeras podem ser abertas simultaneamente...
python -c "
import cv2
cap0 = cv2.VideoCapture(0, cv2.CAP_DSHOW)
cap1 = cv2.VideoCapture(1, cv2.CAP_DSHOW)
status0 = cap0.isOpened()
status1 = cap1.isOpened()
print(f'Entrada (0): {\"OK\" if status0 else \"FALHA\"}')
print(f'Saída (1): {\"OK\" if status1 else \"FALHA\"}')
if status0 and status1:
    print('✓ CONECTIVIDADE SIMULTÂNEA: SUCESSO')
else:
    print('✗ CONECTIVIDADE SIMULTÂNEA: FALHA')
cap0.release()
cap1.release()
" 2>nul

echo.
echo ============================================================
echo                DIAGNÓSTICO CONCLUÍDO
echo ============================================================
echo.
echo Para problemas específicos, verifique:
echo 1. Logs em logs\portaria.log
echo 2. Configurações de câmera na interface
echo 3. URLs RTSP se usando câmeras IP
echo 4. Permissões de acesso à câmera
echo.
pause
