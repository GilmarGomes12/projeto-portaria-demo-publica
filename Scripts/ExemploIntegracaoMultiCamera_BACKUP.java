
// NOTA: Este é um exemplo de integração que deve ser copiado para seu projeto principal
// Para compilar, adicione as dependências do projeto-portaria ao classpath

// Imports simulados para demonstração (substitua pelos imports reais em seu projeto)
// import com.ghg.service.MultiCameraDetectorService;
// import com.ghg.service.MultiCameraDetectorService.TipoPortao;
// import com.ghg.service.PlacaDetectorAdaptivo.PlacaDetectadaCallback;

import java.awt.image.BufferedImage;
import java.util.Scanner;

/**
 * EXEMPLO DE INTEGRAÇÃO - SISTEMA MULTI-CÂMERA v1.5.1
 * =====================================================
 * 
 * IMPORTANTE: Este é um exemplo de código para demonstração.
 * Para usar em seu projeto:
 * 1. Copie este arquivo para src/main/java/com/ghg/example/
 * 2. Descomente os imports reais
 * 3. Adicione as dependências SLF4J ao projeto
 * 
 * Demonstra como integrar o MultiCameraDetectorService no seu sistema
 * para operação simultânea de 2 câmeras (entrada e saída).
 * 
 * @author Gilmar H Gomes
 * @since 08/06/2025
 * @version 1.5.1
 */
public class ExemploIntegracaoMultiCamera {
    
    private static final Logger logger = LoggerFactory.getLogger(ExemploIntegracaoMultiCamera.class);
    
    private MultiCameraDetectorService multiCameraService;
    private boolean sistemaAtivo = false;
    
    /**
     * Inicializa o sistema multi-câmera
     */
    public void inicializarSistema() {
        logger.info("🚀 INICIALIZANDO SISTEMA MULTI-CÂMERA v1.5.1");
        logger.info("=" + "=".repeat(50));
        
        // 1. Criar instância do serviço multi-câmera
        multiCameraService = new MultiCameraDetectorService();
        
        // 2. Configurar callback principal para receber detecções
        multiCameraService.setCallback(new CallbackSistemaPortaria());
        
        // 3. Configurar modo de detecção
        // true = Detecção real com OCR, false = Simulação para testes
        boolean usarDetectorReal = false; // ALTERE para true em produção
        multiCameraService.setUsarDetectorReal(usarDetectorReal);
        
        // 4. Configurar intervalo de detecção (em milissegundos)
        multiCameraService.setIntervaloDeteccao(2000); // 2 segundos
        
        logger.info("✅ Sistema multi-câmera configurado");
        logger.info("🎯 Modo: {}", usarDetectorReal ? "PRODUÇÃO (OCR Real)" : "TESTE (Simulação)");
        logger.info("⏱️ Intervalo: 2000ms entre detecções");
    }
    
    /**
     * Inicia o monitoramento das câmeras
     */
    public void iniciarMonitoramento() {
        if (multiCameraService == null) {
            logger.error("❌ Sistema não foi inicializado! Chame inicializarSistema() primeiro.");
            return;
        }
        
        logger.info("🎬 INICIANDO MONITORAMENTO DAS 2 CÂMERAS");
        
        // Executar diagnóstico inicial
        multiCameraService.executarDiagnostico();
        
        // Iniciar monitoramento completo (ambas as câmeras simultaneamente)
        multiCameraService.iniciarMonitoramentoCompleto();
        
        sistemaAtivo = true;
        logger.info("✅ Sistema de monitoramento ativo!");
    }
    
    /**
     * Para o monitoramento das câmeras
     */
    public void pararMonitoramento() {
        if (multiCameraService != null && sistemaAtivo) {
            logger.info("🛑 PARANDO MONITORAMENTO DAS CÂMERAS");
            multiCameraService.pararMonitoramentoCompleto();
            sistemaAtivo = false;
            logger.info("✅ Monitoramento parado");
        }
    }
    
    /**
     * Reinicia uma câmera específica em caso de problema
     */
    public void reiniciarCamera(TipoPortao camera) {
        if (multiCameraService != null) {
            logger.info("🔄 REINICIANDO CÂMERA: {}", camera.getDescricao());
            multiCameraService.reiniciarCamera(camera);
        }
    }
    
