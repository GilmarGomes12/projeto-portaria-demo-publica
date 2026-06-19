# 🔐 Sistema de Portaria - Fork Público de Engenharia & Arquitetura de Segurança

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://jdk.java.net/21/)
[![Build Status](https://img.shields.io/badge/Build-Success-brightgreen.svg)]()
[![Database](https://img.shields.io/badge/Database-SQLite-blue.svg)](https://www.sqlite.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Este repositório apresenta um **Fork Público de Engenharia** de um sistema de portaria e controle de acesso desktop. 

> [!IMPORTANT]
> **Nota sobre Propriedade Intelectual:** 
> Para conformidade com direitos autorais e proteção de regras comerciais do software original, **todas as regras de negócio proprietárias, integrações de hardware (câmeras/OCR) e views de gestão operacional foram deliberadamente removidas**. Este repositório foca exclusivamente em demonstrar a **arquitetura de segurança, infraestrutura técnica de banco de dados local e padrões de resiliência**.

---

## 🛠️ Destaques Técnicos & Engenharia de Software

O objetivo deste projeto no portfólio é expor soluções de infraestrutura essenciais para aplicações desktop distribuídas com persistência local de dados:

### 1. 🔒 Segurança da Informação & Criptografia
* **Hashing de Senhas Robusto:** Implementação do algoritmo **BCrypt** (`jbcrypt`) para armazenamento seguro de credenciais, garantindo proteção contra ataques de dicionário e rainbow tables ([PasswordUtils.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/utils/PasswordUtils.java)).
* **Tokenização Segura:** Gerador de tokens criptograficamente fortes para fluxos sensíveis de recuperação de senha ([GeradorTokenSimples.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/utils/GeradorTokenSimples.java)).
* **Controle de Acesso Funcional:** Tela de gerenciamento de usuários demonstrativa que implementa autenticação local e níveis de permissão ([JUsuario.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/view/JUsuario.java)).

### 2. ⚡ Persistência Local Resiliente (SQLite)
* **Connection Pooling Otimizado:** Gerenciamento eficiente de conexões simultâneas com SQLite para evitar gargalos de I/O em ambiente desktop ([ConnectionPool.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/data/ConnectionPool.java)).
* **Auto-Recuperação Dinâmica (Anti-Bloqueio):** Implementação do padrão de resiliência [SQLiteReadonlyFixer.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/utils/SQLiteReadonlyFixer.java). Este módulo monitora o banco e aplica dinamicamente comandos `PRAGMA` (como WAL mode, busy_timeout de retentativa e backoff exponencial) para mitigar de forma transparente os erros clássicos de `SQLITE_READONLY` e `SQLITE_BUSY` causados por escrita concorrente.
* **Modelo Relacional Exposto:** O esqueleto relacional do banco de dados (tabelas de moradores, encomendas, prestadores, ocorrências, etc.) está totalmente documentado por meio das instruções de criação estrutural presentes em [ConexaoBanco.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/data/ConexaoBanco.java).

### 3. 🖥️ Interface Gráfica Desacoplada (Swing)
* Estrutura de interface construída sobre Java Swing seguindo separação em camadas, permitindo que a camada de visualização interaja com serviços mockados e stubs de forma limpa.
* Módulos mantidos: Tela de Login ([JLogin.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/view/JLogin.java)), Dashboard Simplificado ([JPrincipal.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/view/JPrincipal.java)), Recuperação de Credenciais ([JRecuperacaoSenha.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/view/JRecuperacaoSenha.java)) e Controle de Usuários ([JUsuario.java](file:///home/gilmar/Projetos/projeto-portaria-demo/projeto-portaria/src/main/java/com/ghg/view/JUsuario.java)).

---

## 📂 Estrutura do Projeto (Higienizado)

```text
projeto-portaria/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ghg/
│   │   │       ├── data/             # Connection Pool, Inicialização e CREATE TABLEs
│   │   │       ├── service/          # Configurações e Stubs de Serviços Desacoplados
│   │   │       ├── utils/            # Hashing BCrypt, Fixer do SQLite e Tokens
│   │   │       └── view/             # Telas desktop (Login, Principal, Usuários)
│   │   └── resources/                # Arquivos de estilização e logback.xml
├── pom.xml                           # Gerenciador de dependências Maven
└── README.md                         # Documentação do portfólio
```

---

## ⚙️ Tecnologias Utilizadas

* **Linguagem:** Java 21 (LTS)
* **Persistência:** SQLite / JDBC
* **Segurança:** jBCrypt (Hashing de senha robusto)
* **Log:** SLF4J com Logback
* **Dependências & Build:** Maven 3.x
* **Email:** JavaMail / Jakarta Mail (Estrutura de envio de token de recuperação)

---

## 🚀 Como Executar Localmente

### Pré-requisitos
* Java JDK 21 instalado
* Maven instalado e configurado nas variáveis de ambiente

### Passos
1. **Clone o repositório:**
   ```bash
   git clone https://github.com/GilmarGomes12/projeto-portaria-demo-publica.git
   cd projeto-portaria-demo-publica/projeto-portaria
   ```

2. **Compile o projeto:**
   ```bash
   mvn clean compile
   ```

3. **Gere o pacote executável (JAR):**
   ```bash
   mvn clean package
   ```

4. **Execute o JAR gerado:**
   ```bash
   java -jar target/projeto-portaria-1.5.jar
   ```

*(Na primeira execução, o banco SQLite `condominio.db` será criado automaticamente no diretório do projeto e as tabelas estruturais de segurança serão geradas).*

---

## 📄 Licença

Este projeto de demonstração arquitetural está licenciado sob a Licença MIT - consulte o arquivo `LICENSE` para obter mais detalhes.
