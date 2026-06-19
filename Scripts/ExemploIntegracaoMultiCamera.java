
// NOTA: Este é um exemplo de integração que deve ser copiado para seu projeto principal
// Para compilar, adicione as dependências do projeto-portaria ao classpath

// Imports necessários para o exemplo funcionar
import java.awt.image.BufferedImage;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

// Imports simulados - Para usar em seu projeto, descomente e ajuste os pacotes:
// import com.ghg.service.MultiCameraDetectorService;
// import com.ghg.service.MultiCameraDetectorService.TipoPortao;
// import com.ghg.service.PlacaDetectorAdaptivo.PlacaDetectadaCallback;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// Enum para tipos de portão
enum TipoPortao {
    ENTRADA("Entrada"),
    SAIDA("Saída");
    
    private final String descricao;
    TipoPortao(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}

// Classes simuladas para este exemplo funcionar como demonstração
interface MultiCameraDetectorService {
    void setCallback(Object callback);
    void setUsarDetectorReal(boolean usar);
    void setIntervaloDeteccao(int intervalo);
    void executarDiagnostico();
    void iniciarMonitoramentoCompleto();
    void pararMonitoramentoCompleto();
    void reiniciarCamera(TipoPortao tipo);
    void logStatusCompleto();
    boolean isMonitorando(TipoPortao tipo);
    boolean isTodasMonitorando();
    boolean isAlgumaMonitorando();
    void shutdown();
}

interface PlacaDetectadaCallback {
    void onPlacaDetectada(String placa, BufferedImage imagem, double confianca);
}

// Logger simulado para o exemplo
class Logger {
    public void info(String msg, Object... args) {
        System.out.println("[INFO] " + String.format(msg.replace("{}", "%s"), args));
    }
    public void error(String msg, Object... args) {
        System.err.println("[ERROR] " + String.format(msg.replace("{}", "%s"), args));
    }
}

class LoggerFactory {
    public static Logger getLogger(Class<?> clazz) {
        return new Logger();
    }
}

// Implementação simulada para demonstração
class MultiCameraDetectorServiceImpl implements MultiCameraDetectorService {
    private PlacaDetectadaCallback callback;
    private boolean usarDetectorReal = false;
    private int intervaloDeteccao = 2000;
    private boolean monitorandoEntrada = false;
    private boolean monitorandoSaida = false;
    
    @Override
    public void setCallback(Object callback) {
        this.callback = (PlacaDetectadaCallback) callback;
    }
    
    @Override
    public void setUsarDetectorReal(boolean usar) {
        this.usarDetectorReal = usar;
    }
    
    @Override
    public void setIntervaloDeteccao(int intervalo) {
        this.intervaloDeteccao = intervalo;
    }
    
    @Override
    public void executarDiagnostico() {
        System.out.println("[DIAGNÓSTICO] Verificando câmeras...");
        System.out.println("[DIAGNÓSTICO] Câmera ENTRADA: OK");
        System.out.println("[DIAGNÓSTICO] Câmera SAÍDA: OK");
    }
    
    @Override
    public void iniciarMonitoramentoCompleto() {
        monitorandoEntrada = true;
        monitorandoSaida = true;
        System.out.println("[SERVICE] Monitoramento iniciado para ambas as câmeras");
        System.out.println("[SERVICE] Modo: " + (usarDetectorReal ? "PRODUÇÃO" : "SIMULAÇÃO"));
        System.out.println("[SERVICE] Intervalo: " + intervaloDeteccao + "ms");
        
        // Simular algumas detecções se há callback configurado
        if (callback != null && !usarDetectorReal) {
            // Simulação de detecção (apenas para exemplo)
            new Thread(() -> {
                try {
                    LockSupport.parkNanos(3_000_000_000L); // 3 segundos
                    callback.onPlacaDetectada("[ENTRADA] ABC1234", null, 0.95);
                    LockSupport.parkNanos(5_000_000_000L); // 5 segundos
                    callback.onPlacaDetectada("[SAIDA] XYZ5678", null, 0.88);
                } catch (Exception e) {
                    System.err.println("Erro na simulação: " + e.getMessage());
                }
            }).start();
        }
    }
    
    @Override
    public void pararMonitoramentoCompleto() {
        monitorandoEntrada = false;
        monitorandoSaida = false;
        System.out.println("[SERVICE] Monitoramento parado");
    }
    
    @Override
    public void reiniciarCamera(TipoPortao tipo) {
        System.out.println("[SERVICE] Reiniciando câmera: " + tipo.getDescricao());
    }
    
    @Override
    public void logStatusCompleto() {
        System.out.println("[SERVICE] Status completo do sistema");
    }
    
    @Override
    public boolean isMonitorando(TipoPortao tipo) {
        return tipo == TipoPortao.ENTRADA ? monitorandoEntrada : monitorandoSaida;
    }
    
    @Override
    public boolean isTodasMonitorando() {
        return monitorandoEntrada && monitorandoSaida;
    }
    
    @Override
    public boolean isAlgumaMonitorando() {
        return monitorandoEntrada || monitorandoSaida;
    }
    
    @Override
    public void shutdown() {
        pararMonitoramentoCompleto();
        System.out.println("[SERVICE] Sistema finalizado");
    }
}

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
        
        // 1. Criar instância do serviço multi-câmera (implementação simulada para exemplo)
        multiCameraService = new MultiCameraDetectorServiceImpl();
        
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
        Logger logger = LoggerFactory.getLogger(ExemploIntegracaoMultiCamera.class);
        
        try {
            // 1. Inicializar
            exemplo.inicializarSistema();
            LockSupport.parkNanos(2_000_000_000L); // 2 segundos
            
            // 2. Iniciar monitoramento
            exemplo.iniciarMonitoramento();
            LockSupport.parkNanos(3_000_000_000L); // 3 segundos
            
            // 3. Verificar status
            exemplo.verificarStatus();
            LockSupport.parkNanos(2_000_000_000L); // 2 segundos
            
            // 4. Simular funcionamento por 15 segundos
            logger.info("🕒 Simulando funcionamento por 15 segundos...");
            LockSupport.parkNanos(15_000_000_000L); // 15 segundos
            
            // 5. Verificar status novamente
            exemplo.verificarStatus();
            LockSupport.parkNanos(1_000_000_000L); // 1 segundo
            
            // 6. Finalizar
            exemplo.finalizarSistema();
            
            System.out.println("\n✅ DEMONSTRAÇÃO CONCLUÍDA!");
            System.out.println("💡 Para modo interativo, execute com: --interactive");
            
        } catch (Exception e) {
            logger.error("Erro durante demonstração", e);
        }
    }
}
