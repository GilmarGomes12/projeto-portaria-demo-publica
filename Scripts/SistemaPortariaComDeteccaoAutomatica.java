// EXEMPLO PRÁTICO DE INTEGRAÇÃO NO SEU SISTEMA EXISTENTE

/* 
 * Este exemplo mostra como integrar o MonitorPlacasService 
 * com o sistema de portaria existente
 */

// ===== IMPORTS NECESSÁRIOS =====
import java.io.*;
import java.util.concurrent.*;

// ===== ADAPTAÇÃO DA SUA CLASSE PRINCIPAL =====
public class SistemaPortariaComDeteccaoAutomatica {
    
    // Instância do monitor de placas
    private MonitorPlacasService monitorPlacas;
    private boolean deteccaoAutomaticaAtiva = false;
    
    // Seus métodos existentes continuam aqui...
    // verificarPlacaNoBanco(), exibirPopup(), etc.
    
    /**
     * NOVO: Inicializar detecção automática
     * Chame este método na inicialização do sistema
     */
    public void inicializarDeteccaoAutomatica() {
        
        // Cria o callback que conecta Python com Java
        MonitorPlacasService.PlacaDetectadaCallback callback = 
            new MonitorPlacasService.PlacaDetectadaCallback() {
            
            @Override
            public void onPlacaDetectada(String placa) {
                // AQUI você conecta com seu código existente
                System.out.println("🚗 Placa detectada automaticamente: " + placa);
                
                // Chama sua lógica existente de verificação
                processarPlacaDetectada(placa);
            }
            
            @Override
            public void onErro(String erro) {
                System.err.println("❌ Erro na detecção: " + erro);
                // Aqui você pode logar o erro ou mostrar notificação
                
                // Se for erro crítico, pode desativar detecção automática
                if (erro.contains("ERRO_FATAL")) {
                    pararDeteccaoAutomatica();
                }
            }
            
            @Override
            public void onStatusMudou(String status) {
                System.out.println("ℹ️ Status: " + status);
                // Aqui você pode atualizar algum label na interface
                // Ex: labelStatus.setText(status);
            }
        };
        
        // Cria o serviço de monitoramento
        monitorPlacas = new MonitorPlacasService(callback);
        
        // Configurar interface se necessário
        configurarInterfaceDeteccao();
    }
    
    /**
     * NOVO: Processar placa detectada automaticamente
     * Este método substitui o clique manual do botão
     */
    private void processarPlacaDetectada(String placa) {
        
        // === ADAPTE AQUI COM SEU CÓDIGO EXISTENTE ===
        
        // 1. Verificar no banco de dados
        boolean autorizado = verificarPlacaNoBanco(placa);
        
        // 2. Exibir popup com resultado
        if (autorizado) {
            exibirPopupAutorizado(placa);
            // Talvez abrir portão automaticamente?
            // abrirPortaoAutomatico();
        } else {
            exibirPopupNegado(placa);
            // Talvez acionar alerta de segurança?
            // acionarAlertaSeguranca(placa);
        }
        
        // 3. Registrar no log
        registrarEventoPlaca(placa, autorizado);
        
        // 4. Atualizar interface se necessário
        atualizarListaRecente(placa);
    }
    
    /**
     * NOVO: Iniciar monitoramento de câmera
     */
    public boolean iniciarMonitoramentoCameras() {
        if (monitorPlacas == null) {
            inicializarDeteccaoAutomatica();
        }
        
        // CONFIGURE AQUI A URL DA SUA CÂMERA
        String urlCamera = "0"; // Câmera USB - MUDE conforme necessário
        // String urlCamera = "rtsp://admin:senha@192.168.1.100:554/stream1"; // Câmera IP
        
        boolean sucesso = monitorPlacas.iniciarMonitoramento(urlCamera);
        
        if (sucesso) {
            deteccaoAutomaticaAtiva = true;
            System.out.println("✅ Detecção automática de placas INICIADA");
            
            // Atualizar interface
            // buttonIniciarDeteccao.setText("Parar Detecção");
            // buttonIniciarDeteccao.setBackground(Color.RED);
            
        } else {
            System.err.println("❌ Falha ao iniciar detecção automática");
        }
        
        return sucesso;
    }
    
