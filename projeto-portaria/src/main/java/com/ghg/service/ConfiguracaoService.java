package com.ghg.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ghg.data.ConexaoBanco;

/**
 * Serviço para gerenciar configurações personalizáveis do sistema
 * 
 * @author Gilmar H Gomes
 * @since 08/01/2026
 * @version 1.0
 */
public class ConfiguracaoService {
    
    private static final Logger LOGGER = Logger.getLogger(ConfiguracaoService.class.getName());
    private static final String PASTA_RECURSOS = "recursos_personalizados";
    private static final String CHAVE_PAPEL_PAREDE = "papel_parede_customizado";
    private static final String CHAVE_NOME_CONDOMINIO = "nome_condominio";
    private static final String NOME_CONDOMINIO_PADRAO = "Condomínio Varandas do Praia";
    
    /**
     * Salva o caminho do papel de parede personalizado
     * 
     * @param caminhoImagem Caminho da imagem selecionada
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean salvarPapelParede(String caminhoImagem) {
        try {
            // Cria pasta de recursos personalizados se não existir
            Path pastaRecursos = Paths.get(PASTA_RECURSOS);
            if (!Files.exists(pastaRecursos)) {
                Files.createDirectories(pastaRecursos);
                LOGGER.info("Pasta de recursos personalizados criada: " + pastaRecursos.toAbsolutePath());
            }
            
            // Copia a imagem para a pasta de recursos
            File arquivoOrigem = new File(caminhoImagem);
            String nomeArquivo = "papel_parede_" + System.currentTimeMillis() + getExtensao(arquivoOrigem.getName());
            Path destino = pastaRecursos.resolve(nomeArquivo);
            
            Files.copy(arquivoOrigem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Papel de parede copiado para: " + destino.toAbsolutePath());
            
            // Salva o caminho no banco de dados
            return salvarConfiguracao(CHAVE_PAPEL_PAREDE, destino.toString());
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar papel de parede: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtém o caminho do papel de parede personalizado
     * 
     * @return Caminho do papel de parede ou null se não houver customização
     */
    public String obterPapelParede() {
        String caminho = obterConfiguracao(CHAVE_PAPEL_PAREDE);
        
        // Verifica se o arquivo ainda existe
        if (caminho != null && !caminho.isEmpty()) {
            File arquivo = new File(caminho);
            if (arquivo.exists()) {
                return caminho;
            } else {
                LOGGER.warning("Papel de parede personalizado não encontrado: " + caminho);
                // Remove a configuração inválida
                removerConfiguracao(CHAVE_PAPEL_PAREDE);
            }
        }
        
        return null;
    }
    
    /**
     * Restaura o papel de parede padrão do sistema
     * 
     * @return true se restaurou com sucesso
     */
    public boolean restaurarPapelPadrao() {
        String caminhoAntigo = obterConfiguracao(CHAVE_PAPEL_PAREDE);
        
        // Remove o arquivo personalizado se existir
        if (caminhoAntigo != null && !caminhoAntigo.isEmpty()) {
            try {
                File arquivo = new File(caminhoAntigo);
                if (arquivo.exists()) {
                    Files.delete(arquivo.toPath());
                    LOGGER.info("Papel de parede personalizado removido: " + caminhoAntigo);
                }
            } catch (IOException e) {
                LOGGER.warning("Não foi possível remover arquivo antigo: " + e.getMessage());
            }
        }
        
        // Remove a configuração do banco
        return removerConfiguracao(CHAVE_PAPEL_PAREDE);
    }
    
    /**
     * Salva uma configuração no banco de dados
     */
    public boolean salvarConfiguracao(String chave, String valor) {
        String sql = "INSERT OR REPLACE INTO configuracoes_sistema (chave, valor) VALUES (?, ?)";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, chave);
            stmt.setString(2, valor);
            stmt.executeUpdate();
            
            LOGGER.info("Configuração salva: " + chave + " = " + valor);
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar configuração: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtém uma configuração do banco de dados
     */
    public String obterConfiguracao(String chave) {
        String sql = "SELECT valor FROM configuracoes_sistema WHERE chave = ?";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, chave);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("valor");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao obter configuração: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Remove uma configuração do banco de dados
     */
    private boolean removerConfiguracao(String chave) {
        String sql = "DELETE FROM configuracoes_sistema WHERE chave = ?";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, chave);
            stmt.executeUpdate();
            
            LOGGER.info("Configuração removida: " + chave);
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover configuração: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Salva o nome do condomínio personalizado
     * 
     * @param nomeCondominio Nome do condomínio
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean salvarNomeCondominio(String nomeCondominio) {
        if (nomeCondominio == null || nomeCondominio.trim().isEmpty()) {
            LOGGER.warning("Nome do condomínio inválido");
            return false;
        }
        return salvarConfiguracao(CHAVE_NOME_CONDOMINIO, nomeCondominio.trim());
    }
    
    /**
     * Obtém o nome do condomínio personalizado
     * 
     * @return Nome do condomínio ou nome padrão se não houver customização
     */
    public String obterNomeCondominio() {
        String nome = obterConfiguracao(CHAVE_NOME_CONDOMINIO);
        return (nome != null && !nome.trim().isEmpty()) ? nome : NOME_CONDOMINIO_PADRAO;
    }
    
    /**
     * Restaura o nome do condomínio padrão
     * 
     * @return true se restaurou com sucesso
     */
    public boolean restaurarNomeCondominoPadrao() {
        return removerConfiguracao(CHAVE_NOME_CONDOMINIO);
    }
    
    /**
     * Extrai a extensão do arquivo
     */
    private String getExtensao(String nomeArquivo) {
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        if (ultimoPonto > 0) {
            return nomeArquivo.substring(ultimoPonto);
        }
        return ".png"; // Padrão
    }
    
    /**
     * Salva o texto personalizado de um documento (Termo ou Regulamento)
     * 
     * @param chave Chave do documento (TEXTO_TERMO ou TEXTO_REGULAMENTO)
     * @param texto Conteúdo do texto
     * @return true se salvou com sucesso
     */
    public boolean salvarTextoDocumento(String chave, String texto) {
        if (chave == null || chave.trim().isEmpty()) {
            LOGGER.warning("Chave de documento inválida");
            return false;
        }
        return salvarConfiguracao(chave, texto);
    }
    
    /**
     * Obtém o texto personalizado de um documento
     * 
     * @param chave Chave do documento (TEXTO_TERMO ou TEXTO_REGULAMENTO)
     * @return Texto do documento ou null se não cadastrado
     */
    public String obterTextoDocumento(String chave) {
        return obterConfiguracao(chave);
    }
    
    /**
     * Verifica se existe texto cadastrado para um documento
     * 
     * @param chave Chave do documento
     * @return true se existe texto cadastrado
     */
    public boolean existeTextoDocumento(String chave) {
        String texto = obterTextoDocumento(chave);
        return texto != null && !texto.trim().isEmpty();
    }
}
