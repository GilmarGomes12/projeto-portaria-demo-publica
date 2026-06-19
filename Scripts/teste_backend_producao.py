#!/usr/bin/env python3
"""
Teste específico de câmera com diferentes backends para ambiente de produção
"""
import cv2
import time
import numpy as np

def testar_backend(camera_id, backend, backend_name):
    """Testa um backend específico"""
    print(f"\n--- Testando {backend_name} ---")
    
    try:
        # Abre câmera com backend específico
        cap = cv2.VideoCapture(camera_id, backend)
        
        if not cap.isOpened():
            print(f"❌ Falha ao abrir câmera com {backend_name}")
            return False
        
        # Configura resolução (importante para produção)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
        cap.set(cv2.CAP_PROP_FPS, 30)
        
        # Testa leitura de frames
        success_count = 0
        total_attempts = 5
        
        for i in range(total_attempts):
            ret, frame = cap.read()
            if ret:
                success_count += 1
            time.sleep(0.1)
        
        if success_count > 0:
            # Pega informações da câmera
            width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
            height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
            fps = cap.get(cv2.CAP_PROP_FPS)
            
            print(f"✅ {backend_name}: {success_count}/{total_attempts} frames lidos")
            print(f"   Resolução: {width}x{height}")
            print(f"   FPS: {fps}")
            
            cap.release()
            return True
        else:
            print(f"❌ {backend_name}: Nenhum frame lido")
            cap.release()
            return False
            
    except Exception as e:
        print(f"❌ Erro com {backend_name}: {e}")
        return False

def main():
    print("=== TESTE DE BACKENDS PARA PRODUÇÃO ===")
    print("Verificando qual backend funciona melhor...")
    
    camera_id = 0  # Câmera integrada
    
    # Backends a testar (ordem de prioridade para Windows)
    backends_to_test = [
        (cv2.CAP_DSHOW, "DirectShow (DSHOW)"),
        (cv2.CAP_MSMF, "Media Foundation (MSMF)"),
        (cv2.CAP_V4L2, "Video4Linux2 (V4L2)"),
        (cv2.CAP_ANY, "Padrão do Sistema")
    ]
    
    working_backends = []
    
    for backend, name in backends_to_test:
        if testar_backend(camera_id, backend, name):
            working_backends.append((backend, name))
    
    print(f"\n📊 RESUMO:")
    if working_backends:
        print("✅ Backends funcionando:")
        for backend, name in working_backends:
            print(f"   - {name}")
        
        # Recomendação
        print(f"\n💡 RECOMENDAÇÃO PARA PRODUÇÃO:")
        if any("DSHOW" in name for _, name in working_backends):
            print("   Use DirectShow (DSHOW) - Mais estável no Windows")
        elif any("MSMF" in name for _, name in working_backends):
            print("   Use Media Foundation (MSMF) - Padrão Windows 10+")
        else:
            print(f"   Use: {working_backends[0][1]}")
    else:
        print("❌ Nenhum backend funcionando!")
    
    # Teste adicional de estabilidade
    print(f"\n=== TESTE DE ESTABILIDADE (10 segundos) ===")
    if working_backends:
        backend, name = working_backends[0]
        testar_estabilidade(camera_id, backend, name)

def testar_estabilidade(camera_id, backend, backend_name):
    """Testa estabilidade por 10 segundos"""
    print(f"Testando estabilidade com {backend_name}...")
    
    try:
        cap = cv2.VideoCapture(camera_id, backend)
        if not cap.isOpened():
            print("❌ Não foi possível abrir câmera para teste de estabilidade")
            return
        
        start_time = time.time()
        frame_count = 0
        error_count = 0
        
        while time.time() - start_time < 10:  # 10 segundos
            ret, frame = cap.read()
            if ret:
                frame_count += 1
            else:
                error_count += 1
            
            time.sleep(0.033)  # ~30 FPS
        
        cap.release()
        
        duration = time.time() - start_time
        fps_real = frame_count / duration
        
        print(f"✅ Teste de estabilidade:")
        print(f"   Frames capturados: {frame_count}")
        print(f"   Erros: {error_count}")
        print(f"   FPS real: {fps_real:.2f}")
        print(f"   Taxa de sucesso: {(frame_count/(frame_count+error_count)*100):.1f}%")
        
    except Exception as e:
        print(f"❌ Erro no teste de estabilidade: {e}")

if __name__ == "__main__":
    main()
