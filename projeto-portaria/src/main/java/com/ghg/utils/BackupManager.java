package com.ghg.utils;

import com.ghg.data.ConexaoBanco;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Classe utilitária para gerenciar backups do banco de dados
 * 
 * @author Gilmar Humberto Gomes
 * @since 09/04/2025
 * @version 1.0
 */
public class BackupManager {

    /**
     * Realiza o backup do banco de dados
     * Permite ao usuário escolher o diretório de destino
     * 
     * @return true se o backup foi realizado com sucesso, false caso contrário
     */
    public static boolean realizarBackupComSelecaoDiretorio() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione o diretório para salvar o backup");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            String diretorioDestino = fileChooser.getSelectedFile().getAbsolutePath();
            String caminhoBackup = ConexaoBanco.realizarBackup(diretorioDestino);

            if (caminhoBackup != null) {
                JOptionPane.showMessageDialog(null,
                        "Backup realizado com sucesso!\nSalvo em: " + caminhoBackup,
                        "Backup Concluído",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                        "Erro ao realizar backup. Verifique o log para mais detalhes.",
                        "Erro de Backup",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    /**
     * Realiza o backup do banco de dados no diretório padrão (backups/)
     * 
     * @return true se o backup foi realizado com sucesso, false caso contrário
     */
    public static boolean realizarBackupAutomatico() {
        String caminhoBackup = ConexaoBanco.realizarBackup();

        if (caminhoBackup != null) {
            JOptionPane.showMessageDialog(null,
                    "Backup automático realizado com sucesso!\nSalvo em: " + caminhoBackup,
                    "Backup Concluído",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showMessageDialog(null,
                    "Erro ao realizar backup automático. Verifique o log para mais detalhes.",
                    "Erro de Backup",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Restaura um backup do banco de dados
     * Permite ao usuário selecionar o arquivo de backup
     * 
     * @return true se a restauração foi realizada com sucesso, false caso contrário
     */
    public static boolean restaurarBackup() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione o arquivo de backup para restaurar");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Banco de Dados (*.db)", "db"));

        // Tenta selecionar o diretório padrão de backups
        File dirBackups = new File("backups");
        if (dirBackups.exists() && dirBackups.isDirectory()) {
            fileChooser.setCurrentDirectory(dirBackups);
        }

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String caminhoBackup = fileChooser.getSelectedFile().getAbsolutePath();

            // Confirmar restauração
            int confirmacao = JOptionPane.showConfirmDialog(null,
                    "ATENÇÃO! A restauração irá substituir o banco de dados atual.\n" +
                            "Todos os dados atuais serão perdidos.\n\n" +
                            "Deseja continuar?",
                    "Confirmar Restauração",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacao == JOptionPane.YES_OPTION) {
                if (ConexaoBanco.restaurarBackup(caminhoBackup)) {
                    JOptionPane.showMessageDialog(null,
                            "Backup restaurado com sucesso!\n" +
                                    "É recomendável reiniciar a aplicação.",
                            "Restauração Concluída",
                            JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Erro ao restaurar backup. Verifique o log para mais detalhes.",
                            "Erro de Restauração",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return false;
    }
}