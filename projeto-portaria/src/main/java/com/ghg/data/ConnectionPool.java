package com.ghg.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pool de conexões para o banco de dados SQLite
 * @author Gilmar H Gomes
 * @since 24/05/2025
 * @version 1.5 // Version incremented
 */
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static final int MAX_CONNECTIONS = 10; // Configuração do tamanho máximo do pool
    private static final int TIMEOUT_SECONDS = 15; // Aumentado o timeout para obter conexão
    private static final ConnectionPool INSTANCE = new ConnectionPool();

    private final BlockingQueue<Connection> availableConnections;
    private final AtomicInteger openConnectionsCount; // Renomeado para clareza
    private final ThreadLocal<Connection> currentThreadConnection; // Renomeado para clareza
    private final ThreadLocal<Long> connectionAcquiredTime;

    private ConnectionPool() {
        availableConnections = new ArrayBlockingQueue<>(MAX_CONNECTIONS);
        openConnectionsCount = new AtomicInteger(0);
        currentThreadConnection = new ThreadLocal<>();
        connectionAcquiredTime = new ThreadLocal<>();
        logger.info("Pool de conexões inicializado. Capacidade máxima: {}, Timeout para obter conexão: {}s",
                MAX_CONNECTIONS, TIMEOUT_SECONDS);
    }

    public static ConnectionPool getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = currentThreadConnection.get();

        if (conn != null) {
            try {
                if (conn.isClosed() || !isConnectionValid(conn)) {
                    logger.debug("Conexão thread-local (ID: {}) está fechada ou inválida. Removendo.", conn.hashCode());
                    closeAndDecrement(conn); // Fecha e decrementa o contador
                    currentThreadConnection.remove();
                    connectionAcquiredTime.remove();
                    conn = null; // Força a obtenção de uma nova
                } else {
                    // Verifica se a conexão está sendo usada por muito tempo (potencial leak)
                    Long acquiredTime = connectionAcquiredTime.get();
                    if (acquiredTime != null && (System.currentTimeMillis() - acquiredTime > 300000)) { // 5 minutos
                        logger.warn("Thread {} está usando a mesma conexão (ID: {}) por mais de 5 minutos. Possível vazamento.",
                                Thread.currentThread().getName(), conn.hashCode());
                    }
                    // logger.trace("Reutilizando conexão thread-local (ID: {}) para Thread: {}", conn.hashCode(), Thread.currentThread().getName());
                    return conn;
                }
            } catch (SQLException e) {
                logger.warn("Erro ao verificar conexão thread-local (ID: {}): {}. Removendo.", (conn != null ? conn.hashCode() : "null"), e.getMessage());
                if (conn != null) closeAndDecrement(conn);
                currentThreadConnection.remove();
                connectionAcquiredTime.remove();
                conn = null;
            }
        }

        // Tenta obter uma conexão existente do pool
        conn = availableConnections.poll();
        if (conn != null) {
            try {
                if (conn.isClosed() || !isConnectionValid(conn)) {
                    logger.debug("Conexão do pool (ID: {}) está fechada ou inválida. Descartando.", conn.hashCode());
                    closeAndDecrement(conn); // Apenas fecha e decrementa, não cria nova aqui ainda
                    conn = null; // Força a criação ou espera abaixo
                } else {
                    // logger.trace("Conexão (ID: {}) obtida do pool de disponíveis.", conn.hashCode());
                    prepareConnectionForUse(conn); // Garante que está pronta
                    currentThreadConnection.set(conn);
                    connectionAcquiredTime.set(System.currentTimeMillis());
                    return conn;
                }
            } catch (SQLException e) {
                 logger.warn("Erro ao validar/preparar conexão do pool (ID: {}): {}. Descartando.", (conn != null ? conn.hashCode() : "null"), e.getMessage());
                 if (conn != null) closeAndDecrement(conn);
                 conn = null;
            }
        }


        // Se não há conexões válidas no poll OU se a conexão do poll era inválida
        if (openConnectionsCount.get() < MAX_CONNECTIONS) {
            if (openConnectionsCount.incrementAndGet() <= MAX_CONNECTIONS) { // Double-check com incremento atômico
                try {
                    conn = createNewConnection();
                    // logger.trace("Nova conexão (ID: {}) criada. Total de conexões abertas: {}", conn.hashCode(), openConnectionsCount.get());
                    currentThreadConnection.set(conn);
                    connectionAcquiredTime.set(System.currentTimeMillis());
                    return conn;
                } catch (SQLException | ClassNotFoundException e) {
                    openConnectionsCount.decrementAndGet(); // Reverte incremento se falhou
                    logger.error("Falha ao criar nova conexão: {}", e.getMessage(), e);
                    throw new SQLException("Falha ao criar nova conexão para o pool.", e);
                }
            } else {
                openConnectionsCount.decrementAndGet(); // Não conseguiu criar pois excederia MAX_CONNECTIONS
                // logger.debug("Limite máximo de conexões ({}) atingido, não foi possível criar nova. Tentando aguardar...", MAX_CONNECTIONS);
            }
        }

        // Se atingiu o limite ou não pôde criar nova, aguarda uma conexão ficar disponível
        // logger.debug("Aguardando por conexão disponível no pool (timeout: {}s)... Total de conexões abertas: {}", TIMEOUT_SECONDS, openConnectionsCount.get());
        try {
            conn = availableConnections.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (conn != null) {
                 try {
                    if (conn.isClosed() || !isConnectionValid(conn)) {
                        logger.warn("Conexão obtida do pool após espera (ID: {}) está fechada ou inválida. Descartando.", conn.hashCode());
                        closeAndDecrement(conn);
                        // Não tenta criar outra aqui, lança exceção para o chamador decidir
                        throw new SQLException("Conexão obtida do pool após espera estava inválida.");
                    }
                    // logger.trace("Conexão (ID: {}) obtida do pool após espera.", conn.hashCode());
                    prepareConnectionForUse(conn);
                    currentThreadConnection.set(conn);
                    connectionAcquiredTime.set(System.currentTimeMillis());
                    return conn;
                } catch (SQLException e) {
                    logger.warn("Erro ao validar/preparar conexão do pool (ID: {}) após espera: {}. Descartando.", (conn != null ? conn.hashCode() : "null"), e.getMessage());
                    if (conn != null) closeAndDecrement(conn);
                    throw e; // Relança para indicar falha
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Thread interrompida enquanto aguardava por conexão no pool.");
            throw new SQLException("Aguardar por conexão no pool foi interrompido.", e);
        }

        logger.error("Timeout: Não foi possível obter conexão do pool após {} segundos. Conexões abertas: {}", TIMEOUT_SECONDS, openConnectionsCount.get());
        throw new SQLException("Timeout esperando por conexão disponível no pool. Conexões abertas: " + openConnectionsCount.get() + "/" + MAX_CONNECTIONS);
    }


    public void releaseConnection(Connection conn) {
        if (conn == null) return;

        // logger.trace("Tentando liberar conexão (ID: {}) para o pool.", conn.hashCode());
        Connection threadConn = currentThreadConnection.get();
        if (threadConn == conn) {
            currentThreadConnection.remove();
            connectionAcquiredTime.remove();
            // logger.trace("Conexão (ID: {}) removida do ThreadLocal.", conn.hashCode());
        } else if (threadConn != null) {
            // Isso pode acontecer se uma thread liberar uma conexão que não "pegou" via getConnection
            // ou se a lógica de liberação for chamada de forma inesperada.
            logger.warn("Tentativa de liberar conexão (ID: {}) que não pertence à ThreadLocal atual (que tem ID: {}). Isso pode indicar um problema de gerenciamento de conexão.",
                    conn.hashCode(), threadConn.hashCode());
        }


        try {
            if (conn.isClosed()) {
                logger.debug("Conexão (ID: {}) já estava fechada ao ser liberada. Apenas decrementando contador.", conn.hashCode());
                openConnectionsCount.decrementAndGet(); // Garante que o contador seja decrementado
                return;
            }

            if (!conn.getAutoCommit()) {
                try {
                    conn.rollback();
                    logger.debug("Rollback executado na conexão (ID: {}) antes de liberar.", conn.hashCode());
                } catch (SQLException e) {
                    logger.warn("Aviso: Falha ao executar rollback na conexão (ID: {}) durante liberação: {}. Pode ser que não havia transação ativa.", conn.hashCode(), e.getMessage());
                } finally {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException eSetAutoCommit) {
                         logger.error("CRÍTICO: Falha ao restaurar autoCommit=true na conexão (ID: {}). A conexão pode estar instável. Fechando-a.", conn.hashCode(), eSetAutoCommit);
                         closeAndDecrement(conn); // Fecha em caso de erro grave
                         return;
                    }
                }
            }

            cleanupConnection(conn); // Limpa estados da sessão (PRAGMAs de sessão etc.)

            if (!availableConnections.offer(conn)) {
                logger.warn("Pool de conexões cheio ({}). Não foi possível adicionar a conexão (ID: {}). Fechando-a.", MAX_CONNECTIONS, conn.hashCode());
                closeAndDecrement(conn);
            } else {
                // logger.trace("Conexão (ID: {}) retornada ao pool. Disponíveis: {}", conn.hashCode(), availableConnections.size());
            }
        } catch (SQLException e) {
            logger.error("Erro ao liberar conexão (ID: {}): {}. Tentando fechar e decrementar.", conn.hashCode(), e.getMessage(), e);
            closeAndDecrement(conn);
        }
    }

    private void closeAndDecrement(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                logger.debug("Conexão (ID: {}) fechada.", conn.hashCode());
            }
        } catch (SQLException e) {
            logger.warn("Erro ao fechar conexão (ID: {}): {}", (conn != null ? conn.hashCode() : "null"), e.getMessage());
        } finally {
            // Sempre decrementa se a intenção era fechar uma conexão que o pool gerenciava
            // Cuidado para não decrementar duas vezes se releaseConnection chamar isso e já tiver decrementado
            // No entanto, o openConnectionsCount deve refletir conexões que o pool ACHA que estão abertas.
            int currentCount = openConnectionsCount.get();
            if (currentCount > 0) { // Evita decrementar abaixo de zero, embora AtomicInteger previna isso.
                 openConnectionsCount.decrementAndGet();
            }
             // logger.trace("Contador de conexões decrementado. Atual: {}", openConnectionsCount.get());
        }
    }


    public void closeAllConnections() {
        logger.info("Fechando todas as conexões do pool...");
        int closedCount = 0;
        Connection conn;
        while ((conn = availableConnections.poll()) != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    closedCount++;
                }
            } catch (SQLException e) {
                logger.warn("Erro ao fechar conexão (ID: {}) do pool durante shutdown: {}", conn.hashCode(), e.getMessage());
            }
        }
        // Também precisamos considerar as conexões que estão em uso (nas ThreadLocals)
        // Esta parte é mais complexa, pois não temos uma lista direta delas.
        // Idealmente, as threads deveriam liberar suas conexões antes do shutdown.
        // O que podemos fazer é zerar o contador de conexões abertas.
        int remaining = openConnectionsCount.getAndSet(0);
        if (remaining > closedCount) {
             logger.warn("{} conexões estavam potencialmente em uso e não no pool disponível durante o shutdown. Elas serão abandonadas.", (remaining - closedCount));
        }
        logger.info("{} conexões disponíveis foram fechadas. O contador de conexões abertas foi zerado.", closedCount);
    }


    private boolean isConnectionValid(Connection conn) {
        if (conn == null) return false;
        try (Statement stmt = conn.createStatement()) {
            // Um timeout curto para esta validação, para não bloquear por muito tempo.
            // SQLite não suporta setQueryTimeout em Statement diretamente desta forma para PRAGMA.
            // O busy_timeout da conexão deve cobrir isso.
            stmt.execute("SELECT 1"); // Simples e eficaz para SQLite
            return true;
        } catch (SQLException e) {
            // logger.trace("Conexão (ID: {}) inválida: {}", conn.hashCode(), e.getMessage());
            return false;
        }
    }


    private void prepareConnectionForUse(Connection conn) throws SQLException {
        // Garante que a conexão está em autoCommit=true, pois é o esperado para uso geral do pool.
        if (!conn.getAutoCommit()) {
            try {
                conn.rollback(); // Tenta reverter qualquer transação pendente
            } catch (SQLException e) {
                 logger.warn("Aviso: Falha ao executar rollback na conexão (ID: {}) ao prepará-la: {}. Pode ser que não havia transação.", conn.hashCode(), e.getMessage());
            }
            conn.setAutoCommit(true);
        }

        // Aplica PRAGMAs de sessão que queremos em todas as conexões do pool.
        // Estes são geralmente seguros para serem aplicados repetidamente.
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 20000;"); // 20 segundos (ajustado)
            stmt.execute("PRAGMA cache_size = -10000;"); // 10MB (valor negativo é em KiB)
            stmt.execute("PRAGMA temp_store = MEMORY;");
            stmt.execute("PRAGMA foreign_keys = ON;"); // Habilitar FK por conexão é uma boa prática
        } catch (SQLException e) {
            logger.warn("Aviso: Erro ao configurar PRAGMAs de sessão (busy_timeout, etc.) na conexão (ID: {}): {}", conn.hashCode(), e.getMessage());
            // Não lança exceção aqui, a conexão pode ainda ser utilizável
        }

        // Revalida a conexão após as preparações
        if (!isConnectionValid(conn)) {
            throw new SQLException("Conexão (ID: " + conn.hashCode() +") tornou-se inválida após preparação.");
        }
    }

    private Connection createNewConnection() throws SQLException, ClassNotFoundException {
        Connection conn = DatabaseLocator.connectToDatabase(); // Obtém conexão física
        // logger.debug("Nova conexão física (ID: {}) criada.", conn.hashCode());

        // Configurações iniciais importantes que podem ser feitas uma vez na conexão física
        // Estas são herdadas ou configuradas pelo DatabaseLocator.
        // `configurarPropriedadesAvancadas` (como WAL) é melhor feito uma vez
        // na inicialização do BD, não em cada nova conexão do pool.
        // O DatabaseLocator.connectToDatabase() já chama configurarPropriedadesBasicas.

        // Prepara a conexão para uso no pool (PRAGMAs de sessão, autoCommit)
        try {
            prepareConnectionForUse(conn);
        } catch (SQLException e) {
            logger.error("Falha ao preparar nova conexão (ID: {}): {}. Fechando-a.", conn.hashCode(), e.getMessage());
            try {
                conn.close();
            } catch (SQLException closeEx) { /* ignorar */ }
            throw e; // Relança para que getConnection() saiba que falhou
        }
        return conn;
    }

    /**
     * Limpa alguns estados da conexão antes de devolvê-la ao pool.
     * Focado em PRAGMAs de sessão que podem ter sido alterados.
     * @param conn A conexão a ser limpa
     */
    private void cleanupConnection(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Redefine o busy_timeout para um valor padrão do pool, caso tenha sido alterado.
            stmt.execute("PRAGMA busy_timeout = 10000;"); // 10 segundos
            // PRAGMA optimize foi removido daqui por ser potencialmente custoso.
            // Outros PRAGMAs de sessão podem ser resetados aqui se necessário.
        } catch (SQLException e) {
            logger.warn("Aviso: Erro ao limpar (resetar PRAGMAs de sessão) conexão (ID: {}): {}", conn.hashCode(), e.getMessage());
        }
    }

    // Para fins de monitoramento/debug
    public int getAvailableConnectionsCount() {
        return availableConnections.size();
    }

    public int getOpenConnectionsCount() {
        return openConnectionsCount.get();
    }
}