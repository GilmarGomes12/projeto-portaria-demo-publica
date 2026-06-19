package com.ghg.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghg.utils.PasswordUtils;

/**
 * @author Gilmar H Gomes
 * @since 27/03/2025
 * @version 1.5 // Version incremented
 * @description Classe de conexão com o banco de dados SQLite
 * @description Cria as tabelas necessárias para o funcionamento do sistema
 */
public class ConexaoBanco {
    private static final Logger logger = LoggerFactory.getLogger(ConexaoBanco.class);
    private static final String NOME_ARQUIVO_BANCO = "condominio.db"; // Usar constante
    //  Considerar externalizar NOME_ARQUIVO_BANCO para um arquivo de configuração.

    /**
     * Estabelece conexão com o banco de dados SQLite usando o pool de conexões
     *
     * @return Conexão com o banco
     * @throws SQLException           Em caso de erro na conexão
     */
    public static Connection conectar() throws SQLException {
        try {
            Connection conn = ConnectionPool.getInstance().getConnection();
            logger.debug("Conexão obtida do pool.");

            if (logger.isTraceEnabled()) {
                String dbPath = DatabaseLocator.getDatabasePath(); // Pode ser útil para confirmar
                logger.trace("Usando banco de dados em: {}", dbPath);
            }
            return conn;
        } catch (SQLException e) {
            logger.error("Erro ao obter conexão do pool: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Libera a conexão de volta para o pool
     *
     * @param conn A conexão a ser liberada
     */
    public static void desconectar(Connection conn) {
        ConnectionPool.getInstance().releaseConnection(conn);
    }

    /**
     * Método para obter conexão com o banco de dados.
     * Utiliza o pool de conexões para gerenciamento eficiente.
     *
     * @return Conexão com o banco de dados
     * @throws SQLException Em caso de erro na conexão
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = ConnectionPool.getInstance().getConnection();
            logger.debug("Conexão obtida do pool via getConnection().");

            if (logger.isTraceEnabled()) {
                String dbPath = DatabaseLocator.getDatabasePath();
                logger.trace("Usando banco de dados em: {}", dbPath);
            }
            return conn;
        } catch (SQLException e) {
            logger.error("Erro ao obter conexão do pool via getConnection(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Verifica e atualiza a estrutura da tabela morador.
     * Adiciona a coluna 'data_cadastro' se não existir, sem perder dados.
     *
     * @param conn Conexão com o banco de dados (geralmente uma conexão direta durante a inicialização)
     * @throws SQLException Em caso de erro na operação
     */
    private static void verificarEAtualizarTabelaMorador(Connection conn) throws SQLException {
        String tableName = "morador";
        String columnNameToCheck = "data_cadastro";

        try (Statement stmt = conn.createStatement()) {
            // Verifica se a tabela existe
            try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
                if (!rs.next()) {
                    logger.info("Tabela {} não existe. Criando...", tableName);
                    criarTabelaMorador(conn); // Cria a tabela com a estrutura completa
                    return; // Já foi criada, não precisa verificar coluna
                }
            }

            // Verifica se a coluna data_cadastro existe
            boolean columnExists = false;
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
                while (rs.next()) {
                    if (columnNameToCheck.equalsIgnoreCase(rs.getString("name"))) {
                        columnExists = true;
                        break;
                    }
                }
            }

            if (!columnExists) {
                logger.info("Coluna '{}' não encontrada na tabela {}. Adicionando...", columnNameToCheck, tableName);
                // Adiciona a coluna com um valor padrão para registros existentes e NOT NULL para novos
                // Usar (date('now')) para SQLite ou CURRENT_DATE se suportado diretamente na versão.
                // O formato 'YYYY-MM-DD' é o padrão para DATE no SQLite.
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnNameToCheck + " DATE NOT NULL DEFAULT (date('now'))");
                logger.info("Coluna '{}' adicionada com sucesso à tabela {}.", columnNameToCheck, tableName);
            } else {
                logger.debug("Coluna '{}' já existe na tabela {}.", columnNameToCheck, tableName);
            }
        }
    }

