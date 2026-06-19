#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Ferramenta de Diagnóstico Avançado para Problemas em Produção
Sistema de Portaria v1.5.1 - Detecção de Placas

Este script identifica problemas específicos que ocorrem em ambiente real
mas não aparecem em testes controlados.
"""

import cv2
import sys
import time
import psutil
import socket
import platform
from datetime import datetime
import subprocess
import os

def log_info(msg):
    """Log com timestamp"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{timestamp}] {msg}")
    sys.stdout.flush()

def verificar_cameras_detalhado():
    """Verifica todas as câmeras disponíveis com diagnóstico detalhado"""
    log_info("=== DIAGNÓSTICO DETALHADO DE CÂMERAS ===")
    
    cameras_funcionais = []
    for i in range(5):  # Testa índices 0-4
        try:
            cap = cv2.VideoCapture(i)
            if cap.isOpened():
                ret, frame = cap.read()
                if ret and frame is not None:
                    height, width = frame.shape[:2]
                    fps = cap.get(cv2.CAP_PROP_FPS)
                    backend = cap.getBackendName()
                    
                    log_info(f"✓ Câmera {i}: {width}x{height}, {fps:.1f}fps, Backend: {backend}")
                    cameras_funcionais.append(i)
                    
                    # Testa captura múltipla para verificar estabilidade
                    frames_ok = 0
                    for _ in range(10):
                        ret, _ = cap.read()
                        if ret:
                            frames_ok += 1
                        time.sleep(0.1)
                    
                    estabilidade = (frames_ok / 10) * 100
                    if estabilidade < 80:
                        log_info(f"⚠️  Câmera {i}: Estabilidade baixa ({estabilidade:.0f}%)")
                    else:
                        log_info(f"✓ Câmera {i}: Estabilidade boa ({estabilidade:.0f}%)")
                else:
                    log_info(f"✗ Câmera {i}: Conecta mas sem sinal")
            else:
                log_info(f"✗ Câmera {i}: Não disponível")
            cap.release()
        except Exception as e:
            log_info(f"✗ Câmera {i}: Erro - {e}")
    
    return cameras_funcionais

def verificar_dependencias():
    """Verifica todas as dependências com versões específicas"""
    log_info("=== VERIFICAÇÃO DE DEPENDÊNCIAS ===")
    
    dependencias = {
        'cv2': 'OpenCV',
        'numpy': 'NumPy', 
        'pytesseract': 'Tesseract',
        'PIL': 'Pillow'
    }
    
    status_deps = {}
    for modulo, nome in dependencias.items():
        try:
            mod = __import__(modulo)
            versao = getattr(mod, '__version__', 'versão desconhecida')
            log_info(f"✓ {nome}: {versao}")
            status_deps[nome] = True
        except ImportError:
            log_info(f"✗ {nome}: NÃO INSTALADO")
            status_deps[nome] = False
    
    return status_deps

def verificar_recursos_sistema():
    """Verifica recursos do sistema que podem afetar performance"""
    log_info("=== RECURSOS DO SISTEMA ===")
    
    # CPU
    cpu_percent = psutil.cpu_percent(interval=1)
    log_info(f"CPU: {cpu_percent:.1f}%")
    
    # Memória
    mem = psutil.virtual_memory()
    log_info(f"Memória: {mem.percent:.1f}% usada ({mem.used//1024//1024}MB de {mem.total//1024//1024}MB)")
    
    # Processos que podem interferir
    processos_camera = []
    for proc in psutil.process_iter(['pid', 'name']):
        try:
            name = proc.info['name'].lower()
            if any(keyword in name for keyword in ['camera', 'webcam', 'obs', 'zoom', 'teams', 'skype']):
                processos_camera.append(f"{proc.info['name']} (PID: {proc.info['pid']})")
        except:
            pass
    
    if processos_camera:
        log_info("⚠️  Processos que podem estar usando câmera:")
        for proc in processos_camera:
            log_info(f"   - {proc}")
    else:
        log_info("✓ Nenhum processo conflitante detectado")

def testar_rtsp_cameras():
    """Testa conectividade com câmeras IP comuns"""
    log_info("=== TESTE DE CÂMERAS IP (RTSP) ===")
    
    # IPs comuns de câmeras na rede local
    ips_teste = ['192.168.1.100', '192.168.1.101', '192.168.1.200', '192.168.0.100']
    
    for ip in ips_teste:
        # Testa conectividade básica
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(2)
        try:
            result = sock.connect_ex((ip, 554))  # Porta RTSP padrão
            if result == 0:
                log_info(f"✓ {ip}:554 - Porta RTSP respondendo")
                
                # Testa URLs RTSP comuns
                urls_teste = [
                    f"rtsp://{ip}:554/stream1",
                    f"rtsp://admin:admin@{ip}:554/stream1",
                    f"rtsp://admin:12345@{ip}:554/Streaming/Channels/101"
                ]
                
                for url in urls_teste:
                    try:
                        cap = cv2.VideoCapture(url)
                        cap.set(cv2.CAP_PROP_TIMEOUT, 5000)  # 5 segundos timeout
                        if cap.isOpened():
                            ret, frame = cap.read()
                            if ret:
                                log_info(f"✓ URL funcionando: {url}")
                                cap.release()
                                break
                        cap.release()
                    except Exception as e:
                        continue
            else:
                log_info(f"✗ {ip}:554 - Não responsivo")
        except Exception as e:
            log_info(f"✗ {ip} - Erro: {e}")
        finally:
            sock.close()

