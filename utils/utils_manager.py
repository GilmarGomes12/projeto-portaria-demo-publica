#!/usr/bin/env python3
"""
Gerenciador Central de Utilitários - Sistema de Portaria v1.5.1
Script principal para executar todas as ferramentas de manutenção
"""
import os
import subprocess
from datetime import datetime

# Tenta importar o módulo de monitor de performance
try:
    from monitor_performance import analisar_logs
    MONITOR_DISPONIVEL = True
except ImportError:
    MONITOR_DISPONIVEL = False


def mostrar_menu():
    """Exibe o menu principal dos utilitários"""
    print("=" * 60)
    print("  UTILITÁRIOS DO SISTEMA DE PORTARIA v1.5.1")
    print("=" * 60)
    print()
    print("1. 📊 Monitor de Performance")
    print("2. ✅ Validação Completa do Sistema")
    print("3. 🔍 Análise Detalhada de Logs")
    print("4. 📈 Relatório Executivo")
    print("5. 🧹 Limpeza de Logs (Em desenvolvimento)")
    print("6. 💾 Backup Automático (Em desenvolvimento)")
    print("7. ❓ Ajuda")
    print("0. 🚪 Sair")
    print()
    return input("Escolha uma opção (0-7): ").strip()


def executar_monitor():
    """Executa o monitor de performance"""
    print("\n🔄 Iniciando Monitor de Performance...")
    
    if not MONITOR_DISPONIVEL:
        print("❌ Módulo monitor_performance não disponível")
        return False
    
    try:
        analisar_logs()
        return True
    except (OSError, IOError) as e:
        print(f"❌ Erro de arquivo/sistema: {e}")
        return False
    except (ImportError, AttributeError, TypeError) as e:
        print(f"❌ Erro no módulo de análise: {e}")
        return False


def executar_validacao():
    """Executa a validação do sistema"""
    print("\n🔄 Iniciando Validação do Sistema...")
    try:
        if os.name == 'nt':  # Windows
            result = subprocess.run(['validacao_sistema.bat'],
                                    cwd=os.path.dirname(__file__),
                                    capture_output=True, text=True, check=False)
            print(result.stdout)
            if result.stderr:
                print("Erros:", result.stderr)
            return result.returncode == 0
        else:  # Linux/Mac
            print("⚠️ Script de validação disponível apenas para Windows")
            return False
    except subprocess.SubprocessError as e:
        print(f"❌ Erro ao executar processo: {e}")
        return False
    except (PermissionError, ValueError) as e:
        print(f"❌ Erro de permissão ou configuração: {e}")
        return False
    except (OSError, FileNotFoundError) as e:
        print(f"❌ Erro de arquivo/sistema: {e}")
        return False


def gerar_relatorio_executivo():
    """Gera um relatório executivo resumido"""
    print("\n📋 Gerando Relatório Executivo...")

    log_file = "../logs/portaria.log"
    if not os.path.exists(log_file):
        print("❌ Arquivo de log não encontrado")
        return False

    try:
        # Análise rápida
        with open(log_file, 'r', encoding='utf-8', errors='ignore') as f:
            linhas = f.readlines()

        total_linhas = len(linhas)
        data_inicio = "N/A"
        data_fim = "N/A"

        # Pega primeira e última linha com timestamp
        for linha in linhas:
            if any(x in linha for x in ['INFO', 'DEBUG', 'ERROR']):
                data_inicio = linha[:19] if len(linha) > 19 else "N/A"
                break

        for linha in reversed(linhas):
            if any(x in linha for x in ['INFO', 'DEBUG', 'ERROR']):
                data_fim = linha[:19] if len(linha) > 19 else "N/A"
                break

        print("\n" + "=" * 50)
        print("  RELATÓRIO EXECUTIVO - SISTEMA DE PORTARIA")
        print("=" * 50)
        print(f"📅 Período analisado: {data_inicio} até {data_fim}")
        print(f"📄 Total de linhas de log: {total_linhas:,}")
        print(f"🕐 Gerado em: {datetime.now().strftime('%d/%m/%Y %H:%M:%S')}")

        # Contadores rápidos
        deteccoes = len([l for l in linhas if 'Placa detectada' in l])
        erros = len([l for l in linhas if any(x in l.upper()
                    for x in ['ERROR', 'ERRO', 'EXCEPTION'])])
        reconexoes = len([l for l in linhas if 'Conexão thread-local' in l])

        print("\n📊 RESUMO DE ATIVIDADE:")
        print(f"    Detecções de placas: {deteccoes}")
        print(f"    Reconexões de DB: {reconexoes}")
        print(f"    Erros detectados: {erros}")

        status = "🟢 SAUDÁVEL"
        if erros > 10:
            status = "🔴 CRÍTICO"
        elif erros > 5 or reconexoes > 100:
            status = "🟡 ATENÇÃO"

        print(f"\n🚦 STATUS GERAL: {status}")
        print("=" * 50)

        return True

    except (OSError, IOError, UnicodeDecodeError) as e:
        print(f"❌ Erro ao acessar arquivo de log: {e}")
        return False
    except (ValueError, IndexError, AttributeError) as e:
        print(f"❌ Erro ao processar dados do log: {e}")
        return False


