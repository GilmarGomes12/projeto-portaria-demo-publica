#!/usr/bin/env python3
"""
Teste simples de câmera para diagnóstico rápido
"""
import cv2
import sys
import time

def testar_camera(camera_id=0):
    """Testa uma câmera específica"""
    print(f"Testando câmera {camera_id}...")
    
    try:
        # Tenta abrir a câmera
        cap = cv2.VideoCapture(camera_id)
        
        if not cap.isOpened():
            print(f"❌ Não foi possível abrir a câmera {camera_id}")
            return False
        
        # Tenta ler um frame
        ret, frame = cap.read()
        if ret:
            height, width = frame.shape[:2]
            print(f"✅ Câmera {camera_id} funcionando - Resolução: {width}x{height}")
            
            # Testa algumas propriedades
            fps = cap.get(cv2.CAP_PROP_FPS)
            print(f"   FPS: {fps}")
            
            cap.release()
            return True
        else:
            print(f"❌ Não foi possível ler frame da câmera {camera_id}")
            cap.release()
            return False
            
    except Exception as e:
        print(f"❌ Erro ao testar câmera {camera_id}: {e}")
        return False

def main():
    print("=== TESTE RÁPIDO DE CÂMERAS ===")
    
    # Testa câmeras de 0 a 3
    cameras_funcionando = []
    for i in range(4):
        if testar_camera(i):
            cameras_funcionando.append(i)
    
    print(f"\n📊 RESULTADO:")
    if cameras_funcionando:
        print(f"✅ Câmeras funcionando: {cameras_funcionando}")
    else:
        print("❌ Nenhuma câmera USB detectada")
    
    # Verifica backends disponíveis
    print("\n=== BACKENDS OPENCV DISPONÍVEIS ===")
    backends = cv2.videoio_registry.getBackends()
    for backend in backends:
        name = cv2.videoio_registry.getBackendName(backend)
        print(f"- {name}")

if __name__ == "__main__":
    main()
