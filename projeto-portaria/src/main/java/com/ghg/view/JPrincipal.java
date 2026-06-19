package com.ghg.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import com.ghg.service.ConfiguracaoService;
import com.ghg.utils.BackupManager;
import com.ghg.utils.BackupScheduler;
import java.awt.Color;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.JComboBox;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;

/**
 * @author Gilmar H Gomes
 * @since 27/03/2025
 * @version 1.3 // Versão atualizada com backup automático
 * @description Classe principal do sistema de portaria
 */

public class JPrincipal extends JFrame {
    private JPanel contentPane;
    private Image backgroundImage;
    private int nivelAcesso; // Adicionar variável para armazenar o nível de acesso
    private String nomeUsuarioLogado; // Variável para armazenar o nome do usuário logado
    private JLabel lblUsuarioLogado; // Label que exibirá o nome do usuário logado
    private JButton btnBackup;
    private JButton btnUsuario;
    
    // Mapa para controlar janelas abertas (uma instância por tipo de tela)
    private Map<String, JFrame> janelasAbertas = new HashMap<>();
    
    // Flag para evitar loop infinito ao trocar tema
    private boolean atualizandoTema = false;
    
    // Instância estática do BackupScheduler para acesso global
    private static BackupScheduler backupScheduler;

    public int getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(int nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
    
    /**
     * Obtém a instância do BackupScheduler
     * @return BackupScheduler instância global
     */
    public static BackupScheduler getBackupScheduler() {
        if (backupScheduler == null) {
            backupScheduler = new BackupScheduler();
        }
        return backupScheduler;
    }    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                JPrincipal frame = new JPrincipal(1); // Nível de acesso padrão para testes
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public JPrincipal() {
        this(0); // Construtor padrão chama o construtor com nível de acesso 0 (sem acesso)
    }    public JPrincipal(int nivelAcesso) {
        this.nivelAcesso = nivelAcesso; // Armazenar o nível de acesso
        
        // Força o tema claro (FlatLightLaf) na inicialização
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Erro ao configurar tema claro: " + ex.getMessage());
        }
        
        // Garantir que a decoração da janela seja a nativa do sistema
        // isso ajuda a evitar problemas com espaços indesejados
        setUndecorated(false);

        try {
            // Tenta carregar papel de parede personalizado primeiro
            ConfiguracaoService configuracaoService = new ConfiguracaoService();
            String papelPersonalizado = configuracaoService.obterPapelParede();
            
            if (papelPersonalizado != null) {
                // Carrega papel de parede personalizado
                File arquivoPersonalizado = new File(papelPersonalizado);
                backgroundImage = ImageIO.read(arquivoPersonalizado);
                System.out.println("Papel de parede personalizado carregado: " + papelPersonalizado);
            } else {
                // Carrega papel de parede padrão (logocondo.png)
                URL bgImageUrl = JPrincipal.class.getResource("/com/ghg/resources/logocondo.png");
                if (bgImageUrl != null) {
                    backgroundImage = ImageIO.read(bgImageUrl);
                    System.out.println("Imagem de fundo carregada com sucesso: logocondo.png");
                } else {
                    System.err
                            .println("Erro: Imagem de fundo não encontrada no classpath: /com/ghg/resources/logocondo.png");

                    // Tenta carregar logo3.jpg como alternativa
                    URL alternativeBgUrl = JPrincipal.class.getResource("/com/ghg/resources/logo3.jpg");
                    if (alternativeBgUrl != null) {
                        backgroundImage = ImageIO.read(alternativeBgUrl);
                        System.out.println("Imagem de fundo alternativa carregada com sucesso: logo3.jpg");
                    } else {
                        System.err.println("Erro: Imagem de fundo alternativa também não encontrada");
                        backgroundImage = null;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler a imagem de fundo: " + e.getMessage());
            backgroundImage = null;
        } catch (IllegalArgumentException e) {
            System.err.println("Erro: URL da imagem de fundo inválida ou nula.");
            backgroundImage = null;
        }

        setIconImage(
                Toolkit.getDefaultToolkit().getImage(JPrincipal.class.getResource("/com/ghg/resources/cidade.png")));        
        setFont(new Font("Dialog", Font.BOLD, 20));
        setTitle("Sistema Gerenciamento de Portaria");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Impede que o botão X feche a janela

        // Permitir redimensionamento para funcionamento correto da maximização
        setResizable(true);
        
        // Definir dimensões da janela para ocupar toda a tela disponível
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        
        // Definir a posição exata (0,0) e tamanho da janela para corresponder exatamente à tela
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Configurar para iniciar maximizada
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Inicializa o nome do usuário com base no nível de acesso
        setUsuarioLogadoByNivel(nivelAcesso);

        contentPane = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(10, 10));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        ConfiguracaoService configuracaoService = new ConfiguracaoService();
        JLabel lblNewLabel = new JLabel(configuracaoService.obterNomeCondominio());
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblNewLabel.setForeground(Color.WHITE); // Define a cor do texto do título para branco

        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(lblNewLabel);
        contentPane.add(titlePanel, BorderLayout.NORTH);

        // Substitui o grid layout por um layout com painéis laterais
        JPanel centralPanel = new JPanel(new BorderLayout(20, 0));
        centralPanel.setOpaque(false);

        // Criando os painéis laterais
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        leftPanel.setOpaque(false);

        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        rightPanel.setOpaque(false);

        // Configurando as margens dos painéis laterais
        leftPanel.setBorder(new EmptyBorder(20, 30, 20, 10));
        rightPanel.setBorder(new EmptyBorder(20, 10, 20, 30));

        // Criar os botões
        btnUsuario = createButton("Usuários", "/com/ghg/resources/utilizador128.png");
        JButton btnSair = createButton("Sair", "/com/ghg/resources/sair_novo.png");
        btnBackup = createButton("Backup", "/com/ghg/resources/backup.png");
        JButton btnTrocarUsuario = createButton("Trocar Usuário", "/com/ghg/resources/sair-do-usuario.png");

        // Habilita os botões baseados em permissões
        boolean temPermissao = (nivelAcesso == 1 || nivelAcesso == 2);
        btnBackup.setEnabled(temPermissao);
        btnUsuario.setEnabled(temPermissao);

        // Adiciona os botões aos painéis laterais
        leftPanel.add(btnUsuario);
        leftPanel.add(btnSair);

        rightPanel.add(btnBackup);
        rightPanel.add(btnTrocarUsuario);

        // Adiciona os painéis ao painel central principal
        centralPanel.add(leftPanel, BorderLayout.WEST);
        centralPanel.add(rightPanel, BorderLayout.EAST);

        contentPane.add(centralPanel, BorderLayout.CENTER);

        // Criar painel para mostrar o usuário logado no rodapé
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);

        // Painel esquerdo para o seletor de modo
        JPanel leftFooterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 35, 0));
        leftFooterPanel.setOpaque(false);
        String[] modos = {"Claro", "Escuro"};
        final JComboBox<String> comboModo = new JComboBox<>(modos);
        comboModo.setSelectedIndex(0); // Padrão: Claro
        comboModo.setToolTipText("Alternar modo de cor do sistema");
        