def mostrar_ajuda():
    """Exibe informações de ajuda"""
    print("\n" + "=" * 60)
    print("  AJUDA - UTILITÁRIOS DO SISTEMA DE PORTARIA")
    print("=" * 60)
    print()
    print("📊 MONITOR DE PERFORMANCE:")
    print("   • Analisa logs detalhadamente")
    print("   • Calcula estatísticas de OCR")
    print("   • Monitora pool de conexões")
    print("   • Detecta erros automaticamente")
    print()
    print("✅ VALIDAÇÃO DO SISTEMA:")
    print("   • Verifica compilação Maven")
    print("   • Testa dependências OCR")
    print("   • Valida estrutura de arquivos")
    print("   • Executa teste funcional")
    print()
    print("📈 RELATÓRIO EXECUTIVO:")
    print("   • Resumo rápido do sistema")
    print("   • Contadores principais")
    print("   • Status de saúde geral")
    print()
    print("📁 LOCALIZAÇÃO DOS ARQUIVOS:")
    print("   • Logs: ../logs/portaria.log")
    print("   • Config: ../projeto-portaria/")
    print("   • Utils: ./")
    print()
    print("🆘 TROUBLESHOOTING:")
    print("   • Se erro de permissão: execute como administrador")
    print("   • Se erro de Python: verifique instalação")
    print("   • Se erro de compilação: verifique Java/Maven")
    print("=" * 60)


def main():
    """Função principal do gerenciador"""
    print("Iniciando Gerenciador de Utilitários...")
    print(f"Diretório atual: {os.getcwd()}")

    while True:
        try:
            opcao = mostrar_menu()

            if opcao == '0':
                print("\n👋 Encerrando utilitários. Até logo!")
                break
            elif opcao == '1':
                executar_monitor()
            elif opcao == '2':
                executar_validacao()
            elif opcao == '3':
                executar_monitor()  # Mesmo que opção 1 por enquanto
            elif opcao == '4':
                gerar_relatorio_executivo()
            elif opcao == '5':
                print("\n🚧 Funcionalidade em desenvolvimento...")
            elif opcao == '6':
                print("\n🚧 Funcionalidade em desenvolvimento...")
            elif opcao == '7':
                mostrar_ajuda()
            else:
                print("\n❌ Opção inválida. Tente novamente.")

            if opcao != '0':
                input("\nPressione Enter para continuar...")

        except KeyboardInterrupt:
            print("\n\n👋 Interrompido pelo usuário. Encerrando...")
            break
        except EOFError:
            print("\n\n👋 Entrada interrompida. Encerrando...")
            break
        except (ValueError, TypeError) as e:
            print(f"\n❌ Erro de entrada inválida: {e}")
            input("Pressione Enter para continuar...")
        except (OSError, IOError) as e:
            print(f"\n❌ Erro de sistema/arquivo: {e}")
            input("Pressione Enter para continuar...")


if __name__ == "__main__":
    main()
