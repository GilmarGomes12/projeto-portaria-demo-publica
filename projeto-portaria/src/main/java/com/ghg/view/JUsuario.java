package com.ghg.view;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.ghg.data.ConexaoBanco;
import com.ghg.utils.PasswordUtils;

import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gilmar H Gomes
 * @since 27/03/2025
 * @version 1.0
 * @description Tela de Cadastro de Usúario do sistema de portaria
 */

public class JUsuario extends JDialog {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField textFieldNomeCompleto;
    private JTextField textFieldUsuario;
    private JTextField textFieldEmail; // Campo para e-mail do usuário
    private JPasswordField passwordFieldSenha1;
    private JPasswordField passwordFieldSenha2;
    private JComboBox<String> comboBox;
    private JComboBox<String> comboBoxExcluir;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JUsuario dialog = new JUsuario();
                    dialog.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Verifica se um nome de usuário já existe no banco de dados
     * 
     * @param login O nome de usuário a ser verificado
     * @return true se o usuário já existe, false caso contrário
     */
    private boolean usuarioExiste(String login) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE login = ?";        
        try (Connection conn = ConexaoBanco.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao verificar usuário existente: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * Verifica se uma senha já existe no banco de dados
     * 
     * @param senhaHash O hash da senha a ser verificado
     * @return true se a senha já existe, false caso contrário
     */
    private boolean senhaExiste(String senhaHash) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE senha = ?";

        try (Connection conn = ConexaoBanco.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, senhaHash);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao verificar senha existente: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }    public JUsuario() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(JUsuario.class.getResource("/com/ghg/resources/cidade.png")));
        setFont(new Font("Dialog", Font.BOLD, 14));
        setTitle("Registrar Usúario");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Desativa o botão fechar no X
        setResizable(false); // Desabilita maximização
        setModal(true); // Define o diálogo como modal
        setBounds(100, 100, 597, 640); // Altura ampliada para comportar seção de exclusão
        
        // Adiciona listener para o botão fechar (X) para mostrar mensagem ao usuário
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JOptionPane.showMessageDialog(JUsuario.this,
                        "Por favor, utilize o botão 'Sair' para fechar esta janela.",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Centraliza a janela na tela (deve ser chamado APÓS definir tamanho)
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblNewLabel = new JLabel("");
        lblNewLabel.setIcon(new ImageIcon(JUsuario.class.getResource("/com/ghg/resources/utilizador128.png")));
        lblNewLabel.setBounds(227, 10, 128, 142);
        contentPane.add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Nome Completo:");
        lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblNewLabel_1.setBounds(10, 155, 107, 25);
        contentPane.add(lblNewLabel_1);

        textFieldNomeCompleto = new JTextField();
        textFieldNomeCompleto.setBounds(118, 156, 455, 25);
        contentPane.add(textFieldNomeCompleto);
        textFieldNomeCompleto.setColumns(10);

        JLabel lblNewLabel_1_1 = new JLabel("Usúario:");
        lblNewLabel_1_1.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblNewLabel_1_1.setBounds(10, 190, 107, 25);
        contentPane.add(lblNewLabel_1_1);

        textFieldUsuario = new JTextField();
        textFieldUsuario.setColumns(10);
        textFieldUsuario.setBounds(118, 194, 455, 25);
        contentPane.add(textFieldUsuario);

        JLabel lblEmail = new JLabel("E-mail:");
        lblEmail.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblEmail.setBounds(10, 229, 107, 25);
        contentPane.add(lblEmail);

        textFieldEmail = new JTextField();
        textFieldEmail.setColumns(10);
        textFieldEmail.setBounds(118, 229, 455, 25);
        contentPane.add(textFieldEmail);

        JLabel lblNewLabelSenha1 = new JLabel("Senha:");
        lblNewLabelSenha1.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblNewLabelSenha1.setBounds(10, 264, 107, 25);
        contentPane.add(lblNewLabelSenha1);

        passwordFieldSenha1 = new JPasswordField();
        passwordFieldSenha1.setBounds(118, 264, 455, 25);
        contentPane.add(passwordFieldSenha1);

        JLabel lblNewLabelSenha2 = new JLabel("Confirmar Senha:");
        lblNewLabelSenha2.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblNewLabelSenha2.setBounds(10, 299, 107, 25);
        contentPane.add(lblNewLabelSenha2);

        passwordFieldSenha2 = new JPasswordField();
        passwordFieldSenha2.setBounds(118, 303, 455, 25);
        contentPane.add(passwordFieldSenha2);        JLabel lblPerfilUsario = new JLabel("Perfil Usúario:");
        lblPerfilUsario.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblPerfilUsario.setBounds(10, 338, 107, 25);
        contentPane.add(lblPerfilUsario);

        comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Tahoma", Font.BOLD, 12));
        comboBox.setModel(new DefaultComboBoxModel<>(
                new String[] { "Administrador", "Sindico", "Subsíndico", "Porteiro", "Zelador" }));
        comboBox.setBounds(118, 339, 455, 30); // Aumentei a altura do combobox para melhor visibilidade
        contentPane.add(comboBox);JButton btnSalvar = new JButton("Salvar Usúario");
        btnSalvar.setIcon(new ImageIcon(JUsuario.class.getResource("/com/ghg/resources/salve-16.png")));
        btnSalvar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnSalvar.setBounds(118, 410, 150, 30); // Ajustei o posicionamento vertical e aumentei a altura
        contentPane.add(btnSalvar);

        btnSalvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                String senha1 = new String(passwordFieldSenha1.getPassword());
                String senha2 = new String(passwordFieldSenha2.getPassword());
                String username = textFieldUsuario.getText();
                String nome_completo = textFieldNomeCompleto.getText();
                String email = textFieldEmail.getText(); // Obter e-mail do campo correspondente
                
                // Validar campos obrigatórios
                if (username.trim().isEmpty() || nome_completo.trim().isEmpty() || senha1.isEmpty() || email.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(JUsuario.this, 
                            "Todos os campos são obrigatórios.", 
                            "Campos vazios", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Validar nome de usuário (apenas letras, números e pontos)
                if (!username.matches("^[a-zA-Z0-9.]+$")) {
                    JOptionPane.showMessageDialog(JUsuario.this, 
                            "O nome de usuário deve conter apenas letras, números e pontos.", 
                            "Nome de usuário inválido", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Validar formato de e-mail
                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    JOptionPane.showMessageDialog(JUsuario.this, 
                            "Por favor, informe um endereço de e-mail válido.", 
                            "E-mail inválido", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!senha1.equals(senha2)) {
                    JOptionPane.showMessageDialog(JUsuario.this, "As senhas não coincidem.", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String senhaHash = PasswordUtils.hashPassword(senha1);
                String perfilSelecionado = (String) comboBox.getSelectedItem();                // Verificar se o usuário já existe no banco de dados
                if (usuarioExiste(username)) {
                    JOptionPane.showMessageDialog(JUsuario.this,
                            "Este nome de usuário já está sendo utilizado. Por favor, escolha outro nome de usuário.",
                            "Usuário duplicado",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Verificar se a senha já existe no banco de dados
                if (senhaExiste(senhaHash)) {
                    JOptionPane.showMessageDialog(JUsuario.this,
                            "Esta senha já está sendo utilizada por outro usuário. Por favor, escolha uma senha diferente.",
                            "Senha duplicada",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int nivelAcesso = 0;
                switch (perfilSelecionado) {
                    case "Administrador":
                        nivelAcesso = 1;
                        break;
                    case "Sindico":
                        nivelAcesso = 2;
                        break;
                    case "Subsíndico":
                        nivelAcesso = 3;
                        break;
                    case "Porteiro":
                        nivelAcesso = 4;
                        break;
                    case "Zelador":
                        nivelAcesso = 5;
                        break;

                }                // Usar transação manual para cadastrar o usuário
                final String sql = "INSERT INTO usuarios (nome_completo, login, senha, nivel_acesso, email) VALUES (?, ?, ?, ?, ?)";
                
                try (Connection conn = ConexaoBanco.getConnection()) {
                    conn.setAutoCommit(false); // Iniciar transação
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        // Definir os parâmetros
                        pstmt.setString(1, nome_completo);
                        pstmt.setString(2, username);
                        pstmt.setString(3, senhaHash);
                        pstmt.setInt(4, nivelAcesso);
                        pstmt.setString(5, email);
                        
                        // Executar a inserção
                        int affectedRows = pstmt.executeUpdate();
                        
                        if (affectedRows > 0) {
                            conn.commit(); // Confirmar transação
                            JOptionPane.showMessageDialog(JUsuario.this, 
                                    "Usuário criado com sucesso!", 
                                    "Sucesso",
                                    JOptionPane.INFORMATION_MESSAGE);
                            carregarUsuariosParaExclusao(); // Atualizar lista de exclusão
                            // Limpar campos do formulário
                            textFieldNomeCompleto.setText("");
                            textFieldUsuario.setText("");
                            textFieldEmail.setText("");
                            passwordFieldSenha1.setText("");
                            passwordFieldSenha2.setText("");
                        } else {
                            conn.rollback(); // Reverter transação
                            JOptionPane.showMessageDialog(JUsuario.this,
                                    "Não foi possível criar o usuário. Por favor, tente novamente.",
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }                    } catch (SQLException ex) {
                        conn.rollback(); // Reverter transação em caso de erro
                        throw ex; // Re-lançar exceção para tratamento externo                    
                    }
                } catch (SQLException ex) {
                    System.out.println("Erro ao criar usuário: " + ex.getMessage());
                    ex.printStackTrace();
                    
                    // Verificar tipos de erro para feedback ao usuário
                    String mensagemErro = ex.getMessage();
                    if (mensagemErro != null) {
                        if (mensagemErro.contains("UNIQUE constraint")) {
                            JOptionPane.showMessageDialog(JUsuario.this, 
                                    "Este nome de usuário já está sendo utilizado. Por favor, escolha outro.", 
                                    "Erro de Cadastro", 
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (mensagemErro.contains("SQLITE_BUSY") || mensagemErro.contains("locked")) {
                            JOptionPane.showMessageDialog(JUsuario.this,
                                    "O banco de dados está ocupado no momento. Por favor, aguarde alguns segundos e tente novamente.", 
                                    "Banco de Dados Ocupado", 
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(JUsuario.this,
                                    "Ocorreu um erro ao criar o usuário: " + mensagemErro, 
                                    "Erro", 
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(JUsuario.this,
                                "Ocorreu um erro desconhecido ao criar o usuário.", 
                                "Erro", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });        JButton btnSair = new JButton("Sair");
        btnSair.setIcon(new ImageIcon(JUsuario.class.getResource("/com/ghg/resources/sair16.png")));
        btnSair.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnSair.setBounds(304, 410, 150, 30);
        contentPane.add(btnSair);
        btnSair.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // ── Seção: Excluir Usuário ──────────────────────────────────────────────
        JSeparator separador = new JSeparator(SwingConstants.HORIZONTAL);
        separador.setBounds(10, 456, 563, 10);
        contentPane.add(separador);

        JLabel lblTituloExcluir = new JLabel("── Excluir Usuário ──");
        lblTituloExcluir.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblTituloExcluir.setBounds(178, 468, 220, 22);
        contentPane.add(lblTituloExcluir);

        JLabel lblSelecionar = new JLabel("Selecionar Usuário:");
        lblSelecionar.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblSelecionar.setBounds(10, 500, 130, 25);
        contentPane.add(lblSelecionar);

        comboBoxExcluir = new JComboBox<>();
        comboBoxExcluir.setFont(new Font("Tahoma", Font.PLAIN, 12));
        comboBoxExcluir.setBounds(145, 500, 428, 30);
        contentPane.add(comboBoxExcluir);
        carregarUsuariosParaExclusao();

        JButton btnExcluir = new JButton("Excluir Usuário");
        btnExcluir.setIcon(new ImageIcon(JUsuario.class.getResource("/com/ghg/resources/excluir16.png")));
        btnExcluir.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnExcluir.setBounds(145, 545, 160, 30);
        contentPane.add(btnExcluir);

        JButton btnAtualizarLista = new JButton("Atualizar Lista");
        btnAtualizarLista.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnAtualizarLista.setBounds(320, 545, 140, 30);
        contentPane.add(btnAtualizarLista);

        btnAtualizarLista.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                carregarUsuariosParaExclusao();
            }
        });

        btnExcluir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String itemSelecionado = (String) comboBoxExcluir.getSelectedItem();
                if (itemSelecionado == null || itemSelecionado.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(JUsuario.this,
                            "Selecione um usuário para excluir.",
                            "Nenhum usuário selecionado",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Extrair o login do item exibido no formato "login  (Perfil)"
                String loginParaExcluir = itemSelecionado.split(" ")[0].trim();

                if (loginParaExcluir.equalsIgnoreCase("admin")) {
                    JOptionPane.showMessageDialog(JUsuario.this,
                            "O usuário 'admin' não pode ser excluído.",
                            "Operação não permitida",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int confirmacao = JOptionPane.showConfirmDialog(JUsuario.this,
                        "Tem certeza que deseja excluir o usuário '" + loginParaExcluir + "'?\n"
                                + "Esta ação não poderá ser desfeita.",
                        "Confirmar Exclusão",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmacao == JOptionPane.YES_OPTION) {
                    excluirUsuario(loginParaExcluir);
                }
            }
        });
    }

    /**
     * Carrega os usuários cadastrados (exceto admin) no ComboBox de exclusão.
     */
    private void carregarUsuariosParaExclusao() {
        String sql = "SELECT login, nivel_acesso FROM usuarios WHERE login <> 'admin' ORDER BY login";
        List<String> itens = new ArrayList<>();

        try (Connection conn = ConexaoBanco.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String login = rs.getString("login");
                int nivel = rs.getInt("nivel_acesso");
                String perfil;
                switch (nivel) {
                    case 1: perfil = "Administrador"; break;
                    case 2: perfil = "Síndico"; break;
                    case 3: perfil = "Subsíndico"; break;
                    case 4: perfil = "Porteiro"; break;
                    case 5: perfil = "Zelador"; break;
                    default: perfil = "Desconhecido";
                }
                itens.add(login + "  (" + perfil + ")");
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao carregar usuários: " + ex.getMessage());
            ex.printStackTrace();
        }

        comboBoxExcluir.setModel(new DefaultComboBoxModel<>(itens.toArray(new String[0])));

        if (itens.isEmpty()) {
            comboBoxExcluir.addItem("Nenhum usuário cadastrado");
        }
    }

    /**
     * Exclui o usuário com o login informado do banco de dados.
     *
     * @param login Login do usuário a ser excluído
     */
    private void excluirUsuario(String login) {
        String sql = "DELETE FROM usuarios WHERE login = ? AND login <> 'admin'";

        try (Connection conn = ConexaoBanco.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, login);
                int linhasAfetadas = pstmt.executeUpdate();

                if (linhasAfetadas > 0) {
                    conn.commit();
                    JOptionPane.showMessageDialog(JUsuario.this,
                            "Usuário '" + login + "' excluído com sucesso!",
                            "Exclusão Concluída",
                            JOptionPane.INFORMATION_MESSAGE);
                    carregarUsuariosParaExclusao(); // Atualizar lista
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(JUsuario.this,
                            "Usuário não encontrado ou não pode ser excluído.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao excluir usuário: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(JUsuario.this,
                    "Erro ao excluir o usuário: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}