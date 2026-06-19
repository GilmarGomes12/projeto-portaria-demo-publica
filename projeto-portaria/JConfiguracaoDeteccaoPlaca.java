package com.ghg.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ghg.service.DeteccaoPlacaService;
import com.ghg.service.PlacaDetectorAdapterAdaptivo;

/**
 * Tela de configuração do serviço de detecção de placas
 * 
 * @author Gilmar H Gomes
 * @since 18/05/2025
 * @version 1.0
 */
public class JConfiguracaoDeteccaoPlaca extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private final DeteccaoPlacaService servicoDeteccao;
    private final PlacaDetectorAdapterAdaptivo detectorAdapter;
    
    // Componentes da UI
    private JToggleButton btnHabilitarDeteccao;
    private JLabel lblStatus;
    private JCheckBox chkModoReal;
    private JTextField txtCameraURL;
    private JTextField txtIntervaloCaptura;
    private JSlider sliderConfianca;
    private JLabel lblValorConfianca;
    
    /**
     * Cria a tela de configuração da detecção de placas
     * 
     * @param servicoDeteccao O serviço de detecção de placas a ser configurado
     */
    public JConfiguracaoDeteccaoPlaca(DeteccaoPlacaService servicoDeteccao) {
        this.servicoDeteccao = servicoDeteccao;
        this.detectorAdapter = servicoDeteccao.getDetectorAdapter();
        
        setTitle("Configuração da Detecção de Placas");
        setSize(600, 400);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        inicializarComponentes();
    }
    
    /**
     * Inicializa os componentes da tela
     */
    private void inicializarComponentes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Título
        JLabel lblTitulo = new JLabel("Configuração da Detecção de Placas", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Painel de abas para organizar configurações
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // === Aba Principal - controles básicos ===
        JPanel tabPrincipal = new JPanel(new BorderLayout(10, 10));
        tabPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Status da detecção
        JPanel panelStatus = new JPanel(new BorderLayout(10, 10));
        lblStatus = new JLabel("Status: Detecção Pausada", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        panelStatus.add(lblStatus, BorderLayout.NORTH);
        
        // Botão para habilitar/desabilitar
        btnHabilitarDeteccao = new JToggleButton("Ativar Detecção");
        btnHabilitarDeteccao.setSelected(false);  // Desativado por padrão        btnHabilitarDeteccao.addActionListener((ActionEvent e) -> {
            boolean ativado = btnHabilitarDeteccao.isSelected();
            if (ativado) {
                System.out.println("[DEBUG] UI: Botão 'Ativar Detecção' pressionado");
                System.out.println("[DEBUG] UI: Modo Real = " + chkModoReal.isSelected());
                
                // Atualiza status visual imediatamente
                lblStatus.setText("Status: Iniciando detecção...");
                btnHabilitarDeteccao.setEnabled(false);
                
                // Executa em thread separada para não travar a UI
                new Thread(() -> {
                    try {
                        servicoDeteccao.reiniciarDeteccao();
                        
                        // Atualiza UI na thread principal
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            if (servicoDeteccao.isAtivo()) {
                                btnHabilitarDeteccao.setText("Desativar Detecção");
                                lblStatus.setText("Status: Detecção Ativa ✓");
                                System.out.println("[DEBUG] UI: Detecção iniciada com sucesso");
                            } else {
                                lblStatus.setText("Status: Erro ao iniciar detecção ✗");
                                System.out.println("[DEBUG] UI: Falha ao iniciar detecção");
                            }
                            btnHabilitarDeteccao.setEnabled(true);
                        });
                    } catch (Exception ex) {
                        System.err.println("[ERROR] UI: Exceção ao iniciar detecção: " + ex.getMessage());
                        ex.printStackTrace();
                        
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            lblStatus.setText("Status: Erro - " + ex.getMessage());
                            btnHabilitarDeteccao.setEnabled(true);
                        });
                    }
                }).start();
                
            } else {
                System.out.println("[DEBUG] UI: Botão 'Pausar Detecção' pressionado");
                servicoDeteccao.pausarDeteccao();
                btnHabilitarDeteccao.setText("Ativar Detecção");
                lblStatus.setText("Status: Detecção Pausada");
            }
        });
        panelStatus.add(btnHabilitarDeteccao, BorderLayout.CENTER);
        tabPrincipal.add(panelStatus, BorderLayout.NORTH);
        
        // Seleção de modo (simulado/real)
        JPanel panelModo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelModo.setBorder(BorderFactory.createTitledBorder("Modo de Operação"));
        
        chkModoReal = new JCheckBox("Usar Hardware Real");
        chkModoReal.setToolTipText("Selecione para usar câmera real e OCR. Desmarque para usar simulação.");
        chkModoReal.setSelected(detectorAdapter.estaModoReal());
        chkModoReal.addActionListener((ActionEvent e) -> {
            boolean usarHardwareReal = chkModoReal.isSelected();
            detectorAdapter.setModoReal(usarHardwareReal);
            txtCameraURL.setEnabled(usarHardwareReal);
            txtIntervaloCaptura.setEnabled(usarHardwareReal);
            sliderConfianca.setEnabled(usarHardwareReal);
        });
        panelModo.add(chkModoReal);
        
        tabPrincipal.add(panelModo, BorderLayout.CENTER);
        
        // === Aba de Hardware - configurações avançadas ===
        JPanel tabHardware = new JPanel(new GridBagLayout());
        tabHardware.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        
        // URL da Câmera
        JLabel lblCameraURL = new JLabel("URL da Câmera:");
        c.gridx = 0;
        c.gridy = 0;
        tabHardware.add(lblCameraURL, c);
        
        txtCameraURL = new JTextField(20);
        txtCameraURL.setToolTipText("RTSP://... para câmera IP ou 0, 1, 2... para câmera USB");
        txtCameraURL.setText("0"); // Valor padrão - câmera 0
        txtCameraURL.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarCameraURL();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarCameraURL();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizarCameraURL();
            }
            
            private void atualizarCameraURL() {
                detectorAdapter.setConfigCameraURL(txtCameraURL.getText().trim());
            }
        });
        c.gridx = 1;
        c.gridy = 0;
        tabHardware.add(txtCameraURL, c);
        
        // Intervalo de Captura
        JLabel lblIntervaloCaptura = new JLabel("Intervalo de Captura (ms):");
        c.gridx = 0;
        c.gridy = 1;
        tabHardware.add(lblIntervaloCaptura, c);
        
        txtIntervaloCaptura = new JTextField(20);
        txtIntervaloCaptura.setText("500"); // 500ms padrão
        txtIntervaloCaptura.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarIntervalo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarIntervalo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizarIntervalo();
            }
            
            private void atualizarIntervalo() {
                try {
                    int intervalo = Integer.parseInt(txtIntervaloCaptura.getText().trim());
                    if (intervalo > 0) {
                        detectorAdapter.setConfigIntervaloCaptura(intervalo);
                    }
                } catch (NumberFormatException ex) {
                    // Ignora entradas inválidas
                }
            }
        });
        c.gridx = 1;
        c.gridy = 1;
        tabHardware.add(txtIntervaloCaptura, c);
        
        // Nível de Confiança OCR
        JLabel lblConfianca = new JLabel("Nível de Confiança OCR:");
        c.gridx = 0;
        c.gridy = 2;
        tabHardware.add(lblConfianca, c);
        
        JPanel panelSlider = new JPanel(new BorderLayout(5, 0));
        sliderConfianca = new JSlider(JSlider.HORIZONTAL, 0, 100, 75);
        sliderConfianca.setMajorTickSpacing(25);
        sliderConfianca.setMinorTickSpacing(5);
        sliderConfianca.setPaintTicks(true);
        sliderConfianca.setPaintLabels(true);
        
        lblValorConfianca = new JLabel("75%");
        lblValorConfianca.setHorizontalAlignment(SwingConstants.RIGHT);
        
        sliderConfianca.addChangeListener((ChangeEvent e) -> {
            int valor = sliderConfianca.getValue();
            lblValorConfianca.setText(valor + "%");
            detectorAdapter.setConfigConfiancaMinima(valor / 100.0);
        });
        
        panelSlider.add(sliderConfianca, BorderLayout.CENTER);
        panelSlider.add(lblValorConfianca, BorderLayout.EAST);
        
        c.gridx = 1;
        c.gridy = 2;
        tabHardware.add(panelSlider, c);
        
        // Informações sobre OpenCV/Tesseract
        JLabel lblInfo = new JLabel("<html>Para utilizar câmera real, é necessário:<br>" +
                "- OpenCV 4.x ou superior<br>" +
                "- Tesseract OCR 4.x ou superior<br>" +
                "- Drivers da câmera instalados</html>");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.insets = new Insets(20, 5, 5, 5);
        tabHardware.add(lblInfo, c);
        
        // Adicionar abas ao painel
        tabbedPane.addTab("Principal", tabPrincipal);
        tabbedPane.addTab("Hardware", tabHardware);
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        // Botões de ação
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener((ActionEvent e) -> {
            // Salvar configurações e fechar diálogo
            dispose();
        });
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener((ActionEvent e) -> dispose());
        
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        // Desativar controles se não estiver em modo real
        boolean modoReal = detectorAdapter.estaModoReal();
        txtCameraURL.setEnabled(modoReal);
        txtIntervaloCaptura.setEnabled(modoReal);
        sliderConfianca.setEnabled(modoReal);
        
        add(panel);
    }
}