# Guia Completo de Migração para Ubuntu

## 📋 Visão Geral

Este guia detalha o processo completo para migrar o **Sistema de Portaria v1.6.1** de macOS/Windows para um ambiente de desenvolvimento Ubuntu.

## 🎯 Pré-requisitos

- Ubuntu 20.04 LTS ou superior (22.04 LTS recomendado)
- Conexão com internet
- Usuário com permissões sudo
- Mínimo 4GB RAM e 10GB espaço em disco

---

## 🚀 Etapa 1: Configuração Automática do Ambiente

### Método Rápido (Recomendado)

```bash
# 1. Tornar o script executável
chmod +x [configurar_ambiente_ubuntu.sh](http://_vscodecontentref_/3)

# 2. Executar o script
[configurar_ambiente_ubuntu.sh](http://_vscodecontentref_/4)

# 3. Recarregar as variáveis de ambiente
source ~/.bashrc