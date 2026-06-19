/**
 * EXEMPLO DE INTEGRAÇÃO - SISTEMA MULTI-CÂMERA v1.5.1
 * =====================================================
 * 
 * ESTE É UM ARQUIVO DE DOCUMENTAÇÃO/EXEMPLO
 * NÃO DEVE SER COMPILADO DIRETAMENTE
 * 
 * Para usar este código em seu projeto:
 * 1. Copie para src/main/java/com/ghg/example/ExemploIntegracaoMultiCamera.java
 * 2. Adicione os imports corretos:
 *    - import com.ghg.service.MultiCameraDetectorService;
 *    - import com.ghg.service.MultiCameraDetectorService.TipoPortao;
 *    - import com.ghg.service.PlacaDetectorAdaptivo.PlacaDetectadaCallback;
 *    - import org.slf4j.Logger;
 *    - import org.slf4j.LoggerFactory;
 * 3. Compile com o classpath do projeto principal
 * 
 * ESTRUTURA DE INTEGRAÇÃO:
 * 
 * 1. INICIALIZAÇÃO DO SISTEMA:
 *    MultiCameraDetectorService service = new MultiCameraDetectorService();
 *    service.setCallback(callback);
 *    service.setUsarDetectorReal(false); // true para produção
 * 
 * 2. CALLBACK PARA RECEBER DETECÇÕES:
 *    PlacaDetectadaCallback callback = (placa, imagem, confianca) -> {
 *        if (placa.startsWith("[ENTRADA]")) {
 *            // Processar entrada
 *            String placaLimpa = placa.replace("[ENTRADA] ", "");
 *            processarVeiculoEntrada(placaLimpa);
 *        } else if (placa.startsWith("[SAIDA]")) {
 *            // Processar saída
 *            String placaLimpa = placa.replace("[SAIDA] ", "");
 *            processarVeiculoSaida(placaLimpa);
 *        }
 *    };
 * 
 * 3. CONTROLE DO SISTEMA:
 *    service.iniciarMonitoramentoCompleto();  // Inicia ambas câmeras
 *    service.pararMonitoramentoCompleto();    // Para ambas câmeras
 *    service.reiniciarCamera(TipoPortao.ENTRADA); // Reinicia câmera específica
 * 
 * 4. MONITORAMENTO DE STATUS:
 *    boolean entradaOk = service.isMonitorando(TipoPortao.ENTRADA);
 *    boolean saidaOk = service.isMonitorando(TipoPortao.SAIDA);
 *    boolean todasOk = service.isTodasMonitorando();
 * 
 * 5. FINALIZAÇÃO:
 *    service.shutdown(); // Libera todos os recursos
 * 
 * TIPOS DE PORTÃO DISPONÍVEIS:
 * - TipoPortao.ENTRADA (Câmera 0)
 * - TipoPortao.SAIDA   (Câmera 1)
 * 
 * FUNCIONALIDADES IMPLEMENTADAS:
 * ✅ Exit codes Python corretos (1=normal, 2=erro)
 * ✅ Backend DirectShow para estabilidade  
 * ✅ Sistema de retry com 3 tentativas
 * ✅ Timeouts de produção (30s)
 * ✅ Multi-câmeras simultâneas
 * ✅ Logs detalhados com SLF4J
 * ✅ Tratamento robusto de erros
 * ✅ Threading assíncrono
 * ✅ Graceful shutdown
 * 
 * @author Gilmar H Gomes
 * @since 08/06/2025
 * @version 1.5.1
 */
public class ExemploIntegracaoMultiCamera_DOCUMENTACAO {
    
    public static void main(String[] args) {
        System.out.println("📋 EXEMPLO DE INTEGRAÇÃO - SISTEMA MULTI-CÂMERA v1.5.1");
        System.out.println("========================================================");
        System.out.println();
        System.out.println("Este arquivo contém a documentação e exemplos de código");
        System.out.println("para integrar o sistema multi-câmera em seu projeto.");
        System.out.println();
        System.out.println("📁 Para usar:");
        System.out.println("1. Copie o código para seu projeto principal");
        System.out.println("2. Adicione os imports necessários");
        System.out.println("3. Configure o classpath adequadamente");
        System.out.println();
        System.out.println("📚 Consulte os comentários no código para detalhes");
        System.out.println("    de implementação e integração.");
        System.out.println();
        System.out.println("✅ Sistema pronto para produção!");
        System.out.println();
        
        // Exemplos de uso dos métodos de processamento
        System.out.println("🔧 EXEMPLOS DE PROCESSAMENTO:");
        exemploProcessarEntrada("ABC1234");
        exemploProcessarSaida("XYZ5678");
    }
    
    /**
     * Exemplo de método para processar entrada de veículo
     */
    private static void exemploProcessarEntrada(String placa) {
        System.out.println("🟢 ENTRADA: " + placa);
        
        // AQUI: Integre com seu sistema de controle
        // Exemplo:
        // - Verificar autorização no banco de dados
        // - Registrar entrada com timestamp
        // - Acionar abertura do portão
        // - Enviar notificação WhatsApp/Email
        // - Atualizar relatórios
    }
    
    /**
     * Exemplo de método para processar saída de veículo
     */
    private static void exemploProcessarSaida(String placa) {
        System.out.println("🔴 SAÍDA: " + placa);
        
        // AQUI: Integre com seu sistema de controle
        // Exemplo:
        // - Registrar saída com timestamp
        // - Calcular tempo de permanência
        // - Acionar abertura do portão
        // - Atualizar status do veículo
        // - Gerar relatório de movimentação
    }
}
