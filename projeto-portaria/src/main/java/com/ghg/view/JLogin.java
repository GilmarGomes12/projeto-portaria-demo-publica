package com.ghg.view;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Font;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;
import javax.swing.event.AncestorListener;

import com.ghg.data.ConexaoBanco;
import com.ghg.service.ConfiguracaoService;
import com.ghg.utils.PasswordUtils;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.event.AncestorEvent;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Gilmar H Gomes
 * @since 27/03/2025
 * @version 1.1 // Versão atualizada
 */

public class JLogin extends JFrame {
    private JPanel contentPane;
    private JTextField textUser;
    private JPasswordField passwordField;
    private JButton btnAcessar;
    private JButton btnEsqueciSenha; // Botão para recuperação de senha
    private JPanel panel; // Adicionado como atributo da classe para ser acessível em todos os métodos
    // Referência para a tela principal quando usado para trocar usuário
    private JPrincipal telaPrincipalOrigem;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JLogin frame = new JLogin();
                    frame.setVisible(true);
                    frame.setLocationRelativeTo(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public JLogin() {
        // Força o tema claro (FlatLightLaf) na inicialização
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Erro ao configurar tema claro no JLogin: " + ex.getMessage());
        }
        
        setIconImage(Toolkit.getDefaultToolkit().getImage(JLogin.class.getResource("/com/ghg/resources/cidade.png")));
        setFont(new Font("Dialog", Font.BOLD, 14));
        setTitle("Login");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Impede o fechamento pelo botão X        setResizable(false); // Desabilita maximização
        setBounds(100, 100, 611, 520); // Aumentada altura do JFrame
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        panel = new JPanel();
        panel.setBackground(new Color(224, 224, 224));
        panel.setBounds(134, 44, 329, 380); // Aumentada altura do painel
        contentPane.add(panel);
        panel.setLayout(null);

        JLabel lblDataHoraLogin = new JLabel("Data/Hora de Login:");
        lblDataHoraLogin.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblDataHoraLogin.setBounds(21, 10, 129, 26);
        panel.add(lblDataHoraLogin);

        JLabel lblRDataHoraLogin = new JLabel("");
        lblRDataHoraLogin.addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                LocalDateTime agora = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String dataHoraFormatada = agora.format(formatter);
                lblRDataHoraLogin.setText(dataHoraFormatada);
            }

            public void ancestorMoved(AncestorEvent event) {
            }

