#!/usr/bin/env python3
"""
=================================================================================
TESTE INTEGRADO DO SISTEMA MULTI-CÂMERA v1.5.1
=================================================================================

Script de teste para validar o funcionamento do sistema de detecção de placas
com suporte otimizado para 2 câmeras (entrada e saída).

Funcionalidades testadas:
- Conectividade simultânea das 2 câmeras
- Detecção independente por câmera
- Identificação de origem (ENTRADA/SAÍDA)
- Tratamento de falhas individuais
- Restart automático de câmeras

@author: Gilmar H Gomes
@data: 08/06/2025
@versao: 1.5.1
"""

import os
import sys
import time
import subprocess
import platform
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed
import json

# Configurações
PROJETO_DIR = r"c:\Development\projeto-portaria-1.5\projeto-portaria"
SCRIPTS_DIR = r"c:\Development\projeto-portaria-1.5\Scripts"
PYTHON_DETECTOR = os.path.join(
    PROJETO_DIR, "src", "main", "resources", "python", "detector", "detector_placa_final.py")


class TesteSistemaMultiCamera:
    def __init__(self):
        self.resultados = {
            'inicio': datetime.now().isoformat(),
            'testes': {},
            'cameras': {
                'entrada': {'id': 0, 'status': 'nao_testada'},
                'saida': {'id': 1, 'status': 'nao_testada'}
            },
            'status_geral': 'iniciando'
        }

    def log(self, mensagem, tipo='INFO'):
        timestamp = datetime.now().strftime("%H:%M:%S")
        prefixo = {
            'INFO': '🔍',
            'SUCCESS': '✅',
            'WARNING': '⚠️',
            'ERROR': '❌',
            'CAMERA': '📷'
        }.get(tipo, 'ℹ️')

        print(f"[{timestamp}] {prefixo} {mensagem}")

    def verificar_ambiente(self):
        """Verifica se o ambiente está configurado corretamente"""
        self.log("=== VERIFICAÇÃO DO AMBIENTE ===")

        # Verificar Python
        python_version = platform.python_version()
        self.log(f"Python: {python_version}")

        # Verificar arquivo detector
        if os.path.exists(PYTHON_DETECTOR):
            self.log(f"Detector Python encontrado: ✅", 'SUCCESS')
        else:
            self.log(
                f"Detector Python não encontrado: {PYTHON_DETECTOR}", 'ERROR')
            return False

        # Verificar dependências Python
        try:
            import cv2
            self.log(f"OpenCV: {cv2.__version__} ✅", 'SUCCESS')
        except ImportError:
            self.log("OpenCV não encontrado", 'WARNING')

        return True

    def testar_camera_individual(self, camera_id, nome_camera):
        """Testa uma câmera individual"""
        self.log(f"Testando {nome_camera} (Câmera {camera_id})", 'CAMERA')

        try:
            # Comando para testar a câmera
            cmd = [
                sys.executable,
                PYTHON_DETECTOR,
                "--camera", str(camera_id),
                "--backend", "directshow",
                "--timeout", "10",
                "--test-only"
            ]

            start_time = time.time()
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=15
            )
            end_time = time.time()

            tempo_resposta = round(end_time - start_time, 2)

            # Interpretar resultado
            if result.returncode == 0:
                self.log(
                    f"✅ {nome_camera}: CONECTADA ({tempo_resposta}s)", 'SUCCESS')
                self.resultados['cameras'][nome_camera.lower()
                                           ]['status'] = 'conectada'
                self.resultados['cameras'][nome_camera.lower(
                )]['tempo_resposta'] = tempo_resposta
                return True
            elif result.returncode == 1:
                self.log(
                    f"⚠️ {nome_camera}: Conectada mas sem placa detectada ({tempo_resposta}s)", 'WARNING')
                self.resultados['cameras'][nome_camera.lower()
                                           ]['status'] = 'sem_placa'
                self.resultados['cameras'][nome_camera.lower(
                )]['tempo_resposta'] = tempo_resposta
                return True
            else:
                self.log(
                    f"❌ {nome_camera}: ERRO DE CONEXÃO (código {result.returncode})", 'ERROR')
                self.resultados['cameras'][nome_camera.lower()
                                           ]['status'] = 'erro_conexao'
                self.resultados['cameras'][nome_camera.lower(
                )]['erro'] = result.stderr.strip()
                return False

        except subprocess.TimeoutExpired:
            self.log(f"❌ {nome_camera}: TIMEOUT (>15s)", 'ERROR')
            self.resultados['cameras'][nome_camera.lower()
                                       ]['status'] = 'timeout'
            return False
        except Exception as e:
            self.log(f"❌ {nome_camera}: EXCEPTION - {str(e)}", 'ERROR')
            self.resultados['cameras'][nome_camera.lower()
                                       ]['status'] = 'exception'
            self.resultados['cameras'][nome_camera.lower()]['erro'] = str(e)
            return False

    def testar_conectividade_simultanea(self):
        """Testa conectividade de ambas as câmeras simultaneamente"""
        self.log("=== TESTE DE CONECTIVIDADE SIMULTÂNEA ===")

        cameras = [
            (0, 'ENTRADA'),
            (1, 'SAIDA')
        ]

        resultados = {}

        # Teste simultâneo usando ThreadPoolExecutor
        with ThreadPoolExecutor(max_workers=2) as executor:
            futures = {
                executor.submit(self.testar_camera_individual, cam_id, nome): nome
                for cam_id, nome in cameras
            }

            for future in as_completed(futures):
                nome_camera = futures[future]
                try:
                    sucesso = future.result()
                    resultados[nome_camera] = sucesso
                except Exception as e:
                    self.log(f"Erro no teste de {nome_camera}: {e}", 'ERROR')
                    resultados[nome_camera] = False

        return resultados

    def testar_deteccao_simultanea(self):
        """Testa detecção simultânea em ambas as câmeras por 30 segundos"""
        self.log("=== TESTE DE DETECÇÃO SIMULTÂNEA (30s) ===")

        def executar_deteccao(camera_id, nome_camera):
            """Executa detecção em uma câmera"""
            cmd = [
                sys.executable,
                PYTHON_DETECTOR,
                "--camera", str(camera_id),
                "--backend", "directshow",
                "--timeout", "30",
                "--continuous"
            ]

            try:
                self.log(f"Iniciando detecção em {nome_camera}...", 'CAMERA')
                result = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=35
                )

                # Contar detecções na saída
                output_lines = result.stdout.split('\n')
                deteccoes = [
                    line for line in output_lines if 'PLACA_DETECTADA:' in line]

                self.log(
                    f"📋 {nome_camera}: {len(deteccoes)} detecções", 'CAMERA')
                return len(deteccoes)

            except subprocess.TimeoutExpired:
                self.log(
                    f"⏰ {nome_camera}: Teste finalizado por timeout", 'WARNING')
                return 0
            except Exception as e:
                self.log(
                    f"❌ {nome_camera}: Erro na detecção - {str(e)}", 'ERROR')
                return -1

        # Execução simultânea
        cameras = [(0, 'ENTRADA'), (1, 'SAIDA')]
        deteccoes_total = {}

        with ThreadPoolExecutor(max_workers=2) as executor:
            futures = {
                executor.submit(executar_deteccao, cam_id, nome): nome
                for cam_id, nome in cameras
            }

            for future in as_completed(futures):
                nome_camera = futures[future]
                try:
                    deteccoes = future.result()
                    deteccoes_total[nome_camera] = deteccoes
                except Exception as e:
                    self.log(
                        f"Erro na detecção simultânea de {nome_camera}: {e}", 'ERROR')
                    deteccoes_total[nome_camera] = -1

        self.resultados['deteccoes_simultaneas'] = deteccoes_total
        return deteccoes_total

    def testar_tratamento_falhas(self):
        """Testa o tratamento de falhas individuais"""
        self.log("=== TESTE DE TRATAMENTO DE FALHAS ===")

        # Simular falha tentando conectar câmera inexistente
        resultado_falha = self.testar_camera_individual(99, 'INEXISTENTE')

        if not resultado_falha:
            self.log("✅ Tratamento de falhas funcionando corretamente", 'SUCCESS')
            return True
        else:
            self.log("❌ Problema no tratamento de falhas", 'ERROR')
            return False

    def gerar_relatorio(self):
        """Gera relatório final dos testes"""
        self.log("=== RELATÓRIO FINAL ===")

        # Status das câmeras
        entrada_ok = self.resultados['cameras']['entrada']['status'] in [
            'conectada', 'sem_placa']
        saida_ok = self.resultados['cameras']['saida']['status'] in [
            'conectada', 'sem_placa']

        if entrada_ok and saida_ok:
            self.log("🎯 SISTEMA MULTI-CÂMERA: TOTALMENTE OPERACIONAL", 'SUCCESS')
            self.resultados['status_geral'] = 'operacional'
        elif entrada_ok or saida_ok:
            self.log("⚠️ SISTEMA MULTI-CÂMERA: PARCIALMENTE OPERACIONAL", 'WARNING')
            self.resultados['status_geral'] = 'parcial'
        else:
            self.log("❌ SISTEMA MULTI-CÂMERA: NÃO OPERACIONAL", 'ERROR')
            self.resultados['status_geral'] = 'falha'

        # Detalhes
        self.log(
            f"📷 Câmera ENTRADA (0): {self.resultados['cameras']['entrada']['status']}")
        self.log(
            f"📷 Câmera SAÍDA (1): {self.resultados['cameras']['saida']['status']}")

        # Salvar relatório JSON
        relatorio_file = os.path.join(
            SCRIPTS_DIR, 'relatorio_teste_multi_camera.json')
        self.resultados['fim'] = datetime.now().isoformat()

        with open(relatorio_file, 'w', encoding='utf-8') as f:
            json.dump(self.resultados, f, indent=2, ensure_ascii=False)

        self.log(f"📄 Relatório salvo em: {relatorio_file}")

    def executar_todos_testes(self):
        """Executa toda a suíte de testes"""
        self.log("🚀 INICIANDO TESTE INTEGRADO DO SISTEMA MULTI-CÂMERA")
        self.log("="*60)

        try:
            # 1. Verificar ambiente
            if not self.verificar_ambiente():
                self.log("❌ Ambiente não configurado corretamente", 'ERROR')
                return False

            # 2. Teste de conectividade
            resultados_conectividade = self.testar_conectividade_simultanea()
            self.resultados['testes']['conectividade'] = resultados_conectividade

            # 3. Teste de detecção simultânea (apenas se pelo menos uma câmera estiver conectada)
            if any(resultados_conectividade.values()):
                deteccoes = self.testar_deteccao_simultanea()
                self.resultados['testes']['deteccao_simultanea'] = deteccoes
            else:
                self.log(
                    "⚠️ Pulando teste de detecção (nenhuma câmera disponível)", 'WARNING')

            # 4. Teste de tratamento de falhas
            falhas_ok = self.testar_tratamento_falhas()
            self.resultados['testes']['tratamento_falhas'] = falhas_ok

            # 5. Gerar relatório
            self.gerar_relatorio()

            return True

        except Exception as e:
            self.log(f"❌ Erro durante execução dos testes: {str(e)}", 'ERROR')
            self.resultados['erro_geral'] = str(e)
            return False


def main():
    """Função principal"""
    print("🎬 TESTE INTEGRADO - SISTEMA MULTI-CÂMERA v1.5.1")
    print("=" * 60)
    print("Sistema otimizado para 2 câmeras: ENTRADA (0) e SAÍDA (1)")
    print("=" * 60)

    teste = TesteSistemaMultiCamera()
    sucesso = teste.executar_todos_testes()

    if sucesso:
        print("\n✅ TESTES CONCLUÍDOS COM SUCESSO!")
    else:
        print("\n❌ TESTES FINALIZADOS COM PROBLEMAS!")

    print("\n📋 Para ver o relatório detalhado:")
    print(f"   {os.path.join(SCRIPTS_DIR, 'relatorio_teste_multi_camera.json')}")

    return 0 if sucesso else 1


if __name__ == "__main__":
    sys.exit(main())
