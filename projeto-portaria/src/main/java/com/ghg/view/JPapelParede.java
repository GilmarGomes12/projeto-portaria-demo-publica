package com.ghg.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ghg.service.ConfiguracaoService;

/**
 * Tela para personalização do papel de parede da tela principal
 * 
 * @author Gilmar H Gomes
 * @since 08/01/2026
 * @version 1.0
 */
public class JPapelParede extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JPapelParede.class.getName());
    
    private JPanel contentPane;
    private JPanel panelPreview;
    private Image imagemPreview;
    private String caminhoImagemSelecionada;
    private ConfiguracaoService configuracaoService;
    private JLabel lblStatusImagem;
    
    /**
     * Construtor da tela de personalização de papel de parede
     */
    public JPapelParede() {
        configuracaoService = new ConfiguracaoService();
        initComponents();
        carregarPapelAtual();
    }
    
    /**
     * Inicializa os componentes da interface
     */
    private void initComponents() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(
            JPapelParede.class.getResource("/com/ghg/resources/cidade.png")));
        setTitle("Personalizar Papel de Parede");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);
        
        // Painel do título
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        titlePanel.setBorder(new EmptyBorder(15, 10, 15, 10));
        
        JLabel lblTitulo = new JLabel("Personalizar Papel de Parede");
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(lblTitulo);
        
        contentPane.add(titlePanel, BorderLayout.NORTH);
        
        // Painel central com preview
        JPanel centralPanel = new JPanel(new BorderLayout(10, 10));
        centralPanel.setBackground(Color.WHITE);
        centralPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Label de instrução
        JLabel lblInstrucao = new JLabel("Visualização do Papel de Parede:");
        lblInstrucao.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblInstrucao.setHorizontalAlignment(SwingConstants.CENTER);
        centralPanel.add(lblInstrucao, BorderLayout.NORTH);
        
        // Painel de preview da imagem
        panelPreview = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imagemPreview != null) {
                    // Desenha a imagem mantendo proporção
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imgWidth = imagemPreview.getWidth(this);
                    int imgHeight = imagemPreview.getHeight(this);
                    
                    double scaleX = (double) panelWidth / imgWidth;
                    double scaleY = (double) panelHeight / imgHeight;
                    double scale = Math.min(scaleX, scaleY);
                    
                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);
                    
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;
                    
                    g.drawImage(imagemPreview, x, y, scaledWidth, scaledHeight, this);
                } else {
                    // Desenha texto quando não há imagem
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.DARK_GRAY);
                    g.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    String texto = "Nenhuma imagem selecionada";
                    int textWidth = g.getFontMetrics().stringWidth(texto);
                    g.drawString(texto, (getWidth() - textWidth) / 2, getHeight() / 2);
                }
            }
        };
        
        panelPreview.setPreferredSize(new Dimension(700, 350));
        panelPreview.setBackground(Color.WHITE);
        panelPreview.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 130, 180), 2),
            new EmptyBorder(5, 5, 5, 5)
        ));
        centralPanel.add(panelPreview, BorderLayout.CENTER);
        
        // Label de status
        lblStatusImagem = new JLabel("Papel de parede padrão");
        lblStatusImagem.setFont(new Font("Tahoma", Font.ITALIC, 12));
        lblStatusImagem.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatusImagem.setForeground(Color.GRAY);
        centralPanel.add(lblStatusImagem, BorderLayout.SOUTH);
        
        contentPane.add(centralPanel, BorderLayout.CENTER);
        
        // Painel inferior com botões
        JPanel footerPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnSelecionar = createButton("Selecionar Imagem", "/com/ghg/resources/busca16.png");
        JButton btnRestaurar = createButton("Restaurar Padrão", "/com/ghg/resources/atualizar.png");
        JButton btnAplicar = createButton("Aplicar", "/com/ghg/resources/salve-16.png");
        JButton btnFechar = createButton("Fechar", "/com/ghg/resources/sair16.png");
        
        // Ações dos botões
        btnSelecionar.addActionListener(e -> selecionarImagem());
        btnRestaurar.addActionListener(e -> restaurarPadrao());
        btnAplicar.addActionListener(e -> aplicarPapelParede());
        btnFechar.addActionListener(e -> dispose());
        
        footerPanel.add(btnSelecionar);
        footerPanel.add(btnRestaurar);
        footerPanel.add(btnAplicar);
        footerPanel.add(btnFechar);
        
        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Cria um botão estilizado
     */
    private JButton createButton(String texto, String iconPath) {
        JButton button = new JButton(texto);
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(150, 40));
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 130, 180), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        try {
            ImageIcon icon = new ImageIcon(JPapelParede.class.getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            LOGGER.warning("Ícone não encontrado: " + iconPath);
        }
        
        return button;
    }
    
    /**
     * Carrega o papel de parede atual (customizado ou padrão)
     */
    private void carregarPapelAtual() {
        try {
            String caminhoCustomizado = configuracaoService.obterPapelParede();
            
            if (caminhoCustomizado != null) {
                // Carrega imagem customizada
                File arquivo = new File(caminhoCustomizado);
                imagemPreview = ImageIO.read(arquivo);
                caminhoImagemSelecionada = caminhoCustomizado;
                lblStatusImagem.setText("Papel de parede personalizado: " + arquivo.getName());
                lblStatusImagem.setForeground(new Color(0, 128, 0));
            } else {
                // Carrega imagem padrão
                java.net.URL urlPadrao = JPapelParede.class.getResource("/com/ghg/resources/logocondo.png");
                if (urlPadrao != null) {
                    imagemPreview = ImageIO.read(urlPadrao);
                    lblStatusImagem.setText("Papel de parede padrão do sistema");
                    lblStatusImagem.setForeground(Color.GRAY);
                }
            }
            
            panelPreview.repaint();
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao carregar papel de parede atual: " + e.getMessage(), e);
        }
    }
    
    /**
     * Abre diálogo para selecionar uma imagem
     */
    private void selecionarImagem() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione uma imagem para o papel de parede");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Imagens (PNG, JPG, JPEG)", "png", "jpg", "jpeg"));
        
        int resultado = fileChooser.showOpenDialog(this);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();
            
            try {
                // Carrega a imagem para preview
                imagemPreview = ImageIO.read(arquivoSelecionado);
                caminhoImagemSelecionada = arquivoSelecionado.getAbsolutePath();
                
                lblStatusImagem.setText("Imagem selecionada: " + arquivoSelecionado.getName() + 
                    " (Clique em 'Aplicar' para confirmar)");
                lblStatusImagem.setForeground(new Color(255, 140, 0)); // Laranja para indicar pendência
                
                panelPreview.repaint();
                
                LOGGER.info("Imagem selecionada: " + caminhoImagemSelecionada);
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao carregar imagem: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(this,
                    "Erro ao carregar a imagem selecionada.\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Restaura o papel de parede padrão do sistema
     */
    private void restaurarPadrao() {
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente restaurar o papel de parede padrão?",
            "Confirmar Restauração",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            boolean sucesso = configuracaoService.restaurarPapelPadrao();
            
            if (sucesso) {
                JOptionPane.showMessageDialog(this,
                    "Papel de parede padrão restaurado com sucesso!\n" +
                    "Reinicie o sistema para ver as alterações.",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recarrega o preview
                carregarPapelAtual();
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erro ao restaurar papel de parede padrão.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Aplica o papel de parede selecionado
     */
    private void aplicarPapelParede() {
        if (caminhoImagemSelecionada == null || caminhoImagemSelecionada.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor, selecione uma imagem primeiro.",
                "Atenção",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean sucesso = configuracaoService.salvarPapelParede(caminhoImagemSelecionada);
        
        if (sucesso) {
            JOptionPane.showMessageDialog(this,
                "Papel de parede aplicado com sucesso!\n" +
                "Reinicie o sistema para ver as alterações.",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            
            lblStatusImagem.setText("Papel de parede personalizado aplicado");
            lblStatusImagem.setForeground(new Color(0, 128, 0));
            
        } else {
            JOptionPane.showMessageDialog(this,
                "Erro ao aplicar papel de parede.\n" +
                "Verifique as permissões e tente novamente.",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