        // Usa PopupMenuListener para aplicar o tema apenas quando o popup fechar
        // Isso permite que o usuário navegue normalmente pelas opções
        comboModo.addPopupMenuListener(new PopupMenuListener() {
            private String modoAnterior = "Claro";
            
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // Salva o modo atual quando o popup abrir
                modoAnterior = (String) comboModo.getSelectedItem();
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // Quando o popup fechar, verifica se o modo mudou
                String modoNovo = (String) comboModo.getSelectedItem();
                
                if (modoNovo != null && !modoNovo.equals(modoAnterior) && !atualizandoTema) {
                    // Modo mudou, aplica o novo tema
                    aplicarTema(modoNovo, comboModo);
                }
            }
            
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // Popup cancelado, não faz nada
            }
        });
        
        leftFooterPanel.add(comboModo);
        footerPanel.add(leftFooterPanel, BorderLayout.WEST);

        // Painel direito para o usuário logado
        JPanel rightFooterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightFooterPanel.setOpaque(false);
        lblUsuarioLogado = new JLabel();
        lblUsuarioLogado.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblUsuarioLogado.setForeground(Color.WHITE); // Define a cor do texto do nome do usuário para branco
        atualizarLabelUsuario(); // Atualiza o texto do label com o nome do usuário
        rightFooterPanel.add(lblUsuarioLogado);
        footerPanel.add(rightFooterPanel, BorderLayout.EAST);

        contentPane.add(footerPanel, BorderLayout.SOUTH);

        // Log para verificar permissões
        if (temPermissao) {
            System.out.println("Botão de configurações e backup habilitados para nível de acesso: " + nivelAcesso);
        } else {
            System.out.println("Botão de configurações e backup desabilitados para nível de acesso: " + nivelAcesso);
        }

        // Event handlers dos botões
        btnUsuario.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JUsuario usuarioTela = new JUsuario();
                usuarioTela.setLocationRelativeTo(null);
                usuarioTela.setVisible(true);
            }
        });

        btnSair.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Para o BackupScheduler antes de sair
                if (backupScheduler != null) {
                    backupScheduler.parar();
                }
                System.exit(0);
            }
        });

        btnBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exibirMenuBackup();
            }
        });

        btnTrocarUsuario.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                trocarUsuario();
            }
        });
        
        // Inicializa o BackupScheduler
        initializeBackupScheduler();
    }
    
    /**
     * Inicializa o agendador de backup automático
     */
    private void initializeBackupScheduler() {
        try {
            backupScheduler = new BackupScheduler();
            backupScheduler.iniciar();
            System.out.println("BackupScheduler inicializado com sucesso");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar BackupScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JButton createButton(String text, String imagePath) {
        JButton button = new JButton();
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(130, 60));
        button.setText(text);
        try {
            URL iconUrl = JPrincipal.class.getResource(imagePath);

            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);

                Image image = icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
                icon = new ImageIcon(image);

                button.setIcon(icon);
                button.setHorizontalTextPosition(SwingConstants.CENTER);
                button.setVerticalTextPosition(SwingConstants.BOTTOM);
            } else {
                System.err.println("Erro: Ícone não encontrado no classpath: " + imagePath);
            }

        } catch (Exception e) {            System.err.println("Erro ao carregar ou processar a imagem do botão '" + text + "': " + imagePath + " - "
                    + e.getMessage());
        }

        return button;
    }

    /**
     * Exibe um menu de opções para backup e restauração
     */
    private void exibirMenuBackup() {
        String[] options = { "Fazer Backup", "Fazer Backup (escolher diretório)", "Restaurar Backup", "Cancelar" };
        int choice = JOptionPane.showOptionDialog(this,
                "Escolha uma opção:",
                "Gerenciamento de Backup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0: // Fazer Backup (automático)
                BackupManager.realizarBackupAutomatico();
                break;
            case 1: // Fazer Backup (escolher diretório)
                BackupManager.realizarBackupComSelecaoDiretorio();
                break;
            case 2: // Restaurar Backup
                int confirmacao = JOptionPane.showConfirmDialog(this,
                        "ATENÇÃO! A restauração irá reiniciar a aplicação.\n" +
                                "Salve todos os dados antes de continuar.\n\n" +
                                "Deseja continuar?",
                        "Confirmar Restauração",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmacao == JOptionPane.YES_OPTION) {
                    if (BackupManager.restaurarBackup()) {
                        JOptionPane.showMessageDialog(this,
                                "O sistema será reiniciado para aplicar a restauração.",
                                "Reiniciando Sistema",
                                JOptionPane.INFORMATION_MESSAGE);
                        // Reinicia a aplicação
                        reiniciarAplicacao();
                    }
                }
                break;
            default: // Cancelar ou fechou a janela
                break;
        }
    }

    /**
     * Reinicia a aplicação após uma restauração de backup
     */
    private void reiniciarAplicacao() {
        dispose(); // Fecha a janela atual

        // Reinicia a aplicação chamando o método main da classe App
        EventQueue.invokeLater(() -> {
            try {
                Class.forName("com.ghg.App")
                        .getMethod("main", String[].class)
                        .invoke(null, (Object) new String[0]);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Erro ao reiniciar a aplicação: " + e.getMessage(),
                        "Erro ao Reiniciar",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    /**
     * Abre a tela de login para troca de usuário e atualiza as permissões na tela
     * principal
     */
    private void trocarUsuario() {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja trocar de usuário?\n" +
                        "As telas abertas serão fechadas.",
                "Trocar Usuário",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (resposta == JOptionPane.YES_OPTION) {
            setVisible(false); // Oculta a tela principal temporariamente

            // Criar e mostrar a tela de login
            JLogin loginTela = new JLogin(this);
            loginTela.setVisible(true);
            loginTela.setLocationRelativeTo(null);
        }
    }

    /**
     * Atualiza a interface após a troca de usuário
     */
    public void atualizarAposLogin(int novoNivelAcesso) {
        this.nivelAcesso = novoNivelAcesso;

        // Atualiza o nome do usuário logado com base no novo nível de acesso
        setUsuarioLogadoByNivel(novoNivelAcesso);

        // Atualiza as permissões dos botões
        boolean temPermissao = (nivelAcesso == 1 || nivelAcesso == 2);

        if (btnBackup != null) {
            btnBackup.setEnabled(temPermissao);
        }
        if (btnUsuario != null) {
            btnUsuario.setEnabled(temPermissao);
        }

        // Log para verificar permissões
        System.out.println("Usuário trocado. Novo nível de acesso: " + nivelAcesso);

        setVisible(true); // Torna a tela visível novamente
    }

    /**
     * Define o nome do usuário logado com base no nível de acesso
     * 
     * @param nivel Nível de acesso do usuário (1=Admin, 2=Síndico, 3=Subsíndico,
     *              4=Porteiro, 5=Zelador)
     */
    private void setUsuarioLogadoByNivel(int nivel) {
        switch (nivel) {
            case 1:
                this.nomeUsuarioLogado = "Administrador";
                break;
            case 2:
                this.nomeUsuarioLogado = "Síndico";
                break;
            case 3:
                this.nomeUsuarioLogado = "Subsíndico";
                break;
            case 4:
                this.nomeUsuarioLogado = "Porteiro";
                break;
            case 5:
                this.nomeUsuarioLogado = "Zelador";
                break;
            default:
                this.nomeUsuarioLogado = "Visitante";
                break;
        }

        // Se já criou a interface, atualiza o label
        if (lblUsuarioLogado != null) {
            atualizarLabelUsuario();
        }
    }

    /**
     * Atualiza o texto do label de usuário com o nome do usuário logado
     */
    private void atualizarLabelUsuario() {
        if (lblUsuarioLogado != null && nomeUsuarioLogado != null) {
            lblUsuarioLogado.setText("Usuário logado: " + nomeUsuarioLogado);
        }
    }

    /**
     * Gerencia a abertura de janelas de forma única.
     * Se uma janela do mesmo tipo já estiver aberta, traz ela para frente.
     * Se uma janela diferente estiver aberta, fecha a anterior e abre a nova.
     * 
     * @param chave Identificador único da janela (ex: "encomendas", "visitantes")
     * @param janela A instância JFrame a ser gerenciada
     */
    private void abrirJanelaUnica(String chave, JFrame janela) {
        // Verifica se já existe uma janela deste tipo aberta
        if (janelasAbertas.containsKey(chave)) {
            JFrame janelaExistente = janelasAbertas.get(chave);
            
            // Se a janela ainda está visível, apenas traz para frente
            if (janelaExistente.isVisible()) {
                janelaExistente.toFront();
                janelaExistente.requestFocus();
                return;
            } else {
                // Se não está visível, remove do mapa
                janelasAbertas.remove(chave);
            }
        }
        
        // Fecha qualquer outra janela aberta antes de abrir a nova
        for (Map.Entry<String, JFrame> entry : new HashMap<>(janelasAbertas).entrySet()) {
            if (!entry.getKey().equals(chave)) {
                JFrame janelaAFechar = entry.getValue();
                if (janelaAFechar.isVisible()) {
                    janelaAFechar.dispose();
                }
                janelasAbertas.remove(entry.getKey());
            }
        }
        
        // Adiciona listener para remover do mapa quando a janela for fechada
        janela.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                janelasAbertas.remove(chave);
            }
        });
        
        // Registra e exibe a nova janela
        janelasAbertas.put(chave, janela);
        janela.setVisible(true);
    }
    
    /**
     * Aplica o tema selecionado (Claro ou Escuro)
     * 
     * @param modoSelecionado O modo a ser aplicado ("Claro" ou "Escuro")
     * @param comboModo O combobox de seleção de tema
     */
    private void aplicarTema(String modoSelecionado, JComboBox<String> comboModo) {
        // Executa a troca de tema em uma thread separada para não bloquear a UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Define a flag para evitar recursão
            atualizandoTema = true;
            
            try {
                // Aplica o tema
                if ("Escuro".equals(modoSelecionado)) {
                    FlatDarkLaf.setup();
                } else {
                    FlatLightLaf.setup();
                }
                
                // Atualiza todos os componentes do frame para aplicar o novo tema
                javax.swing.SwingUtilities.updateComponentTreeUI(this);
                
                // Atualiza também todas as janelas abertas
                for (JFrame janela : janelasAbertas.values()) {
                    if (janela != null && janela.isDisplayable()) {
                        javax.swing.SwingUtilities.updateComponentTreeUI(janela);
                    }
                }
                
                // Restaura o índice correto do combobox após a atualização
                comboModo.setSelectedItem(modoSelecionado);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao aplicar tema: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                // Sempre restaura a flag após um pequeno delay
                javax.swing.Timer timer = new javax.swing.Timer(150, evt -> {
                    atualizandoTema = false;
                    ((javax.swing.Timer)evt.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    /**
     * Define o nome do usuário logado
     * 
     * @param nomeUsuario Nome do usuário a ser exibido
     */
    public void setUsuarioLogado(String nomeUsuario) {
        this.nomeUsuarioLogado = nomeUsuario;
        atualizarLabelUsuario();
    }
}