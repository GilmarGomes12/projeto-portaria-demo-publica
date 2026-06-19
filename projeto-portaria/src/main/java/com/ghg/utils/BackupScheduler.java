package com.ghg.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ghg.data.ConexaoBanco;
import com.ghg.service.ConfiguracaoService;

/**
 * Gerenciador de backups automáticos agendados.
 * Utiliza ScheduledExecutorService para executar backups semanais.
 * 
 * @author Gilmar H Gomes
 * @since 15/01/2026
 * @version 1.0
 */
public class BackupScheduler {
    
    private static final Logger LOGGER = Logger.getLogger(BackupScheduler.class.getName());
    private static final String CHAVE_BACKUP_HABILITADO = "backup_automatico_habilitado";
    private static final String CHAVE_BACKUP_DIA_SEMANA = "backup_automatico_dia_semana";
    private static final String CHAVE_BACKUP_HORARIO = "backup_automatico_horario";
    private static final String CHAVE_ULTIMA_EXECUCAO = "backup_automatico_ultima_execucao";
    
    private ScheduledExecutorService scheduler;
    private ConfiguracaoService configuracaoService;
    private boolean executando = false;
    
    /**
     * Construtor do BackupScheduler
     */
    public BackupScheduler() {
        this.configuracaoService = new ConfiguracaoService();
    }
    
    /**
     * Inicia o agendamento de backups automáticos
     */
    public void iniciar() {
        if (executando) {
            LOGGER.info("BackupScheduler já está em execução");
            return;
        }
        
        if (!isBackupHabilitado()) {
            LOGGER.info("Backup automático está desabilitado");
            return;
        }
        
        try {
            scheduler = Executors.newScheduledThreadPool(1);
            
            // Calcula o delay até o próximo backup
            long delayInicial = calcularDelayProximoBackup();
            
            // Agenda execução semanal
            scheduler.scheduleAtFixedRate(
                this::executarBackupSemanal,
                delayInicial,
                TimeUnit.DAYS.toMinutes(7), // 7 dias em minutos
                TimeUnit.MINUTES
            );
            
            executando = true;
            LOGGER.info("BackupScheduler iniciado. Próximo backup em " + delayInicial + " minutos");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao iniciar BackupScheduler: " + e.getMessage(), e);
        }
    }
    
    /**
     * Para o agendamento de backups
     */
    public void parar() {
        if (scheduler != null && !scheduler.isShutdown()) {
            try {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                executando = false;
                LOGGER.info("BackupScheduler parado com sucesso");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interrupção ao parar BackupScheduler", e);
            }
        }
    }
    
