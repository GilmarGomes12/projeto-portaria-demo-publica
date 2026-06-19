package com.ghg.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Classe simplificada para testar apenas a geração de token
 * 
 * @author Gilmar Humberto Gomes
 * @since 09/04/2025
 * @version 1.0
 */
public class GeradorTokenSimples {
    
    /**
     * Gera um token aleatório
     * @return O token gerado
     */
    public static String gerarToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24]; // 192 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    public static void main(String[] args) {
        System.out.println("=== Teste de Geração de Token ===");
        
        // Gerar e mostrar alguns tokens
        for (int i = 0; i < 5; i++) {
            String token = gerarToken();
            System.out.println("Token " + (i+1) + ": " + token);
            System.out.println("Tamanho: " + token.length() + " caracteres");
        }
        
        // Verificar unicidade
        String token1 = gerarToken();
        String token2 = gerarToken();
        System.out.println("\nVerificando unicidade:");
        System.out.println("Token 1: " + token1);
        System.out.println("Token 2: " + token2);
        System.out.println("São iguais? " + token1.equals(token2));
    }
}