    /**
     * Cria a tabela morador se ela não existir.
     *
     * @param conn Conexão com o banco de dados (geralmente uma conexão direta durante a inicialização)
     * @throws SQLException Em caso de erro na operação
     */
    private static void criarTabelaMorador(Connection conn) throws SQLException {
        String sqlMorador = "CREATE TABLE IF NOT EXISTS morador (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bloco VARCHAR(20) NOT NULL," +
                "apartamento VARCHAR(20) NOT NULL," +
                "proprietario BOOLEAN DEFAULT FALSE," +
                "locatario BOOLEAN DEFAULT FALSE," +
                "nome_principal VARCHAR(255) NOT NULL," +
                "data_nasc_principal DATE," +
                "fone_residencial VARCHAR(20)," +
                "fone_comercial_principal VARCHAR(20)," +
                "celular_principal VARCHAR(20)," +
                "email_principal VARCHAR(255)," +
                "profissao_principal VARCHAR(100)," +
                "imobiliaria VARCHAR(150)," +
                "data_cadastro DATE NOT NULL DEFAULT (date('now'))," + // Adicionado DEFAULT
                "nome_conjuge VARCHAR(255)," +
                "data_nasc_conjuge DATE," +
                "celular_conjuge VARCHAR(20)," +
                "fone_comercial_conjuge VARCHAR(20)," +
                "email_conjuge VARCHAR(255)," +
                "nome_ocupante1 VARCHAR(255)," +
                "parentesco_ocupante1 VARCHAR(50)," +
                "data_nasc_ocupante1 DATE," +
                "celular_ocupante1 VARCHAR(20)," +
                "nome_ocupante2 VARCHAR(255)," +
                "parentesco_ocupante2 VARCHAR(50)," +
                "data_nasc_ocupante2 DATE," +
                "celular_ocupante2 VARCHAR(20)," +
                "nome_ocupante3 VARCHAR(255)," +
                "parentesco_ocupante3 VARCHAR(50)," +
                "data_nasc_ocupante3 DATE," +
                "celular_ocupante3 VARCHAR(20)," +
                "marca_veiculo1 VARCHAR(50)," +
                "modelo_veiculo1 VARCHAR(50)," +
                "cor_veiculo1 VARCHAR(30)," +
                "placa_veiculo1 VARCHAR(10)," +
                "marca_veiculo2 VARCHAR(50)," +
                "modelo_veiculo2 VARCHAR(50)," +
                "cor_veiculo2 VARCHAR(30)," +
                "placa_veiculo2 VARCHAR(10)," +
                "tem_animais BOOLEAN DEFAULT FALSE," +
                "quantos_animais INT," +
                "quais_animais VARCHAR(255)," +
                "nome_contato_emergencia VARCHAR(255)," +
                "celular_contato_emergencia VARCHAR(20)," +
                "observacoes TEXT" +
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlMorador);
            logger.info("Tabela 'morador' verificada/criada com sucesso.");
        } catch (SQLException e) {
            logger.error("Erro ao criar/verificar tabela 'morador': {}", e.getMessage(), e);
            throw e; // Propaga a exceção
        }
    }

    /**
     * Cria todas as tabelas necessárias para o funcionamento do sistema.
     * Usa uma conexão direta para configuração inicial e PRAGMAs persistentes.
     */
    public static void criarTabelas() {
        // Usamos synchronized para garantir que apenas um thread por vez execute a criação de tabelas
        //  Para gerenciamento avançado de schema e migrações, considerar o uso de ferramentas como Flyway ou Liquibase.
        synchronized (ConexaoBanco.class) {
            Connection conn = null;
            try {
                // Obter conexão direta para configuração inicial do banco e DDL
                // Esta conexão NÃO vem do pool e é gerenciada localmente.
                logger.info("Iniciando criação/verificação de tabelas com conexão direta...");
                conn = DatabaseLocator.connectToDatabase(); // Pode lançar ClassNotFoundException e SQLException

                // Configurar PRAGMAs persistentes importantes (como WAL) nesta conexão direta.
                // Essas configurações afetam o arquivo do banco de dados.
                logger.info("Configurando PRAGMAs persistentes (ex: WAL mode)...");
                try (Statement stmt = conn.createStatement()) {
                    // Tenta ativar o WAL mode. É persistente no banco.
                    stmt.execute("PRAGMA journal_mode = WAL;");
                    // Configura o nível de sincronização. NORMAL é um bom equilíbrio para WAL.
                    stmt.execute("PRAGMA synchronous = NORMAL;");
                    logger.info("PRAGMA journal_mode=WAL e synchronous=NORMAL configurados (ou já estavam).");
                } catch (SQLException e) {
                    // Não é fatal se não conseguir definir (pode já estar definido ou não ser suportado em algum contexto raro)
                    logger.warn("Aviso: Não foi possível definir PRAGMAs persistentes (journal_mode, synchronous): {}. O sistema continuará.", e.getMessage());
                }

                // Garante que está com autocommit habilitado para DDLs individuais
                conn.setAutoCommit(true);

                logger.info("Verificando e atualizando tabelas...");

                // Tabela Perfil Visitante - CORRIGIDA para estrutura correta
                String sqlPerfilVisitante = "CREATE TABLE IF NOT EXISTS perfil_visitante (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "codigo TEXT NOT NULL UNIQUE, " +
                        "nome TEXT NOT NULL, " +
                        "rg TEXT, " +
                        "telefone TEXT" +
                        ")";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlPerfilVisitante);
                    logger.info("Tabela 'perfil_visitante' verificada/criada com sucesso.");
                }

                // Verificar e criar/atualizar cada tabela
                verificarEAtualizarTabelaMorador(conn); // Usa a nova lógica com ALTER TABLE
                // A tabela 'visitantes' agora será criada/atualizada pela sua própria função dedicada
                // que já inclui a lógica de verificação e adição de colunas, incluindo perfil_id.
                // A definição SQL de 'visitantes' dentro deste método será removida.
                verificarEAtualizarTabelaVisitantes(conn);


                // Tabela de apartamentos
                String sqlApartamentos = "CREATE TABLE IF NOT EXISTS apartamentos (" +
                        "bloco VARCHAR(20) NOT NULL," +
                        "numero VARCHAR(20) NOT NULL," +
                        "PRIMARY KEY (bloco, numero)" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlApartamentos);
                    logger.info("Tabela 'apartamentos' verificada com sucesso.");
                }

                // Tabela Usuários
                String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome_completo VARCHAR(255) NOT NULL," +
                        "login VARCHAR(50) NOT NULL UNIQUE," +
                        "senha VARCHAR(255) NOT NULL," +
                        "nivel_acesso INTEGER NOT NULL," +
                        "email VARCHAR(255)," +
                        "token_recuperacao VARCHAR(255)," +
                        "data_expiracao_token TIMESTAMP" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlUsuarios);
                    logger.info("Tabela 'usuarios' verificada com sucesso.");

                    verificarEAtualizarTabelaUsuarios(conn); // Verifica e adiciona colunas se necessário

                    // Verifica se já existe um usuário administrador
                    try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios WHERE login = 'admin'")) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            String rawPassword = java.util.UUID.randomUUID().toString().substring(0, 12);
                            String senhaHash = PasswordUtils.hashPassword(rawPassword);
                            String insertAdmin = "INSERT INTO usuarios (nome_completo, login, senha, nivel_acesso) VALUES ('Administrador', 'admin', ?, 1)";
                            try (PreparedStatement pstmt = conn.prepareStatement(insertAdmin)) {
                                pstmt.setString(1, senhaHash);
                                pstmt.executeUpdate();
                                logger.info("Usuário administrador padrão criado com sucesso. Senha gerada: {}", rawPassword);
                                
                                // Exibição melhorada no terminal
                                System.out.println("\n" + "=".repeat(80));
                                System.out.println("🔐 CREDENCIAIS DE PRIMEIRO ACESSO - SISTEMA DE PORTARIA 🔐");
                                System.out.println("=".repeat(80));
                                System.out.println("   USUÁRIO: admin");
                                System.out.println("   SENHA:   " + rawPassword);
                                System.out.println("=".repeat(80));
                                System.out.println("⚠️  IMPORTANTE: Anote esta senha agora! Ela não será exibida novamente.");
                                System.out.println("📄 Esta informação também foi salva no arquivo: senha_inicial.txt");
                                System.out.println("=".repeat(80) + "\n");
                                
                                // Salvar senha em arquivo para consulta posterior
                                salvarSenhaInicialEmArquivo(rawPassword);
                            }
                        }
                    }
                }

                // Tabela Agendamentos
                String sqlAgendamentos = "CREATE TABLE IF NOT EXISTS agendamentos (" +
                        "codigo VARCHAR(10) PRIMARY KEY," +
                        "nome_morador VARCHAR(255) NOT NULL," +
                        "bloco VARCHAR(10)," +
                        "apartamento VARCHAR(10)," +
                        "data_cadastro TEXT," + // Alterado para TEXT, formato ISO8601 recomendado
                        "data_reserva TEXT NOT NULL," + // Alterado para TEXT, formato ISO8601 recomendado
                        "local_reserva VARCHAR(100) NOT NULL," +
                        "periodo VARCHAR(20) NOT NULL," +
                        "observacoes TEXT," +
                        "telefone_contato VARCHAR(20)," + // Novo campo v1.6.0
                        "email_contato VARCHAR(255)" +    // Novo campo v1.6.0
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlAgendamentos);
                    logger.info("Tabela 'agendamentos' verificada com sucesso.");
                    
                    // Verificar e adicionar colunas novas se não existirem (migração v1.6.0)
                    verificarEAtualizarTabelaAgendamentos(conn);
                }

                // Tabela Encomendas
                String sqlEncomendas = "CREATE TABLE IF NOT EXISTS encomendas (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "protocolo TEXT NOT NULL UNIQUE," +
                        "data_hora_recebimento TEXT NOT NULL," + // Formato ISO8601 recomendado
                        "nome_destinatario TEXT NOT NULL," +
                        "bloco VARCHAR(20) NOT NULL," +
                        "apartamento VARCHAR(20) NOT NULL," +
                        "tipo_encomenda TEXT NOT NULL," +
                        "codigo_rastreio TEXT," +
                        "empresa_entrega TEXT," +
                        "nome_entregador TEXT," +
                        "rg TEXT," +
                        "nome_porteiro TEXT NOT NULL," +
                        "quem_retirou TEXT," +
                        "data_hora_retirada TEXT," + // Formato ISO8601 recomendado
                        "telefone TEXT," +
                        "observacoes TEXT," +
                        "morador_id INTEGER NULL," +
                        "FOREIGN KEY(morador_id) REFERENCES morador(id) ON DELETE SET NULL" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlEncomendas);
                    logger.info("Tabela 'encomendas' verificada com sucesso.");
                }

                // Tabela Ocorrências
                String sqlOcorrencias = "CREATE TABLE IF NOT EXISTS ocorrencias (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "numero_protocolo TEXT UNIQUE NOT NULL," +
                        "data_hora_recebimento TEXT NOT NULL," + // Formato ISO8601 recomendado
                        "nome_funcionario TEXT NOT NULL," +
                        "descricao TEXT NOT NULL," +
                        "status VARCHAR(20) NOT NULL" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlOcorrencias);
                    logger.info("Tabela 'ocorrencias' verificada com sucesso.");
                }

                // Tabela Anexos de Ocorrências (v1.7.0)
                String sqlAnexosOcorrencias = "CREATE TABLE IF NOT EXISTS anexos_ocorrencias (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "protocolo_ocorrencia TEXT NOT NULL," +
                        "nome_arquivo VARCHAR(255) NOT NULL," +
                        "tipo_mime VARCHAR(50) NOT NULL," +
                        "imagem_blob BLOB NOT NULL," +
                        "tamanho_bytes INTEGER NOT NULL," +
                        "data_upload TEXT NOT NULL," + // Formato ISO8601
                        "descricao_anexo TEXT," +
                        "ordem_exibicao INTEGER DEFAULT 1," +
                        "FOREIGN KEY (protocolo_ocorrencia) REFERENCES ocorrencias(numero_protocolo) ON DELETE CASCADE" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlAnexosOcorrencias);
                    logger.info("Tabela 'anexos_ocorrencias' verificada com sucesso.");
                }

                // Índices para anexos_ocorrencias (v1.7.0)
                String sqlIndexProtocolo = "CREATE INDEX IF NOT EXISTS idx_anexos_protocolo " +
                        "ON anexos_ocorrencias(protocolo_ocorrencia);";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlIndexProtocolo);
                    logger.info("Índice 'idx_anexos_protocolo' verificado com sucesso.");
                }

                String sqlIndexOrdem = "CREATE INDEX IF NOT EXISTS idx_anexos_ordem " +
                        "ON anexos_ocorrencias(protocolo_ocorrencia, ordem_exibicao);";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlIndexOrdem);
                    logger.info("Índice 'idx_anexos_ordem' verificado com sucesso.");
                }

                // Tabela Prestadores
                String sqlPrestadores = "CREATE TABLE IF NOT EXISTS prestadores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "perfil_prestador_id INTEGER," + // NOVO v1.6.0
                        "nome_empresa TEXT," +
                        "nome_prestador TEXT NOT NULL," +
                        "rg TEXT NOT NULL," +
                        "telefone TEXT NOT NULL," +
                        "servico TEXT," +
                        "local_servico TEXT NOT NULL," +
                        "bloco TEXT NULL," +
                        "apartamento TEXT NULL," +
                        "data_hora_entrada TEXT NOT NULL," + // Formato ISO8601 recomendado
                        "data_hora_saida TEXT," + // Formato ISO8601 recomendado
                        "quem_autorizou TEXT NOT NULL," +
                        "observacoes TEXT," +
                        "morador_id INTEGER," +
                        "FOREIGN KEY (perfil_prestador_id) REFERENCES perfil_prestador(id) ON DELETE SET NULL," + // NOVO v1.6.0
                        "FOREIGN KEY (morador_id) REFERENCES morador(id) ON DELETE SET NULL," +
                        "FOREIGN KEY (bloco, apartamento) REFERENCES apartamentos(bloco, numero) ON DELETE SET NULL" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlPrestadores);
                    logger.info("Tabela 'prestadores' verificada com sucesso.");
                }

                // Tabela Perfil de Prestadores (v1.6.0)
                String sqlPerfilPrestador = "CREATE TABLE IF NOT EXISTS perfil_prestador (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome_empresa TEXT," +
                        "nome_prestador TEXT NOT NULL," +
                        "rg TEXT NOT NULL UNIQUE," +
                        "telefone TEXT NOT NULL," +
                        "categoria TEXT NOT NULL DEFAULT 'OUTROS'," +
                        "periodicidade TEXT," +
                        "servico_padrao TEXT," +
                        "data_cadastro TEXT DEFAULT (datetime('now'))," +
                        "ultima_visita TEXT," +
                        "total_visitas INTEGER DEFAULT 0," +
                        "ativo INTEGER DEFAULT 1," +
                        "observacoes TEXT" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlPerfilPrestador);
                    logger.info("Tabela 'perfil_prestador' verificada com sucesso.");
                }

                // Tabela Configuração de Limpeza de Prestadores (v1.6.0)
                String sqlConfiguracaoLimpeza = "CREATE TABLE IF NOT EXISTS configuracao_limpeza_prestadores (" +
                        "categoria TEXT PRIMARY KEY," +
                        "dias_retencao INTEGER NOT NULL," +
                        "limpar_automatico INTEGER DEFAULT 1," +
                        "ultima_limpeza TEXT," +
                        "registros_limpos INTEGER DEFAULT 0" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlConfiguracaoLimpeza);
                    logger.info("Tabela 'configuracao_limpeza_prestadores' verificada com sucesso.");
                    
                    // Inserir configurações padrão se não existirem
                    String sqlVerificarConfig = "SELECT COUNT(*) as total FROM configuracao_limpeza_prestadores";
                    try (Statement stmtVerif = conn.createStatement();
                         ResultSet rs = stmtVerif.executeQuery(sqlVerificarConfig)) {
                        if (rs.next() && rs.getInt("total") == 0) {
                            String sqlInsertConfig = "INSERT INTO configuracao_limpeza_prestadores " +
                                    "(categoria, dias_retencao) VALUES " +
                                    "('FUNCIONARIO_FIXO', 0)," +
                                    "('FUNCIONARIO_EVENTUAL', 90)," +
                                    "('PRESTADOR_CONDOMINIO', 15)," +
                                    "('PRESTADOR_MANUTENCAO', 15)," +
                                    "('PRESTADOR_ENTREGA', 30)," +
                                    "('OUTROS', 30)";
                            stmt.execute(sqlInsertConfig);
                            logger.info("Configurações padrão de limpeza inseridas.");
                        }
                    }
                }

                // Adicionar coluna perfil_prestador_id se não existir (v1.6.0 - Migração segura)
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rsColumns = stmt.executeQuery("PRAGMA table_info(prestadores)");
                    boolean temPerfilPrestadorId = false;
                    while (rsColumns.next()) {
                        if ("perfil_prestador_id".equals(rsColumns.getString("name"))) {
                            temPerfilPrestadorId = true;
                            break;
                        }
                    }
                    rsColumns.close();
                    
                    if (!temPerfilPrestadorId) {
                        stmt.execute("ALTER TABLE prestadores ADD COLUMN perfil_prestador_id INTEGER DEFAULT NULL");
                        logger.info("Coluna 'perfil_prestador_id' adicionada à tabela 'prestadores'.");
                    } else {
                        logger.debug("Coluna 'perfil_prestador_id' já existe na tabela 'prestadores'.");
                    }
                }

                // Criar índices para performance (v1.6.0)
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_prestador_perfil ON prestadores(perfil_prestador_id)");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_prestador_data ON prestadores(data_hora_entrada)");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_perfil_rg ON perfil_prestador(rg)");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_perfil_categoria ON perfil_prestador(categoria)");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_perfil_ativo ON perfil_prestador(ativo)");
                    logger.info("Índices de prestadores criados com sucesso.");
                }

                // A criação da tabela 'visitantes' foi movida para dentro de verificarEAtualizarTabelaVisitantes
                // para centralizar a lógica e evitar a duplicação da DDL.
                // A DDL que estava aqui foi removida.

                // Tabela de configurações do sistema
                String sqlConfiguracoes = "CREATE TABLE IF NOT EXISTS configuracoes_sistema (" +
                        "chave TEXT PRIMARY KEY," +
                        "valor TEXT NOT NULL," +
                        "data_atualizacao TEXT DEFAULT (datetime('now'))" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlConfiguracoes);
                    logger.info("Tabela 'configuracoes_sistema' verificada com sucesso.");
                }

                // Tabela de documentos para agendamentos (v1.6.0)
                String sqlDocumentosAgendamento = "CREATE TABLE IF NOT EXISTS documentos_agendamento (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "tipo VARCHAR(50) NOT NULL," +
                        "nome_arquivo VARCHAR(255) NOT NULL," +
                        "conteudo_pdf BLOB NOT NULL," +
                        "tamanho_bytes INTEGER NOT NULL," +
                        "data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "usuario_upload VARCHAR(100)," +
                        "versao INTEGER DEFAULT 1," +
                        "ativo BOOLEAN DEFAULT 1," +
                        "observacoes TEXT" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sqlDocumentosAgendamento);
                    logger.info("Tabela 'documentos_agendamento' verificada com sucesso.");
                    
                    // Criar índices para performance
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_doc_tipo_ativo ON documentos_agendamento(tipo, ativo)");
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_doc_data_upload ON documentos_agendamento(data_upload DESC)");
                    logger.info("Índices de 'documentos_agendamento' criados com sucesso.");
                }

                logger.info("Verificação e criação de tabelas concluída com sucesso.");

            } catch (SQLException e) {
                logger.error("Erro CRÍTICO ao criar/verificar tabelas: {}", e.getMessage(), e);
                throw new RuntimeException("Falha crítica na inicialização do banco de dados. Não foi possível criar/verificar tabelas.", e);
            } catch (ClassNotFoundException e) {
                logger.error("CRÍTICO: Driver JDBC SQLite não encontrado: {}. A aplicação não pode continuar.", e.getMessage());
                throw new RuntimeException("Driver JDBC SQLite não encontrado. A aplicação não pode continuar.", e);
            } finally {
                // Fecha a conexão direta usada para a inicialização
                if (conn != null) {
                    try {
                        conn.close();
                        logger.debug("Conexão direta de inicialização fechada.");
                    } catch (SQLException ex) {
                        logger.error("Erro ao fechar conexão direta de inicialização: {}", ex.getMessage());
                    }
                }
            }
        }
    }


    /**
     * Verifica se a tabela visitantes existe e tem as colunas necessárias.
     * Cria a tabela se não existir ou adiciona as colunas faltantes.
     *
     * @param conn Conexão com o banco de dados (geralmente uma conexão direta durante a inicialização)
     * @throws SQLException Em caso de erro
     */
    private static void verificarEAtualizarTabelaVisitantes(Connection conn) throws SQLException {
        String tableName = "visitantes";
        try (Statement stmt = conn.createStatement()) {
            // Verifica se a tabela existe
            try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
                if (!rs.next()) {
                    logger.info("Tabela {} não existe. Criando...", tableName);
                    criarTabelaVisitantes(conn); // Chama o método que contém a DDL completa e correta
                    return; // Tabela criada, não precisa verificar colunas individualmente agora
                }
            }

            logger.debug("Tabela {} existe. Verificando colunas...", tableName);

            // Verifica colunas existentes
            boolean temRg = false;
            boolean temDataHoraEntrada = false;
            boolean temPerfilId = false;

            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
                while (rs.next()) {
                    String columnName = rs.getString("name");
                    if ("rg".equalsIgnoreCase(columnName)) temRg = true;
                    if ("data_hora_entrada".equalsIgnoreCase(columnName)) temDataHoraEntrada = true;
                    if ("perfil_id".equalsIgnoreCase(columnName)) temPerfilId = true;
                }
            }

            if (!temRg) {
                logger.info("Coluna 'rg' não encontrada na tabela {}. Adicionando...", tableName);
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN rg TEXT");
                logger.info("Coluna 'rg' adicionada com sucesso.");
            } else {
                logger.debug("Coluna 'rg' já existe.");
            }

            if (!temDataHoraEntrada) {
                logger.info("Coluna 'data_hora_entrada' não encontrada na tabela {}. Adicionando...", tableName);
                // Usar datetime('now', 'localtime') para SQLite para o padrão.
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN data_hora_entrada TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))");
                logger.info("Coluna 'data_hora_entrada' adicionada com sucesso.");
            } else {
                logger.debug("Coluna 'data_hora_entrada' já existe.");
            }

            if (!temPerfilId) {
                logger.info("Coluna 'perfil_id' não encontrada na tabela {}. Adicionando...", tableName);
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN perfil_id INTEGER");
                // Adicionar a chave estrangeira pode ser mais complexo se a coluna já tiver dados
                // e a tabela referenciada (perfil_visitante) não tiver IDs correspondentes.
                // No entanto, para uma nova coluna, isso é geralmente seguro.
                // SQLite não permite adicionar FKs com ALTER TABLE diretamente de forma simples em todas as versões/cenários.
                // A melhor abordagem é recriar a tabela ou garantir que a FK seja definida na criação inicial.
                // Como a criação inicial agora é robusta, este ALTER TABLE é um fallback.
                // Se a FK não puder ser adicionada aqui, a criação inicial via criarTabelaVisitantes() já a terá.
                // Para simplificar, não tentaremos adicionar a constraint FK aqui via ALTER,
                // confiando que criarTabelaVisitantes() a define corretamente na criação.
                // Se a tabela já existe e a coluna perfil_id foi adicionada, mas a FK não,
                // isso indica uma inconsistência que idealmente seria resolvida por uma migração mais robusta.
                logger.info("Coluna 'perfil_id' adicionada. A chave estrangeira é definida na criação da tabela.");
            } else {
                logger.debug("Coluna 'perfil_id' já existe.");
            }

        } catch (SQLException e) {
            logger.error("Erro ao verificar/atualizar tabela '{}': {}", tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Cria a tabela visitantes com a estrutura correta, incluindo perfil_id e chaves estrangeiras.
     *
     * @param conn Conexão com o banco de dados (geralmente uma conexão direta durante a inicialização)
     * @throws SQLException Em caso de erro
     */
    private static void criarTabelaVisitantes(Connection conn) throws SQLException {
        String sqlVisitantes = "CREATE TABLE IF NOT EXISTS visitantes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "codigo TEXT NOT NULL," +
                "nome TEXT NOT NULL," +
                "rg TEXT," +
                "telefone TEXT," +
                "bloco TEXT NOT NULL," +
                "apartamento TEXT NOT NULL," +
                "data_hora_entrada TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))," + // Padrão adicionado
                "data_hora_saida TEXT," +
                "quem_autorizou TEXT NOT NULL," +
                "observacoes TEXT," +
                "morador_id INTEGER," +
                "perfil_id INTEGER," + // Adicionada coluna perfil_id
                "FOREIGN KEY (morador_id) REFERENCES morador(id) ON DELETE SET NULL," + // Adicionado ON DELETE SET NULL
                "FOREIGN KEY (perfil_id) REFERENCES perfil_visitante(id) ON DELETE SET NULL" + // Adicionada FK para perfil_id
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVisitantes);
            logger.info("Tabela 'visitantes' verificada/criada com sucesso com estrutura completa.");
        } catch (SQLException e) {
            logger.error("Erro ao criar tabela 'visitantes': {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Realiza backup do banco de dados para um diretório específico
     *
     * @param diretorioDestino Diretório onde será salvo o arquivo de backup (opcional)
     * @return O caminho completo do arquivo de backup gerado ou null em caso de erro
     */
    public static String realizarBackup(String diretorioDestino) {
        String dbPath = DatabaseLocator.getDatabasePath();
        if (dbPath == null || !new File(dbPath).exists()) {
             logger.error("Banco de dados não encontrado em '{}' para backup.", dbPath);
             return null;
        }

        if (diretorioDestino == null || diretorioDestino.trim().isEmpty()) {
            diretorioDestino = "backups";
        }

        try {
            File diretorio = new File(diretorioDestino);
            if (!diretorio.exists()) {
                if (diretorio.mkdirs()) {
                    logger.info("Diretório de backup criado: {}", diretorioDestino);
                } else {
                    logger.error("Não foi possível criar o diretório de backup: {}", diretorioDestino);
                    return null;
                }
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dataHora = dateFormat.format(new Date());
            String nomeArquivoBackup = "backup_" + new File(NOME_ARQUIVO_BANCO).getName() + "_" + dataHora; // Usa o nome do arquivo original
            String caminhoCompleto = diretorioDestino + File.separator + nomeArquivoBackup;

            Path origem = Paths.get(dbPath);
            Path destino = Paths.get(caminhoCompleto);
            Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Backup realizado com sucesso: {} -> {}", dbPath, caminhoCompleto);
            return caminhoCompleto;
        } catch (IOException e) {
            logger.error("Erro ao realizar backup do banco de dados de {}: {}", dbPath, e.getMessage(), e);
            return null;
        }
    }

    public static String realizarBackup() {
        return realizarBackup("backups");
    }

    /**
     * Restaura um backup do banco de dados.
     * <p>
     * ATENÇÃO CRÍTICA: Esta operação é PERIGOSA se a aplicação estiver ativa e usando
     * o banco de dados. Para uma restauração segura, todas as conexões do pool
     * devem ser fechadas (ConnectionPool.getInstance().closeAllConnections()),
     * a aplicação deve ser impedida de criar novas conexões, a cópia realizada,
     * e então o pool reinicializado (ou a aplicação reiniciada).
     * A implementação atual apenas copia o arquivo e NÃO lida com o estado da aplicação.
     * </p>
     * @param caminhoBackup Caminho completo do arquivo de backup
     * @return true se a restauração foi bem-sucedida (cópia do arquivo), false caso contrário
     */
    public static boolean restaurarBackup(String caminhoBackup) {
        logger.warn("INICIANDO RESTAURAÇÃO DE BACKUP. ATENÇÃO CRÍTICA: Esta operação é PERIGOSA com a aplicação ativa. " +
                    "Idealmente, a aplicação deveria ser PARADA ou o POOL DE CONEXÕES completamente DRENADO e BLOQUEADO antes de prosseguir. " +
                    "A implementação atual APENAS COPIA O ARQUIVO e não gerencia o estado do pool de conexões. " +
                    "RISCO DE CORRUPÇÃO DE DADOS OU ESTADO INCONSISTENTE DA APLICAÇÃO.");

        String dbPathDestino = DatabaseLocator.getDatabasePath();
        if (dbPathDestino == null) {
            logger.error("Não foi possível determinar o caminho do banco de dados de destino para restauração.");
            return false;
        }

        try {
            File arquivoBackup = new File(caminhoBackup);
            if (!arquivoBackup.exists()) {
                logger.error("Arquivo de backup não encontrado: {}", caminhoBackup);
                return false;
            }

            Path origem = Paths.get(caminhoBackup);
            Path destino = Paths.get(dbPathDestino);

            // Tenta criar o diretório pai do banco de dados de destino, se não existir.
            File parentDir = destino.getParent().toFile();
            if (!parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    logger.info("Diretório pai do banco de dados de destino criado: {}", parentDir.getAbsolutePath());
                } else {
                    logger.error("Não foi possível criar o diretório pai do banco de dados de destino: {}", parentDir.getAbsolutePath());
                    // Prosseguir pode ser ok se o arquivo for na raiz, mas é um aviso.
                }
            }


            Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Backup restaurado com sucesso: {} -> {}", caminhoBackup, dbPathDestino);


            return true;        } catch (IOException e) {
            logger.error("Erro ao restaurar backup do banco de dados de {} para {}: {}", caminhoBackup, dbPathDestino, e.getMessage(), e);
            return false;
        } catch (SecurityException e) {
            logger.error("Erro de permissão durante a restauração do backup: {}", e.getMessage(), e);
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            // Teste de criação de tabelas
            logger.info("Iniciando teste de criação de tabelas...");
            criarTabelas(); // Chama o método principal de criação/verificação
            logger.info("Teste de criação de tabelas concluído.");

            // Teste de conexão (opcional, apenas para depuração)
            /*
            logger.info("Testando conexão via pool...");
            try (Connection conn = conectar()) {
                if (conn != null) {
                    logger.info("Conexão de teste bem-sucedida. AutoCommit: {}", conn.getAutoCommit());
                    desconectar(conn);
                    logger.info("Conexão de teste liberada.");
                } else {
                    logger.error("Falha ao obter conexão de teste do pool.");
                }
            } catch (SQLException e) {
                logger.error("Erro durante o teste de conexão: {}", e.getMessage(), e);
            }
            */        } catch (RuntimeException e) {
            logger.error("Erro de execução no método main de ConexaoBanco: {}", e.getMessage(), e);
        }
    }

    /**
     * Método mantido para compatibilidade com código existente.
     *
     * @deprecated Use getConnection() em vez disso, que já inclui retentativas.
     * @return Conexão com o banco ou null em caso de erro
     */
    @Deprecated
    public static Connection getConexao() {
        try {
            return conectar(); // Chama o método padronizado
        } catch (SQLException e) {
            logger.error("Erro ao obter conexão (método depreciado getConexao): {}", e.getMessage(), e);
            return null; // Mantém o comportamento original de retornar null em caso de erro
        }
    }

    /**
     * Fecha/libera uma conexão. Se for do pool, devolve ao pool.
     * Se for uma conexão direta (raro, exceto inicialização), fecha-a.
     *
     * @param conn Conexão a ser fechada/liberada
     */
    public static void fecharConexao(Connection conn) {
        if (conn != null) {
            try {
                // A heurística para detectar se é do pool ou não é complexa.
                // A melhor prática é: se você pegou do pool com getConnection()/conectar(),
                // use desconectar() ou fecharConexao() (que chamará desconectar).
                // Se foi uma conexão direta (e.g. DatabaseLocator.connectToDatabase()), feche com conn.close().
                // Para simplificar, assumimos que a maioria das chamadas a fecharConexao são para conexões de pool.
                if (!conn.isClosed()) { // Só tenta operar em conexões abertas
                    // Tentar devolver ao pool. Se não for do pool, o pool pode rejeitar ou a implementação de releaseConnection pode lidar.
                    // O ConnectionPool.releaseConnection agora é mais robusto.
                    desconectar(conn);
                    // logger.debug("Conexão solicitada para fechamento/liberação processada pelo pool.");
                }            } catch (SQLException e) {
                logger.error("Erro ao verificar estado da conexão antes de fechar/liberar: {}", e.getMessage());
                // Como último recurso, tentar fechar diretamente se houver erro.
                try {
                    if (!conn.isClosed()) conn.close();
                } catch (SQLException ex) {
                    logger.error("Erro persistente ao tentar fechar conexão diretamente: {}", ex.getMessage());
                }
            }
        }
    }

    public static void fecharRecursos(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Erro ao fechar ResultSet: {}", e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Erro ao fechar Statement: {}", e.getMessage());
            }
        }
        fecharConexao(conn); // Usa o método unificado
    }

    /**
     * Verifica se a tabela usuários tem as colunas de recuperação de senha e email,
     * e as adiciona se necessário.
     *
     * @param conn Conexão com o banco de dados (geralmente uma conexão direta durante a inicialização)
     * @throws SQLException Em caso de erro na operação
     */
    private static void verificarEAtualizarTabelaUsuarios(Connection conn) throws SQLException {
        String tableName = "usuarios";
        try (Statement stmt = conn.createStatement()) {
            boolean temEmail = false;
            boolean temTokenRecuperacao = false;
            boolean temDataExpiracaoToken = false;

            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
                while (rs.next()) {
                    String columnName = rs.getString("name");
                    if ("email".equalsIgnoreCase(columnName)) temEmail = true;
                    if ("token_recuperacao".equalsIgnoreCase(columnName)) temTokenRecuperacao = true;
                    if ("data_expiracao_token".equalsIgnoreCase(columnName)) temDataExpiracaoToken = true;
                }
            }

            if (!temEmail) {
                logger.info("Adicionando coluna 'email' à tabela {}", tableName);
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN email VARCHAR(255)");
                logger.info("Coluna 'email' adicionada com sucesso.");
            }
            if (!temTokenRecuperacao) {
                logger.info("Adicionando coluna 'token_recuperacao' à tabela {}", tableName);
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN token_recuperacao VARCHAR(255)");
                logger.info("Coluna 'token_recuperacao' adicionada com sucesso.");
            }
            if (!temDataExpiracaoToken) {
                logger.info("Adicionando coluna 'data_expiracao_token' à tabela {}", tableName);
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN data_expiracao_token TIMESTAMP");
                logger.info("Coluna 'data_expiracao_token' adicionada com sucesso.");
            }
            if(temEmail && temTokenRecuperacao && temDataExpiracaoToken) {
                logger.debug("Tabela '{}' já possui todas as colunas de recuperação de senha.", tableName);
            }

        } catch (SQLException e) {
            logger.error("Erro ao verificar/atualizar tabela '{}': {}", tableName, e.getMessage());
            throw e;
        }
    }

    public static void recriarTabelaEncomendas() {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    // Desativa temporariamente as chaves estrangeiras
                    stmt.execute("PRAGMA foreign_keys = OFF;");
                    
                    // Remove a tabela existente
                    stmt.execute("DROP TABLE IF EXISTS encomendas;");
                    
                    // Cria a nova tabela
                    String sqlEncomendas = "CREATE TABLE IF NOT EXISTS encomendas (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "protocolo TEXT NOT NULL UNIQUE," +
                            "data_hora_recebimento TEXT NOT NULL," +
                            "nome_destinatario TEXT NOT NULL," +
                            "bloco VARCHAR(20) NOT NULL," +
                            "apartamento VARCHAR(20) NOT NULL," +
                            "tipo_encomenda TEXT NOT NULL," +
                            "codigo_rastreio TEXT," +
                            "empresa_entrega TEXT," +
                            "nome_entregador TEXT," +
                            "rg TEXT," +
                            "nome_porteiro TEXT NOT NULL," +
                            "quem_retirou TEXT," +
                            "data_hora_retirada TEXT," +
                            "telefone TEXT," +
                            "observacoes TEXT," +
                            "morador_id INTEGER NULL," + // Alterado para permitir NULL
                            "FOREIGN KEY(morador_id) REFERENCES morador(id) ON DELETE SET NULL" + // Adicionado ON DELETE SET NULL
                            ");";
                    
                    stmt.execute(sqlEncomendas);
                    
                    // Reativa as chaves estrangeiras
                    stmt.execute("PRAGMA foreign_keys = ON;");
                    
                    logger.info("Tabela 'encomendas' recriada com sucesso.");
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao recriar tabela 'encomendas': " + e.getMessage());
        } finally {
            fecharConexao(conn);
        }
    }
    
    /**
     * Salva a senha inicial do administrador em um arquivo para consulta posterior
     * 
     * @param senha A senha inicial gerada
     */
    private static void salvarSenhaInicialEmArquivo(String senha) {
        try {
            java.nio.file.Path arquivoSenha = java.nio.file.Paths.get("senha_inicial.txt");
            java.time.LocalDateTime agora = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            
            String conteudo = String.join("\n",
                "================================================================",
                "🔐 CREDENCIAIS DE PRIMEIRO ACESSO - SISTEMA DE PORTARIA 🔐",
                "================================================================",
                "",
                "   USUÁRIO: admin",
                "   SENHA:   " + senha,
                "",
                "   Data de Criação: " + agora.format(formatter),
                "",
                "================================================================",
                "⚠️  SEGURANÇA:",
                "   • Esta senha é válida apenas para o primeiro login",
                "   • Altere a senha imediatamente após o primeiro acesso",
                "   • Delete este arquivo após anotar a senha em local seguro",
                "   • Nunca compartilhe estas credenciais",
                "================================================================",
                "",
                "💡 INSTRUÇÕES:",
                "   1. Use as credenciais acima para fazer login no sistema",
                "   2. Acesse 'Configurações > Registrar Usuário' para alterar sua senha",
                "   3. Configure outros usuários conforme necessário",
                "   4. Delete este arquivo após configurar o sistema",
                "",
                "================================================================",
                "📧 Suporte: Para dúvidas, consulte a documentação ou entre",
                "    em contato com o suporte técnico.",
                "================================================================"
            );
            
            java.nio.file.Files.write(arquivoSenha, conteudo.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            logger.info("Arquivo com credenciais iniciais salvo em: {}", arquivoSenha.toAbsolutePath());
              } catch (IOException e) {
            logger.error("Erro de I/O ao salvar arquivo de credenciais iniciais: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("Erro de permissão ao salvar arquivo de credenciais iniciais: {}", e.getMessage());
        }
    }
    
    /**
     * Verifica e atualiza a tabela agendamentos para incluir novos campos (v1.6.0)
     * Adiciona telefone_contato e email_contato se não existirem
     * 
     * @param conn Conexão ativa com o banco de dados
     */
    private static void verificarEAtualizarTabelaAgendamentos(Connection conn) {
        try {
            // Verifica se as colunas já existem
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, "agendamentos", null);
            
            boolean temTelefone = false;
            boolean temEmail = false;
            
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if ("telefone_contato".equalsIgnoreCase(columnName)) {
                    temTelefone = true;
                } else if ("email_contato".equalsIgnoreCase(columnName)) {
                    temEmail = true;
                }
            }
            rs.close();
            
            // Adiciona telefone_contato se não existir
            if (!temTelefone) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE agendamentos ADD COLUMN telefone_contato VARCHAR(20)");
                    logger.info("Coluna 'telefone_contato' adicionada à tabela 'agendamentos' (v1.6.0)");
                }
            }
            
            // Adiciona email_contato se não existir
            if (!temEmail) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE agendamentos ADD COLUMN email_contato VARCHAR(255)");
                    logger.info("Coluna 'email_contato' adicionada à tabela 'agendamentos' (v1.6.0)");
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar/atualizar tabela 'agendamentos': " + e.getMessage());
        }
    }
}