    /**
     * Executa o backup semanal e limpa backups antigos
     */
    private void executarBackupSemanal() {
        try {
            LOGGER.info("Iniciando backup automático semanal...");
            
            // Realiza o backup
            String caminhoBackup = ConexaoBanco.realizarBackup();
            
            if (caminhoBackup != null && !caminhoBackup.isEmpty()) {
                // Salva data/hora da última execução
                salvarDataUltimaExecucao();
                
                // Limpa backups antigos (mantém apenas os últimos 4 semanais)
                limparBackupsAntigos();
                
                LOGGER.info("Backup automático concluído com sucesso: " + caminhoBackup);
            } else {
                LOGGER.warning("Falha ao realizar backup automático");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro durante execução do backup automático: " + e.getMessage(), e);
        }
    }
    
    /**
     * Limpa backups antigos, mantendo apenas os últimos 4 backups semanais
     */
    private void limparBackupsAntigos() {
        try {
            java.io.File pastaBackups = new java.io.File("backups");
            
            if (!pastaBackups.exists() || !pastaBackups.isDirectory()) {
                return;
            }
            
            // Lista arquivos de backup ordenados por data de modificação
            java.io.File[] arquivos = pastaBackups.listFiles((dir, nome) -> 
                nome.startsWith("backup_") && nome.endsWith(".db")
            );
            
            if (arquivos == null || arquivos.length <= 4) {
                return; // Mantém todos se houver 4 ou menos
            }
            
            // Ordena por data de modificação (mais recente primeiro)
            java.util.Arrays.sort(arquivos, (a, b) -> 
                Long.compare(b.lastModified(), a.lastModified())
            );
            
            // Remove arquivos antigos (mantém apenas os 4 mais recentes)
            for (int i = 4; i < arquivos.length; i++) {
                if (arquivos[i].delete()) {
                    LOGGER.info("Backup antigo removido: " + arquivos[i].getName());
                } else {
                    LOGGER.warning("Não foi possível remover backup: " + arquivos[i].getName());
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao limpar backups antigos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calcula o delay em minutos até o próximo backup agendado
     */
    private long calcularDelayProximoBackup() {
        try {
            int diaSemana = obterDiaSemanaConfiguracao();
            String horario = obterHorarioConfiguracao();
            
            LocalDateTime agora = LocalDateTime.now();
            LocalTime horaBackup = LocalTime.parse(horario);
            
            // Calcula a próxima ocorrência do dia da semana
            DayOfWeek diaBackup = DayOfWeek.of(diaSemana);
            LocalDate proximaData = agora.toLocalDate()
                .with(TemporalAdjusters.nextOrSame(diaBackup));
            
            LocalDateTime proximoBackup = LocalDateTime.of(proximaData, horaBackup);
            
            // Se já passou a hora hoje, agenda para a próxima semana
            if (proximoBackup.isBefore(agora) || proximoBackup.isEqual(agora)) {
                proximoBackup = proximoBackup.plusWeeks(1);
            }
            
            // Calcula diferença em minutos
            long minutos = java.time.Duration.between(agora, proximoBackup).toMinutes();
            
            LOGGER.info(String.format("Próximo backup agendado para: %s (em %d minutos)", 
                proximoBackup, minutos));
            
            return minutos;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao calcular delay do próximo backup. Usando delay padrão.", e);
            // Delay padrão: 1 dia
            return TimeUnit.DAYS.toMinutes(1);
        }
    }
    
    /**
     * Verifica se o backup automático está habilitado
     */
    public boolean isBackupHabilitado() {
        String valor = configuracaoService.obterConfiguracao(CHAVE_BACKUP_HABILITADO);
        return "true".equalsIgnoreCase(valor);
    }
    
    /**
     * Habilita ou desabilita o backup automático
     */
    public void setBackupHabilitado(boolean habilitado) {
        configuracaoService.salvarConfiguracao(CHAVE_BACKUP_HABILITADO, String.valueOf(habilitado));
        
        if (habilitado && !executando) {
            iniciar();
        } else if (!habilitado && executando) {
            parar();
        }
    }
    
    /**
     * Obtém o dia da semana configurado (1 = Segunda, 7 = Domingo)
     */
    public int obterDiaSemanaConfiguracao() {
        String valor = configuracaoService.obterConfiguracao(CHAVE_BACKUP_DIA_SEMANA);
        return valor != null ? Integer.parseInt(valor) : 7; // Padrão: Domingo
    }
    
    /**
     * Salva o dia da semana para backup (1 = Segunda, 7 = Domingo)
     */
    public void salvarDiaSemana(int dia) {
        if (dia < 1 || dia > 7) {
            throw new IllegalArgumentException("Dia da semana deve estar entre 1 e 7");
        }
        configuracaoService.salvarConfiguracao(CHAVE_BACKUP_DIA_SEMANA, String.valueOf(dia));
    }
    
    /**
     * Obtém o horário configurado (formato HH:mm)
     */
    public String obterHorarioConfiguracao() {
        String valor = configuracaoService.obterConfiguracao(CHAVE_BACKUP_HORARIO);
        return valor != null ? valor : "02:00"; // Padrão: 02:00
    }
    
    /**
     * Salva o horário para backup (formato HH:mm)
     */
    public void salvarHorario(String horario) {
        try {
            LocalTime.parse(horario); // Valida formato
            configuracaoService.salvarConfiguracao(CHAVE_BACKUP_HORARIO, horario);
        } catch (Exception e) {
            throw new IllegalArgumentException("Horário inválido. Use o formato HH:mm");
        }
    }
    
    /**
     * Obtém a data/hora da última execução
     */
    public String obterDataUltimaExecucao() {
        return configuracaoService.obterConfiguracao(CHAVE_ULTIMA_EXECUCAO);
    }
    
    /**
     * Salva a data/hora da última execução
     */
    private void salvarDataUltimaExecucao() {
        String dataHora = LocalDateTime.now().toString();
        configuracaoService.salvarConfiguracao(CHAVE_ULTIMA_EXECUCAO, dataHora);
    }
    
    /**
     * Verifica se o scheduler está em execução
     */
    public boolean isExecutando() {
        return executando;
    }
}
