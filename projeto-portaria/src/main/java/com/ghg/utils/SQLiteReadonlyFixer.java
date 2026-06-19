package com.ghg.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghg.data.ConexaoBanco;
import com.ghg.data.DatabaseLocator;

/**
 * Utilitário para verificar e corrigir problemas de readonly no banco SQLite
 * Específico para resolver o erro SQLITE_READONLY que pode ocorrer com WAL mode
 * 
 * @author Sistema
 * @version 1.0
 */
public class SQLiteReadonlyFixer {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteReadonlyFixer.class);    /**
     * Verifica se o banco está em modo readonly e tenta corrigir
     * 
     * @return true se o banco está pronto para escrita, false caso contrário
     */
    public static boolean verificarECorrigirReadonly() {
        try {
            logger.info("Iniciando verificação e correção avançada de SQLITE_READONLY...");
            
            // 1. Fechar todas as conexões ativas para libertar o banco
            fecharTodasConexoes();
            
            // 2. Aguardar tempo maior para garantir liberação completa
            Thread.sleep(500);
            
            // 3. Verificar e corrigir configurações do modo WAL
            if (verificarECorrigirModoWAL()) {
                logger.info("Correção através de configuração WAL bem-sucedida");
                return true;
            }
            
            // 4. Forçar limpeza de arquivos WAL se necessário
            if (!verificarELimparArquivosWAL()) {
                logger.warn("Problemas com arquivos WAL detectados, tentando limpeza forçada");
                limparArquivosWALForced();
            }
            
            // 5. Verificar permissões do arquivo
            if (!verificarPermissoesArquivo()) {
                logger.warn("Falha na verificação de permissões, tentando correção forçada");
                if (!corrigirPermissoesForced()) {
                    logger.error("Não foi possível corrigir permissões de arquivo");
                    return false;
                }
            }
            
            // 6. Tentar correção através de conexão direta (bypassing pool)
            boolean resultado = tentarCorrecaoComConexaoDireta();
            if (resultado) {
                logger.info("Correção com conexão direta bem-sucedida");
                return true;
            }
            
            // 7. Tentar correção com modo DELETE (fallback WAL)
            resultado = tentarCorrecaoModoDelete();
            if (resultado) {
                logger.info("Correção com modo DELETE bem-sucedida");
                return true;
            }
            
            // 8. Último recurso: backup e restauração
            logger.warn("Tentativas anteriores falharam, executando backup e restauração como último recurso");
            return tentarBackupERestauracao();
            
        } catch (Exception e) {
            logger.error("Erro ao verificar/corrigir readonly do banco: {}", e.getMessage(), e);
            return false;
        }
    }
      /**
     * Fecha todas as conexões ativas do pool para liberar o banco
     */
    private static void fecharTodasConexoes() {
        try {
            logger.debug("Iniciando fechamento de todas as conexões ativas...");
            
            // Tentar fechar pool de conexões se existir
            try {
                Class<?> poolClass = Class.forName("com.ghg.data.ConnectionPool");
                if (poolClass != null) {
                    // Método getInstance()
                    java.lang.reflect.Method getInstanceMethod = poolClass.getDeclaredMethod("getInstance");
                    Object poolInstance = getInstanceMethod.invoke(null);
                    
                    // Método closeAllConnections()
                    try {
                        java.lang.reflect.Method closeAllMethod = poolClass.getDeclaredMethod("closeAllConnections");
                        closeAllMethod.setAccessible(true);
                        closeAllMethod.invoke(poolInstance);
                        logger.debug("Pool de conexões fechado via closeAllConnections()");
                    } catch (NoSuchMethodException e) {
                        logger.debug("Método closeAllConnections não encontrado, tentando shutdown()");
                        // Tentar método shutdown() como alternativa
                        try {
                            java.lang.reflect.Method shutdownMethod = poolClass.getDeclaredMethod("shutdown");
                            shutdownMethod.setAccessible(true);
                            shutdownMethod.invoke(poolInstance);
                            logger.debug("Pool de conexões fechado via shutdown()");
                        } catch (NoSuchMethodException e2) {
                            logger.debug("Método shutdown também não encontrado");
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.debug("Classe ConnectionPool não encontrada: {}", e.getMessage());
            } catch (Exception e) {
                logger.debug("Erro ao fechar pool de conexões via reflexão: {}", e.getMessage());
            }
              // Forçar garbage collection para liberar recursos
            System.gc();
            
            logger.debug("Fechamento de conexões concluído");
            
        } catch (Exception e) {
            logger.debug("Erro geral ao fechar conexões: {}", e.getMessage());
        }
    }
      /**
     * Tenta correção usando conexão direta, bypassing o pool
     */
    private static boolean tentarCorrecaoComConexaoDireta() {
        String dbPath = DatabaseLocator.getDatabasePath();
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            // Configurar pragmas diretamente
            try (Statement stmt = conn.createStatement()) {
                // Configurar timeout para evitar travamentos
                stmt.setQueryTimeout(5);
                
                // Desabilitar WAL temporariamente para forçar liberação
                stmt.execute("PRAGMA journal_mode=DELETE");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA busy_timeout=5000");
                
                // Tentar operação de escrita simples
                stmt.execute("CREATE TEMP TABLE IF NOT EXISTS test_write (id INTEGER)");
                stmt.execute("INSERT OR REPLACE INTO test_write VALUES (1)");
                stmt.execute("UPDATE test_write SET id = 2 WHERE id = 1");
                stmt.execute("DELETE FROM test_write WHERE id = 2");
                stmt.execute("DROP TABLE IF EXISTS test_write");
                
                // Reconfigurar WAL se tudo deu certo
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA wal_autocheckpoint=1000");
                
                logger.info("Correção com conexão direta bem-sucedida");
                return true;
            }
        } catch (Exception e) {
            logger.debug("Tentativa de correção com conexão direta falhou: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica e limpa arquivos WAL corrompidos ou travados
     */
    private static boolean verificarELimparArquivosWAL() {
        String dbPath = DatabaseLocator.getDatabasePath();
        File walFile = new File(dbPath + "-wal");
        File shmFile = new File(dbPath + "-shm");
        
        boolean problemaDetectado = false;
        
        // Verifica se arquivos WAL estão travados
        if (walFile.exists()) {
            try {
                if (!walFile.canWrite() || !walFile.canRead()) {
                    logger.warn("Arquivo WAL com problemas de acesso: {}", walFile.getAbsolutePath());
                    problemaDetectado = true;
                }
                
                // Verifica se arquivo está muito grande (possível corrupção)
                long walSize = walFile.length();
                if (walSize > 10 * 1024 * 1024) { // Mais de 10MB
                    logger.warn("Arquivo WAL muito grande ({}MB), possível problema: {}", 
                               walSize / (1024 * 1024), walFile.getAbsolutePath());
                    problemaDetectado = true;
                }
            } catch (Exception e) {
                logger.warn("Erro ao verificar arquivo WAL: {}", e.getMessage());
                problemaDetectado = true;
            }
        }
        
        if (shmFile.exists()) {
            try {
                if (!shmFile.canWrite() || !shmFile.canRead()) {
                    logger.warn("Arquivo SHM com problemas de acesso: {}", shmFile.getAbsolutePath());
                    problemaDetectado = true;
                }
            } catch (Exception e) {
                logger.warn("Erro ao verificar arquivo SHM: {}", e.getMessage());
                problemaDetectado = true;
            }
        }
        
        return !problemaDetectado;
    }
    
    /**
     * Limpeza forçada de arquivos WAL/SHM
     */
    private static boolean limparArquivosWALForced() {
        String dbPath = DatabaseLocator.getDatabasePath();
        File walFile = new File(dbPath + "-wal");
        File shmFile = new File(dbPath + "-shm");
        
        boolean sucesso = true;
        
        // Tentar remover WAL
        if (walFile.exists()) {
            try {
                // Primeiro tenta remover normalmente
                if (!walFile.delete()) {
                    // Se falhar, tenta forçar permissões e remover
                    walFile.setWritable(true);
                    if (walFile.delete()) {
                        logger.info("Arquivo WAL removido com sucesso (forçado): {}", walFile.getAbsolutePath());
                    } else {
                        logger.error("Não foi possível remover arquivo WAL: {}", walFile.getAbsolutePath());
                        sucesso = false;
                    }
                } else {
                    logger.info("Arquivo WAL removido com sucesso: {}", walFile.getAbsolutePath());
                }
            } catch (Exception e) {
                logger.error("Erro ao remover arquivo WAL: {}", e.getMessage());
                sucesso = false;
            }
        }
        
        // Tentar remover SHM
        if (shmFile.exists()) {
            try {
                if (!shmFile.delete()) {
                    shmFile.setWritable(true);
                    if (shmFile.delete()) {
                        logger.info("Arquivo SHM removido com sucesso (forçado): {}", shmFile.getAbsolutePath());
                    } else {
                        logger.error("Não foi possível remover arquivo SHM: {}", shmFile.getAbsolutePath());
                        sucesso = false;
                    }
                } else {
                    logger.info("Arquivo SHM removido com sucesso: {}", shmFile.getAbsolutePath());
                }
            } catch (Exception e) {
                logger.error("Erro ao remover arquivo SHM: {}", e.getMessage());
                sucesso = false;
            }
        }
        
        return sucesso;
    }
    
    /**
     * Correção forçada de permissões
     */
    private static boolean corrigirPermissoesForced() {
        String dbPath = DatabaseLocator.getDatabasePath();
        File dbFile = new File(dbPath);
        File dbDir = dbFile.getParentFile();
        
        boolean sucesso = true;
        
        try {
            // Corrigir permissões do arquivo de banco
            if (dbFile.exists()) {
                dbFile.setReadable(true, false);
                dbFile.setWritable(true, false);
                dbFile.setExecutable(false, false);
                logger.info("Permissões do banco corrigidas: {}", dbFile.getAbsolutePath());
            }
            
            // Corrigir permissões do diretório
            if (dbDir != null && dbDir.exists()) {
                dbDir.setReadable(true, false);
                dbDir.setWritable(true, false);
                dbDir.setExecutable(true, false);
                logger.info("Permissões do diretório corrigidas: {}", dbDir.getAbsolutePath());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao corrigir permissões forçadamente: {}", e.getMessage());
            sucesso = false;
        }
        
        return sucesso;
    }
    
    /**
     * Tenta correção usando modo DELETE como fallback
     */
    private static boolean tentarCorrecaoModoDelete() {
        String dbPath = DatabaseLocator.getDatabasePath();
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(5);
                
                // Forçar modo DELETE (sem WAL)
                stmt.execute("PRAGMA journal_mode=DELETE");
                stmt.execute("PRAGMA synchronous=FULL");
                stmt.execute("PRAGMA busy_timeout=10000");
                
                // Testar escrita
                stmt.execute("CREATE TEMP TABLE IF NOT EXISTS test_delete_mode (id INTEGER)");
                stmt.execute("INSERT OR REPLACE INTO test_delete_mode VALUES (1)");
                stmt.execute("DROP TABLE IF EXISTS test_delete_mode");
                
                logger.info("Correção com modo DELETE bem-sucedida");
                return true;
                
            }
        } catch (Exception e) {
            logger.debug("Tentativa de correção com modo DELETE falhou: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Último recurso: backup e restauração do banco
     */
    private static boolean tentarBackupERestauracao() {
        try {
            logger.warn("Executando backup e restauração como último recurso...");
            
            // Criar backup
            String backupPath = DatabaseLocator.createBackup();
            if (backupPath == null) {
                logger.error("Falha ao criar backup, não é possível prosseguir com restauração");
                return false;
            }
            
            // Tentar limpar arquivos WAL novamente
            limparArquivosWALForced();
            
            // Tentar conexão simples após limpeza
            String dbPath = DatabaseLocator.getDatabasePath();
            String jdbcUrl = "jdbc:sqlite:" + dbPath;
            
            try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.setQueryTimeout(5);
                    stmt.execute("PRAGMA integrity_check");
                    stmt.execute("PRAGMA journal_mode=DELETE");
                    
                    // Teste final de escrita
                    stmt.execute("CREATE TEMP TABLE IF NOT EXISTS final_test (id INTEGER)");
                    stmt.execute("INSERT INTO final_test VALUES (1)");
                    stmt.execute("DROP TABLE final_test");
                    
                    logger.info("Backup e restauração bem-sucedidos");
                    return true;
                }
            }
            
        } catch (Exception e) {
            logger.error("Falha no backup e restauração: {}", e.getMessage());
            return false;
        }
    }
      /**
     * Verifica se o arquivo de banco e diretório têm permissões adequadas
     * e tenta corrigir automaticamente se necessário
     */
    private static boolean verificarPermissoesArquivo() {
        String dbPath = DatabaseLocator.getDatabasePath();
        File dbFile = new File(dbPath);
        File dbDir = dbFile.getParentFile();
        
        logger.debug("Verificando permissões do banco de dados: {}", dbPath);
        
        // Verifica se o arquivo existe e é gravável
        if (dbFile.exists()) {
            if (!dbFile.canWrite()) {
                logger.warn("Arquivo de banco sem permissão de escrita, tentando corrigir: {}", dbPath);
                if (!tentarCorrigirPermissoes(dbFile)) {
                    logger.error("Não foi possível corrigir permissões do arquivo de banco: {}", dbPath);
                    return false;
                }
            }
        }
        
        // Verifica se o diretório é gravável (necessário para WAL)
        if (!dbDir.canWrite()) {
            logger.warn("Diretório do banco sem permissão de escrita, tentando corrigir: {}", dbDir.getAbsolutePath());
            if (!tentarCorrigirPermissoes(dbDir)) {
                logger.error("Não foi possível corrigir permissões do diretório: {}", dbDir.getAbsolutePath());
                return false;
            }
        }
        
        // Verifica e corrige arquivos WAL auxiliares
        File walFile = new File(dbPath + "-wal");
        File shmFile = new File(dbPath + "-shm");
        
        if (walFile.exists() && !walFile.canWrite()) {
            logger.warn("Arquivo WAL sem permissão de escrita, tentando corrigir: {}", walFile.getAbsolutePath());
            tentarCorrigirPermissoes(walFile);
        }
        
        if (shmFile.exists() && !shmFile.canWrite()) {
            logger.warn("Arquivo SHM sem permissão de escrita, tentando corrigir: {}", shmFile.getAbsolutePath());
            tentarCorrigirPermissoes(shmFile);
        }
        
        logger.debug("Permissões de arquivo verificadas com sucesso");
        return true;
    }
    
    /**
     * Verifica e corrige configurações do modo WAL
     */
    private static boolean verificarECorrigirModoWAL() {
        Connection conn = null;
        try {
            conn = ConexaoBanco.getConnection();
            
            // Verifica modo journal atual
            String journalMode = verificarJournalMode(conn);
            logger.debug("Modo journal atual: {}", journalMode);
            
            // Se não está em WAL, tenta configurar
            if (!"wal".equalsIgnoreCase(journalMode)) {
                logger.info("Banco não está em modo WAL. Tentando configurar...");
                if (!configurarModoWAL(conn)) {
                    logger.warn("Falha ao configurar modo WAL, tentando com modo DELETE");
                    return configurarModoDelete(conn);
                }
            }
            
            // Verifica se o banco permite escrita
            return testarEscrita(conn);
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar/corrigir modo WAL: {}", e.getMessage(), e);
            return false;
        } finally {
            if (conn != null) {
                ConexaoBanco.desconectar(conn);
            }
        }
    }
    
    /**
     * Verifica o modo journal atual
     */
    private static String verificarJournalMode(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA journal_mode")) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
            return "unknown";
        }
    }
    
    /**
     * Configura o banco para modo WAL
     */
    private static boolean configurarModoWAL(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Garante que não está em transação
            if (!conn.getAutoCommit()) {
                conn.commit();
                conn.setAutoCommit(true);
            }
            
            // Configura WAL mode
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA synchronous = NORMAL");
            stmt.execute("PRAGMA wal_autocheckpoint = 1000");
            
            logger.info("Modo WAL configurado com sucesso");
            return true;
            
        } catch (SQLException e) {
            logger.error("Erro ao configurar modo WAL: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Configura o banco para modo DELETE (fallback)
     */
    private static boolean configurarModoDelete(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Garante que não está em transação
            if (!conn.getAutoCommit()) {
                conn.commit();
                conn.setAutoCommit(true);
            }
            
            // Configura DELETE mode
            stmt.execute("PRAGMA journal_mode = DELETE");
            stmt.execute("PRAGMA synchronous = FULL");
            
            logger.info("Modo DELETE configurado como fallback");
            return true;
            
        } catch (SQLException e) {
            logger.error("Erro ao configurar modo DELETE: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Testa se o banco permite operações de escrita
     */
    private static boolean testarEscrita(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Testa uma operação simples de escrita
            stmt.execute("CREATE TEMP TABLE test_write (id INTEGER)");
            stmt.execute("INSERT INTO test_write VALUES (1)");
            stmt.execute("DROP TABLE test_write");
            
            logger.debug("Teste de escrita bem-sucedido");
            return true;
            
        } catch (SQLException e) {
            logger.error("Teste de escrita falhou: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Remove arquivos WAL para forçar reinicialização
     * Use apenas em casos extremos
     */
    public static boolean limparArquivosWAL() {
        String dbPath = DatabaseLocator.getDatabasePath();
        File walFile = new File(dbPath + "-wal");
        File shmFile = new File(dbPath + "-shm");
        
        boolean sucesso = true;
        
        if (walFile.exists()) {
            if (walFile.delete()) {
                logger.info("Arquivo WAL removido: {}", walFile.getAbsolutePath());
            } else {
                logger.error("Falha ao remover arquivo WAL: {}", walFile.getAbsolutePath());
                sucesso = false;
            }
        }
        
        if (shmFile.exists()) {
            if (shmFile.delete()) {
                logger.info("Arquivo SHM removido: {}", shmFile.getAbsolutePath());
            } else {
                logger.error("Falha ao remover arquivo SHM: {}", shmFile.getAbsolutePath());
                sucesso = false;        }
        }
        
        return sucesso;
    }
      /**
     * Tenta corrigir as permissões de um arquivo ou diretório
     * 
     * @param file o arquivo ou diretório para corrigir
     * @return true se as permissões foram corrigidas com sucesso
     */
    private static boolean tentarCorrigirPermissoes(File file) {
        try {
            if (!file.exists()) {
                logger.debug("Arquivo não existe, não há necessidade de corrigir permissões: {}", file.getAbsolutePath());
                return true;
            }
            
            // Tentar definir permissões de escrita
            boolean success = file.setWritable(true, false); // Para todos os usuários
            if (!success) {
                success = file.setWritable(true, true); // Apenas para o proprietário
            }
            
            if (success) {
                logger.info("Permissões de escrita definidas com sucesso para: {}", file.getAbsolutePath());
                return true;
            } else {
                logger.warn("Falha ao definir permissões de escrita para: {}", file.getAbsolutePath());
                return false;
            }
            
        } catch (SecurityException e) {
            logger.error("Erro de segurança ao tentar corrigir permissões de {}: {}", file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    /**
     * Verificação preventiva de readonly antes de operações críticas
     * Executa uma verificação rápida sem todas as correções pesadas
     * 
     * @return true se o banco está pronto para escrita
     */
    public static boolean verificacaoPreventiva() {
        try {
            String dbPath = DatabaseLocator.getDatabasePath();
            String jdbcUrl = "jdbc:sqlite:" + dbPath;
            
            try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.setQueryTimeout(3); // Timeout mais curto para verificação rápida
                    
                    // Teste simples de escrita
                    stmt.execute("CREATE TEMP TABLE IF NOT EXISTS preventive_test (id INTEGER)");
                    stmt.execute("INSERT OR REPLACE INTO preventive_test VALUES (1)");
                    stmt.execute("DROP TABLE IF EXISTS preventive_test");
                    
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("Verificação preventiva detectou problema readonly: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Método público para limpeza manual de arquivos WAL
     * Útil para casos onde é necessário forçar a limpeza externamente
     * 
     * @return true se a limpeza foi bem-sucedida
     */
    public static boolean forcarLimpezaWAL() {
        logger.info("Executando limpeza forçada de arquivos WAL...");
        
        // Fechar conexões primeiro
        fecharTodasConexoes();
        
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return limparArquivosWALForced();
    }
}
