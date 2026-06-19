package com.ghg.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.net.URL;
import java.net.URISyntaxException;
import java.security.CodeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe responsável por localizar e estabelecer conexão direta com o banco de dados SQLite
 *
 * @author Sistema
 * @version 1.0
 */
public class DatabaseLocator {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseLocator.class);
    private static final String DB_FILENAME = "condominio.db";
    
    /**
     * Obtém o caminho absoluto para o arquivo do banco de dados.
     * Tenta localizar o banco de dados de forma inteligente, dependendo se está rodando de um JAR ou em ambiente de DEV.
     *
     * @return O caminho completo para o arquivo do banco de dados.
     */
    public static String getDatabasePath() {
        // O método getProductionDatabasePath() agora contém a lógica aprimorada.
        return getProductionDatabasePath();
    }

    /**
     * Retorna o caminho do banco de dados.
     * Se executando de um JAR, espera o DB ao lado do JAR.
     * Se em ambiente de desenvolvimento, tenta localizar na raiz do projeto.
     *
     * @return Caminho absoluto para o banco de dados.
     */
    private static String getProductionDatabasePath() {
        File dbFile = null;
        try {
            CodeSource codeSource = DatabaseLocator.class.getProtectionDomain().getCodeSource();
            URL location = codeSource.getLocation();
            File sourceFile = new File(location.toURI());

            if (sourceFile.isFile() && sourceFile.getName().toLowerCase().endsWith(".jar")) {
                // Executando de um JAR: o DB deve estar ao lado do JAR.
                dbFile = new File(sourceFile.getParentFile(), DB_FILENAME);
                logger.info("Executando de JAR. Caminho do banco de dados determinado: {}", dbFile.getAbsolutePath());
            } else {
                // Não está executando de um JAR (provavelmente ambiente de desenvolvimento/IDE).
                // System.getProperty("user.dir") no VSCode/Eclipse geralmente é a raiz do módulo.
                // Ex: c:\\Development\\projeto-portaria-1.4\\projeto-portaria
                // O condominio.db está em c:\\Development\\projeto-portaria-1.4
                File userDir = new File(System.getProperty("user.dir"));
                File dbInProjectRoot = new File(userDir.getParentFile(), DB_FILENAME);

                if (dbInProjectRoot.exists()) {
                    dbFile = dbInProjectRoot;
                    logger.info("Ambiente de DEV (user.dir no módulo). Usando DB da raiz do projeto: {}", dbFile.getAbsolutePath());
                } else {
                    // Fallback: talvez user.dir seja a raiz do projeto, ou o DB está no user.dir por algum motivo.
                    // Ou o DB ainda não existe e será criado aqui.
                    dbFile = new File(userDir, DB_FILENAME);
                    logger.info("Ambiente de DEV (user.dir na raiz ou DB local, ou DB a ser criado). Usando DB em: {}", dbFile.getAbsolutePath());
                }
            }
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException ao determinar o caminho do banco de dados. Usando fallback para user.dir.", e);
            // Fallback mais simples em caso de erro
            dbFile = new File(System.getProperty("user.dir"), DB_FILENAME);
        } catch (Exception e) {
            logger.error("Exceção inesperada ao determinar o caminho do banco de dados. Usando fallback para user.dir.", e);
            dbFile = new File(System.getProperty("user.dir"), DB_FILENAME);
        }
        
        logger.info("Caminho final efetivamente usado para o banco de dados: {}", dbFile.getAbsolutePath());
        return dbFile.getAbsolutePath();
    }

    /**
     * Obtém o diretório pai onde o arquivo de banco de dados está localizado.
     *
     * @return Caminho absoluto do diretório do banco de dados.
     */
    public static String getDatabaseDirectory() {
        String dbPath = getDatabasePath();
        File dbFile = new File(dbPath);
        return dbFile.getParent(); // Retorna null se dbPath não tiver pai (improvável)
    }

    /**
     * Verifica se o arquivo do banco de dados já existe no caminho determinado.
     *
     * @return true se o arquivo do banco de dados existir, false caso contrário.
     */
    public static boolean databaseExists() {
        return new File(getDatabasePath()).exists();
    }

    /**
     * Cria um backup do arquivo de banco de dados atual.
     * O backup é salvo no mesmo diretório do banco de dados original, com um timestamp no nome.
     *
     * @return O caminho absoluto do arquivo de backup criado, ou null em caso de falha.
     */
    public static String createBackup() {
        String originalDbPath = getDatabasePath();
        File sourceFile = new File(originalDbPath);

        if (!sourceFile.exists()) {
            logger.warn("Tentativa de backup falhou: Arquivo de banco de dados original não encontrado em '{}'.", originalDbPath);
            return null;
        }

        String dbNameOnly = sourceFile.getName();
        String baseName = dbNameOnly.contains(".") ? dbNameOnly.substring(0, dbNameOnly.lastIndexOf('.')) : dbNameOnly;
        String extension = dbNameOnly.contains(".") ? dbNameOnly.substring(dbNameOnly.lastIndexOf('.')) : "";

        String backupFilename = String.format("%s_backup_%d%s",
                baseName,
                System.currentTimeMillis(),
                extension);

        File backupDir = sourceFile.getParentFile();
        if (backupDir == null) { // Segurança, embora improvável para um caminho absoluto
            logger.error("Não foi possível determinar o diretório de backup para '{}'.", originalDbPath);
            return null;
        }
        File backupFile = new File(backupDir, backupFilename);

        try {
            java.nio.file.Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Backup do banco de dados criado com sucesso em: {}", backupFile.getAbsolutePath());
            return backupFile.getAbsolutePath();
        } catch (IOException e) {
            logger.error("Erro ao criar backup de '{}' para '{}': {}", originalDbPath, backupFile.getAbsolutePath(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Estabelece conexão direta com o banco de dados SQLite
     *
     * @return Conexão direta com o banco
     * @throws ClassNotFoundException Se o driver SQLite não for encontrado
     * @throws SQLException Em caso de erro na conexão
     */
    public static Connection connectToDatabase() throws ClassNotFoundException, SQLException {
        // Carregar driver SQLite
        Class.forName("org.sqlite.JDBC");

        String dbPath = getDatabasePath();
        String url = "jdbc:sqlite:" + dbPath;

        logger.debug("Estabelecendo conexão direta com o banco: {}", url);

        Connection conn = DriverManager.getConnection(url);

        // Configurações básicas para a conexão direta
        if (conn != null) {
            logger.debug("Conexão direta estabelecida com sucesso");
        }

        return conn;
    }
}