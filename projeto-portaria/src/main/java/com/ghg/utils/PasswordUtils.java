package com.ghg.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    /**
     * @author Gilmar H Gomes
     * @since 27/03/2025
     * @version 1.0
     * @description Classe utilitária para manipulação de senhas
     * @description Utiliza a biblioteca BCrypt para hash de senhas
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String candidate, String hashed) {
        return BCrypt.checkpw(candidate, hashed);
    }

    public static void main(String[] args) {

        String senhaOriginal = "minhaSenhaSecreta";
        String senhaHash = hashPassword(senhaOriginal);

        System.out.println("Senha original: " + senhaOriginal);
        System.out.println("Senha Hash: " + senhaHash);

        String senhaDigitadaCorreta = "minhaSenhaSecreta";
        String senhaDigitadaErrada = "senhaErrada";

        boolean senhaCorretaVerificada = checkPassword(senhaDigitadaCorreta, senhaHash);
        boolean senhaErradaVerificada = checkPassword(senhaDigitadaErrada, senhaHash);

        System.out.println("Senha correta verifica: " + senhaCorretaVerificada);
        System.out.println("Senha errada verifica: " + senhaErradaVerificada);
    }
}
