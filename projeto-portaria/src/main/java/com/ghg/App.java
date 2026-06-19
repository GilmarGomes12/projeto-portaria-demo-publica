package com.ghg;

import java.io.File;
import java.io.RandomAccessFile; // Adicionado para obter o caminho do banco
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghg.data.ConexaoBanco;
import com.ghg.data.DatabaseLocator;
import com.ghg.data.ShutdownHook; // Adicionado SLF4J
import com.ghg.view.JLogin; // Adicionado SLF4J

/**
 * @author Gilmar H Gomes
 * @since 27/03/2025
 * @version 1.5 // Version incremented
 * @description Classe principal do sistema de portaria
 * @description Inicializa o sistema e abre a tela de login
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class); // Logger SLF4J
    // Flag para ativar testes de diagnóstico
    private static final boolean MODO_TESTE = false; // Recomendo false para produção normal

    public static void main(String[] args) {
        // ConAgra o look and feel do sistema
        configurarLookAndFeel();

        logger.info("Iniciando sistema de portaria...");

        // Verifica se há outros processos usando o arquivo do banco de dados
        // Isso é uma tentativa, pode não ser 100% eficaz para todos os cenários de lock do SQLite.
        verificarELiberarArquivoBancoDados();

        try {
            // Não é mais necessário chamar DatabaseInitializer.initialize();
            // A inicialização do pool e outras configurações são feitas por ConexaoBanco e DatabaseUtils.

            // Registra o hook de finalização para garantir que os recursos (ex: pool) são liberados.
            ShutdownHook.register();
            logger.info("ShutdownHook registrado.");

            // Tenta criar/verificar as tabelas com mecanismo de retry usando ScheduledExecutorService.
            int maxTentativasCriacaoTabelas = 3;
            int esperaEntreTentativasMs = 2000; // 2 segundos

            logger.info("Iniciando criação/verificação das tabelas do banco de dados...");
            
            boolean tabelasOk = criarTabelasComRetry(maxTentativasCriacaoTabelas, esperaEntreTentativasMs);

            if (!tabelasOk) {
                logger.error("Não foi possível criar/verificar as tabelas do banco de dados após {} tentativas.", maxTentativasCriacaoTabelas);
                throw new RuntimeException("Falha crítica na inicialização do banco de dados: tabelas não puderam ser criadas/verificadas.");
            }

            // Lógica de testes removida pois MODO_TESTE = false torna o código inacessível
            // Se precisar de testes no futuro, altere MODO_TESTE para true

            // Executa diagnósticos adicionais se modo teste estiver ativado
            if (MODO_TESTE) {
                logger.info("MODO TESTE ATIVADO - Executando diagnósticos adicionais...");
                executarDiagnosticosTeste();
            }

            // Comentado até a classe ser criada ou se não for mais necessário.
            // logger.info("Executando diagnóstico do banco de dados (se aplicável)...");
            // DatabaseDiagnostic.diagnoseAndFix();

        } catch (RuntimeException e) {
            logger.error("Erro de runtime durante a inicialização: {}", e.getMessage(), e);
            // Tratar erros de runtime que podem ser recuperáveis
        } catch (Exception e) {
            logger.error("Erro CRÍTICO inesperado durante a inicialização do banco de dados. A aplicação pode não funcionar corretamente.", e.getMessage(), e);
            // Captura qualquer outra exceção não prevista
        }

        logger.info("Inicialização do sistema concluída. Abrindo tela de login...");
        // Inicia a tela de login
        final JLogin loginScreen = new JLogin();

        // O ShutdownHook global já cuida do fechamento de recursos do banco.
        // O WindowListener específico para chamar DatabaseInitializer.shutdown() foi removido.
        // Se você precisar de ações específicas ao fechar ESTA janela, pode adicionar um listener,
        // mas não para o shutdown do banco de dados, que já está coberto.
        /*
        loginScreen.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Ações específicas ao fechar a janela de login, se houver.
                // NÃO precisa chamar nada relacionado ao shutdown do banco aqui.
                logger.info("Janela de login fechada pelo usuário.");
            }
        });
        */

        loginScreen.setLocationRelativeTo(null);
        loginScreen.setVisible(true);
    }

    /**
     * Executa diagnósticos adicionais quando o modo teste está ativado
     */
    private static void executarDiagnosticosTeste() {
        logger.info("=== DIAGNÓSTICOS DE TESTE ===");
        
        // Informações do sistema
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        logger.info("User Dir: {}", System.getProperty("user.dir"));
        
        // Informações do banco
        String dbPath = DatabaseLocator.getDatabasePath();
        logger.info("Database Path: {}", dbPath);
        
        if (dbPath != null) {
            File dbFile = new File(dbPath);
            logger.info("Database exists: {}", dbFile.exists());
            if (dbFile.exists()) {
                logger.info("Database size: {} bytes", dbFile.length());
                logger.info("Database readable: {}", dbFile.canRead());
                logger.info("Database writable: {}", dbFile.canWrite());
            }
        }
        
        logger.info("=== FIM DOS DIAGNÓSTICOS ===");
    }

    private static void configurarLookAndFeel() {
        try {
            // Força propriedades para modo claro antes de configurar o Look and Feel
            UIManager.put("Theme.name", "Light");
            UIManager.put("Theme.mode", "light");
            System.setProperty("sun.java2d.uiScale", "1.0");
            
            // No Linux, força o uso de GTK+ em modo claro ao invés do tema do sistema
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("linux")) {
                // Define propriedades GTK para forçar tema claro
                System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                System.setProperty("swing.gtkthemefile", "");
                // Força o tema claro no GTK
                UIManager.put("gtk.theme.name", "Adwaita");
                UIManager.put("gtk.theme.variant", "light");
            }
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(false);
            logger.debug("Look and Feel do sistema configurado para modo claro.");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.warn("Erro ao configurar look and feel: {}. Usando o padrão do Java.", e.getMessage());
        }
    }

    /**
     * Tenta verificar se o arquivo do banco de dados está bloqueado por outro processo.
     * Esta é uma verificação básica e pode não cobrir todos os tipos de locks do SQLite.
     */
    private static void verificarELiberarArquivoBancoDados() {
        String dbPath = DatabaseLocator.getDatabasePath();
        if (dbPath == null) {
            logger.warn("Não foi possível obter o caminho do banco de dados para verificação de lock.");
            return;
        }

        File dbFile = new File(dbPath);
        logger.debug("Verificando arquivo do banco de dados em: {}", dbFile.getAbsolutePath());

        if (dbFile.exists()) {
            // Assegura que o diretório pai exista, caso contrário, RandomAccessFile pode falhar
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    logger.info("Diretório pai do banco de dados criado: {}", parentDir.getAbsolutePath());
                } else {
                    logger.warn("Falha ao criar diretório pai do banco de dados: {}. A verificação de lock pode falhar.", parentDir.getAbsolutePath());
                    // Não necessariamente um erro fatal aqui, a conexão pode criar o arquivo de qualquer maneira.
                }
            }

            try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw");
                 FileChannel channel = raf.getChannel()) {

                FileLock lock = null;
                try {
                    lock = channel.tryLock(); // Tenta obter um lock exclusivo no arquivo
                    if (lock != null) {
                        logger.info("Arquivo do banco de dados '{}' não parece estar bloqueado por outro processo (conseguimos o lock).", dbFile.getName());
                    } else {
                        logger.warn("Arquivo do banco de dados '{}' PARECE estar bloqueado por outro processo (não conseguimos o lock). Tentando aguardar...", dbFile.getName());
                        // Substituir Thread.sleep por uma abordagem não bloqueante ou usar um mecanismo de retry similar
                        aguardarLiberacaoArquivo(2000);
                    }
                } finally {
                    if (lock != null && lock.isValid()) {
                        lock.release(); // Libera o lock se foi obtido
                    }
                }
            } catch (java.nio.channels.OverlappingFileLockException ofle) {
                logger.warn("Arquivo do banco de dados '{}' definitivamente bloqueado por outro processo (OverlappingFileLockException).", dbFile.getName());
                aguardarLiberacaoArquivo(2000);
            } catch (Exception e) {
                logger.warn("Aviso: Não foi possível verificar o estado de bloqueio do arquivo do banco de dados '{}': {}", dbFile.getName(), e.getMessage());
            }
        } else {
            logger.info("Arquivo do banco de dados '{}' não existe. Será criado na primeira conexão.", dbFile.getName());
        }
    }
    
    /**
     * Aguarda a liberação do arquivo usando um mecanismo não bloqueante
     */
    private static void aguardarLiberacaoArquivo(long millis) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            CountDownLatch latch = new CountDownLatch(1);
            scheduler.schedule(() -> latch.countDown(), millis, TimeUnit.MILLISECONDS);
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Espera para liberação do arquivo do banco interrompida.");
        } finally {
            scheduler.shutdown();
        }
    }

    /**
     * Cria/verifica as tabelas do banco com mecanismo de retry usando ScheduledExecutorService
     * @param maxTentativas número máximo de tentativas
     * @param esperaMs tempo de espera entre tentativas em milissegundos
     * @return true se as tabelas foram criadas com sucesso, false caso contrário
     */
    private static boolean criarTabelasComRetry(int maxTentativas, long esperaMs) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> resultado = new AtomicReference<>(false);
        AtomicReference<Exception> ultimaExcecao = new AtomicReference<>();
        
        try {
            criarTabelasComRetryInterno(scheduler, latch, resultado, ultimaExcecao, 0, maxTentativas, esperaMs);
            
            // Aguarda até que todas as tentativas sejam concluídas ou sucesso
            latch.await();
            
            if (!resultado.get() && ultimaExcecao.get() != null) {
                logger.error("Falha final na criação das tabelas", ultimaExcecao.get());
            }
            
            return resultado.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Criação de tabelas interrompida");
            return false;
        } finally {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
        }
    }
    
    /**
     * Método interno recursivo para retry das tabelas
     */
    private static void criarTabelasComRetryInterno(ScheduledExecutorService scheduler, 
                                                   CountDownLatch latch, 
                                                   AtomicReference<Boolean> resultado,
                                                   AtomicReference<Exception> ultimaExcecao,
                                                   int tentativaAtual, 
                                                   int maxTentativas, 
                                                   long esperaMs) {
        
        Runnable tarefa = () -> {
            try {
                ConexaoBanco.criarTabelas();
                resultado.set(true);
                logger.info("Tabelas do banco de dados criadas/verificadas com sucesso na tentativa {}.", tentativaAtual + 1);
                latch.countDown();
            } catch (Exception e) {
                ultimaExcecao.set(e);
                logger.warn("Falha na tentativa {} de criar/verificar tabelas: {}", tentativaAtual + 1, e.getMessage());
                
                if (tentativaAtual + 1 < maxTentativas) {
                    logger.info("Agendando próxima tentativa em {}ms...", esperaMs);
                    criarTabelasComRetryInterno(scheduler, latch, resultado, ultimaExcecao, 
                                              tentativaAtual + 1, maxTentativas, esperaMs);
                } else {
                    logger.error("Esgotadas todas as {} tentativas de criar/verificar tabelas.", maxTentativas);
                    latch.countDown();
                }
            }
        };
        
        if (tentativaAtual == 0) {
            // Primeira tentativa - executa imediatamente
            scheduler.execute(tarefa);
        } else {
            // Tentativas subsequentes - agenda com delay
            scheduler.schedule(tarefa, esperaMs, TimeUnit.MILLISECONDS);
        }
    }
}