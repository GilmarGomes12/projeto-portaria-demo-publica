#!/usr/bin/env python3
"""
Script de análise de problemas de produção para detecção de placas
Análise do comportamento real vs. comportamento esperado
"""
import cv2  # pylint: disable=import-error
import sys
import os
import subprocess
import json
from datetime import datetime


def log_message(msg):
    """Log com timestamp"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{timestamp}] {msg}")


def testar_script_detector():
    """Testa o script detector_placa_final.py"""
    log_message("=== TESTE DO SCRIPT DETECTOR ===")

    script_path = r"c:\Development\projeto-portaria-1.5\projeto-portaria\src\main\resources\python\detector\detector_placa_final.py"

    if not os.path.exists(script_path):
        log_message("❌ Script detector não encontrado")
        return False

    try:
        # Executa o script com timeout
        log_message("Executando script de detecção...")
        result = subprocess.run(
            [sys.executable, script_path, "0"],
            capture_output=True,
            text=True,
            timeout=10,  # 10 segundos de timeout
            check=False  # Não levanta exceção para códigos não-zero
        )

        log_message(f"Código de saída: {result.returncode}")

        if result.stdout:
            log_message(f"STDOUT: {result.stdout}")

        if result.stderr:
            log_message(f"STDERR: {result.stderr}")

        # Analisa o resultado
        if result.returncode == 0:
            log_message("✅ Script executou com sucesso")
            return True
        elif result.returncode == 1:
            if "ERRO_DETECCAO" in result.stdout:
                log_message(
                    "⚠️ Script executou mas não detectou placa (normal)")
                return True
            else:
                log_message("❌ Script falhou com erro")
                return False
        else:
            log_message(f"❌ Script falhou com código {result.returncode}")
            return False

    except subprocess.TimeoutExpired:
        log_message("⚠️ Script timeout - pode estar aguardando entrada")
        return False
    except (OSError, subprocess.SubprocessError, FileNotFoundError) as e:
        log_message(f"❌ Erro ao executar script: {e}")
        return False


def analisar_configuracao_camera():
    """Analisa configuração de câmera para produção"""
    log_message("=== ANÁLISE DE CONFIGURAÇÃO DE CÂMERA ===")

    # pylint: disable=no-member
    # Testa diferentes backends
    backends = [
        (cv2.CAP_DSHOW, "DirectShow"),
        (cv2.CAP_MSMF, "Media Foundation"),
        (cv2.CAP_ANY, "Padrão")
    ]

    for backend, name in backends:
        log_message(f"Testando {name}...")

        try:
            cap = cv2.VideoCapture(0, backend)

            if cap.isOpened():
                # Testa configurações típicas de produção
                cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
                cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
                cap.set(cv2.CAP_PROP_FPS, 30)

                ret, _ = cap.read()  # Ignoramos o frame retornado
                if ret:
                    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
                    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
                    fps = cap.get(cv2.CAP_PROP_FPS)

                    log_message(f"✅ {name}: {width}x{height} @ {fps}fps")
                else:
                    log_message(f"❌ {name}: Não conseguiu ler frame")

                cap.release()
            else:
                log_message(f"❌ {name}: Não conseguiu abrir câmera")

        except (OSError, ValueError, RuntimeError) as e:
            log_message(f"❌ {name}: Erro - {e}")


def simular_ambiente_producao():
    """Simula condições de ambiente de produção"""
    log_message("=== SIMULAÇÃO DE AMBIENTE DE PRODUÇÃO ===")

    # Verifica recursos do sistema
    try:
        import psutil

        cpu_percent = psutil.cpu_percent(interval=1)
        memory = psutil.virtual_memory()
        disk = psutil.disk_usage('C:')

        log_message(f"CPU: {cpu_percent}%")
        log_message(
            f"Memória: {memory.percent}% ({memory.available//1024//1024}MB livres)")
        log_message(
            f"Disco: {disk.percent}% ({disk.free//1024//1024//1024}GB livres)")

        # Alerta se recursos baixos
        if cpu_percent > 80:
            log_message("⚠️ CPU alta - pode afetar detecção")
        if memory.percent > 80:
            log_message("⚠️ Memória alta - pode afetar detecção")
        if disk.percent > 90:
            log_message("⚠️ Disco cheio - pode afetar logs")

    except ImportError:
        log_message(
            "⚠️ psutil não disponível - instale com: pip install psutil")


def verificar_dependencias():
    """Verifica todas as dependências necessárias"""
    log_message("=== VERIFICAÇÃO DE DEPENDÊNCIAS ===")

    # Lista de dependências críticas
    deps = [
        ("cv2", "OpenCV"),
        ("pytesseract", "PyTesseract"),
        ("numpy", "NumPy"),
        ("PIL", "Pillow")
    ]

    all_ok = True

    for module, name in deps:
        try:
            __import__(module)
            log_message(f"✅ {name}")
        except ImportError:
            log_message(f"❌ {name} - FALTANDO")
            all_ok = False

    # Verifica Tesseract OCR
    try:
        import pytesseract
        version = pytesseract.get_tesseract_version()
        log_message(f"✅ Tesseract OCR: {version}")
    except (ImportError, pytesseract.TesseractNotFoundError, OSError) as e:
        log_message(f"❌ Tesseract OCR: {e}")
        all_ok = False

    return all_ok


def gerar_relatorio_producao():
    """Gera relatório completo de análise de produção"""
    log_message("=== GERANDO RELATÓRIO DE PRODUÇÃO ===")

    relatorio = {
        "timestamp": datetime.now().isoformat(),
        "sistema": "Windows",
        "versao_python": sys.version,
        "testes": {}
    }

    # Executa todos os testes
    relatorio["testes"]["dependencias"] = verificar_dependencias()

    # Salva relatório
    try:
        with open("relatorio_producao.json", "w", encoding="utf-8") as f:
            json.dump(relatorio, f, indent=2, ensure_ascii=False)
        log_message("✅ Relatório salvo em relatorio_producao.json")
    except (OSError, IOError, PermissionError) as e:
        log_message(f"❌ Erro ao salvar relatório: {e}")


def main():
    """Função principal de análise"""
    log_message("INICIANDO ANÁLISE DE PROBLEMAS DE PRODUÇÃO")
    log_message("=" * 60)

    # Executa análises
    verificar_dependencias()
    analisar_configuracao_camera()
    simular_ambiente_producao()
    testar_script_detector()

    log_message("=" * 60)
    log_message("ANÁLISE CONCLUÍDA")

    # Recomendações
    log_message("\n💡 RECOMENDAÇÕES PARA PRODUÇÃO:")
    log_message("1. Use DirectShow (CAP_DSHOW) no Windows para estabilidade")
    log_message("2. Configure timeout adequado para scripts Python")
    log_message("3. Implemente retry automático para falhas temporárias")
    log_message("4. Configure logging detalhado para debugging")
    log_message("5. Monitore recursos do sistema continuamente")


if __name__ == "__main__":
    main()
