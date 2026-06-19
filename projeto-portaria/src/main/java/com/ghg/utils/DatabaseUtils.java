package com.ghg.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ghg.data.ConexaoBanco; // Importa a classe principal de conexão

/**
 * Utilitário para operações de banco de dados.
 * Oferece métodos para gerenciamento seguro de recursos,
 * incluindo conexões, statements, resultsets e transações explícitas.
 *
 * @author Gilmar H Gomes (base original) & IA Assistiva (refinamentos)
 * @since 24/05/2025
 * @version 1.6 // Version incremented
 */
public class DatabaseUtils {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);
    private static final ThreadLocal<Connection> transactionConnectionHolder = new ThreadLocal<>(); // Renomeado para clareza

    /**
     * Inicia uma transação.
     * Todas as operações subsequentes na mesma thread que usarem {@link #obterConexao()}
     * ou passarem a conexão retornada por este método usarão a mesma conexão
     * até que a transação seja finalizada com {@link #confirmarTransacao()} ou {@link #reverterTransacao()}.
     *
     * @return A conexão associada à transação.
     * @throws SQLException Se ocorrer um erro ao obter a conexão ou configurar a transação.
     */
    public static Connection iniciarTransacao() throws SQLException {
        Connection conn = transactionConnectionHolder.get();
        if (conn == null || conn.isClosed()) {
            // ConexaoBanco.conectar() agora só lança SQLException, que pode englobar
            // problemas de driver (ClassNotFound) internamente tratados pelo pool.
            conn = ConexaoBanco.conectar(); // Obtém conexão do pool
            logger.debug("Nova conexão (ID: {}) obtida do pool para iniciar transação.", conn.hashCode());
            try {
                conn.setAutoCommit(false);
                transactionConnectionHolder.set(conn);
                logger.info("Transação iniciada na conexão (ID: {}).", conn.hashCode());
            } catch (SQLException e) {
                logger.error("Falha ao configurar autoCommit=false na conexão (ID: {}). Liberando conexão.", conn.hashCode(), e);
                ConexaoBanco.desconectar(conn); // Libera a conexão se a configuração da transação falhar
                throw e; // Propaga o erro
            }
        } else if (conn.getAutoCommit()) { // Conexão existente na thread, mas autoCommit era true
            conn.setAutoCommit(false); // Configura para transação
            logger.info("Transação reconfigurada (autoCommit=false) na conexão existente da thread (ID: {}).", conn.hashCode());
        } else {
            // A conexão já está na ThreadLocal e com autoCommit=false, indicando transação já ativa.
            logger.debug("Transação já está ativa na conexão da thread (ID: {}).", conn.hashCode());
        }
        return conn;
    }

    /**
     * Confirma (commit) a transação ativa na thread atual.
     * A conexão é então configurada para autoCommit=true e liberada de volta ao pool.
     *
     * @throws SQLException Se a thread não tiver uma transação ativa, ou se ocorrer um erro durante o commit.
     */
    public static void confirmarTransacao() throws SQLException {
        Connection conn = transactionConnectionHolder.get();
        if (conn == null || conn.isClosed()) {
            logger.warn("Tentativa de confirmar transação sem uma conexão ativa ou válida na thread.");
            if (conn != null) transactionConnectionHolder.remove(); // Limpa se a conexão estiver fechada
            return; // Ou poderia lançar uma exceção se preferir um contrato mais estrito
        }

        if (conn.getAutoCommit()) {
            logger.warn("Tentativa de confirmar transação em uma conexão (ID: {}) que já está em autoCommit=true. Nenhuma ação de commit será realizada.", conn.hashCode());
            // Mesmo assim, vamos limpar o holder e liberar a conexão, pois a intenção era finalizar.
            transactionConnectionHolder.remove();
            ConexaoBanco.desconectar(conn);
            return;
        }

        try {
            conn.commit();
            logger.info("Transação confirmada (commit) com sucesso na conexão (ID: {}).", conn.hashCode());
        } catch (SQLException e) {
            logger.error("Falha ao confirmar (commit) transação na conexão (ID: {}).", conn.hashCode(), e);
            // Não tentar reverter automaticamente aqui, pois o estado pode ser incerto.
            // Apenas propagar o erro do commit.
            throw e;
        } finally {
            // Sempre tentar restaurar autoCommit e liberar a conexão, mesmo se o commit falhar.
            try {
                if (!conn.isClosed()) { // Só opera se não estiver fechada
                    conn.setAutoCommit(true);
                }
            } catch (SQLException eSetAutoCommit) {
                logger.error("Erro CRÍTICO ao restaurar autoCommit=true na conexão (ID: {}) após tentativa de commit. A conexão pode estar instável.", conn.hashCode(), eSetAutoCommit);
                // O pool tentará lidar com isso quando a conexão for liberada.
            } finally {
                transactionConnectionHolder.remove();
                ConexaoBanco.desconectar(conn); // Devolve ao pool
                logger.debug("Conexão (ID: {}) liberada para o pool após tentativa de commit.", conn.hashCode());
            }
        }
    }

    /**
     * Reverte (rollback) a transação ativa na thread atual.
     * A conexão é então configurada para autoCommit=true e liberada de volta ao pool.
     *
     * @throws SQLException Se a thread não tiver uma transação ativa, ou se ocorrer um erro durante o rollback.
     */
    public static void reverterTransacao() throws SQLException {
        Connection conn = transactionConnectionHolder.get();
        if (conn == null || conn.isClosed()) {
            logger.warn("Tentativa de reverter transação sem uma conexão ativa ou válida na thread.");
            if (conn != null) transactionConnectionHolder.remove();
            return; // Ou lançar exceção
        }

        if (conn.getAutoCommit()) {
            logger.warn("Tentativa de reverter transação em uma conexão (ID: {}) que já está em autoCommit=true. Nenhuma ação de rollback será realizada.", conn.hashCode());
            transactionConnectionHolder.remove();
            ConexaoBanco.desconectar(conn);
            return;
        }

        try {
            conn.rollback();
            logger.info("Transação revertida (rollback) com sucesso na conexão (ID: {}).", conn.hashCode());
        } catch (SQLException e) {
            logger.error("Falha ao reverter (rollback) transação na conexão (ID: {}).", conn.hashCode(), e);
            throw e;
        } finally {
            try {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException eSetAutoCommit) {
                logger.error("Erro CRÍTICO ao restaurar autoCommit=true na conexão (ID: {}) após tentativa de rollback. A conexão pode estar instável.", conn.hashCode(), eSetAutoCommit);
            } finally {
                transactionConnectionHolder.remove();
                ConexaoBanco.desconectar(conn); // Devolve ao pool
                logger.debug("Conexão (ID: {}) liberada para o pool após tentativa de rollback.", conn.hashCode());
            }
        }
    }

    /**
     * Obtém uma conexão com o banco de dados.
     * Se uma transação estiver ativa na thread atual (iniciada com {@link #iniciarTransacao()}),
     * retorna a conexão associada a essa transação.
     * Caso contrário, obtém uma nova conexão do pool {@link ConexaoBanco}.
     *
     * @return Uma conexão com o banco de dados.
     * @throws SQLException Se ocorrer um erro ao obter a conexão.
     */
    public static Connection obterConexao() throws SQLException {
        Connection conn = transactionConnectionHolder.get();
        if (conn != null && !conn.isClosed()) {
            // logger.trace("Retornando conexão transacional (ID: {}) da thread atual.", conn.hashCode());
            return conn;
        }
        // logger.trace("Nenhuma transação ativa na thread, obtendo nova conexão do pool.");
        // ConexaoBanco.conectar() agora só lança SQLException
        Connection newConn = ConexaoBanco.conectar();
        // logger.trace("Conexão (ID: {}) obtida do pool para uso não transacional (ou início de nova transação).", newConn.hashCode());
        return newConn;
    }

    /**
     * Fecha os recursos de banco de dados (ResultSet, Statement e, opcionalmente, Connection) de forma segura.
     * A conexão SÓ é fechada (devolvida ao pool) se NÃO fizer parte de uma transação ativa
     * gerenciada por esta classe (ou seja, não é a conexão em {@code transactionConnectionHolder}).
     *
     * @param conn A conexão utilizada para criar os recursos.
     * @param stmt O Statement a ser fechado (pode ser null).
     * @param rs   O ResultSet a ser fechado (pode ser null).
     */
    public static void fecharRecursos(Connection conn, Statement stmt, ResultSet rs) {
        closeQuietly(rs); // Fecha ResultSet silenciosamente
        closeQuietly(stmt); // Fecha Statement silenciosamente

        if (conn != null) {
            Connection transConn = transactionConnectionHolder.get();
            // Só desconecta a conexão se ela não for a conexão transacional da thread atual.
            // A conexão transacional é gerenciada por confirmarTransacao/reverterTransacao.
            if (conn != transConn) {
                // logger.trace("Fechando conexão (ID: {}) que não é transacional da thread.", conn.hashCode());
                ConexaoBanco.desconectar(conn); // Devolve ao pool
            } else {
                // logger.trace("Mantendo conexão transacional (ID: {}) aberta, será gerenciada por confirmar/reverter.", conn.hashCode());
            }
        }
    }

    /**
     * Fecha um recurso {@link AutoCloseable} (como Connection, Statement, ResultSet)
     * de forma silenciosa, apenas logando um aviso em caso de erro.
     *
     * @param resource O recurso a ser fechado (pode ser null).
     */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) { // Captura genérica pois AutoCloseable.close() lança Exception
                // Não relançar, apenas logar. Útil em blocos finally.
                logger.warn("Erro ao fechar recurso '{}' silenciosamente: {}", resource.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}