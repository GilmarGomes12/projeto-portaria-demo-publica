package com.ghg.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Removido: import com.ghg.utils.DatabaseInitializer; (Chamaremos o pool diretamente)
import java.sql.Connection; // Para PRAGMA optimize
import java.sql.Statement;  // Para PRAGMA optimize
import java.sql.SQLException; // Para PRAGMA optimize

/**
 * Classe de gancho para finalização da JVM
 * Garante que os recursos do banco de dados são liberados adequadamente
 * @author Gilmar H Gomes
 * @since 24/05/2025
 * @version 1.5 // Version incremented
 */
public class ShutdownHook extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    @Override
    public void run() {
        logger.info("Iniciando processo de finalização da aplicação...");

        // 1. Tentar otimizar o banco de dados (opcional, mas bom no shutdown)
        // Isso requer uma conexão. Vamos pegar uma diretamente, fora do pool,
        // pois o pool estará sendo desligado.
        logger.info("Tentando executar PRAGMA optimize no banco de dados...");
        Connection directConn = null;
        try {
            directConn = DatabaseLocator.connectToDatabase(); // Conexão direta
            if (directConn != null) {
                try (Statement stmt = directConn.createStatement()) {
                    stmt.execute("PRAGMA optimize;");
                    logger.info("PRAGMA optimize executado com sucesso.");
                } catch (SQLException e) {
                    logger.warn("Falha ao executar PRAGMA optimize: {}", e.getMessage());
                } finally {
                    try {
                        directConn.close();
                    } catch (SQLException e) {
                        // Ignorar
                    }
                }
            }
        } catch (Exception e) { // ClassNotFoundException, SQLException
            logger.warn("Não foi possível obter conexão para PRAGMA optimize: {}", e.getMessage());
        }


        // 2. Finalizar o pool de conexões
        logger.info("Liberando recursos do pool de conexões do banco de dados...");
        try {
            ConnectionPool.getInstance().closeAllConnections();
            logger.info("Pool de conexões finalizado e recursos liberados com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao liberar recursos do pool de conexões durante finalização: {}", e.getMessage(), e);
        }

        logger.info("Processo de finalização da aplicação concluído.");
    }

    /**
     * Registra o hook de finalização
     */
    public static void register() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        logger.info("Hook de finalização (ShutdownHook) registrado na JVM.");
    }
}