    /**
     * NOVO: Parar monitoramento
     */
    public void pararDeteccaoAutomatica() {
        if (monitorPlacas != null) {
            monitorPlacas.pararMonitoramento();
            deteccaoAutomaticaAtiva = false;
            
            System.out.println("⏹️ Detecção automática PARADA");
            
            // Atualizar interface
            // buttonIniciarDeteccao.setText("Iniciar Detecção");
            // buttonIniciarDeteccao.setBackground(Color.GREEN);
        }
    }
    
    /**
     * NOVO: Verificar se detecção está ativa
     */
    public boolean isDeteccaoAutomaticaAtiva() {
        return deteccaoAutomaticaAtiva;
    }
    
    /**
     * ADAPTAÇÃO: Método de teste manual (seu botão existente)
     * Agora pode testar sem interromper o monitoramento automático
     */
    public void testarDeteccaoManual() {
        if (monitorPlacas != null) {
            // Testa detecção única sem interromper monitoramento contínuo
            String resultado = monitorPlacas.testarDeteccaoUnica("0");
            
            if (resultado != null && !resultado.startsWith("ERRO")) {
                processarPlacaDetectada(resultado);
            } else {
                System.out.println("Teste manual: Nenhuma placa detectada");
            }
        }
    }
    
    // ===== SEUS MÉTODOS EXISTENTES (não mudam) =====
    
    private boolean verificarPlacaNoBanco(String placa) {
        // Seu código existente aqui
        return true; // placeholder
    }
    
    private void exibirPopupAutorizado(String placa) {
        // Seu código existente de popup
        System.out.println("✅ ACESSO AUTORIZADO: " + placa);
    }
    
    private void exibirPopupNegado(String placa) {
        // Seu código existente de popup
        System.out.println("❌ ACESSO NEGADO: " + placa);
    }
    
    private void registrarEventoPlaca(String placa, boolean autorizado) {
        // Seu código existente de log/banco
    }
    
    private void atualizarListaRecente(String placa) {
        // Seu código existente de interface
    }
    
    // ===== INTEGRAÇÃO COM INTERFACE GRÁFICA =====
    
    /**
     * PÚBLICO: Configurar interface para detecção automática
     * Chame este método após criar a interface gráfica
     */
    public void configurarInterfaceDeteccao() {
        // Se você tem um JButton na interface:
        
        // JButton btnDeteccaoAuto = new JButton("Iniciar Detecção Automática");
        // btnDeteccaoAuto.addActionListener(e -> {
        //     if (!isDeteccaoAutomaticaAtiva()) {
        //         iniciarMonitoramentoCameras();
        //     } else {
        //         pararDeteccaoAutomatica();
        //     }
        // });
    }
    
    /**
     * EXEMPLO: Método chamado no fechamento da aplicação
     */
    public void encerrarSistema() {
        // Parar detecção antes de fechar
        pararDeteccaoAutomatica();
        
        // Limpar recursos
        if (monitorPlacas != null) {
            monitorPlacas.shutdown();
        }
        
        // Seus outros métodos de limpeza...
    }
}

// ===== EXEMPLO DE CONFIGURAÇÃO INICIAL =====
/*
public static void main(String[] args) {
    SistemaPortariaComDeteccaoAutomatica sistema = 
        new SistemaPortariaComDeteccaoAutomatica();
    
    // Inicializar detecção automática na abertura do sistema
    sistema.inicializarDeteccaoAutomatica();
    
    // Iniciar monitoramento (opcional - pode ser via botão também)
    sistema.iniciarMonitoramentoCameras();
    
    // Seu código de interface existente...
}
*/
