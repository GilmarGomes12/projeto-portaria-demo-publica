package com.ghg.view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.ghg.utils.RecuperacaoSenhaUtils;

import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;

/**
 * @author Gilmar H Gomes
 * @since 21/05/2023
 * @version 1.0
 * @description Tela para recuperação de senha
 */
public class JRecuperacaoSenha extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JPanel panelSolicitacao;
    private JPanel panelRedefinicao;
    private JTextField txtEmail;
    private JTextField txtToken;
    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmacaoSenha;

    /**
     * Create the frame.
     */
    public JRecuperacaoSenha() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(JLogin.class.getResource("/com/ghg/resources/cidade.png")));
        setFont(new Font("Dialog", Font.BOLD, 14));
        setTitle("Recuperação de Senha");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setBounds(100, 100, 450, 350);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        // Tab pane para alternar entre solicitação e redefinição
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(10, 11, 414, 289);
        contentPane.add(tabbedPane);
        
        // Painel de solicitação de recuperação
        panelSolicitacao = new JPanel();
        tabbedPane.addTab("Solicitar Recuperação", null, panelSolicitacao, null);
        panelSolicitacao.setLayout(null);
        
        JLabel lblLogo = new JLabel("");
        lblLogo.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/chaves64.png")));
        lblLogo.setBounds(166, 11, 72, 64);
        panelSolicitacao.add(lblLogo);
        
        JLabel lblInstrucoes = new JLabel("<html><div style='text-align: center;'>Digite seu e-mail cadastrado para<br>receber instruções de recuperação</div></html>");
        lblInstrucoes.setHorizontalAlignment(JLabel.CENTER);
        lblInstrucoes.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblInstrucoes.setBounds(10, 86, 389, 36);
        panelSolicitacao.add(lblInstrucoes);
        
        JLabel lblEmail = new JLabel("E-mail:");
        lblEmail.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblEmail.setBounds(10, 133, 69, 14);
        panelSolicitacao.add(lblEmail);
        
        txtEmail = new JTextField();
        txtEmail.setBounds(89, 131, 310, 20);
        panelSolicitacao.add(txtEmail);
        txtEmail.setColumns(10);
        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    solicitarRecuperacao();
                }
            }
        });
        
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/conecte16.png")));
        btnEnviar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnEnviar.setBounds(89, 170, 120, 30);
        panelSolicitacao.add(btnEnviar);
        btnEnviar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                solicitarRecuperacao();
            }
        });
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnCancelar.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/sair16.png")));
        btnCancelar.setBounds(219, 170, 120, 30);
        panelSolicitacao.add(btnCancelar);        btnCancelar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Botão para testar conexão de email
        JButton btnTestarConexao = new JButton("Testar Conexão Email");
        btnTestarConexao.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnTestarConexao.setBounds(89, 207, 250, 30);
        panelSolicitacao.add(btnTestarConexao);
        btnTestarConexao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testarConexaoEmail();
            }
        });
        
        // Painel de redefinição de senha
        panelRedefinicao = new JPanel();
        tabbedPane.addTab("Redefinir Senha", null, panelRedefinicao, null);
        panelRedefinicao.setLayout(null);
        
        JLabel lblLogo2 = new JLabel("");
        lblLogo2.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/chaves64.png")));
        lblLogo2.setBounds(166, 11, 72, 64);
        panelRedefinicao.add(lblLogo2);
        
        JLabel lblInstrucoes2 = new JLabel("<html><div style='text-align: center;'>Digite o código recebido por e-mail<br>e sua nova senha</div></html>");
        lblInstrucoes2.setHorizontalAlignment(JLabel.CENTER);
        lblInstrucoes2.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblInstrucoes2.setBounds(10, 86, 389, 30);
        panelRedefinicao.add(lblInstrucoes2);
          JLabel lblToken = new JLabel("Código:");
        lblToken.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblToken.setBounds(10, 133, 89, 14);
        panelRedefinicao.add(lblToken);
        
        txtToken = new JTextField();
        txtToken.setBounds(109, 131, 290, 20);
        panelRedefinicao.add(txtToken);
        txtToken.setColumns(10);
        
        JLabel lblNovaSenha = new JLabel("Nova Senha:");
        lblNovaSenha.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblNovaSenha.setBounds(10, 164, 89, 14);
        panelRedefinicao.add(lblNovaSenha);
        
        txtNovaSenha = new JPasswordField();
        txtNovaSenha.setBounds(109, 162, 290, 20);
        panelRedefinicao.add(txtNovaSenha);
        
        JLabel lblConfirmar = new JLabel("Confirmar:");
        lblConfirmar.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblConfirmar.setBounds(10, 195, 89, 14);
        panelRedefinicao.add(lblConfirmar);
        
        txtConfirmacaoSenha = new JPasswordField();
        txtConfirmacaoSenha.setBounds(109, 193, 290, 20);
        panelRedefinicao.add(txtConfirmacaoSenha);
        txtConfirmacaoSenha.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    redefinirSenha();
                }
            }
        });
        
        JButton btnRedefinir = new JButton("Redefinir Senha");
        btnRedefinir.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/conecte16.png")));
        btnRedefinir.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnRedefinir.setBounds(109, 224, 150, 30);
        panelRedefinicao.add(btnRedefinir);
        btnRedefinir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redefinirSenha();
            }
        });
        
        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnVoltar.setIcon(new ImageIcon(JLogin.class.getResource("/com/ghg/resources/sair16.png")));
        btnVoltar.setBounds(269, 224, 130, 30);
        panelRedefinicao.add(btnVoltar);
        btnVoltar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Por padrão, exibir a primeira aba
        tabbedPane.setSelectedIndex(0);
    }
    
    /**
     * Método para tratar a solicitação de recuperação de senha
     */
    private void solicitarRecuperacao() {
        String email = txtEmail.getText().trim();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe seu e-mail.", "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        
        // Verificar se o e-mail existe no sistema
        if (!RecuperacaoSenhaUtils.verificarEmailExistente(email)) {
            JOptionPane.showMessageDialog(this, "E-mail não encontrado no sistema.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Obter o login associado ao e-mail
        String login = RecuperacaoSenhaUtils.obterLoginPorEmail(email);
        if (login == null) {
            JOptionPane.showMessageDialog(this, "Erro ao processar a solicitação.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Gerar e armazenar token
        String token = RecuperacaoSenhaUtils.gerarEArmazenarToken(email);
        if (token == null) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar o código de recuperação.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
          // Enviar e-mail
        boolean enviado = RecuperacaoSenhaUtils.enviarEmailRecuperacao(email, token, login);
        
        if (enviado) {
            JOptionPane.showMessageDialog(this, "Um e-mail com instruções foi enviado para " + email, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            // Mudar para a aba de redefinição
            tabbedPane.setSelectedIndex(1);        } else {
            JOptionPane.showMessageDialog(this, 
                    "Não foi possível enviar o e-mail de recuperação.\nVerifique sua configuração de e-mail ou entre em contato com o administrador do sistema.", 
                    "Aviso", 
                    JOptionPane.WARNING_MESSAGE);
            // Não avança para a próxima aba quando falha o envio
        }
    }
      /**
     * Método para testar a conexão com o servidor de email
     */
    private void testarConexaoEmail() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        boolean sucesso = RecuperacaoSenhaUtils.testarConexaoEmail();
        
        setCursor(Cursor.getDefaultCursor());
        
        if (sucesso) {
            JOptionPane.showMessageDialog(this, 
                "Conexão com o servidor de email testada com sucesso!", 
                "Teste de Conexão", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Não foi possível conectar ao servidor de email.\n" +
                "Verifique as configurações de SMTP, porta, usuário e senha.", 
                "Erro de Conexão", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Método para tratar a redefinição de senha
     */
    private void redefinirSenha() {
        String token = txtToken.getText().trim();
        String novaSenha = new String(txtNovaSenha.getPassword());
        String confirmacao = new String(txtConfirmacaoSenha.getPassword());
        
        // Validar entradas
        if (token.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o código recebido por e-mail.", "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            txtToken.requestFocus();
            return;
        }
        
        if (novaSenha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe a nova senha.", "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            txtNovaSenha.requestFocus();
            return;
        }
        
        if (!novaSenha.equals(confirmacao)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", "Erro", JOptionPane.ERROR_MESSAGE);
            txtNovaSenha.requestFocus();
            return;
        }
        
        // Validar o token
        if (!RecuperacaoSenhaUtils.validarToken(token)) {
            JOptionPane.showMessageDialog(this, "Código inválido ou expirado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Redefinir a senha
        boolean redefinido = RecuperacaoSenhaUtils.redefinirSenha(token, novaSenha);
        
        if (redefinido) {
            JOptionPane.showMessageDialog(this, "Senha redefinida com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Fecha a janela
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao redefinir a senha. Por favor, tente novamente.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}