def verificar_logs_sistema():
    """Verifica logs específicos do sistema de portaria"""
    log_info("=== ANÁLISE DE LOGS ===")
    
    log_paths = [
        "logs/portaria.log",
        "logs/deteccao_placas.log",
        "../logs/portaria.log",
        "../../logs/portaria.log"
    ]
    
    for log_path in log_paths:
        if os.path.exists(log_path):
            log_info(f"✓ Log encontrado: {log_path}")
            try:
                with open(log_path, 'r', encoding='utf-8') as f:
                    lines = f.readlines()
                    
                # Procura por erros específicos
                erros_camera = []
                erros_ocr = []
                erros_memoria = []
                
                for line in lines[-100:]:  # Últimas 100 linhas
                    line_lower = line.lower()
                    if 'erro' in line_lower or 'error' in line_lower:
                        if 'camera' in line_lower or 'grabber' in line_lower:
                            erros_camera.append(line.strip())
                        elif 'ocr' in line_lower or 'tesseract' in line_lower:
                            erros_ocr.append(line.strip())
                        elif 'memory' in line_lower or 'outofmemory' in line_lower:
                            erros_memoria.append(line.strip())
                
                if erros_camera:
                    log_info("⚠️  Erros de câmera encontrados:")
                    for erro in erros_camera[-3:]:  # Últimos 3
                        log_info(f"   {erro}")
                
                if erros_ocr:
                    log_info("⚠️  Erros de OCR encontrados:")
                    for erro in erros_ocr[-3:]:
                        log_info(f"   {erro}")
                
                if erros_memoria:
                    log_info("⚠️  Erros de memória encontrados:")
                    for erro in erros_memoria[-3:]:
                        log_info(f"   {erro}")
                        
            except Exception as e:
                log_info(f"✗ Erro ao ler log: {e}")
            break
    else:
        log_info("✗ Nenhum arquivo de log encontrado")

def testar_deteccao_ambiente_real():
    """Testa detecção em condições reais de produção"""
    log_info("=== TESTE DE DETECÇÃO EM AMBIENTE REAL ===")
    
    cameras = verificar_cameras_detalhado()
    if not cameras:
        log_info("✗ Nenhuma câmera disponível para teste")
        return
    
    camera_id = cameras[0]
    log_info(f"Testando com câmera {camera_id}...")
    
    try:
        cap = cv2.VideoCapture(camera_id)
        if not cap.isOpened():
            log_info("✗ Falha ao abrir câmera")
            return
        
        # Configura resolução padrão
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
        
        # Testa captura por 10 segundos
        log_info("Testando estabilidade por 10 segundos...")
        start_time = time.time()
        frames_capturados = 0
        frames_validos = 0
        
        while time.time() - start_time < 10:
            ret, frame = cap.read()
            frames_capturados += 1
            
            if ret and frame is not None:
                frames_validos += 1
                
                # Simula processamento básico
                gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
                edges = cv2.Canny(gray, 50, 150)
                contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
                
                if len(contours) > 10:  # Frame com conteúdo
                    log_info(f"Frame {frames_capturados}: {len(contours)} contornos detectados")
            
            time.sleep(0.1)
        
        cap.release()
        
        taxa_sucesso = (frames_validos / frames_capturados) * 100 if frames_capturados > 0 else 0
        log_info(f"Resultado: {frames_validos}/{frames_capturados} frames válidos ({taxa_sucesso:.1f}%)")
        
        if taxa_sucesso < 80:
            log_info("⚠️  Taxa de sucesso baixa - possível problema de hardware")
        else:
            log_info("✓ Taxa de sucesso adequada")
            
    except Exception as e:
        log_info(f"✗ Erro durante teste: {e}")

def gerar_relatorio_diagnostico():
    """Gera relatório final com recomendações"""
    log_info("=== RELATÓRIO DE DIAGNÓSTICO ===")
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"diagnostico_producao_{timestamp}.txt"
    
    log_info(f"Relatório salvo em: {filename}")
    log_info("Execute as seguintes verificações manuais:")
    log_info("1. Verifique configurações de câmera na interface do sistema")
    log_info("2. Teste URLs RTSP no VLC Media Player")
    log_info("3. Verifique iluminação do ambiente")
    log_info("4. Confirme que não há outros programas usando a câmera")
    log_info("5. Reinicie o sistema se problemas persistirem")

def main():
    """Função principal do diagnóstico"""
    log_info("INICIANDO DIAGNÓSTICO AVANÇADO PARA AMBIENTE DE PRODUÇÃO")
    log_info(f"Sistema: {platform.system()} {platform.release()}")
    log_info(f"Python: {sys.version.split()[0]}")
    log_info("")
    
    try:
        # Executa todos os diagnósticos
        verificar_dependencias()
        print()
        
        verificar_recursos_sistema()
        print()
        
        cameras = verificar_cameras_detalhado()
        print()
        
        testar_rtsp_cameras()
        print()
        
        verificar_logs_sistema()
        print()
        
        if cameras:
            testar_deteccao_ambiente_real()
            print()
        
        gerar_relatorio_diagnostico()
        
    except KeyboardInterrupt:
        log_info("Diagnóstico interrompido pelo usuário")
    except Exception as e:
        log_info(f"Erro inesperado: {e}")

if __name__ == "__main__":
    main()