    /**
     * Verifica status do sistema
     */
    public void verificarStatus() {
        if (multiCameraService != null) {
            multiCameraService.logStatusCompleto();
            
            // Verificações específicas
            boolean entradaOk = multiCameraService.isMonitorando(TipoPortao.ENTRADA);
            boolean saidaOk = multiCameraService.isMonitorando(TipoPortao.SAIDA);
            
            logger.info("📊 STATUS INDIVIDUAL:");
            logger.info("   📷 ENTRADA: {}", entradaOk ? "ATIVO ✅" : "INATIVO ❌");
            logger.info("   📷 SAÍDA: {}", saidaOk ? "ATIVO ✅" : "INATIVO ❌");
            
            if (multiCameraService.isTodasMonitorando()) {
                logger.info("🎯 TODAS AS CÂMERAS OPERACIONAIS!");
            } else if (multiCameraService.isAlgumaMonitorando()) {
                logger.info("⚠️ OPERAÇÃO PARCIAL (apenas algumas câmeras ativas)");
            } else {
                logger.info("❌ NENHUMA CÂMERA ATIVA!");
            }
        }
    }
    
    /**
     * Finaliza o sistema e libera recursos
     */
    public void finalizarSistema() {
        if (multiCameraService != null) {
            logger.info("🔚 FINALIZANDO SISTEMA MULTI-CÂMERA");
            multiCameraService.shutdown();
            multiCameraService = null;
            sistemaAtivo = false;
            logger.info("✅ Sistema finalizado");
        }
    }
    
    /**
     * Callback personalizado para o sistema de portaria
     * Recebe as detecções de placas de ambas as câmeras
     */
    private class CallbackSistemaPortaria implements PlacaDetectadaCallback {
        
        @Override
        public void onPlacaDetectada(String placa, BufferedImage imagem, double confianca) {
            // A placa já vem com identificação da câmera: [ENTRADA] ABC1234 ou [SAIDA] XYZ5678
            logger.info("🚗 NOVA DETECÇÃO: {}", placa);
            logger.info("   📊 Confiança: {}%", Math.round(confianca * 100));
            
            // Identificar origem da detecção
            if (placa.startsWith("[ENTRADA]")) {
                String placaLimpa = placa.replace("[ENTRADA] ", "");
                processarVeiculoEntrada(placaLimpa, imagem, confianca);
            } else if (placa.startsWith("[SAIDA]")) {
                String placaLimpa = placa.replace("[SAIDA] ", "");
                processarVeiculoSaida(placaLimpa, imagem, confianca);
            } else {
                // Detecção sem identificação de origem (fallback)
                processarVeiculoGenerico(placa, imagem, confianca);
            }
        }
        
        private void processarVeiculoEntrada(String placa, BufferedImage imagem, double confianca) {
            logger.info("🟢 ENTRADA DETECTADA: {}", placa);
            
            // AQUI: Integre com seu sistema de controle de entrada
            // Exemplo:
            // - Verificar se veículo está autorizado
            // - Registrar entrada no banco de dados
            // - Acionar abertura do portão de entrada
            // - Enviar notificação
            
            // Exemplo de integração:
            // autorizacaoService.verificarVeiculoEntrada(placa);
            // portaoService.abrirPortaoEntrada();
            // notificacaoService.enviarNotificacao("Veículo " + placa + " entrando");
        }
        
        private void processarVeiculoSaida(String placa, BufferedImage imagem, double confianca) {
            logger.info("🔴 SAÍDA DETECTADA: {}", placa);
            
            // AQUI: Integre com seu sistema de controle de saída
            // Exemplo:
            // - Registrar saída no banco de dados
            // - Calcular tempo de permanência
            // - Acionar abertura do portão de saída
            // - Gerar relatório de movimentação
            
            // Exemplo de integração:
            // registroService.registrarSaida(placa);
            // portaoService.abrirPortaoSaida();
            // relatorioService.atualizarMovimentacao(placa);
        }
        
