package com.ghg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe responsável por gerenciar as instâncias dos serviços na aplicação.
 * Versão simplificada para demonstração (Open Source).
 */
public class ServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);
    private static ServiceManager instance;
    
    private ServiceManager() {
        logger.info("Inicializando ServiceManager (Versão Demo)");
    }
    
    public static synchronized ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }
    
    public synchronized void reiniciarServicos() {
        logger.info("Reiniciando serviços");
    }
}
