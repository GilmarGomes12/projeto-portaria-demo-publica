package com.ghg.utils;

import java.awt.Desktop;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sistema de logging detalhado para adicionar ao JEncomenda.java
 * para monitorar exatamente o que acontece quando a notificação WhatsApp é disparada.
 * 
 * Este arquivo gera código que pode ser copiado para o sistema real
 * para adicionar logs detalhados e identificar qualquer problema.
 */

/**
 * @author Gilmar Humberto Gomes
 * @since 09/04/2025
 * @version 1.0
 */
public class SistemaLogsWhatsApp {
    
    private static final String LOG_FILE = "whatsapp_debug.log";
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("SISTEMA DE LOGS DETALHADOS - WhatsApp Automático");
        System.out.println("=".repeat(80));
        
        // Teste do sistema de logs
        testarSistemaLogs();
        
        // Gerar código para inserir no JEncomenda.java
        gerarCodigoParaJEncomenda();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LOGS GERADOS EM: " + LOG_FILE);
        System.out.println("=".repeat(80));
    }
    
    private static void testarSistemaLogs() {
        System.out.println("\n1. TESTANDO SISTEMA DE LOGS");
        System.out.println("-".repeat(50));
        
        // Simular uma execução do método enviarNotificacaoWhatsApp
        String telefone = "11987654321";
        String mensagem = "🏢 ENCOMENDA REGISTRADA\n\nApartamento: 101\nDestinatário: João da Silva\nData/Hora: " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + 
                         "\nPortaria XYZ";
        
        enviarNotificacaoWhatsAppComLogs(telefone, mensagem);
    }
    
    /**
     * Versão melhorada do método enviarNotificacaoWhatsApp com logs detalhados
     */
    public static void enviarNotificacaoWhatsAppComLogs(String telefone, String mensagem) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        
        logDetalhado("========== INÍCIO NOTIFICAÇÃO WHATSAPP ==========");
        logDetalhado("Timestamp: " + timestamp);
        logDetalhado("Telefone recebido: '" + telefone + "'");
        logDetalhado("Mensagem recebida (length=" + mensagem.length() + "):");
        logDetalhado(mensagem);
        
        try {
            // Passo 1: Validar telefone
            logDetalhado("\n--- PASSO 1: VALIDAÇÃO DO TELEFONE ---");
            if (telefone == null || telefone.trim().isEmpty()) {
                logDetalhado("ERRO: Telefone é null ou vazio");
                return;
            }
            
            telefone = telefone.trim();
            logDetalhado("Telefone após trim: '" + telefone + "'");
            
            // Passo 2: Processar telefone
            logDetalhado("\n--- PASSO 2: PROCESSAMENTO DO TELEFONE ---");
            String telefoneNumerico = telefone.replaceAll("[^0-9]", "");
            logDetalhado("Telefone após remover não-numéricos: '" + telefoneNumerico + "'");
            
            if (!telefoneNumerico.startsWith("55")) {
                telefoneNumerico = "55" + telefoneNumerico;
                logDetalhado("Adicionado código do país: '" + telefoneNumerico + "'");
            }
            
            // Passo 3: Codificar mensagem
            logDetalhado("\n--- PASSO 3: CODIFICAÇÃO DA MENSAGEM ---");
            String mensagemCodificada;
            try {
                mensagemCodificada = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
                logDetalhado("Mensagem codificada com sucesso (length=" + mensagemCodificada.length() + ")");
            } catch (Exception e) {
                logDetalhado("ERRO ao codificar mensagem: " + e.getMessage());
                return;
            }
            
            // Passo 4: Gerar URL
            logDetalhado("\n--- PASSO 4: GERAÇÃO DA URL ---");
            String url = "https://wa.me/" + telefoneNumerico + "?text=" + mensagemCodificada;
            logDetalhado("URL gerada (length=" + url.length() + "):");
            logDetalhado(url);
            
            if (url.length() > 2048) {
                logDetalhado("AVISO: URL muito longa (" + url.length() + " chars)");
            }
            
            // Passo 5: Verificar Desktop API
            logDetalhado("\n--- PASSO 5: VERIFICAÇÃO DESKTOP API ---");
            logDetalhado("Desktop.isDesktopSupported(): " + Desktop.isDesktopSupported());
            
            if (!Desktop.isDesktopSupported()) {
                logDetalhado("ERRO: Desktop API não suportado");
                return;
            }
            
            Desktop desktop = Desktop.getDesktop();
            logDetalhado("Desktop.getDesktop() obtido com sucesso");
            logDetalhado("BROWSE suportado: " + desktop.isSupported(Desktop.Action.BROWSE));
            
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                logDetalhado("ERRO: Desktop.Action.BROWSE não suportado");
                return;
            }
            
            // Passo 6: Abrir navegador
            logDetalhado("\n--- PASSO 6: ABERTURA DO NAVEGADOR ---");
            logDetalhado("Tentando abrir navegador...");
            
            long inicioTempo = System.currentTimeMillis();
            
            try {
                URI uri = new URI(url);
                logDetalhado("URI criada com sucesso: " + uri.toString());
                
                desktop.browse(uri);
                
                long tempoDecorrido = System.currentTimeMillis() - inicioTempo;
                logDetalhado("✅ SUCESSO! Navegador aberto em " + tempoDecorrido + "ms");
                logDetalhado("Thread atual: " + Thread.currentThread().getName());
                
                // Verificação adicional
                logDetalhado("\n--- VERIFICAÇÃO PÓS-ABERTURA ---");
                try {
                    Thread.sleep(1000); // Aguardar 1 segundo
                    logDetalhado("Aguardado 1 segundo após abertura");
                } catch (InterruptedException ie) {
                    logDetalhado("Thread interrompida durante aguardo");
                }
                
            } catch (Exception e) {
                long tempoDecorrido = System.currentTimeMillis() - inicioTempo;
                logDetalhado("❌ ERRO ao abrir navegador (" + tempoDecorrido + "ms):");
                logDetalhado("Tipo: " + e.getClass().getSimpleName());
                logDetalhado("Mensagem: " + e.getMessage());
                logDetalhado("Stack trace:");
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            logDetalhado("❌ ERRO GERAL na notificação WhatsApp:");
            logDetalhado("Tipo: " + e.getClass().getSimpleName());
            logDetalhado("Mensagem: " + e.getMessage());
            e.printStackTrace();
        } finally {
            logDetalhado("========== FIM NOTIFICAÇÃO WHATSAPP ==========\n");
        }
    }
    
    /**
     * Log detalhado que escreve no arquivo e no console
     */
    private static void logDetalhado(String mensagem) {
        String timestampLog = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String mensagemCompleta = "[" + timestampLog + "] " + mensagem;
        
        // Escrever no console
        System.out.println(mensagemCompleta);
        
        // Escrever no arquivo
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(mensagemCompleta);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }
    
    private static void gerarCodigoParaJEncomenda() {
        System.out.println("\n2. CÓDIGO PARA ADICIONAR NO JEncomenda.java");
        System.out.println("-".repeat(50));
        
        String codigo = """
        // ==================== CÓDIGO PARA ADICIONAR NO JEncomenda.java ====================
        
        // 1. Adicionar imports no topo da classe:
        import java.io.FileWriter;
        import java.io.PrintWriter;
        
        // 2. Adicionar método de log detalhado na classe JEncomenda:
        private void logWhatsApp(String mensagem) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            String logMessage = "[" + timestamp + "] " + mensagem;
            
            // Log no console (se disponível)
            System.out.println("WhatsApp Debug: " + logMessage);
            
            // Log em arquivo
            try (PrintWriter writer = new PrintWriter(new FileWriter("whatsapp_debug.log", true))) {
                writer.println(logMessage);
                writer.flush();
            } catch (Exception e) {
                System.err.println("Erro ao escrever log WhatsApp: " + e.getMessage());
            }
        }
        
        // 3. Modificar o método enviarNotificacaoWhatsApp para adicionar logs:
        private void enviarNotificacaoWhatsApp(String telefone, String mensagem) {
            logWhatsApp("========== INÍCIO NOTIFICAÇÃO WHATSAPP ==========");
            logWhatsApp("Telefone: " + telefone);
            logWhatsApp("Mensagem length: " + mensagem.length());
            
            try {
                // Remove todos os caracteres não numéricos do telefone
                String telefoneNumerico = telefone.replaceAll("[^0-9]", "");
                logWhatsApp("Telefone numérico: " + telefoneNumerico);
                
                // Adiciona código do país se necessário
                if (!telefoneNumerico.startsWith("55")) {
                    telefoneNumerico = "55" + telefoneNumerico;
                    logWhatsApp("Adicionado código país: " + telefoneNumerico);
                }
                
                // Codifica a mensagem para URL
                String mensagemCodificada = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
                logWhatsApp("Mensagem codificada length: " + mensagemCodificada.length());
                
                // Cria a URL do WhatsApp
                String url = "https://wa.me/" + telefoneNumerico + "?text=" + mensagemCodificada;
                logWhatsApp("URL gerada (length=" + url.length() + "): " + url.substring(0, Math.min(100, url.length())) + "...");
                
                // Verifica se Desktop API está disponível
                logWhatsApp("Desktop.isDesktopSupported(): " + Desktop.isDesktopSupported());
                
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    logWhatsApp("Tentando abrir navegador...");
                    long inicio = System.currentTimeMillis();
                    
                    abrirNavegadorComLink(url);
                    
                    long tempo = System.currentTimeMillis() - inicio;
                    logWhatsApp("✅ Navegador aberto com sucesso em " + tempo + "ms");
                } else {
                    logWhatsApp("❌ Desktop.BROWSE não suportado");
                }
                
            } catch (Exception e) {
                logWhatsApp("❌ ERRO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            } finally {
                logWhatsApp("========== FIM NOTIFICAÇÃO WHATSAPP ==========\\n");
            }
        }
        
        // ==================== FIM DO CÓDIGO ====================
        """;
        
        System.out.println(codigo);
        
        // Salvar código em arquivo
        try (PrintWriter writer = new PrintWriter(new FileWriter("codigo_logs_whatsapp.txt"))) {
            writer.println(codigo);
            System.out.println("\n✅ Código salvo em: codigo_logs_whatsapp.txt");
        } catch (IOException e) {
            System.out.println("❌ Erro ao salvar código: " + e.getMessage());
        }
    }
}
