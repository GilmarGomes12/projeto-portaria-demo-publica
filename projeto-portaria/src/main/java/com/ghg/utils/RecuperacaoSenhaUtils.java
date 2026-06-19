package com.ghg.utils;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghg.data.ConexaoBanco;
import com.ghg.data.DatabaseLocator;

/**
 * @author Gilmar H Gomes
 * @since 21/05/2025
 * @version 1.0
 * @description Classe utilitária para recuperação de senha
 */
public class RecuperacaoSenhaUtils {
    private static final Logger logger = LoggerFactory.getLogger(RecuperacaoSenhaUtils.class);
    
    // Configurações para envio de e-mail (devem ser atualizadas com valores reais em produção)
    // Nota: Para Gmail, use uma "Senha de Aplicativo" gerada nas configurações de segurança do Google
    private static final String EMAIL_REMETENTE = "seu-email@gmail.com";
    private static final String SENHA_REMETENTE = "sua-senha-de-aplicativo"; // Senha de Aplicativo de 16 caracteres
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    // Tempo de expiração do token em horas
    private static final int HORAS_VALIDADE_TOKEN = 24;
    
    /**
     * Gera um token aleatório para recuperação de senha
     * @return O token gerado
     */
    public static String gerarToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24]; // 192 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);    }
    
    /**
     * Verifica se um e-mail existe no sistema
     * @param email O e-mail para verificar
     * @return true se o e-mail existe, false caso contrário
     */
    public static boolean verificarEmailExistente(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        Connection conn = null;
        
        try {
            conn = ConexaoBanco.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar e-mail existente: {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                ConexaoBanco.desconectar(conn);
            }
        }
        
        return false;
    }    /**
     * Recupera o login do usuário pelo e-mail
     * @param email O e-mail do usuário
     * @return O login do usuário ou null se não encontrado
     */
    public static String obterLoginPorEmail(String email) {
        String sql = "SELECT login FROM usuarios WHERE email = ?";
        Connection conn = null;
        
        try {
            conn = ConexaoBanco.getConnection();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("login");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter login por e-mail: {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                ConexaoBanco.desconectar(conn);
            }
        }
        
        return null;
    }    /**
     * Gera e armazena um token para recuperação de senha
     * Versão simplificada que evita transações aninhadas
     * 
     * @param email O e-mail do usuário
     * @return O token gerado, ou null em caso de erro
     */
    public static synchronized String gerarEArmazenarToken(String email) {
        logger.debug("Gerando token para: {}", email);
        
        // Verificação preventiva de readonly antes de iniciar operação crítica
        if (!SQLiteReadonlyFixer.verificacaoPreventiva()) {
            logger.warn("Verificação preventiva detectou problema readonly, executando correção...");
            if (!SQLiteReadonlyFixer.verificarECorrigirReadonly()) {
                logger.error("Não foi possível corrigir problema readonly antes de gerar token");
                return null;
            }
        }
        
        String token = gerarToken();
        LocalDateTime dataExpiracao = LocalDateTime.now().plusHours(HORAS_VALIDADE_TOKEN);
        
        // Validação de parâmetros de entrada
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Email inválido fornecido para geração de token");
            return null;
        }
        
        // Verifica se o email existe antes de tentar a atualização
        if (!verificarEmailExistente(email)) {
            logger.warn("Tentativa de gerar token para email inexistente: {}", email);
            return null;
        }
        
        String sql = "UPDATE usuarios SET token_recuperacao = ?, data_expiracao_token = ? WHERE email = ?";
        Connection conn = null;
        
        try {
            int tentativas = 0;
            int maxTentativas = 3;
            boolean sucesso = false;
            int esperaBaseMs = 200;
            
            while (!sucesso && tentativas < maxTentativas) {
                tentativas++;
                
                try {
                    // Obtém uma conexão
                    conn = ConexaoBanco.getConnection();
                    if (conn == null) {
                        logger.error("Tentativa {}: Não foi possível obter conexão", tentativas);
                        if (tentativas < maxTentativas) {
                            Thread.sleep(esperaBaseMs * tentativas);
                            continue;
                        }
                        return null;
                    }
                    
                    // Garante que está em modo autocommit (sem transação explícita)
                    conn.setAutoCommit(true);
                    
                    // Executa a atualização em uma única operação
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, token);
                        pstmt.setTimestamp(2, Timestamp.valueOf(dataExpiracao));
                        pstmt.setString(3, email);
                        pstmt.setQueryTimeout(10); // 10 segundos timeout
                        
                        int linhasAfetadas = pstmt.executeUpdate();
                        logger.debug("Tentativa {}: Linhas afetadas: {}", tentativas, linhasAfetadas);
                        
                        if (linhasAfetadas > 0) {
                            sucesso = true;
                            logger.info("Token gerado com sucesso para {} (tentativa {})", email, tentativas);
                        } else {
                            logger.warn("Tentativa {}: Nada atualizado para email: {}", tentativas, email);
                            if (tentativas < maxTentativas) {
                                Thread.sleep(esperaBaseMs * tentativas);
                            }
                        }
                    }
                    
                } catch (SQLException e) {
                    logger.warn("Tentativa {}: Erro SQL: {}", tentativas, e.getMessage());
                    if (tentativas < maxTentativas) {
                        try {
                            Thread.sleep(esperaBaseMs * tentativas);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            logger.warn("Thread interrompido: {}", ie.getMessage());
                            return null;
                        }
                    }
                } finally {
                    if (conn != null) {
                        ConexaoBanco.desconectar(conn);
                        conn = null;
                    }
                }
            }
            
            if (sucesso) {
                return token;
            } else {
                logger.warn("Não foi possível gerar token para {} após {} tentativas", email, maxTentativas);
                return null;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Operação interrompida: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar token: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Envia um e-mail com instruções para recuperação de senha
     * @param email O e-mail do destinatário
     * @param token O token de recuperação
     * @param login O login do usuário
     * @return true se o e-mail foi enviado com sucesso, false caso contrário
     */    public static boolean enviarEmailRecuperacao(String email, String token, String login) {
        System.out.println("Enviando email para: " + email);
        System.out.println("Token: " + token);
        System.out.println("Login: " + login);
        
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_REMETENTE);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_REMETENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Recuperação de Senha - Sistema de Portaria");
            
            // Corpo do e-mail
            String corpoEmail = "Prezado(a) usuário(a),\n\n" +
                    "Recebemos uma solicitação de recuperação de senha para o login: " + login + "\n\n" +
                    "Para redefinir sua senha, utilize o código abaixo no sistema:\n\n" +
                    token + "\n\n" +
                    "Este código é válido por " + HORAS_VALIDADE_TOKEN + " horas.\n\n" +
                    "Caso não tenha solicitado esta recuperação, por favor ignore este e-mail.\n\n" +
                    "Atenciosamente,\n" +
                    "Equipe do Sistema de Portaria";
            
            message.setText(corpoEmail);
            
            Transport.send(message);
            System.out.println("Email enviado com sucesso!");
            return true;
        } catch (MessagingException e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            e.printStackTrace();
            logger.error("Erro ao enviar e-mail de recuperação: {}", e.getMessage(), e);
            return false;
        }
    }    /**
     * Verifica se um token de recuperação é válido
     * Versão aprimorada com tratamento ainda melhor de concorrência e bloqueios
     * 
     * @param token O token a ser verificado
     * @return true se o token é válido, false caso contrário
     */
    public static boolean validarToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Tentativa de validar token nulo ou vazio");
            return false;
        }
        
        // Usar uma consulta com timeout explícito para evitar bloqueios
        String sql = "SELECT data_expiracao_token FROM usuarios WHERE token_recuperacao = ?";
        Connection conn = null;
        
        try {
            int tentativas = 0;
            int maxTentativas = 5; // Aumentado para 5 tentativas
            int esperaBaseMs = 50; // Espera inicial menor para operações de leitura
            
            while (tentativas < maxTentativas) {
                tentativas++;
                
                try {
                    // Tentar obter conexão do pool primeiro (mais rápido)
                    try {
                        conn = ConexaoBanco.getConnection();
                        if (conn != null) {
                            logger.debug("Obteve conexão do pool para validar token");
                        }
                    } catch (Exception e) {
                        logger.warn("Não foi possível obter conexão do pool, tentando conexão direta");
                        conn = null;
                    }
                    
                    // Se não conseguiu do pool, tenta conexão direta
                    if (conn == null) {
                        try {
                            conn = DatabaseLocator.connectToDatabase();
                            logger.debug("Obteve conexão direta para validar token");
                        } catch (Exception e) {
                            logger.error("Tentativa {}: Não foi possível obter conexão: {}", tentativas, e.getMessage());
                            if (tentativas < maxTentativas) {
                                Thread.sleep(esperaBaseMs * tentativas);
                                continue;
                            }
                            return false;
                        }
                    }
                    
                    // Configuração otimizada para consultas de leitura
                    try (var stmt = conn.createStatement()) {
                        // Timeout maior para leitura (geralmente não bloqueia outras operações)
                        stmt.execute("PRAGMA busy_timeout = 8000"); 
                        // Modo somente leitura para esta consulta
                        stmt.execute("PRAGMA query_only = ON");
                        // Menor isolamento para leituras mais rápidas
                        stmt.execute("PRAGMA read_uncommitted = ON"); 
                    } catch (SQLException e) {
                        // Log do erro mas continua tentando a consulta
                        logger.warn("Erro ao configurar modo somente leitura: {}", e.getMessage());
                    }
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, token);
                        pstmt.setQueryTimeout(5); // Timeout de 5 segundos para consulta
                        
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                Timestamp dataExpiracao = rs.getTimestamp("data_expiracao_token");
                                if (dataExpiracao != null) {
                                    LocalDateTime agora = LocalDateTime.now();
                                    boolean tokenValido = agora.isBefore(dataExpiracao.toLocalDateTime());
                                    
                                    if (tokenValido) {
                                        logger.debug("Token {} é válido (expira em {})", token, dataExpiracao);
                                    } else {
                                        logger.warn("Token {} expirado (expirou em {})", token, dataExpiracao);
                                    }
                                    
                                    return tokenValido;
                                } else {
                                    logger.warn("Token {} encontrado mas sem data de expiração", token);
                                }
                            } else {
                                logger.debug("Token {} não encontrado no banco de dados", token);
                            }
                        }
                    }
                    
                    // Se chegou aqui sem retornar, o token não é válido
                    return false;
                    
                } catch (SQLException e) {
                    // Tratamento especial para erros de bloqueio
                    if (e.getMessage().contains("SQLITE_BUSY") || e.getMessage().contains("locked")) {
                        logger.warn("Tentativa {}: Banco ocupado ao validar token: {}", tentativas, e.getMessage());
                    } else {
                        logger.error("Tentativa {}: Erro ao validar token: {}", tentativas, e.getMessage());
                    }
                    
                    if (tentativas < maxTentativas) {
                        try {
                            // Backoff exponencial
                            int waitTime = esperaBaseMs * (1 << (tentativas - 1));
                            logger.debug("Aguardando {}ms antes da próxima tentativa", waitTime);
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Thread interrompido ao validar token");
                } finally {
                    // Sempre fechar a conexão atual
                    if (conn != null) {
                        try {
                            ConexaoBanco.fecharConexao(conn); // Método unificado para fechar conexões
                        } catch (Exception e) {
                            logger.warn("Erro ao fechar conexão: {}", e.getMessage());
                        }
                        conn = null;
                    }
                }
            }
            
            logger.warn("Todas as {} tentativas falharam ao validar token", maxTentativas);
        } catch (Exception e) {
            // Captura qualquer outro erro inesperado
            logger.error("Erro inesperado ao validar token: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Método para testar a conexão com o servidor de e-mail
     * @return true se a conexão foi bem-sucedida, false caso contrário
     */
    public static boolean testarConexaoEmail() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.debug", "true");  // Ativa logs detalhados
        
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_REMETENTE);
                }
            });
            
            // Testa a conexão
            Transport transport = session.getTransport("smtp");
            transport.connect(SMTP_HOST, EMAIL_REMETENTE, SENHA_REMETENTE);
            transport.close();
            
            logger.info("Conexão com servidor SMTP testada com sucesso!");
            return true;
        } catch (MessagingException e) {
            logger.error("Erro ao testar conexão com servidor SMTP: {}", e.getMessage(), e);
            return false;
        }
    }    /**
     * Redefine a senha do usuário que possui o token especificado
     * @param token O token de recuperação
     * @param novaSenha A nova senha
     * @return true se a senha foi redefinida com sucesso, false caso contrário
     */    
    public static boolean redefinirSenha(String token, String novaSenha) {
        // Verificação preventiva de readonly antes de iniciar operação crítica
        if (!SQLiteReadonlyFixer.verificacaoPreventiva()) {
            logger.warn("Verificação preventiva detectou problema readonly, executando correção...");
            if (!SQLiteReadonlyFixer.verificarECorrigirReadonly()) {
                logger.error("Não foi possível corrigir problema readonly antes de redefinir senha");
                return false;
            }
        }
        
        String senhaHash = PasswordUtils.hashPassword(novaSenha);
        String sql = "UPDATE usuarios SET senha = ?, token_recuperacao = NULL, data_expiracao_token = NULL " +
                     "WHERE token_recuperacao = ?";
        
        Connection conn = null;
        int maxTentativas = 2;
        
        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {
                // Obter uma conexão do pool otimizado
                conn = ConexaoBanco.getConnection();            
            // Verificar se a conexão é válida
            if (conn == null) {
                logger.error("Tentativa {}: Não foi possível obter uma conexão com o banco de dados", tentativa);
                if (tentativa < maxTentativas) {
                    continue;
                }
                return false;
            }
            
            // Desativar autocommit para gerenciar a transação manualmente
            boolean autoCommitOriginal = conn.getAutoCommit();
            if (autoCommitOriginal) {
                conn.setAutoCommit(false);
            }
            
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, senhaHash);
                    pstmt.setString(2, token);
                    
                    int linhasAfetadas = pstmt.executeUpdate();
                    logger.debug("Linhas afetadas ao redefinir senha: {}", linhasAfetadas);
                    
                    if (linhasAfetadas > 0) {
                        conn.commit();
                        logger.info("Senha redefinida com sucesso para o token: {}", token);
                        return true;
                    } else {
                        conn.rollback();
                        logger.warn("Token não encontrado ou expirado: {}", token);
                        return false;
                    }
                }
            } catch (SQLException e) {
                // Rollback em caso de erro
                try {
                    if (conn != null && !conn.getAutoCommit()) {
                        conn.rollback();
                    }
                } catch (SQLException ex) {
                    logger.error("Erro ao realizar rollback: {}", ex.getMessage());
                }
                
                // Verificar se é erro SQLITE_READONLY
                if (e.getMessage().contains("SQLITE_READONLY") || e.getMessage().contains("readonly database")) {
                    logger.warn("Tentativa {}: Detectado erro SQLITE_READONLY, tentando correção...", tentativa);
                    
                    // Sempre devolver a conexão atual ao pool antes da correção
                    if (conn != null) {
                        try {
                            conn.setAutoCommit(autoCommitOriginal);
                        } catch (SQLException ex) {
                            logger.debug("Erro ao restaurar autoCommit antes da correção: {}", ex.getMessage());
                        }
                        ConexaoBanco.desconectar(conn);
                        conn = null;
                    }
                      // Tentar correção SQLITE_READONLY com versão aprimorada
                    try {
                        logger.info("Tentativa {}: Executando correção avançada SQLITE_READONLY...", tentativa);
                        boolean correcaoOk = SQLiteReadonlyFixer.verificarECorrigirReadonly();
                        if (correcaoOk) {
                            logger.info("Tentativa {}: Correção SQLITE_READONLY aplicada com sucesso", tentativa);
                            if (tentativa < maxTentativas) {
                                Thread.sleep(500); // Pausa maior para garantir que a correção se efetive
                            }
                        } else {
                            logger.warn("Tentativa {}: Correção SQLITE_READONLY falhou, mas tentando continuar...", tentativa);
                            if (tentativa < maxTentativas) {
                                Thread.sleep(1000); // Pausa ainda maior quando a correção falha
                            }
                        }
                    } catch (Exception fixEx) {
                        logger.warn("Tentativa {}: Erro ao aplicar correção SQLITE_READONLY: {}", tentativa, fixEx.getMessage());
                        if (tentativa < maxTentativas) {
                            try {
                                Thread.sleep(800); // Pausa quando há erro na correção
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                logger.warn("Thread interrompida durante pausa de correção");
                            }
                        }
                    }
                    
                    // Se não é a última tentativa, continua o loop
                    if (tentativa < maxTentativas) {
                        continue;
                    }
                }
                
                // Para outros erros ou última tentativa, re-lança a exceção
                throw e;
                
            } finally {
                // Restaurar o autoCommit ao estado anterior
                try {
                    if (conn != null && conn.getAutoCommit() != autoCommitOriginal) {
                        conn.setAutoCommit(autoCommitOriginal);
                    }
                } catch (SQLException e) {
                    logger.warn("Erro ao restaurar autoCommit: {}", e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            logger.error("Tentativa {}: Erro ao redefinir senha: {}", tentativa, e.getMessage(), e);
            
            // Se não é a última tentativa e não foi tratado como readonly, pode tentar novamente
            if (tentativa < maxTentativas && !(e.getMessage().contains("SQLITE_READONLY") || e.getMessage().contains("readonly database"))) {
                logger.warn("Tentativa {}: Erro genérico, tentando novamente...", tentativa);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warn("Thread interrompida durante espera entre tentativas");
                    break;
                }
            }
        } finally {
            // Sempre devolver a conexão ao pool
            if (conn != null) {
                ConexaoBanco.desconectar(conn);
                conn = null;
            }
        }
        } // fim do loop for
        
        return false;
    }
}