        private void processarVeiculoGenerico(String placa, BufferedImage imagem, double confianca) {
            logger.info("🟡 DETECÇÃO GENÉRICA: {}", placa);
            
            // Tratamento para detecções sem identificação de origem
            // Pode ser útil para sistemas com apenas uma câmera ou como fallback
        }
    }
    
    /**
     * Menu interativo para demonstração
     */
    public void executarMenuInterativo() {
        Scanner scanner = new Scanner(System.in);
        boolean executando = true;
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎛️  CONSOLE INTERATIVO - SISTEMA MULTI-CÂMERA v1.5.1");
        System.out.println("=".repeat(60));
        
        while (executando) {
            System.out.println("\n📋 OPÇÕES DISPONÍVEIS:");
            System.out.println("1 - Inicializar sistema");
            System.out.println("2 - Iniciar monitoramento");
            System.out.println("3 - Parar monitoramento");
            System.out.println("4 - Verificar status");
            System.out.println("5 - Reiniciar câmera ENTRADA");
            System.out.println("6 - Reiniciar câmera SAÍDA");
            System.out.println("7 - Executar diagnóstico");
            System.out.println("8 - Finalizar sistema");
            System.out.println("0 - Sair");
            System.out.print("\n🎯 Escolha uma opção: ");
            
            try {
                int opcao = scanner.nextInt();
                System.out.println();
                
                switch (opcao) {
                    case 1:
                        inicializarSistema();
                        break;
                    case 2:
                        iniciarMonitoramento();
                        break;
                    case 3:
                        pararMonitoramento();
                        break;
                    case 4:
                        verificarStatus();
                        break;
                    case 5:
                        reiniciarCamera(TipoPortao.ENTRADA);
                        break;
                    case 6:
                        reiniciarCamera(TipoPortao.SAIDA);
                        break;
                    case 7:
                        if (multiCameraService != null) {
                            multiCameraService.executarDiagnostico();
                        } else {
                            System.out.println("❌ Sistema não inicializado!");
                        }
                        break;
                    case 8:
                        finalizarSistema();
                        break;
                    case 0:
                        finalizarSistema();
                        executando = false;
                        System.out.println("👋 Saindo do sistema...");
                        break;
                    default:
                        System.out.println("❌ Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("❌ Entrada inválida! Digite um número.");
                scanner.nextLine(); // Limpar buffer
            }
        }
        
        scanner.close();
    }
    
    /**
     * Método principal para demonstração
     */
    public static void main(String[] args) {
        System.out.println("🎬 EXEMPLO DE INTEGRAÇÃO - SISTEMA MULTI-CÂMERA");
        System.out.println("Sistema otimizado para 2 câmeras: ENTRADA (0) e SAÍDA (1)");
        
        ExemploIntegracaoMultiCamera exemplo = new ExemploIntegracaoMultiCamera();
        
        if (args.length > 0 && args[0].equals("--interactive")) {
            // Modo interativo
            exemplo.executarMenuInterativo();
        } else {
            // Demonstração automática
            demonstracaoAutomatica(exemplo);
        }
    }
    
    /**
     * Demonstração automática do sistema
     */
    private static void demonstracaoAutomatica(ExemploIntegracaoMultiCamera exemplo) {
        try {
            // 1. Inicializar
            exemplo.inicializarSistema();
            Thread.sleep(2000);
            
            // 2. Iniciar monitoramento
            exemplo.iniciarMonitoramento();
            Thread.sleep(3000);
            
            // 3. Verificar status
            exemplo.verificarStatus();
            Thread.sleep(2000);
            
            // 4. Simular funcionamento por 15 segundos
            logger.info("🕒 Simulando funcionamento por 15 segundos...");
            Thread.sleep(15000);
            
            // 5. Verificar status novamente
            exemplo.verificarStatus();
            Thread.sleep(1000);
            
            // 6. Finalizar
            exemplo.finalizarSistema();
            
            System.out.println("\n✅ DEMONSTRAÇÃO CONCLUÍDA!");
            System.out.println("💡 Para modo interativo, execute com: --interactive");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Demonstração interrompida", e);
        } catch (Exception e) {
            logger.error("Erro durante demonstração", e);
        }
    }
}
