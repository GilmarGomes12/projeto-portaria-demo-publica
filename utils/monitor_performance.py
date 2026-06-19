#!/usr/bin/env python3
"""
Monitor de Performance - Sistema de Portaria v1.5.1
Análise detalhada dos logs de performance
"""
import re
import os
from datetime import datetime


def analisar_logs():
    """
    Analisa os logs do sistema de portaria para gerar relatório de performance.

    Esta função lê o arquivo de log 'logs/portaria.log' e extrai informações sobre:
    - Detecções de placas e níveis de confiança
    - Veículos autorizados vs não autorizados
    - Reconexões do pool de conexões
    - Eficiência geral do sistema

    A análise é feita usando expressões regulares para extrair dados específicos
    dos logs e apresenta um relatório detalhado no console com:

    - Estatísticas de detecção (total, alta/média/baixa confiança)
    - Contadores de autorização
    - Status do pool de conexões
    - Taxa de eficiência geral
    - Lista de placas únicas detectadas

    Returns:
        None: A função imprime o relatório diretamente no console

    Raises:
        FileNotFoundError: Implicitamente tratado se o arquivo de log não existir

    Examples:
        >>> analisar_logs()
        ==================================================
          MONITOR DE PERFORMANCE - PORTARIA v1.5.1
        ==================================================
        ...

    Note:
        - Requer o arquivo 'logs/portaria.log' no diretório pai
        - Utiliza encoding UTF-8 com tratamento de erros
        - Performance otimizada para arquivos de log grandes
    """
    # Caminho relativo a partir da pasta utils
    log_file = "../logs/portaria.log"

    if not os.path.exists(log_file):
        print("❌ ERRO: Arquivo de log não encontrado")
        return

    print("=" * 50)
    print("  MONITOR DE PERFORMANCE - PORTARIA v1.5.1")
    print("=" * 50)

    # Contadores
    deteccoes = []
    autorizacoes = {"autorizados": 0, "nao_autorizados": 0}
    reconexoes = 0
    erros = []

    # Padrões regex
    padrao_deteccao = r'Placa detectada.*?(\w+\d+)\s*\(confiança:\s*(\d+)%\)'
    padrao_autorizado = r'✓ Veículo AUTORIZADO'
    padrao_nao_autorizado = r'✗ Veículo NÃO AUTORIZADO'
    padrao_reconexao = r'Conexão thread-local.*está fechada ou inválida'
    padrao_erro = r'(ERROR|ERRO|Exception|Traceback)'

    print("\n[1/4] Analisando detecções de placas...")

    with open(log_file, 'r', encoding='utf-8', errors='ignore') as f:
        for linha in f:
            # Detecções de placas
            match_deteccao = re.search(padrao_deteccao, linha)
            if match_deteccao:
                placa = match_deteccao.group(1)
                confianca = int(match_deteccao.group(2))
                deteccoes.append((placa, confianca))

            # Autorizações
            if re.search(padrao_autorizado, linha):
                autorizacoes["autorizados"] += 1
            elif re.search(padrao_nao_autorizado, linha):
                autorizacoes["nao_autorizados"] += 1

            # Reconexões
            if re.search(padrao_reconexao, linha):
                reconexoes += 1

            # Erros
            if re.search(padrao_erro, linha, re.IGNORECASE):
                erros.append(linha.strip())

    print("[2/4] Calculando estatísticas de confiança...")

    # Análise de confiança
    alta_confianca = len([d for d in deteccoes if d[1] >= 90])
    media_confianca = len([d for d in deteccoes if 75 <= d[1] < 90])
    baixa_confianca = len([d for d in deteccoes if d[1] < 75])

    print("[3/4] Verificando autorizações...")
    print("[4/4] Analisando pool de conexões...")

    # Relatório
    print("\n" + "=" * 50)
    print("  RELATÓRIO DE PERFORMANCE")
    print("=" * 50)

    print("\n📊 DETECÇÃO DE PLACAS:")
    print(f"    Total de detecções: {len(deteccoes)}")
    print(f"    Alta confiança (90-100%): {alta_confianca}")
    print(f"    Média confiança (75-89%): {media_confianca}")
    print(f"    Baixa confiança (<75%): {baixa_confianca}")

    if deteccoes:
        confianca_media = sum(d[1] for d in deteccoes) / len(deteccoes)
        print(f"    Confiança média: {confianca_media:.1f}%")

    print("\n🔐 AUTORIZAÇÕES:")
    print(f"    Veículos autorizados: {autorizacoes['autorizados']}")
    print(f"    Veículos não autorizados: {autorizacoes['nao_autorizados']}")

    print("\n🔗 POOL DE CONEXÕES:")
    print(f"    Reconexões realizadas: {reconexoes}")
    if reconexoes > 50:
        print("    ⚠️  ATENÇÃO: Muitas reconexões detectadas")

    print("\n❌ ERROS DETECTADOS:")
    print(f"    Total de erros: {len(erros)}")
    if len(erros) > 0:
        print("    ⚠️  ATENÇÃO: Erros encontrados nos logs")
        if len(erros) <= 5:
            for erro in erros:
                print(f"    • {erro[:80]}...")
        else:
            print("    • Exibindo apenas os 5 primeiros erros...")
            for erro in erros[:5]:
                print(f"    • {erro[:80]}...")

    print("\n📈 EFICIÊNCIA GERAL:")
    if len(deteccoes) > 0:
        eficiencia = ((alta_confianca + media_confianca)
                      * 100) / len(deteccoes)
        print(f"    Taxa de detecção válida: {eficiencia:.1f}%")

        if eficiencia >= 90:
            print("    ✅ EXCELENTE performance")
        elif eficiencia >= 75:
            print("    ✅ BOA performance")
        else:
            print("    ⚠️  Performance pode ser melhorada")
    else:
        print("    Nenhuma detecção registrada")

    # Placas únicas detectadas
    placas_unicas = set(d[0] for d in deteccoes)
    if placas_unicas:
        print("\n🚗 PLACAS DETECTADAS:")
        for placa in sorted(placas_unicas):
            deteccoes_placa = [d[1] for d in deteccoes if d[0] == placa]
            confianca_max = max(deteccoes_placa)
            qtd_deteccoes = len(deteccoes_placa)
            print(f"    {placa}: {qtd_deteccoes}x (máx: {confianca_max}%)")

    print("\n" + "=" * 50)
    print(f"  DATA/HORA: {datetime.now().strftime('%d/%m/%Y %H:%M:%S')}")
    print("=" * 50)


if __name__ == "__main__":
    analisar_logs()
    input("\nPressione Enter para continuar...")