            public void ancestorRemoved(AncestorEvent event) {
            }
        });
        lblRDataHoraLogin.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblRDataHoraLogin.setBounds(147, 10, 148, 26);
        panel.add(lblRDataHoraLogin);

        ConfiguracaoService configuracaoService = new ConfiguracaoService();
        JLabel lblTitulo = new JLabel(configuracaoService.obterNomeCondominio());
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblTitulo.setBounds(22, 46, 284, 36);
        panel.add(lblTitulo);

        JLabel lblLogo = new JLabel("");
        lblLogo.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/chaves64.png")));
        lblLogo.setBounds(128, 92, 72, 64);
        panel.add(lblLogo);

        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblUsuario.setBounds(53, 166, 129, 26);
        panel.add(lblUsuario);

        textUser = new JTextField();
        textUser.setBounds(53, 190, 223, 25);
        panel.add(textUser);
        textUser.setColumns(10);
        textUser.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnAcessar.doClick();
                }
            }
        });

        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblSenha.setBounds(53, 226, 129, 26);
        panel.add(lblSenha);

        passwordField = new JPasswordField();
        passwordField.setBounds(53, 247, 223, 25);
        panel.add(passwordField);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnAcessar.doClick();
                }
            }
        });

        JButton btnSair = new JButton("Sair");
        btnSair.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btnSair.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnSair.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/sair16.png")));
        btnSair.setBounds(55, 310, 106, 25);
        panel.add(btnSair);

        btnAcessar = new JButton("Acessar");
        btnAcessar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });        btnAcessar.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/conecte16.png")));
        btnAcessar.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnAcessar.setBounds(172, 310, 106, 25);
        panel.add(btnAcessar);
        
        // Botão Esqueci Minha Senha
        btnEsqueciSenha = new JButton("Esqueci a Senha");
        btnEsqueciSenha.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirTelaRecuperacaoSenha();
            }
        });
        // Usa fonte maior para melhor visibilidade
        btnEsqueciSenha.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnEsqueciSenha.setBounds(95, 345, 150, 25); // Aumentado largura e melhor posicionado
        panel.add(btnEsqueciSenha);
    }

    /**
     * Construtor específico para trocar de usuário a partir da tela principal
     * 
     * @param telaPrincipal Referência à tela principal que chamou o login
     */
    public JLogin(JPrincipal telaPrincipal) {
        this();
        this.telaPrincipalOrigem = telaPrincipal;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Nota: Não alteramos mais o botão Sair para Cancelar
        // O botão permanece como "Sair" para fechar completamente o sistema
    }

    private class ResultadoAutenticacao {
        int nivelAcesso;
        String nomeCompleto;

        public ResultadoAutenticacao(int nivelAcesso, String nomeCompleto) {
            this.nivelAcesso = nivelAcesso;
            this.nomeCompleto = nomeCompleto;
        }
    }

    private void realizarLogin() {
        String username = textUser.getText();
        String password = new String(passwordField.getPassword());

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            JOptionPane.showMessageDialog(JLogin.this, "Por favor, preencha todos os campos.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ResultadoAutenticacao resultado = autenticarUsuario(username, password);

        if (resultado != null && resultado.nivelAcesso > 0) {
            JOptionPane.showMessageDialog(JLogin.this, "Login realizado com sucesso!");
            redirecionarComBaseNoNivelAcesso(resultado.nivelAcesso, resultado.nomeCompleto);
        } else {
            JOptionPane.showMessageDialog(JLogin.this, "Usuário ou senha incorretos.", "Erro de Login",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ResultadoAutenticacao autenticarUsuario(String username, String password) {
        // SQL flexível para suportar diferentes estruturas de tabela
        String sql = "SELECT * FROM usuarios WHERE login = ?";        try (Connection conn = ConexaoBanco.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.println("Tentando autenticar usuário: " + username);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String senhaHash = rs.getString("senha");
                int nivelAcesso = 0;
                String nomeCompleto = "";

                // Tenta obter o nome completo do usuário
                try {
                    nomeCompleto = rs.getString("nome");
                    if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
                        nomeCompleto = username; // Usa o login como fallback se o nome estiver vazio
                    }
                    System.out.println("Nome de usuário encontrado: " + nomeCompleto);
                } catch (SQLException e) {
                    System.out.println("Campo 'nome' não encontrado, usando login como nome: " + username);
                    nomeCompleto = username;
                }

                // Tenta obter o nível de acesso de diferentes colunas possíveis
                try {
                    nivelAcesso = rs.getInt("nivel_acesso");
                    System.out.println("Encontrado nível de acesso (nivel_acesso): " + nivelAcesso);
                } catch (SQLException e) {
                    // Tenta outra coluna
                    try {
                        String perfil = rs.getString("perfil");
                        System.out.println("Encontrado perfil: " + perfil);

                        // Converte perfil para nivel_acesso
                        if ("Administrador".equalsIgnoreCase(perfil)) {
                            nivelAcesso = 1;
                        } else if ("Sindico".equalsIgnoreCase(perfil)) {
                            nivelAcesso = 2;
                        } else if ("Subsíndico".equalsIgnoreCase(perfil)) {
                            nivelAcesso = 3;
                        } else if ("Porteiro".equalsIgnoreCase(perfil)) {
                            nivelAcesso = 4;
                        } else if ("Zelador".equalsIgnoreCase(perfil)) {
                            nivelAcesso = 5;
                        }
                    } catch (SQLException e2) {
                        System.out.println("Erro ao obter nivel_acesso ou perfil: " + e2.getMessage());
                    }
                }

                System.out.println("Usuário encontrado, verificando senha...");
                System.out.println("Senha hash armazenada: " + senhaHash);

                // Tenta verificar usando PasswordUtils
                boolean senhaCorreta = false;
                try {
                    senhaCorreta = PasswordUtils.checkPassword(password, senhaHash);
                    System.out.println("Verificação com PasswordUtils: " + senhaCorreta);
                } catch (Exception e) {
                    System.out.println("Erro ao verificar senha com PasswordUtils: " + e.getMessage());
                }

                // Se não funcionar com PasswordUtils, tenta comparação direta (para
                // compatibilidade)
                if (!senhaCorreta && password.equals(senhaHash)) {
                    System.out.println("Senha verificada com comparação direta.");
                    senhaCorreta = true;
                }

                if (senhaCorreta) {
                    System.out.println("Autenticação bem-sucedida! Nível de acesso: " + nivelAcesso);
                    return new ResultadoAutenticacao(nivelAcesso, nomeCompleto);
                } else {
                    System.out.println("Senha incorreta para o usuário: " + username);
                }
            } else {
                System.out.println("Usuário não encontrado no banco de dados: " + username);
            }        } catch (SQLException e) {
            System.out.println("Erro ao autenticar usuário: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void redirecionarComBaseNoNivelAcesso(int nivelAcesso, String nomeCompleto) {
        // Personaliza a mensagem de boas-vindas com o nome do usuário
        String mensagemBemVindo = "Bem-vindo, " + nomeCompleto + "!";

        JOptionPane.showMessageDialog(this, mensagemBemVindo);

        // Sempre abre uma nova tela principal, independentemente de ser login inicial
        // ou troca de usuário
        // Se for troca, descarta a anterior
        if (telaPrincipalOrigem != null) {
            telaPrincipalOrigem.dispose();
        }

        // Cria uma nova tela principal com o novo nível de acesso e nome do usuário
        abrirTelaPrincipal(nivelAcesso, nomeCompleto);

        this.dispose();
    }

    private void abrirTelaPrincipal(int nivelAcesso, String nomeUsuario) {
        JPrincipal telaPrincipal = new JPrincipal(nivelAcesso);
        // Define o nome do usuário na tela principal
        telaPrincipal.setUsuarioLogado(nomeUsuario);
        telaPrincipal.setVisible(true);
        // Não usar setLocationRelativeTo aqui pois interfere com a maximização
    }

    private void abrirTelaRecuperacaoSenha() {
        JRecuperacaoSenha telaRecuperacao = new JRecuperacaoSenha();
        telaRecuperacao.setVisible(true);
        telaRecuperacao.setLocationRelativeTo(this);
    }

}