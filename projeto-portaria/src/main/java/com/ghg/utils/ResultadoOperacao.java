package com.ghg.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gilmar H Gomes
 * @since 12/05/2025
 * @version 1.0
 * @description Classe para representar o resultado de uma operação,
 *              contendo o status de sucesso e mensagens associadas
 */
public class ResultadoOperacao {
    private final boolean sucesso;
    private final List<String> mensagens;
    private String codigoErro;
    
    public ResultadoOperacao(boolean sucesso, List<String> mensagens) {
        this.sucesso = sucesso;
        this.mensagens = mensagens != null ? new ArrayList<>(mensagens) : new ArrayList<>();
        this.codigoErro = null;
    }
    
    public ResultadoOperacao(boolean sucesso, List<String> mensagens, String codigoErro) {
        this.sucesso = sucesso;
        this.mensagens = mensagens != null ? new ArrayList<>(mensagens) : new ArrayList<>();
        this.codigoErro = codigoErro;
    }
    
    public ResultadoOperacao(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagens = new ArrayList<>();
        if (mensagem != null) {
            this.mensagens.add(mensagem);
        }
        this.codigoErro = null;
    }
    
    public ResultadoOperacao(boolean sucesso, String mensagem, String codigoErro) {
        this.sucesso = sucesso;
        this.mensagens = new ArrayList<>();
        if (mensagem != null) {
            this.mensagens.add(mensagem);
        }
        this.codigoErro = codigoErro;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public List<String> getMensagens() {
        return new ArrayList<>(mensagens);
    }
      public String getPrimeiraMensagem() {
        return mensagens != null && !mensagens.isEmpty() ? mensagens.get(0) : "";
    }
    
    public String getCodigoErro() {
        return codigoErro;
    }
    
    public boolean temCodigoErro() {
        return codigoErro != null && !codigoErro.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resultado: ").append(sucesso ? "Sucesso" : "Erro");
        
        if (!mensagens.isEmpty()) {
            sb.append(" - ").append(String.join(", ", mensagens));
        }
        
        if (codigoErro != null && !codigoErro.isEmpty()) {
            sb.append(" (Código: ").append(codigoErro).append(")");
        }
        
        return sb.toString();
    }
}
